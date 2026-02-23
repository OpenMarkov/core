/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * @author jrico
 */

public class EditsHistory {
    private final ArrayDeque<PNEdit> doneEdits = new ArrayDeque<>();
    private final ArrayDeque<PNEdit> undoneEdits = new ArrayDeque<>();
    
    public boolean canUndo() {
        return !doneEdits.isEmpty();
    }
    
    public boolean canRedo() {
        return !undoneEdits.isEmpty();
    }
    
    public @Nullable PNEdit nextEditToUndo() {
        if (!canUndo()) {
            return null;
        }
        return doneEdits.getFirst();
    }
    
    public @Nullable PNEdit nextEditToRedo() {
        if (!canRedo()) {
            return null;
        }
        return undoneEdits.getFirst();
    }
    
    public @Nullable PNEdit undo() {
        if (!canUndo()) {
            return null;
        }
        //Redoes the edit.
        PNEdit undoneEdit = doneEdits.getFirst();
        undoneEdit.undo();
        //Then adds it to the undone list.
        undoneEdits.addFirst(doneEdits.removeFirst());
        return undoneEdit;
    }
    
    /**
     * Similar to undo, but it does not append the undone edit to the undoneEdits list, nor it triggers the undo event.
     * <p>
     * This is used to make an edit to disappear silently.
     */
    public @Nullable PNEdit removeLastDone() {
        if (!canUndo()) {
            return null;
        }
        return doneEdits.removeFirst();
    }
    
    public @Nullable PNEdit redo() {
        if (!canRedo()) {
            return null;
        }
        //Redoes the edit.
        PNEdit redoneEdit = undoneEdits.getFirst();
        redoneEdit.redo();
        //Then adds it to the undone list.
        doneEdits.addFirst(undoneEdits.removeFirst());
        return redoneEdit;
    }
    
    public void removeUndoneEdits() {
        undoneEdits.clear();
    }
    
    public void addEdit(PNEdit edit) {
        removeUndoneEdits();
        doneEdits.addFirst(edit);
    }
    
    public ArrayList<PNEdit> getDoneEdits() {
        return new ArrayList<>(this.doneEdits);
    }
    
    public ArrayList<PNEdit> getUndoneEdits() {
        return new ArrayList<>(this.undoneEdits);
    }
}
