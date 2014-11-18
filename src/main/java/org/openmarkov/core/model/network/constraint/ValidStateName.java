/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import java.util.List;

import org.openmarkov.core.action.NodeStateEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint(name = "NoValidStateName", defaultBehavior = ConstraintBehavior.YES)
public class ValidStateName extends PNConstraint {

	// Flag of the error
	private int type_error;
	// Constants for possible errors
	private final int IS_EMPTY_NAME = 0;
	private final int IS_NAME_ALREADY_EXIST = 1;

	@Override
	public boolean checkEdit(ProbNet probNet, PNEdit edit)
			throws NonProjectablePotentialException, WrongCriterionException {
		// NodeStateEdit
		List<PNEdit> edits = UtilConstraints.getSimpleEditsByType(edit,
				NodeStateEdit.class);
		for (PNEdit simpleEdit : edits) {
			String name = ((NodeStateEdit) simpleEdit).getNewState().getName();

			// Get the trim and lowerCase state
			name = name.trim();
			name = name.toLowerCase();
			
			switch (((NodeStateEdit) simpleEdit).getStateAction()) {
			case ADD:
			case RENAME:
				if ((name == null) || (name.contentEquals(""))) {
					type_error = IS_EMPTY_NAME;
					return false;
				}
				if (!((NodeStateEdit) simpleEdit).getNode().getVariable()
						.chekNewStateName(name)) {
					type_error = IS_NAME_ALREADY_EXIST;
					return false;
				}
				break;
			}
		}
		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		List<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			State[] states = variable.getStates();
			for (State state : states) {
				String name = state.getName();
				if ((name == null) || (name.contentEquals(""))) {
					type_error = IS_EMPTY_NAME;
					return false;
				} else if (!variable.chekNewStateName(name)) {
					type_error = IS_NAME_ALREADY_EXIST;
					return false;
				}
			}

		}
		return true;
	}

	@Override
	protected String getMessage() {
		switch (type_error) {
		case IS_EMPTY_NAME:
			return "there should be no empty names";
		case IS_NAME_ALREADY_EXIST:
			return "There is already a state with that name in the variable.";
		default:
			return "Unknown problem";

		}
	}

}
