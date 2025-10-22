/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint(name = "NoRevelationArc", defaultBehavior = ConstraintBehavior.YES) public class NoRevelationArc
		extends PNConstraint {
    
    @Override public void checkProbNet(ProbNet probNet, ConstraintChecker constraintChecker) {
		// TODO Auto-generated method stub
	}
 
}
