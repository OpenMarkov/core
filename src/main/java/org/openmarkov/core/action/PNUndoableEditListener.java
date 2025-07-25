/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

public interface PNUndoableEditListener extends UndoableEditListener {
    
    /**
     * An undoable edit will happen
     *
     * @param event Event
     * @throws ConstraintViolationException ConstraintViolated
     */
    void undoableEditWillHappen(UndoableEditEvent event) throws DoEditException.ConstraintViolated;
    
    void undoEditHappened(UndoableEditEvent event);
    
}
