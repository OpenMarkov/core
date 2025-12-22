/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
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
    public final UndoManager undoManager;

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
     *
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
    
    @ToCheck(reasonDescription = "This does not produce the expected events in PNEditEventListener", reasonKind = ToCheck.ReasonKind.PROBABLE_BUG)
    /**
     * @see javax.swing.undo.UndoManager#canUndo()
     * @see javax.swing.undo.UndoManager#undo()
     */
    public ArrayList<PNEdit> undo() {
        ArrayList<PNEdit> undoneEdits = new ArrayList<>();
        if (!(withUndo && undoManager.canUndo() && undoManager.canUndo())) {
            return undoneEdits;
        }
        // Same as the undo method but counting the number of edits between parenthesis
        PNEdit undoneEdit = undoManager.undo();
        if (!(undoneEdit instanceof CloseParenthesisEdit closeParenthesisEdit)) {
            undoneEdits.add(undoneEdit);
            for (PNUndoableEditListener listener : listeners) {
                listener.afterUndoingEdit(new PNUndoableEditEvent(undoneEdit));
            }
            return undoneEdits;
        }
        while (true) {
            undoneEdit = undoManager.undo();
            if (undoneEdit instanceof OpenParenthesisEdit openParenthesisEdit
                    && openParenthesisEdit == closeParenthesisEdit.getOpenParenthesisEdit()) {
                for (PNUndoableEditListener listener : listeners) {
                    listener.afterUndoingEdit(new PNUndoableEditEvent(undoneEdit));
                }
                undoneEdits.add(undoneEdit);
                return undoneEdits;
            }
            for (PNUndoableEditListener listener : listeners) {
                listener.afterUndoingEdit(new PNUndoableEditEvent(undoneEdit));
            }
            undoneEdits.add(undoneEdit);
        }
    }
    
    @ToCheck(reasonDescription = "This does not produce the expected events in PNEditEventListener", reasonKind = ToCheck.ReasonKind.PROBABLE_BUG)
    /**
     * @see javax.swing.undo.UndoManager#canRedo()
     * @see javax.swing.undo.UndoManager#redo()
     */
    public ArrayList<PNEdit> redo() {
        ArrayList<PNEdit> redoneEdits = new ArrayList<>();
        if (!(withUndo && undoManager.canRedo())) {
            return redoneEdits;
        }
        PNEdit redoneEdit = undoManager.redo();
        redoneEdits.add(redoneEdit);
        for (PNUndoableEditListener listener : listeners) {
            listener.afterEditHappens(new PNUndoableEditEvent(redoneEdit));
        }
        if (!(redoneEdit instanceof OpenParenthesisEdit openParenthesisEdit)) {
            return redoneEdits;
        }
        while (true) {
            redoneEdit = undoManager.redo();
            redoneEdits.add(redoneEdit);
            for (PNUndoableEditListener listener : listeners) {
                listener.afterEditHappens(new PNUndoableEditEvent(redoneEdit));
            }
            if (redoneEdit instanceof CloseParenthesisEdit closeParenthesisEdit && openParenthesisEdit == closeParenthesisEdit.getOpenParenthesisEdit()) {
                return redoneEdits;
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
    
    @ToCheck(reasonDescription = "This does not produce the expected events in PNEditEventListener", reasonKind = ToCheck.ReasonKind.PROBABLE_BUG)
    /**
     * This method do the same task as undo method, but removing the edits from the list
     */
    public ArrayList<PNEdit> undoAndDelete() {
        ArrayList<PNEdit> undoneEdits = new ArrayList<>();
        if (!(withUndo && undoManager.canUndo() && undoManager.canUndo())) {
            return undoneEdits;
        }
        // Same as the undo method but counting the number of edits between parenthesis
        PNEdit undoneEdit = undoManager.undo();
        if (!(undoneEdit instanceof CloseParenthesisEdit closeParenthesisEdit)) {
            undoneEdits.add(undoneEdit);
            undoManager.removeUndoneEdits();
            for (PNUndoableEditListener listener : listeners) {
                listener.afterUndoingEdit(new PNUndoableEditEvent(undoneEdit));
            }
            return undoneEdits;
        }
        while (true) {
            undoneEdit = undoManager.undo();
            if (undoneEdit instanceof OpenParenthesisEdit openParenthesisEdit
                    && openParenthesisEdit == closeParenthesisEdit.getOpenParenthesisEdit()) {
                undoManager.removeUndoneEdits();
                for (PNUndoableEditListener listener : listeners) {
                    listener.afterUndoingEdit(new PNUndoableEditEvent(undoneEdit));
                }
                undoneEdits.add(undoneEdit);
                return undoneEdits;
            }
            for (PNUndoableEditListener listener : listeners) {
                listener.afterUndoingEdit(new PNUndoableEditEvent(undoneEdit));
            }
            undoneEdits.add(undoneEdit);
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
