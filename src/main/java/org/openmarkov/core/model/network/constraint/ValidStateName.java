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
	
	private TypeError typeError;

	@Override
	public boolean checkEdit(ProbNet probNet, PNEdit edit) {
		// NodeStateEdit
		List<PNEdit> edits = UtilConstraints.getSimpleEditsByType(edit,
				NodeStateEdit.class);
		for (PNEdit simpleEdit : edits) {
			NodeStateEdit nodeStateEdit = (NodeStateEdit) simpleEdit;
			StateAction stateAction = nodeStateEdit.getStateAction();
			String name = (stateAction != StateAction.RENAME) ? nodeStateEdit
					.getNewState().getName() : nodeStateEdit.getNewName();

			// Get the trim and lowerCase state


			switch (stateAction) {
			case ADD:
                String trimmedLowerName = name.toLowerCase();
                if (trimmedLowerName.isBlank()) {
                    this.typeError = TypeError.IS_EMPTY_NAME;
					return false;
				}
				if (!nodeStateEdit.getNode().getVariable().chekNewStateName(trimmedLowerName)) {
                    this.typeError = TypeError.IS_NAME_ALREADY_EXIST;
					return false;
				}
				break;
			case RENAME:
                String trimmedName = name.trim();
                if (trimmedName.isBlank()) {
                    this.typeError = TypeError.IS_EMPTY_NAME;
					return false;
				}
				if (!nodeStateEdit.getNode().getVariable().chekNewStateName(trimmedName)) {
                    this.typeError = TypeError.IS_NAME_ALREADY_EXIST;
					return false;
				}
				break;
			default:
				break;
			}
		}
		return true;
	}

	@Override public boolean checkProbNet(ProbNet probNet) {
		List<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			State[] states = variable.getStates();
			for (State state : states) {
				String name = state.getName();
				if ((name == null) || (name.contentEquals(""))) {
                    this.typeError = TypeError.IS_EMPTY_NAME;
					return false;
				} else if (!variable.chekNewStateName(name)) {
                    this.typeError = TypeError.IS_NAME_ALREADY_EXIST;
					return false;
				}
			}

		}
		return true;
	}

	@Override protected String constraintDescription() {
        return switch (this.typeError) {
            case IS_EMPTY_NAME -> "InvalidStateNameEmptyException";
            case IS_NAME_ALREADY_EXIST -> "InvalidStateNameDuplicatedException";
        };
	}

}
