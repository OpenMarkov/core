/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

public interface PNUndoableEditListener {
    
    
    /**
     * Triggered before an undoable edit is about to happen happened.
     */
    default void undoableEditWillHappen(PNUndoableEditEvent event) {
    }
    
    /**
     * Triggered after an undoable edit has happened.
     */
    default void undoableEditHappened(PNUndoableEditEvent e) {
    }
    
    /**
     * Triggered after undoing an edit.
     */
    default void undoEditHappened(PNUndoableEditEvent event) {
    }
    
}
