package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.LinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
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
	public boolean checkEvent(UndoableEditEvent event)
			throws NotEnoughMemoryException, NonProjectablePotentialException,
			WrongCriterionException {
		
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddLinkEdit.class);
		ProbNet probNet = ((PNUndoableEditEvent)event).getProbNet();
		
		
		for (PNEdit edit : edits) {
			if (((AddLinkEdit)edit).isDirected()) { 
				Variable variable1 = ((AddLinkEdit)edit).getVariable1(); 
				ProbNode node1 = probNet.getProbNode(variable1);
				if(node1.getNodeType()== NodeType.UTILITY)
				{
					Variable variable2 = ((AddLinkEdit)edit).getVariable2(); 
					ProbNode node2 = probNet.getProbNode(variable2);
					if(node2.getNodeType()!= NodeType.UTILITY)
					{
					return false;
					}
				}
			}
		}
		
		ArrayList<PNEdit> edits2 = 
			UtilConstraints.getEditsType(event, LinkEdit.class);
		for (PNEdit edit : edits2) {
			if (((LinkEdit)edit).isDirected()) { 
				Variable variable1 = ((AddLinkEdit)edit).getVariable1(); 
				ProbNode node1 = probNet.getProbNode(variable1);
				if(node1.getNodeType()== NodeType.UTILITY)
				{
					Variable variable2 = ((AddLinkEdit)edit).getVariable2(); 
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
