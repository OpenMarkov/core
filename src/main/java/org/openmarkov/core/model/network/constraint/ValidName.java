/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.NodeNameEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * checks that the name field is filled and there isn't any node with the same
 * name.
 */
public class ValidName extends PNConstraint {
    // Attributes.
    
    /**
     * This method checks that the name field is filled and there isn't any node
     * with the same name.
     *
     * @return true, if the name field isn't empty and there isn't any node with
     * this name; otherwise, false.
     */
    public static boolean checkName(String newName, String currentName, ProbNet probNet) {
        // boolean result = true;
        if ((newName == null) || newName.isEmpty()) {
            return false;
        }
        if (!currentName.equals(newName) && existNode(newName.toUpperCase(), probNet)) {
            return false;
        }
        /*
         * if (!result) { jTextFieldNodeName.requestFocus(); return false; }
         */
        return true;
    }
    
    /**
     * This method checks if exists the specified node.
     *
     * @param name name of the node to search.
     *
     * @return true if the node exists; otherwise, false.
     */
    public static boolean existNode(String name, ProbNet probNet) {
        probNet.getNode(name);
        return true;
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
