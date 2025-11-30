/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;

public interface PNUndoableEditListener {
    
    /**
     * Triggered before an edit is about to happen happened.
     */
    default void beforeEditHappens(PNUndoableEditEvent event) {
    }
    
    /**
     * Triggered after an edit has happened.
     */
    default void afterEditHappens(PNUndoableEditEvent e) {
    }
    
    /**
     * Triggered after an edit has failed.
     * <p>
     * The method {@link PNUndoableEditListener#beforeEditHappens(PNUndoableEditEvent)} must have happened.
     */
    default void onEditFailed(PNUndoableEditEvent event, DoEditException exception) {
    }
    
    /**
     * Triggered before doing the edit and only if at least one of the constraints is violated.
     */
    default void onEditViolatesConstraints(PNUndoableEditEvent pnUndoableEditEvent, ConstraintViolatedException ex) {
    }
    
    /**
     * Triggered before undoing an edit.
     */
    default void beforeUndoingEditHappens(PNUndoableEditEvent event) {
    }
    
    /**
     * Triggered after undoing an edit.
     */
    default void afterUndoingEdit(PNUndoableEditEvent event) {
    }
    
    /**
     * Triggered before redoing an edit.
     */
    default void beforeRedoingEditHappens(PNUndoableEditEvent event) {
    }
    
    /**
     * Triggered after redoing an edit.
     */
    default void afterRedoingEdit(PNUndoableEditEvent event) {
    }
    
    
}
