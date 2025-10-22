/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.List;

/**
 * Inverts an existing link.
 */
@SuppressWarnings("serial") public final class InvertLinkEdit extends BaseLinkEdit {
    
    /**
     * parent node
     */
    private Node node1;
    /**
     * child node
     */
    private Node node2;
    
    /**
     * Parent node's old potentials
     */
    private List<Potential> parentOldPotentials;
    /**
     * Child node's old potentials
     */
    private List<Potential> childOldPotentials;
    
    // Constructor
    
    /**
     * @param probNet    {@code ProbNet}
     * @param variable1  {@code Variable}
     * @param variable2  {@code Variable}
     * @param isDirected {@code boolean}
     */
    public InvertLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, boolean isDirected) {
        super(probNet, variable1, variable2, isDirected);
        node1 = probNet.getNode(variable1.getName());
        node2 = probNet.getNode(variable2.getName());
    }
    
    
    @Override
    public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(DistinctLinks.class) instanceof DistinctLinks constraint) {
            Node node1 = probNet.getNode(this.getVariable1());
            Node node2 = probNet.getNode(this.getVariable2());
            boolean directed = this.isDirected();
            if (!DistinctLinks.checkLink(probNet, node2, node1, directed)) {
                constraintChecker.addException(new ConstraintViolatedException.LinkAlreadyExists(constraint, node2, node1));
            }
        }
        if (probNet.getConstraintOfClass(NoCycle.class) instanceof NoCycle constraint) {
            Node node1 = probNet.getNode(this.getVariable1());
            Node node2 = probNet.getNode(this.getVariable2());
            probNet.removeLink(node1, node2, true);
            boolean existsPath = probNet.existsPath(node1, node2, true);
            probNet.addLink(node1, node2, true);
            if (existsPath) {
                constraintChecker.addException(new ConstraintViolatedException.ThereIsACycle(constraint, node2, node1));
            }
        }
        if (probNet.getConstraintOfClass(NoMixedParents.class) instanceof NoMixedParents constraint) {
            Node node2 = probNet.getNode(this.getVariable2());
            if (node2.getNodeType() == NodeType.UTILITY) {
                Node node1 = probNet.getNode(this.getVariable1());
                if (NoMixedParents.parentNodeIsNotMixed(node2)) {
                    constraintChecker.addException(new ConstraintViolatedException.ParentCannotBeMixed(constraint, node1, node2));
                }
            }
        }
        if (probNet.getConstraintOfClass(NoMultipleLinks.class) instanceof NoMultipleLinks constraint) {
            Node node1 = probNet.getNode(this.getVariable1());
            Node node2 = probNet.getNode(this.getVariable2());
            boolean directed = this.isDirected();
            constraint.checkLink(probNet, constraintChecker, node2, node1, directed);
        }
        if (probNet.getConstraintOfClass(ModelNetworkConstraint.class) instanceof ModelNetworkConstraint constraint
                && !constraint.isLinkInversionAllowed() && !constraint.canEditBeDone(this)) {
            constraintChecker.addException(new ConstraintViolatedException.ModelDoesNotAllowInvertingLink(constraint, this.getVariable1(), this.getVariable2()));
        }
    }
    
    /**
     *
     * @throws DoEditException DoEditException
     */
    @Override public void doEdit() throws DoEditException.CannotInvertLink {
        // Remove links first
        probNet.removeLink(node1, node2, isDirected);
        if (node2.getNodeType() != NodeType.DECISION) {
            // Update potentials
            List<Potential> newPotentials = new ArrayList<>();
            this.childOldPotentials = node2.getPotentials();
            for (Potential oldPotential : childOldPotentials) {
                Potential newPotential = oldPotential.removeVariable(node1.getVariable());
                newPotentials.add(newPotential);
            }
            node2.setPotentials(newPotentials);
        }
        
        // Add inverse link
        probNet.addLink(node2, node1, isDirected);
        if (node2.getNodeType() != NodeType.DECISION) {
            this.parentOldPotentials = node1.getPotentials();
            List<Potential> newPotentials = new ArrayList<>();
            for (Potential oldPotential : parentOldPotentials) {
                // Update potential
                Potential newPotential = oldPotential.addVariable(node2.getVariable());
                newPotentials.add(newPotential);
            }
            node1.setPotentials(newPotentials);
        }
        
        // Checks that the inversion is legal, i.e. it does not produce cycles.
        if (!probNet.checkProbNet()) {
            undo();
            throw new DoEditException.CannotInvertLink(node1, node2, probNet, probNet.getUnsatisfiedConstraints());
        }
    }
    
    @Override public void undo() {
        super.undo();
        probNet.removeLink(variable2, variable1, isDirected);
        probNet.addLink(variable1, variable2, isDirected);
        node1.setPotentials(parentOldPotentials);
        node2.setPotentials(childOldPotentials);
    }
    
    /**
     * Method to compare two InvertLinkEdits comparing the names of
     * the source and destination variable alphabetically.
     *
     * @param obj InvertLinkEdit
     *
     * @return Result of the comparison
     */
    public int compareTo(InvertLinkEdit obj) {
        int result;
        
        if ((
                result = variable1.getName().compareTo(obj.getVariable1().
                                                          getName())
        ) != 0) {
            return result;
        }
        if ((
                result = variable2.getName().compareTo(obj.getVariable2().
                                                          getName())
        ) != 0) {
            return result;
        }
        return 0;
    }
    
    @Override public String getOperationName() {
        return "Invert link";
    }
    
    /**
     * This method assumes that the link is directed, otherwise has no sense.
     *
     * @return {@code String}
     */
    public String toString() {
        return getOperationName() + ": " + variable1 + "-->" + variable2 +
                " ==> " + variable2 + "-->" + variable1;
    }
    
    @Override public BaseLinkEdit getUndoEdit() {
        return new InvertLinkEdit(getProbNet(), getVariable2(), getVariable1(), isDirected());
    }
    
}