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

/**
 * This class implements the DistinctLinks constraint, which establishes that the network
 * can not have two equal links.
 *
 * @author ckonig
 * @author manuel arias
 *
 */
@Constraint(name = "DistinctLinks", defaultBehavior = ConstraintBehavior.YES)
public class DistinctLinks extends PNConstraint {
    
    @Override public boolean checkProbNet(ProbNet probNet) {
        List<Node> nodes = probNet.getNodes();
        for (Node node : nodes) {
            if (probNet.getNumLinks(node) > (
                    probNet.getNumChildren(node) + probNet.getNumParents(node) + probNet.getNumSiblings(node)
            )) {
                return false;
            }
        }
        return true;
    }
    
    
    public static boolean checkLink(ProbNet graph, Node node1, Node node2, boolean directed) {
        return !(
                (graph.getLink(node1, node2, directed) != null) || (
                        !directed && graph.getLink(node2, node1, directed) != null
                )
        );
    }
    
}
