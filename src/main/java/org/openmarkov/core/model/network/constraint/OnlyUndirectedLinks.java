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

@Constraint(name = "OnlyUndirectedLinks", defaultBehavior = ConstraintBehavior.NO) public class OnlyUndirectedLinks
		extends PNConstraint {
    
    // Attributes.
    
    @Override public boolean checkProbNet(ProbNet probNet) {
		List<Node> nodes = probNet.getNodes();
		for (Node node : nodes) {
			// Only check children because with this is enough
			// to look for directed links
			if (probNet.getNumChildren(node) != 0) {
				return false;
			}
		}
		return true;
	}
 
}
