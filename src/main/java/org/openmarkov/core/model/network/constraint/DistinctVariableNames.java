/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.ChangeVariableNameEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint (name = "DistinctVariableNames", defaultBehavior = ConstraintBehavior.YES)
public class DistinctVariableNames extends PNConstraint {

	@Override
	public boolean checkEdit(ProbNet probNet, PNEdit edit) 
			throws NotEnoughMemoryException, NonProjectablePotentialException,
			WrongCriterionException {
		ArrayList<PNEdit> edits = UtilConstraints.getEditsType(edit,
				AddVariableEdit.class);
		ArrayList<Variable> variablesProbNet = probNet.getVariables();
		ArrayList<String> variablesProbNetNames = new ArrayList<String>();
		for (Variable variable : variablesProbNet) {
			variablesProbNetNames.add(variable.getName());
		}

		// get new variables names
		ArrayList<String> newVariablesNames = new ArrayList<String>();
		for (PNEdit simpleEdit : edits) {
			newVariablesNames.add(((AddVariableEdit) simpleEdit).getVariable()
					.getName());
		}

		// check that new variables have distinct names
		int numNewVariables = newVariablesNames.size();
		for (int i = 0; i < numNewVariables - 1; i++) {
			for (int j = i + 1; j < numNewVariables; j++) {
				if (newVariablesNames.get(i)
						.compareTo(newVariablesNames.get(j)) == 0) {
					return false;
				}
			}
		}

		// check that new variables names are distinct than probNet variables
		// names
		for (String newVariableName : newVariablesNames) {
			for (String variableProbNetName : variablesProbNetNames) {
				if (variableProbNetName.compareTo(newVariableName) == 0) {
					return false;
				}
			}
		}

		// ChangeVariableName Edit
        edits = UtilConstraints.getEditsType (edit, ChangeVariableNameEdit.class);
		for (PNEdit simpleEdit : edits) {
			String newName = ((ChangeVariableNameEdit) simpleEdit).getNewName();
			for (String variableProbNetName : variablesProbNetNames) {
				if ((newName.contentEquals(variableProbNetName))) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<Variable> variablesProbNet = probNet.getVariables();
		ArrayList<String> variablesProbNetNames = new ArrayList<String>();
		for (Variable variable : variablesProbNet) {
			variablesProbNetNames.add(variable.getName());
		}

		// check that new variables have distinct names
		int numVariables = variablesProbNetNames.size();
		for (int i = 0; i < numVariables - 1; i++) {
			for (int j = i + 1; j < numVariables; j++) {
				if (variablesProbNetNames.get(i).compareTo(
						variablesProbNetNames.get(j)) == 0) {
					return false;
				}
			}
		}

		return true;
	}

    @Override
    protected String getMessage ()
    {
        return "adding variable with a name that already exists in probNet.";
    }

}
