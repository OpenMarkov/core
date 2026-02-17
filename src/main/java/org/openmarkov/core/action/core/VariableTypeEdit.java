/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.OnlyDiscreteVariables;
import org.openmarkov.core.model.network.constraint.OnlyFiniteStatesVariables;
import org.openmarkov.core.model.network.constraint.OnlyNumericVariables;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.operation.Util;
import org.openmarkov.core.action.base.PNEdit;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") public class VariableTypeEdit extends PNEdit {
	// private ProbNet probNet;
	private Node node;
	private VariableType newType;
	private VariableType currentType;
	private State[] currentStates;

	public VariableTypeEdit(Node node, VariableType newType) {
		super(node.getProbNet());
		this.node = node;
		this.newType = newType;
		this.currentType = node.getVariable().getVariableType();

	}
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(OnlyDiscreteVariables.class) instanceof OnlyDiscreteVariables constraint) {
            if (this.newType != VariableType.DISCRETIZED) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyDiscreteVariablesAllowed(constraint, this.getNode()
                                                                                                                            .getVariable()));
            }
        }
        if (probNet.getConstraintOfClass(OnlyFiniteStatesVariables.class) instanceof OnlyFiniteStatesVariables constraint) {
            if (!OnlyFiniteStatesVariables.nodeIsFinite(this.node.getNodeType(), this.newType)) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyFiniteStatesAllowed(constraint, this.getNode()
                                                                                                                       .getVariable()));
            }
        }
        if (probNet.getConstraintOfClass(OnlyNumericVariables.class) instanceof OnlyNumericVariables constraint) {
            if (this.newType != VariableType.NUMERIC) {
                constraintChecker.addException(new ConstraintViolatedException.OnlyNumericVariablesAllowed(constraint, this.getNode()
                                                                                                                           .getVariable()));
            }
        }
    }
	
	@Override protected void doEdit() {
        // Save the current states
        currentStates = node.getVariable().getStates();


        // Restore the states
        node.getVariable().setStates(currentStates.length == 1
                ? node.getProbNet().getDefaultStates()
                : currentStates);

        if (currentType != newType) {
			node.setVariableTypeConsistently(newType);
        }


        node.resetLink();

	}
    
    @Override public void undo() {
		node.getVariable().setVariableType(currentType);
		node.getVariable().setStates(currentStates);
	}


	public Node getNode() {

		return this.node;
	}


}
