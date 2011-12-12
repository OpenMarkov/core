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

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.LinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint (name = "NoCycles", defaultBehavior = ConstraintBehavior.YES)
public class NoCycle extends PNConstraint {

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		Graph graph = probNet.getGraph();
		ArrayList<Node> nodesGraph = graph.getNodes();
		for (Node parent : nodesGraph) {
			ArrayList<Node> children = parent.getChildren();
			for (Node child : children) {
				if (graph.existsPath(child, parent, true)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	/** @param event <code>UndoableEditEvent</code>
	 * @return <code>true</code> if <code>event</code> comply with this 
	 *   constraint */
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddLinkEdit.class);
		ProbNet probNet = ((PNUndoableEditEvent)event).getProbNet();
		//int u=0;
		Graph graph = probNet.getGraph();
		for (PNEdit edit : edits) {
			if (((AddLinkEdit)edit).isDirected()) { // checks constraint
				Variable variable1 = ((AddLinkEdit)edit).getVariable1(); 
				Node node1 = probNet.getProbNode(variable1).getNode();
				Variable variable2 = ((AddLinkEdit)edit).getVariable2(); 
				Node node2 = probNet.getProbNode(variable2).getNode();
				if (graph.existsPath(node2, node1, true)) {
					return false;
				}
			}
		}
		ArrayList<PNEdit> edits2 = 
			UtilConstraints.getEditsType(event, LinkEdit.class);
		for (PNEdit edit : edits2) {
			if (((LinkEdit)edit).isDirected()) { // checks constraint
				Variable variable1 = ((LinkEdit)edit).getProbNode1().
					getVariable(); 
				Node node1 = ((LinkEdit)edit).getProbNode1().getNode();
				Node node2 = ((LinkEdit)edit).getProbNode2().getNode();
				if (graph.existsPath(node2, node1, true)) {
					return false;
				}
			}
		}
		return true;
	}

    @Override
    protected String getMessage ()
    {
        return "no cycles allowed";
    }
	
	
	
	

}