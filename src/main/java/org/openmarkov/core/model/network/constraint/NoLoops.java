/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

@Constraint(name = "NoLoops", defaultBehavior = ConstraintBehavior.OPTIONAL) public class NoLoops extends PNConstraint {

	@Override public boolean checkProbNet(ProbNet probNet) {
		List<Node> nodesGraph = probNet.getNodes();
		boolean probNetOK = true;
		boolean directed;
		for (Node node1 : nodesGraph) {
			List<Node> neighbors = probNet.getNeighbors(node1);
			for (Node node2 : neighbors) {
				if (probNet.isChild(node1, node2)) {
					probNet.removeLink(node2, node1, true);
					directed = true;
				} else if (probNet.isSibling(node1, node2)) {
					probNet.removeLink(node1, node2, false);
					directed = false;
				} else {
					continue;
				}
				if (probNet.existsPath(node1, node2, false)) {
					probNetOK = false;
				}
				probNet.addLink(node1, node2, directed);
				if (!probNetOK) {
                    return false;
				}
			}
		}
        return true;
	}
 
}
