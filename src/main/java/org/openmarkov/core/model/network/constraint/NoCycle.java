/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

@Constraint(name = "NoCycle", defaultBehavior = ConstraintBehavior.YES) public class NoCycle extends PNConstraint {

	@Override public boolean checkProbNet(ProbNet probNet) {
		for (Node parent : probNet.getNodes()) {
			List<Node> children = probNet.getChildren(parent);
			for (Node child : children) {
				if (probNet.existsPath(child, parent, true)) {
					return false;
				}
			}
		}
		return true;
	}
 
}