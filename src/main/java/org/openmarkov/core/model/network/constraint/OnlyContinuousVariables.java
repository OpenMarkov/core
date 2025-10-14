/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

/**
 * Only continuous variables constraint.
 *
 * @author mpalacios based in OnlyDiscreteVariables class.
 * @version 1.0
 */

@Constraint(name = "OnlyContinuousVariables", defaultBehavior = ConstraintBehavior.OPTIONAL) public class OnlyContinuousVariables
		extends PNConstraint {

	@Override public boolean checkProbNet(ProbNet probNet) {
		List<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			if (variable.getVariableType() != VariableType.NUMERIC) {
				return false;
			}
		}
		return true;
	}
 
}
