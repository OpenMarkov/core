/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.Node;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial") public class NodesMissingPoliciesException extends OpenMarkovException2 {
    
    private final Node conditioningDecision;
    private final List<Node> nodesWithoutPolicy;
    
    public NodesMissingPoliciesException(Node conditioningDecision, List<Node> nodesWithoutPolicy) {
        this.conditioningDecision = conditioningDecision;
        this.nodesWithoutPolicy = nodesWithoutPolicy;
    }
    
    @Override protected @Nullable String getExceptionMessage() {
        var nodesNames = this.nodesWithoutPolicy.stream().map(Node::getName)
                                                .map(name -> "\t-" + name)
                                                .collect(Collectors.joining("\n"));
        
        return "There are " + this.nodesWithoutPolicy.size() + " node(s) other than " + conditioningDecision.getName() + " without policy:" + nodesNames;
    }
    
    @Override protected @Nullable String getExceptionTitle() {
        return "Not all nodes have policies";
    }
    
}
