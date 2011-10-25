package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddLinkEdit;

import openmarkov.exceptions.CanNotDoEditException;
import openmarkov.exceptions.ConstraintViolationException;
import openmarkov.exceptions.NonProjectablePotentialException;
import openmarkov.exceptions.NotEnoughMemoryException;
import openmarkov.exceptions.WrongCriterionException;
import openmarkov.graphs.Graph;
import openmarkov.graphs.Node;
import openmarkov.gui.edit.LinkEdit;
import openmarkov.networks.NodeType;
import openmarkov.networks.ProbNet;
import openmarkov.networks.ProbNode;
import openmarkov.networks.Variable;
import openmarkov.undo.PNUndoableEditEvent;
import openmarkov.undo.edit.PNEdit;

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
