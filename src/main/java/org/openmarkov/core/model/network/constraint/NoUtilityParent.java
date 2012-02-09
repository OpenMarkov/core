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

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.LinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint (name = "NoUtilityParent", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class NoUtilityParent extends PNConstraint  {

	public NoUtilityParent(ProbNet probNet) {
		super(probNet);
	}
    @Override
    public boolean checkProbNet (ProbNet probNet)
    {
        ArrayList<ProbNode> utilityNodes = probNet.getProbNodes (NodeType.UTILITY);
        for (ProbNode utilNode : utilityNodes)
        {
            ArrayList<Node> children = utilNode.getNode ().getChildren ();
            for (Node child : children)
            {
                ProbNode probChild = (ProbNode) child.getObject ();
                if (probChild.getNodeType () != NodeType.UTILITY)
                {
                    return false;
                }
            }
        }
        return true;
    }

	@Override
	public boolean checkEdit(ProbNet probNet, PNEdit edit)
			throws NotEnoughMemoryException, NonProjectablePotentialException,
			WrongCriterionException {
		
        ArrayList<PNEdit> edits = UtilConstraints.getEditsType (edit, AddLinkEdit.class);
		
		for (PNEdit simpleEdit : edits) {
			if (((AddLinkEdit)simpleEdit).isDirected()) { 
				Variable variable1 = ((AddLinkEdit)simpleEdit).getVariable1(); 
				ProbNode node1 = probNet.getProbNode(variable1);
				if(node1.getNodeType()== NodeType.UTILITY)
				{
					Variable variable2 = ((AddLinkEdit)simpleEdit).getVariable2(); 
					ProbNode node2 = probNet.getProbNode(variable2);
					if(node2.getNodeType()!= NodeType.UTILITY)
					{
					return false;
					}
				}
			}
		}
		
        ArrayList<PNEdit> linkEdits = UtilConstraints.getEditsType (edit, LinkEdit.class);
		for (PNEdit simpleEdit : linkEdits) {
			if (((LinkEdit)simpleEdit).isDirected()) { 
				Variable variable1 = ((AddLinkEdit)simpleEdit).getVariable1(); 
				ProbNode node1 = probNet.getProbNode(variable1);
				if(node1.getNodeType()== NodeType.UTILITY)
				{
					Variable variable2 = ((AddLinkEdit)simpleEdit).getVariable2(); 
					ProbNode node2 = probNet.getProbNode(variable2);
					if(node2.getNodeType()!= NodeType.UTILITY)
					{
					return false;
					}
				}
			}
		}
		return true;
	}

    @Override
    protected String getMessage ()
    {
        return "utility only have utility children";
    }


}
