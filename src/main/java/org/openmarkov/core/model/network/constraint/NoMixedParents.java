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
@Constraint(name = "NoMixedParents", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class NoMixedParents extends PNConstraint {

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		List<ProbNode> utilityNodes = probNet.getProbNodes(NodeType.UTILITY);

		for (ProbNode utilNode : utilityNodes) {
			boolean utilityParent = false;
			boolean chanceOrDecisionParent = false;
			List<Node> parents = utilNode.getNode().getParents();
			for (Node parent : parents) {

				ProbNode probParent = (ProbNode) parent.getObject();
				if (probParent.getNodeType() == NodeType.UTILITY) {
					utilityParent = true;

				}
				if (probParent.getNodeType() == NodeType.CHANCE
						|| probParent.getNodeType() == NodeType.DECISION) {

					chanceOrDecisionParent = true;
				}
				if (utilityParent && chanceOrDecisionParent) {
					return false;
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
			if (((AddLinkEdit) simpleEdit).isDirected()) {

				Variable variable2 = ((AddLinkEdit) simpleEdit).getVariable2();
				ProbNode node2 = probNet.getProbNode(variable2);

				if (node2.getNodeType() == NodeType.UTILITY) {
					boolean utilityParent = false;
					boolean chanceOrDecisionParent = false;

					Variable variable1 = ((AddLinkEdit) simpleEdit).getVariable1();
					ProbNode node1 = probNet.getProbNode(variable1);

					if (node1.getNodeType() == NodeType.UTILITY) {
						utilityParent = true;
					}
					if (node1.getNodeType() == NodeType.DECISION
							|| node1.getNodeType() == NodeType.UTILITY) {
						chanceOrDecisionParent = true;
					}

					for (Node parent : node2.getNode().getParents()) {

						ProbNode probParent = (ProbNode) parent.getObject();
						if (probParent.getNodeType() == NodeType.UTILITY) {
							utilityParent = true;

						}
						if (probParent.getNodeType() == NodeType.CHANCE
								|| probParent.getNodeType() == NodeType.DECISION) {

							chanceOrDecisionParent = true;
						}

						if (utilityParent && chanceOrDecisionParent) {
							return false;

						}
					}

				}
			}
		}

		List<PNEdit> edits2 = UtilConstraints.getEditsType(edit,
				LinkEdit.class);
		for (PNEdit simpleEdit : edits2) {
			if (((LinkEdit) simpleEdit).isDirected()) {

				Variable variable2 = ((AddLinkEdit) simpleEdit).getVariable2();
				ProbNode node2 = probNet.getProbNode(variable2);

				if (node2.getNodeType() == NodeType.UTILITY) {
					boolean utilityParent = false;
					boolean chanceOrDecisionParent = false;

					Variable variable1 = ((AddLinkEdit) simpleEdit).getVariable1();
					ProbNode node1 = probNet.getProbNode(variable1);

					if (node1.getNodeType() == NodeType.UTILITY) {
						utilityParent = true;
					}
					if (node1.getNodeType() == NodeType.DECISION
							|| node1.getNodeType() == NodeType.UTILITY) {
						chanceOrDecisionParent = true;
					}

					for (Node parent : node2.getNode().getParents()) {

						ProbNode probParent = (ProbNode) parent.getObject();
						if (probParent.getNodeType() == NodeType.UTILITY) {
							utilityParent = true;

						}
						if (probParent.getNodeType() == NodeType.CHANCE
								|| probParent.getNodeType() == NodeType.DECISION) {

							chanceOrDecisionParent = true;
						}

						if (utilityParent && chanceOrDecisionParent) {
							return false;

						}
					}

				}

			}
		}
		return true;
	}

	@Override
	protected String getMessage() {
		return "utility nodes can not have mixed parents.";
	}
}
