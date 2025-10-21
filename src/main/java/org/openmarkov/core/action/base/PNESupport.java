/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreacheableException;
import org.openmarkov.core.model.network.ProbNet;

import java.util.*;

/**
 * This class is used over a {@code openmarkov.inference.ProbNet} where
 * changes can be undone and redone. One edition has two parts:
 * <ol>
 * <li>Inform the listeners and
 * <li>If the action is not vetoed (with a {@code Exception}) for a
 * listener it does the edition.
 * </ol>
 *
 * @author marias
 */
public class PNESupport /*extends UndoableEditSupport*/ {
    
    /**
     * If {@code true} stores editions in
     * {@code openmarkov.undo#UndoManager} for undo/redo.
     */
    private boolean withUndo;
    
    /**
     * List of undoable edits.
     *
     * @see javax.swing.undo.UndoManager
     */
    private final UndoManager undoManager;

    /*
    private boolean              significantEdits = true;

    private boolean              editsExecuted    = false;
    private int                  editCount;
    */
    /**
     * Stack of open parenthesis, used to get the trace of nested parenthesis
     */
    private final Stack<OpenParenthesisEdit> openParenthesisStack;
    
    // Constructor
    
    /**
     */
    public PNESupport(ProbNet probNet) {
        super();
        this.probNet = probNet;
        this.withUndo = false;
        this.undoManager = new UndoManager();
        this.openParenthesisStack = new Stack<>();
    }
    
    public HashSet<PNUndoableEditListener> getListeners() {
        return listeners;
    }
    
    private HashSet<PNUndoableEditListener> listeners = new HashSet<>();
    
    // Methods
    public void setListeners(Collection<? extends PNUndoableEditListener> listeners) {
        this.listeners = new HashSet<>(listeners);
    }
    
    /**
     * @see javax.swing.undo.UndoManager#canUndo()
     * @see javax.swing.undo.UndoManager#undo()
     */
    public void undo() {
        if (withUndo && undoManager.canUndo()) {
            //Undo all between the parenthesis loop
            PNUndoableEditEvent event = new PNUndoableEditEvent(this, undoManager.nextEditToUndo(), probNet);
            if (event.getEdit() instanceof CloseParenthesisEdit closeParenthesisEdit) {
                while (true) {
                    undoManager.undo();
                    PNUndoableEditEvent event2 = new PNUndoableEditEvent(this, undoManager.nextEditToUndo(), probNet);
                    if (event2.getEdit() instanceof OpenParenthesisEdit openParenthesisEdit
                            && openParenthesisEdit == closeParenthesisEdit.getOpenParenthesisEdit()) {
                        //Open parenthesis found.
                        break;
                    }
                }
            }
            //
            undoManager.undo();
            for (PNUndoableEditListener listener : listeners) {
                listener.undoEditHappened(event);
            }
        }
    }
    
    /**
     * @see javax.swing.undo.UndoManager#canRedo()
     * @see javax.swing.undo.UndoManager#redo()
     */
    public void redo() {
        if (withUndo && undoManager.canRedo()) {
            PNUndoableEditEvent event = new PNUndoableEditEvent(this, undoManager.nextEditToRedo(), probNet);
            if (event.getEdit() instanceof OpenParenthesisEdit) {
                while (true) {
                    undoManager.redo();
                    PNUndoableEditEvent event2 = new PNUndoableEditEvent(this, undoManager.nextEditToRedo(), probNet);
                    if (event2.getEdit() instanceof CloseParenthesisEdit) {
                        //Close parenthesis found.
                        break;
                    }
                }
            }
            undoManager.redo();
            for (PNUndoableEditListener listener : listeners) {
                listener.undoableEditHappened(event);
            }
        }
    }
    
    public UndoManager getUndoManager() {
        return undoManager;
    }
    
    public boolean getCanUndo() {
        return undoManager.canUndo();
    }
    
    public boolean getCanRedo() {
        return undoManager.canRedo();
    }
    
    /**
     * Add a {@code OpenParenthesisEdit} edit instance to
     * {@code undoManager} and increases the parenthesis deph.
     */
    public void openParenthesis() {
        if (withUndo) {
            OpenParenthesisEdit openParenthesisEdit = new OpenParenthesisEdit(this.probNet);
            try {
                openParenthesisEdit.executeEdit();
                openParenthesisStack.push(openParenthesisEdit);
            } catch (DoEditException e) {
                throw new UnreacheableException(e);
            }
        }
    }
    
    /**
     * Add a {@code CloseParenthesisEdit} edit instance to
     * {@code undoManager} and decreases the parenthesis deph.
     */
    public void closeParenthesis() {
        if (withUndo) {
            OpenParenthesisEdit openParenthesisEdit = openParenthesisStack.pop();
            // Associate the openParenthesis to the close parenthesis
            CloseParenthesisEdit closeParenthesisEdit = new CloseParenthesisEdit(this.probNet, openParenthesisEdit);
            try {
                closeParenthesisEdit.executeEdit();
            } catch (DoEditException e) {
                throw new UnreacheableException(e);
            }
        }
    }
    
    /**
     * @return withUndo {@code boolean}.
     */
    public boolean isWithUndo() {
        return withUndo;
    }
    
    /**
     * @param withUndo {@code boolean}.
     */
    public void setWithUndo(boolean withUndo) {
        this.withUndo = withUndo;
    }
    
    /**
     * @return probNet {@code ProbNet}.
     */
    /*
     * public ProbNet getProbNet() { return (ProbNet)realSource; }
     */
    public String toString() {
        String out = "PNESupport. probNet: " + probNet;
        if (listeners != null) {
            out += " Number of listeners: " + listeners.size() + '.';
        } else {
            out += " Number of listeners: 0.";
        }
        if (withUndo) {
            out += " With undo.";
        } else {
            out += " Without undo.";
        }
        return out;
    }
    
    /**
     * This method do the same task as undo method, but removing the edits from the list
     */
    public void undoAndDelete() {
        if (withUndo && undoManager.canUndo() && undoManager.canUndo()) {
            // Same as the undo method but counting the number of edits between parenthesis
            PNUndoableEditEvent event = new PNUndoableEditEvent(this, undoManager.nextEditToUndo(), probNet);
            int numberOfEditsToBeDeleted = 0;
            if (event.getEdit() instanceof CloseParenthesisEdit closeParenthesisEdit) {
                PNUndoableEditEvent event2;
                while (true) {
                    undoManager.undo();
                    numberOfEditsToBeDeleted++;
                    event2 = new PNUndoableEditEvent(this, undoManager.nextEditToUndo(), probNet);
                    if (event2.getEdit() instanceof OpenParenthesisEdit openParenthesisEdit
                            && openParenthesisEdit == closeParenthesisEdit.getOpenParenthesisEdit()) {
                        break;
                    }
                }
            }
            //
            undoManager.undo();
            undoManager.removeUndoneEdits();
            PNUndoableEditEvent eventDeleted = new PNUndoableEditEvent(this, null, probNet);
            for (PNUndoableEditListener listener : listeners) {
                listener.undoEditHappened(eventDeleted);
            }
            
        }
        
    }
    
    public Stack<OpenParenthesisEdit> getOpenParenthesisStack() {
        return openParenthesisStack;
    }
    
    private final ProbNet probNet;
    
    public void addListener(PNUndoableEditListener listener) {
        this.listeners.add(listener);
    }
    
    public void removeListener(PNUndoableEditListener listener) {
        this.listeners.remove(listener);
    }
}
