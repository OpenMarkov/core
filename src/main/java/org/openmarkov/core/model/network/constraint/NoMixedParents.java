/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

/**
 * This class implements the NoMixedParents constraint, which establishes that all the parents
 *  of a utility node belong to only one of these two sets of parents:
 *  - chance and decision nodes
 *  - utility nodes
 *  @author ckonig
 */
@Constraint(name = "NoMixedParents", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class NoMixedParents extends PNConstraint {
    
    @Override public void checkProbNet(ProbNet probNet, ConstraintChecker constraintChecker) {
        for (Node utilityNode : probNet.getNodes(NodeType.UTILITY)) {
            List<Node> parents = probNet.getParents(utilityNode);
            for (Node parent : parents) {
                boolean metCondition = parentNodeIsNotMixed(parent);
                if (!metCondition) {
                    constraintChecker.addException(new ConstraintViolatedException.ParentCannotBeMixed(this, utilityNode, parent));
                }
            }
        }
    }
    
    public static boolean parentNodeIsNotMixed(Node parent) {
        NodeType parentNodeType = parent.getNodeType();
        boolean metCondition = parentNodeType == NodeType.UTILITY || parentNodeType == NodeType.CHANCE || parentNodeType == NodeType.DECISION;
        return metCondition;
    }
 
}
