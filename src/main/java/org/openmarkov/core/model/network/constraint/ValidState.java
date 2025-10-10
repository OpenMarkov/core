/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.NodeStateEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.StateAction;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * Checks that the state field is filled and there isn't any node with the same
 * name.
 */
public class ValidState extends PNConstraint {
    
    /**
     * This method checks that the state field is filled and there isn't any
     * node with the same name.
     *
     * @return true, if the state field isn't empty and there isn't any node with
     * this name; otherwise, false.
     */
    public static boolean checkState(String newState, Node node, StateAction stateAction) {
        return switch (stateAction) {
            case ADD, RENAME -> !((newState == null) || newState.isEmpty() || ValidState.existState(newState, node));
            case REMOVE, MODIFY_VALUE_INTERVAL, MODIFY_DELIMITER_INTERVAL, DOWN, UP -> true;
        };
    }
    
    /**
     * This method checks if exists the state specified.
     *
     * @param node  node to search.
     * @param state state to be checked
     *
     * @return true if the state exists; otherwise, false.
     */
    public static boolean existState(String state, Node node) {
        for (State states : node.getVariable().getStates()) {
            if (states.getName().equalsIgnoreCase(state)) {
                return true;
            }
        }
        return false;
    }
    
    @Override public boolean checkProbNet(ProbNet probNet) {
        List<Variable> variables = probNet.getVariables();
        for (Variable variable : variables) {
            String name = variable.getName();
            if ((name == null) || (name.contentEquals(""))) {
                return false;
            }
        }
        return true;
    }
    
}
