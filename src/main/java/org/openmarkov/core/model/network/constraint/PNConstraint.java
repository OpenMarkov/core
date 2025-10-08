/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.annotation.ImplementationRequirements;
import org.openmarkov.core.annotation.RequiredConstructor;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.network.ProbNet;

import javax.swing.event.UndoableEditEvent;

/**
 * A constraint is a condition that a model must fulfill.<p>
 * This class implements {@code PNUndoableEditListener} because like
 * that all the classes that implement this interface will be able to receive
 * the same messages than {@code UndoableEditListener} and they will be
 * able to be referenced with same identifier.
 */
@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public abstract class PNConstraint implements PNUndoableEditListener, Checkable, ClassLocalizable {
    
    @Override public void undoableEditHappened(UndoableEditEvent e) {
        // Do nothing
    }
    
    /**
     * @param probNet {@code ProbNet}
     * @return {@code true} if the {@code probNet} fulfills the
     * constraint.
     */
    @Override public abstract boolean checkProbNet(ProbNet probNet);

	/*
	//TODO: Extract and implement
	public abstract String getName();
	*/
    
    @Override public String toString() {
        return this.localize();
    }
    
    @Override public boolean equals(Object paramObject) {
        return (paramObject.getClass() == this.getClass());
    }
    
    @Override public int hashCode() {
        int hashCode = 17 + this.getClass().hashCode();
        return hashCode;
    }
    
}
