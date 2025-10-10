/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SumPotential;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a directed or undirected link between two nodes associated to two
 * variables in a {@code ProbNet}
 */
public class AddLinkEdit extends BaseLinkEdit {
    
    /**
     * Resulting link of addition or removal.
     */
    protected Link<Node> link;
    /**
     * The new {@code Potential} of the second node
     */
    protected List<Potential> newPotentials = new ArrayList<>();
    /**
     * parent node
     */
    protected Node node1;
    /**
     * child node
     */
    protected Node node2;
    private boolean updatePotentials;
    /**
     * The last {@code Potential} of the second node before the edition
     */
    private List<Potential> oldPotentials;
    
    // Constructor
    
    /**
     * @param probNet    {@code ProbNet}
     * @param variable1  {@code Variable}
     * @param variable2  {@code Variable}
     * @param isDirected {@code boolean}
     */
    public AddLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, boolean isDirected,
                       boolean updatePotentials) {
        super(probNet, variable1, variable2, isDirected);
        
        node1 = probNet.getNode(variable1);
        node2 = probNet.getNode(variable2);
        this.updatePotentials = updatePotentials;
        this.link = null;
    }
    
    public AddLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, boolean isDirected) {
        this(probNet, variable1, variable2, isDirected, true);
    }
    
    @Override public void checkConstraintsWillBeMet() throws DoEditException.ConstraintViolated {
        if (probNet.getConstraintOfClass(DistinctLinks.class) instanceof DistinctLinks constraint) {
            Node node1 = probNet.getNode(this.getVariable1());
            Node node2 = probNet.getNode(this.getVariable2());
            boolean directed = this.isDirected();
            if (!DistinctLinks.checkLink(probNet, node1, node2, directed)) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(MaxNumParents.class) instanceof MaxNumParents constraint) {
            if (this.isDirected()) {
                Variable variable2 = this.getVariable2();
                Node node2 = probNet.getNode(variable2);
                int numParents = probNet.getNumParents(node2);
                if (numParents >= constraint.getMaxNumParents()) {
                    throw new DoEditException.ConstraintViolated(constraint);
                }
            }
        }
        if (probNet.getConstraintOfClass(NoAlwaysObservedDescendantOfDecision.class) instanceof NoAlwaysObservedDescendantOfDecision constraint) {
            List<Node> decisionNodes = probNet.getNodes(NodeType.DECISION);
            List<Node> alwaysObservedNodes = probNet.getNodes().stream().filter(Node::isAlwaysObserved).toList();
            AddLinkEdit addLinkEdit = this;
            if (addLinkEdit.isDirected()) { // checks constraint
                Node node1 = probNet.getNode(addLinkEdit.getVariable1());
                Node node2 = probNet.getNode(addLinkEdit.getVariable2());
                boolean isValid = !node1.isAlwaysObserved() || (NoAlwaysObservedDescendantOfDecision.itHasSomeAncestorInList(probNet, node1, decisionNodes)
                        && NoAlwaysObservedDescendantOfDecision.itHasSomeDescendantInList(probNet, node2, alwaysObservedNodes));
                if (!isValid) {
                    throw new DoEditException.ConstraintViolated(constraint);
                }
            }
        }
        if (probNet.getConstraintOfClass(NoBackwardLink.class) instanceof NoBackwardLink constraint) {
            if (!NoBackwardLink.allowedLink(this.getVariable1(), this.getVariable2())) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(NoCycle.class) instanceof NoCycle constraint) {
            Node node1 = probNet.getNode(this.getVariable1());
            Node node2 = probNet.getNode(this.getVariable2());
            if (probNet.existsPath(node2, node1, true)) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(NoLoops.class) instanceof NoLoops constraint) {
            Node node1 = probNet.getNode(this.getVariable1());
            Node node2 = probNet.getNode(this.getVariable2());
            if (probNet.existsPath(node2, node1, false)) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(NoMixedParents.class) instanceof NoMixedParents constraint) {
            if (this.isDirected()) {
                Node node2 = probNet.getNode(this.getVariable2());
                if (node2.getNodeType() == NodeType.UTILITY) {
                    Node node1 = probNet.getNode(this.getVariable1());
                    if (NoMixedParents.hasMixedParents(probNet, node1, node2)) {
                        throw new DoEditException.ConstraintViolated(constraint);
                    }
                }
            }
        }
        if (probNet.getConstraintOfClass(NoMultipleLinks.class) instanceof NoMultipleLinks constraint) {
            Node node1 = probNet.getNode(this.getVariable1());
            Node node2 = probNet.getNode(this.getVariable2());
            boolean directed = this.isDirected();
            if (!NoMultipleLinks.checkLink(probNet, node1, node2, directed)) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(NoSelfLoop.class) instanceof NoSelfLoop constraint) {
            if (this.getVariable1().equals(this.getVariable2())) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(NoSuperValueNode.class) instanceof NoSuperValueNode constraint) {
            if (this.getNode1().getNodeType() == NodeType.UTILITY) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(NoUtilityParent.class) instanceof NoUtilityParent constraint) {
            Node node1 = probNet.getNode(this.getVariable1());
            Node node2 = probNet.getNode(this.getVariable2());
            if (node1.getNodeType() == NodeType.UTILITY && node2.getNodeType() != NodeType.UTILITY) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(OnlyDirectedLinks.class) instanceof OnlyDirectedLinks constraint) {
            if (!this.isDirected()) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(OnlyUndirectedLinks.class) instanceof OnlyUndirectedLinks constraint) {
            if (this.isDirected()) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(ModelNetworkConstraint.class) instanceof ModelNetworkConstraint constraint
                && !constraint.isLinkAdditionAllowed() && !constraint.canEditBeDone(this)) {
            throw new DoEditException.ConstraintViolated(constraint);
        }
    }
    
    @Override public void doEdit() {
        probNet.addLink(node1, node2, isDirected);
        this.link = probNet.getLink(node1, node2, isDirected);
        if (updatePotentials) {
            this.oldPotentials = node2.getPotentials();
            // TODO Check if this UTILITY label is outdated
            if (node2.getNodeType() == NodeType.UTILITY && node2.onlyNumericalParents()) {
                // Add a default Sum potential to utility supervalue nodes
                for (Potential oldPotential : oldPotentials) {
                    // Update potential
                    List<Variable> variables = oldPotential.getVariables();
                    if (!variables.contains(node1.getVariable())) {
                        variables.add(node1.getVariable());
                    }
                    Potential newPotential = new SumPotential(variables, oldPotential.getPotentialRole());
                    newPotentials.add(newPotential);
                }
            } else {
                for (Potential oldPotential : oldPotentials) {
                    // Update potential
                    Potential newPotential = oldPotential.addVariable(node1.getVariable());
                    newPotentials.add(newPotential);
                }
            }
            node2.setPotentials(newPotentials);
        }
    }
    
    @Override public void doEdit(ProbNet probNet) throws DoEditException.ConstraintViolated {
        this.checkConstraintsWillBeMet();
        PNEdit.startEdit(this, probNet);
        this.doEdit();
        PNEdit.endEdit(this);
    }
    
    @Override public void undo() {
        super.undo();
        
        node2 = probNet.getNode(variable2.getName());
        
        if (updatePotentials) {
            node2.setPotentials(oldPotentials);
        }
        probNet.removeLink(variable1, variable2, isDirected);
    }
    
    /**
     * Method to compare two AddLinkEdits comparing the names of
     * the source and destination variable alphabetically.
     *
     * @param obj AddLinkEdit to be compared
     *
     * @return result of the comparison
     */
    public int compareTo(AddLinkEdit obj) {
        int result;
        
        if ((
                result = variable1.getName().compareTo(obj.getVariable1().
                                                          getName())
        ) != 0)
            return result;
        if ((
                result = variable2.getName().compareTo(obj.getVariable2().
                                                          getName())
        ) != 0)
            return result;
        return 0;
    }
    
    @Override public String getOperationName() {
        return "Add link";
    }
    
    /**
     * Gets the first {@code Node} object in the link.
     *
     * @return the first {@code Node} object in the link.
     */
    public Node getNode1() {
        return node1;
    }
    
    /**
     * Gets the second {@code Node} object in the link.
     *
     * @return the second {@code Node} object in the link.
     */
    public Node getNode2() {
        return node2;
    }
    
    /**
     * Returns the link.
     *
     * @return the link.
     */
    public Link<Node> getLink() {
        return link;
    }
    
    @Override public BaseLinkEdit getUndoEdit() {
        return new RemoveLinkEdit(getProbNet(), getVariable1(), getVariable2(), isDirected());
    }
}
