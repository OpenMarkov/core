/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

@Constraint(name = "OnlyDirectedLinks", defaultBehavior = ConstraintBehavior.YES) public class OnlyDirectedLinks
		extends PNConstraint {

	@Override public boolean checkProbNet(ProbNet probNet) {
		List<Node> nodes = probNet.getNodes();
		for (Node node : nodes) {
			if (probNet.getNumSiblings(node) != 0) {
				return false;
			}
		}
		return true;
	}
 
}
