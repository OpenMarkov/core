package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.LinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

public class NoUtilityParent implements PNConstraint  {

	
	// Attributes.
	private static NoUtilityParent constraint = null;
	
	
	// Methods
	/** Singleton pattern.
	 * @return The unique instance. 
	 *  <code>NoUtilityParent</code> */
	public static PNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new NoUtilityParent();
		}
		return constraint;
	}
	
	
	
	
	
	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
			throws ConstraintViolationException, CanNotDoEditException,
			NotEnoughMemoryException, NonProjectablePotentialException,
			WrongCriterionException {
		if (!checkEvent(event)) {
			throw new ConstraintViolationException(
				"ConstraintViolationException adding link in probNet: "+
				"utility only have utility children");
		}
		
		
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<ProbNode> utilityNodes = probNet.getProbNodes(NodeType.UTILITY);
		for (ProbNode utilNode : utilityNodes) {
			ArrayList<Node> children=utilNode.getNode().getChildren();
			for (Node child : children) {
				
				ProbNode  probChild=(ProbNode) child.getObject();
				if(probChild.getNodeType()!= NodeType.UTILITY)
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
	
	
	public String toString() {
		return this.getClass().getName();
	}


}
