/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint(name = "MaxNumParents", defaultBehavior = ConstraintBehavior.OPTIONAL) public class MaxNumParents
		extends PNConstraint {

	private int maxNumParents;
    
    public int getMaxNumParents() {
        return this.maxNumParents;
    }
    
    public void setMaxNumParents(int maxNumParents) {
		this.maxNumParents = maxNumParents;
	}
    
    @Override public void checkProbNet(ProbNet probNet, ConstraintChecker constraintChecker) {
		for (Node child : probNet.getNodes()) {
			int numParents = probNet.getNumParents(child);
			if (numParents > maxNumParents) {
                constraintChecker.addException(new ConstraintViolatedException.NodeHasMoreParentsThanAllowed(this, child, numParents, maxNumParents));
			}
		}
    }
 
}
