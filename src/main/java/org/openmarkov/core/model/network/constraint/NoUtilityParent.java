/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

@Constraint(name = "NoUtilityParent", defaultBehavior = ConstraintBehavior.YES) public class NoUtilityParent
        extends PNConstraint {
    
    @Override public boolean checkProbNet(ProbNet probNet) {
        List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
        for (Node utilNode : utilityNodes) {
            List<Node> children = probNet.getChildren(utilNode);
            for (Node child : children) {
                if (child.getNodeType() != NodeType.UTILITY) {
                    return false;
                }
            }
        }
        return true;
    }
    
}
