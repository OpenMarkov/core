/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreacheableException;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import java.util.Stack;
import java.util.Vector;

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
public class PNESupport extends UndoableEditSupport {
    
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
    private final UndoManagerSupport undoManagerSupport;

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
     * @param withUndo {@code boolean}
     */
    public PNESupport(boolean withUndo) {
        super();
        this.withUndo = withUndo;
        this.undoManagerSupport = new UndoManagerSupport();
        this.openParenthesisStack = new Stack<>();
    }
    
    public Vector<UndoableEditListener> getListeners() {
        return listeners;
    }
    
    // Methods
    public void setListeners(Vector<UndoableEditListener> listeners) {
        this.listeners = listeners;
    }
    
    /**
     * First part: Announce to the listeners than an edition can happen
     *
     * @param edit {@code PNEdit}.
     */
    public void announceEdit(PNEdit edit) {
        UndoableEditEvent event = new UndoableEditEvent(this, edit);
        for (UndoableEditListener listener : listeners) {
            ((PNUndoableEditListener) listener).undoableEditWillHappen(event);
        }
    }
    
    /**
     * Second part: It does the edition and inform to the listeners
     *
     * @param edit {@code PNEdit}.
     *
     * @throws DoEditException DoEditException
     */
    public void doEdit(PNEdit edit) throws DoEditException {
        // Inform the listeners that an edition will happen
        // May return an exception
        edit.doEdit();
        if (withUndo) {
            undoManagerSupport.addEdit(edit);
        }
        if (!(edit instanceof OpenParenthesisEdit || edit instanceof CloseParenthesisEdit)) {
            postEdit(edit);// Inform the listeners that an edition has happened
        }
    }
    
    /**
     * @see javax.swing.undo.UndoManager#canUndo()
     * @see javax.swing.undo.UndoManager#undo()
     */
    public void undo() {
        if (withUndo && undoManagerSupport.canUndo()) {
            //Undo all between the parenthesis loop
            UndoableEditEvent event = new UndoableEditEvent(this, undoManagerSupport.editToBeUndone());
            if (event.getEdit() instanceof CloseParenthesisEdit closeParenthesisEdit) {
                while (true) {
                    undoManagerSupport.undo();
                    UndoableEditEvent event2 = new UndoableEditEvent(this, undoManagerSupport.editToBeUndone());
                    if (event2.getEdit() instanceof OpenParenthesisEdit openParenthesisEdit
                            && openParenthesisEdit == closeParenthesisEdit.getOpenParenthesisEdit()) {
                        //Open parenthesis found.
                        break;
                    }
                    /*
                    if (event2.getEdit()
                              .getClass() == OpenParenthesisEdit.class && event2.getEdit() == ((CloseParenthesisEdit) event.getEdit()).getOpenParenthesisEdit()) {
                        break;
                    }
                     */
                }
            }
            //
            undoManagerSupport.undo();
            for (UndoableEditListener listener : listeners) {
                ((PNUndoableEditListener) listener).undoEditHappened(event);
            }
        }
    }
    
    /**
     * @see javax.swing.undo.UndoManager#canRedo()
     * @see javax.swing.undo.UndoManager#redo()
     */
    public void redo() {
        if (withUndo && undoManagerSupport.canRedo()) {
            UndoableEditEvent event = new UndoableEditEvent(this, undoManagerSupport.getCurrentEdit());
            if (event.getEdit() instanceof OpenParenthesisEdit) {
                while (true) {
                    undoManagerSupport.redo();
                    UndoableEditEvent event2 = new UndoableEditEvent(this, undoManagerSupport.getCurrentEdit());
                    if (event2.getEdit() instanceof CloseParenthesisEdit) {
                        //Close parenthesis found.
                        break;
                    }
                }
            }
            undoManagerSupport.redo();
            for (UndoableEditListener listener : listeners) {
                listener.undoableEditHappened(event);
            }
        }
    }
    
    public UndoManagerSupport getUndoManager() {
        return undoManagerSupport;
    }
    
    public boolean getCanUndo() {
        return undoManagerSupport.canUndo();
    }
    
    public boolean getCanRedo() {
        return undoManagerSupport.canRedo();
    }
    
    /**
     * Add a {@code OpenParenthesisEdit} edit instance to
     * {@code undoManager} and increases the parenthesis deph.
     */
    public void openParenthesis() {
        if (withUndo) {
            OpenParenthesisEdit openParenthesisEdit = new OpenParenthesisEdit();
            try {
                this.doEdit(openParenthesisEdit);
                openParenthesisStack.push(openParenthesisEdit);
            } catch (DoEditException e) {
                throw new UnreacheableException(e);
            }
            /*
        	openParenthesis = true;
            editCount = 0;
            editsExecuted = false;
            */
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
            CloseParenthesisEdit closeParenthesisEdit = new CloseParenthesisEdit(openParenthesisEdit);
            try {
                this.doEdit(closeParenthesisEdit);
            } catch (DoEditException e) {
                throw new UnreacheableException(e);
            }
            if (undoManagerSupport.getEditsSize() <= 2) {
                undoManagerSupport.deleteEdits(undoManagerSupport.getEditsSize());
            }

        	/*
            openParenthesis = false;
            significantEdits = true;
            */
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
        String out = "PNESupport. probNet: ";
        if (realSource == null) {
            out += "not defined.";
        } else {
            /*
             * try { String name = (String) ((ProbNet) realSource).getName(); if
             * (name != null) { out = out + name + '.'; } else { out = out +
             * "no name."; } } catch (Exception e) { logger.fatal (e); }
             */
        }
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
        if (withUndo && undoManagerSupport.canUndo() && undoManagerSupport.canUndo()) {
            // Same as the undo method but counting the number of edits between parenthesis
            UndoableEditEvent event = new UndoableEditEvent(this, undoManagerSupport.editToBeUndone());
            int numberOfEditsToBeDeleted = 0;
            if (event.getEdit() instanceof CloseParenthesisEdit closeParenthesisEdit) {
                UndoableEditEvent event2;
                while (true) {
                    undoManagerSupport.undo();
                    numberOfEditsToBeDeleted++;
                    event2 = new UndoableEditEvent(this, undoManagerSupport.editToBeUndone());
                    if (event2.getEdit() instanceof OpenParenthesisEdit openParenthesisEdit
                            && openParenthesisEdit == closeParenthesisEdit.getOpenParenthesisEdit()) {
                        break;
                    }
                }
            }
            //
            undoManagerSupport.undo();
            numberOfEditsToBeDeleted++;
            UndoableEditEvent eventDeleted = new UndoableEditEvent(this, null);
            undoManagerSupport.deleteEdits(numberOfEditsToBeDeleted);
            for (UndoableEditListener listener : listeners) {
                ((PNUndoableEditListener) listener).undoEditHappened(eventDeleted);
            }
            
        }
        
    }
    
    public Stack<OpenParenthesisEdit> getOpenParenthesisStack() {
        return openParenthesisStack;
    }
    
}
