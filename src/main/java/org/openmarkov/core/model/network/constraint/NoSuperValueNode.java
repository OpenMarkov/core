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
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;


@Constraint (name = "NoSuperValueNodes", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class NoSuperValueNode extends PNConstraint {
	
	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> probNodes = probNet.getProbNodes();
		for (ProbNode probNode : probNodes) {
			NodeType nodeType = probNode.getNodeType();
			if ((nodeType == NodeType.SV_PRODUCT) || 
					(nodeType == NodeType.SV_SUM)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkEdit(ProbNet probNet, PNEdit edit) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		// AddVariableEdit
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(edit, AddVariableEdit.class);
		for (PNEdit simpleEdit : edits) {
			NodeType nodeType = ((AddVariableEdit)simpleEdit).getNodeType();
			if ((nodeType == NodeType.SV_PRODUCT) || 
					(nodeType == NodeType.SV_SUM)) {
				return false;
			}
		}
		return true;
	}

    @Override
    protected String getMessage ()
    {
        return "adding a super value node is not allowed";
    }
    
}
