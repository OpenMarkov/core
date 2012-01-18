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

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint(name = "NoMultipleLinks", defaultBehavior = ConstraintBehavior.YES)
public class NoMultipleLinks extends PNConstraint {

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		Graph graph = probNet.getGraph();
		List<Node> nodesGraph = graph.getNodes();
		for (Node node : nodesGraph) {

			for (Link link : node.getLinks()) {
				Node node1 = link.getNode1();
				Node node2 = link.getNode2();
				if (!link.isDirected()) {
					// undirected link
					if ((graph.getLink(node1, node2, true) != null)
							|| (graph.getLink(node2, node1, true) != null)) {
						return false;
					}
				} else {// directed link
					if ((graph.getLink(node1, node2, false) != null)) {
						return false;
					}
				}
			}

		}
		return true;
	}

	@Override
    public boolean checkEdit (ProbNet probNet, PNEdit edit)
            throws NotEnoughMemoryException,
            NonProjectablePotentialException,
            WrongCriterionException
        {
		List<PNEdit> edits = UtilConstraints.getEditsType(edit,
				AddLinkEdit.class);
		for (PNEdit simpleEdit : edits) {

			// adding an undirected link
			if (!((AddLinkEdit) simpleEdit).isDirected()) {

				Graph graph = probNet.getGraph();
				Variable variable1 = ((AddLinkEdit) simpleEdit).getVariable1();
				Node node1 = probNet.getProbNode(variable1).getNode();
				Variable variable2 = ((AddLinkEdit) simpleEdit).getVariable2();
				Node node2 = probNet.getProbNode(variable2).getNode();
				// neither a directed link from node1 -> node2 nor node2 ->
				// node1 may exist
				if ((graph.getLink(node1, node2, true) != null)
						|| (graph.getLink(node2, node1, true) != null)) {
					return false;
				}

			}

			// adding a directed link
			if (((AddLinkEdit) simpleEdit).isDirected()) {
				Graph graph = probNet.getGraph();
				Variable variable1 = ((AddLinkEdit) simpleEdit).getVariable1();
				Node node1 = probNet.getProbNode(variable1).getNode();
				Variable variable2 = ((AddLinkEdit) simpleEdit).getVariable2();
				Node node2 = probNet.getProbNode(variable2).getNode();

				if (graph.getLink(node1, node2, false) != null) {
					return false;
				}

			}

		}
		return true;
	}

	@Override
	protected String getMessage() {

		return " no multiple links allowed.";
	}
}