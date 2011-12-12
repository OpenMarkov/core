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

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.VariableTypeEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint (name = "OnlyFiniteStatesVariables", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class OnlyFiniteStatesVariables extends PNConstraint {

	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddVariableEdit.class);
		
		for (PNEdit edit : edits) {
			Variable variable = ((AddVariableEdit)edit).getVariable(); 
			NodeType nodetype=((AddVariableEdit)edit).getNodeType();
			

			if(nodetype == NodeType.CHANCE || nodetype == NodeType.DECISION )
			{
				VariableType varType=	variable.getVariableType();

				if(!(varType ==VariableType.FINITE_STATES || varType == VariableType.DISCRETIZED))
				{
					return false;
				}	
			}
		}
		edits = 
			UtilConstraints.getEditsType(event, VariableTypeEdit.class);
		for (PNEdit edit : edits) {

			NodeType nodetype=((VariableTypeEdit)edit).getProbNode().getNodeType();

			if(nodetype == NodeType.CHANCE || nodetype == NodeType.DECISION )
			{
				VariableType newType = ((VariableTypeEdit)edit).getNewVariableType(); 

				if(!(newType ==VariableType.FINITE_STATES || newType == VariableType.DISCRETIZED))
				{
					return false;
				}	
			}
		}

		return true;
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<Variable> variables = probNet.getVariables();
		for (Variable variable : variables) {
			ProbNode node= probNet.getProbNode(variable);
			if(node.getNodeType()== NodeType.CHANCE || node.getNodeType()== NodeType.DECISION )
			{

				VariableType varType=	variable.getVariableType();

				if(!(varType ==VariableType.FINITE_STATES || varType == VariableType.DISCRETIZED))
				{
					return false;
				}	
			}
		}
		return true;
	}

    @Override
    protected String getMessage ()
    {
        // TODO Auto-generated method stub
        return "all chance and decision variables must be finite state or discrete";
    }

}
