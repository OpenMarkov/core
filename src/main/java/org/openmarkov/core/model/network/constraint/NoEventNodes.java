/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

/**
 *  This class implements the constraint of not allowing event nodes in the network. It is set by default.
 */
@Constraint(name = "NoEventNodes", defaultBehavior = ConstraintBehavior.YES)
public class NoEventNodes extends PNConstraint {

	@Override public boolean checkProbNet(ProbNet probNet) {
		List<Node> eventNodes = probNet.getNodes(NodeType.EVENT);
		return eventNodes.isEmpty();
	}




	@Override public boolean checkEdit(ProbNet probNet, PNEdit edit)
			throws NonProjectablePotentialException, WrongCriterionException {

		List<PNEdit> edits = UtilConstraints.getSimpleEditsByType(edit, AddNodeEdit.class);
		for (PNEdit simpleEdit : edits) {
			if (((AddNodeEdit) simpleEdit).getNodeType() == NodeType.EVENT) {
				return false;
			}
		}
		return true;

	}

	@Override protected String getMessage() {
		return "No event nodes allowed";
	}

}
