/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.NodeStateEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.StateAction;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

@Constraint(name = "NoValidStateName", defaultBehavior = ConstraintBehavior.YES) public class ValidStateName
        extends PNConstraint {
    
    enum TypeError {
        IS_EMPTY_NAME, IS_NAME_ALREADY_EXIST;
    }
    
    
    @Override public boolean checkProbNet(ProbNet probNet) {
        List<Variable> variables = probNet.getVariables();
        for (Variable variable : variables) {
            State[] states = variable.getStates();
            for (State state : states) {
                String name = state.getName();
                if ((name == null) || (name.contentEquals(""))) {
                    return false;
                }
                if (!variable.chekNewStateName(name)) {
                    return false;
                }
            }
            
        }
        return true;
    }
    
}
