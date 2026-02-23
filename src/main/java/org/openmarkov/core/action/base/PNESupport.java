/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
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
     * {@code openmarkov.undo#EditsHistory} for undo/redo.
     */
    private boolean withUndo;
    
    /**
     * List of undoable edits.
     *
     * @see javax.swing.undo.UndoManager
     */
    public final EditsHistoryStacker editsHistoryStacker;

    /*
    private boolean              significantEdits = true;

    private boolean              editsExecuted    = false;
    private int                  editCount;
    */

    
    // Constructor
    
    /**
     *
     */
    public PNESupport(ProbNet probNet) {
        super();
        this.probNet = probNet;
        this.withUndo = false;
        this.editsHistoryStacker = new EditsHistoryStacker();
    }
    
    public HashSet<PNEditListener> getListeners() {
        return listeners;
    }
    
    private HashSet<PNEditListener> listeners = new HashSet<>();
    
    // Methods
    public void setListeners(Collection<? extends PNEditListener> listeners) {
        this.listeners = new HashSet<>(listeners);
    }
    
    @ToCheck(reasonDescription = "This does not produce the expected events in PNEditEventListener", reasonKind = ToCheck.ReasonKind.PROBABLE_BUG)
    /**
     * @see javax.swing.undo.UndoManager#canRedo()
     * @see javax.swing.undo.UndoManager#redo()
     */
    public ArrayList<PNEdit> redo() {
        var redoneEdit = editsHistoryStacker.getCurrentUndoManager().redo();
        ArrayList<PNEdit> redoneEdits = flattenEdit(redoneEdit);
        for (PNEdit subRedoneEdit : redoneEdits) {
            for (PNEditListener listener : listeners) {
                listener.afterEditExecutes(subRedoneEdit);
            }
        }
        return redoneEdits;
    }
    
    private ArrayList<PNEdit> flattenEdit(@Nullable PNEdit redoneEdit) {
        if (redoneEdit == null) {
            return new ArrayList<>();
        }
        var editsToVisit = new ArrayDeque<PNEdit>();
        editsToVisit.addFirst(redoneEdit);
        var flattenedEdits = new ArrayList<PNEdit>();
        while (!editsToVisit.isEmpty()) {
            var edit = editsToVisit.removeFirst();
            flattenedEdits.add(edit);
            if (edit instanceof CompoundPNEdit compoundEdit) {
                compoundEdit.getEdits().forEach(editsToVisit::addLast);
            }
        }
        return flattenedEdits;
    }
    
    @ToCheck(reasonDescription = "This does not produce the expected events in PNEditEventListener", reasonKind = ToCheck.ReasonKind.PROBABLE_BUG)
    /**
     * @see javax.swing.undo.UndoManager#canUndo()
     * @see javax.swing.undo.UndoManager#undo()
     */
    public ArrayList<PNEdit> undo() {
        var undoneEdit = editsHistoryStacker.getCurrentUndoManager().undo();
        ArrayList<PNEdit> undoneEdits = flattenEdit(undoneEdit);
        for (PNEdit subUndoneEdit : undoneEdits) {
            for (PNEditListener listener : listeners) {
                listener.afterEditExecutes(subUndoneEdit);
            }
        }
        return undoneEdits;
        
    }
    
    public void removeUndoneEdits() {
        editsHistoryStacker.getCurrentUndoManager().removeUndoneEdits();
    }
    
    public EditsHistory getCurrentEditHistory() {
        return editsHistoryStacker.getCurrentUndoManager();
    }
    
    public boolean getCanUndo() {
        return editsHistoryStacker.getCurrentUndoManager().canUndo();
    }
    
    public boolean getCanRedo() {
        return editsHistoryStacker.getCurrentUndoManager().canRedo();
    }
    
    /**
     * Add a {@code OpenParenthesisEdit} edit instance to
     * {@code basicUndoManager} and increases the parenthesis deph.
     */
    public void openNewSubEditHistory() {
        if (withUndo) {
            editsHistoryStacker.openNewSubEditHistory();
        }
    }
    
    /**
     * Add a {@code CloseParenthesisEdit} edit instance to
     * {@code basicUndoManager} and decreases the parenthesis deph.
     */
    public void closeSubEditHistory(CloseEditStackOptions... closeOperations) {
        if (withUndo) {
            editsHistoryStacker.closeSubEditHistory(List.of(closeOperations));
        }
    }
    
    public void cancelLastSubEditHistory() {
        closeSubEditHistory(CloseEditStackOptions.FORGET, CloseEditStackOptions.UNDO);
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
    
    private final ProbNet probNet;
    
    public void addListener(PNEditListener listener) {
        this.listeners.add(listener);
    }
    
    public void removeListener(PNEditListener listener) {
        this.listeners.remove(listener);
    }
}
