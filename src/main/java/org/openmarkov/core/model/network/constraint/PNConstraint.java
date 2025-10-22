/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.PNUndoableEditListener;
import org.openmarkov.core.annotation.ImplementationRequirements;
import org.openmarkov.core.annotation.RequiredConstructor;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.network.ProbNet;


/**
 * A constraint is a condition that a model must fulfill.<p>
 * This class implements {@code PNUndoableEditListener} because like
 * that all the classes that implement this interface will be able to receive
 * the same messages than {@code UndoableEditListener} and they will be
 * able to be referenced with same identifier.
 */
@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public abstract class PNConstraint implements PNUndoableEditListener, ClassLocalizable {
    
    /**
     * @param probNet {@code ProbNet}
     * @return {@code true} if the {@code probNet} fulfills the
     * constraint.
     */
    public abstract void checkProbNet(ProbNet probNet, ConstraintChecker constraintChecker);
    
    public final boolean isMetBy(ProbNet probNet) {
        ConstraintChecker constraintChecker = new ConstraintChecker(probNet);
        checkProbNet(probNet, constraintChecker);
        try {
            constraintChecker.buildAndThrow();
            return true;
        } catch (ConstraintViolatedException e) {
            return false;
        }
    }
    
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
