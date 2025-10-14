/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.ArrayList;
import java.util.List;

@Constraint(name = "DistinctVariableNames", defaultBehavior = ConstraintBehavior.YES)
public class DistinctVariableNames extends PNConstraint {
    
    @Override public boolean checkProbNet(ProbNet probNet) {
        List<Variable> variablesProbNet = probNet.getVariables();
        List<String> variablesProbNetNames = new ArrayList<>();
        for (Variable variable : variablesProbNet) {
            variablesProbNetNames.add(variable.getName());
        }
        // check that new variables have distinct names
        int numVariables = variablesProbNetNames.size();
        for (int i = 0; i < numVariables - 1; i++) {
            for (int j = i + 1; j < numVariables; j++) {
                if (variablesProbNetNames.get(i).compareTo(variablesProbNetNames.get(j)) == 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
}
