/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

public class BasicOperations {
    /**
     * The source probNet
     */
    // private static ProbNet sourceProbNet;
    private static TablePotential getUtilityFunction(Node utilityNode, EvidenceCase evidence) {
        TablePotential newPotential = null;
        Hashtable<Node, TablePotential> hashtable = new Hashtable<>();
        if (!isSuperValueNode(utilityNode)) {
            try {
                // We need to control the scenario where a numeric variable is father of a utility node
                // In this situation, we must use the evidence to project the table
                if (!utilityNode.hasNumericalParents()) {
                    newPotential = utilityNode.getPotentials().get(0).tableProject(null, null).get(0);
                } else {
                    newPotential = utilityNode.getPotentials().get(0).tableProject(evidence, null).get(0);
                }
            } catch (NonProjectablePotentialException | WrongCriterionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
        	for (Node node : utilityNode.getParents()) {
                hashtable.put(node, getUtilityFunction(node, evidence));
            }
            List<TablePotential> potentials = new ArrayList<TablePotential>(hashtable.values());
            Potential utilityPotential = utilityNode.getPotentials().get(0);
            if (utilityPotential instanceof SumPotential) {
                newPotential = DiscretePotentialOperations.sum(potentials);
            } else {
                newPotential = DiscretePotentialOperations.multiply(potentials);
            }
            newPotential.setUtilityVariable(utilityNode.getVariable());
        }
        return newPotential;
    }

    private static boolean isSumSuperValueNode(ProbNet network, Variable utilityVariable) {
        List<Potential> potentials = network.getNode(utilityVariable).getPotentials();
        return (!potentials.isEmpty() && potentials.get(0) instanceof SumPotential);
    }

    /**
     * @param network
     * @return A list of utility nodes that have no children
     */
    public static List<Variable> getTerminalUtilityVariables(ProbNet network) {
        List<Variable> utilityVariables = network.getVariables(NodeType.UTILITY);
        List<Variable> terminalUtilityNodes = new ArrayList<Variable>();
        for (Variable utilityVariable : utilityVariables) {
            Node utilityNode = network.getNode(utilityVariable);
            if (network.getNumChildren(utilityNode) == 0) {
                terminalUtilityNodes.add(utilityVariable);
            }
        }
        return terminalUtilityNodes;
    }

    /**
     * @param network
     * @return A list of utility nodes that have no children
     */
    public static List<Node> getTerminalUtilityNodes(ProbNet network) {
        List<Node> utilityNodes = network.getNodes(NodeType.UTILITY);
        List<Node> terminalUtilityNodes = new ArrayList<>();
        for (Node utilityNode : utilityNodes) {
            if (network.getNumChildren(utilityNode) == 0) {
                terminalUtilityNodes.add(utilityNode);
            }
        }
        return terminalUtilityNodes;
    }

    /**
     * @param sourceProbNet
     * @return A copy of the probNet after removing utility nodes.
     */
    public static ProbNet removeUtilityNodes(ProbNet sourceProbNet) {
        ProbNet network = sourceProbNet.copy();
        for (Variable utilityVariable : network.getVariables(NodeType.UTILITY)) {
            Node node = network.getNode(utilityVariable);
            network.removeNode(node);
        }
        return network;
    }

    /**
     * @param sourceProbNet
     * @param keepComponents
     * @param utilityVariableToKeep
     * @return A copy of the probNet by removing super-value nodes. When
     *         keepComponents is false the output network is equivalent to
     *         'sourceProbNet'. However, when keepComponents is true the output
     *         network has a utility node without children corresponding to each
     *         utility node in 'sourceProbNet', and the utility function is
     *         given explicitly in terms of the ancestors chance and decision
     *         nodes. Parameter 'leaveImplicitSum' only applies when
     *         'keepComponents' is false. When 'leaveImplicitSum' is true then
     *         the output is in the form of influence diagrams with an implicit
     *         sum like those processed by Jensen's variable elimination
     *         algorithm; otherwise the structure of super-value nodes is
     *         reduced into an only utility node. If 'utilityVariableToKeep' is
     *         different from null then it is the only potential to keep.
     *         Otherwise all the variables are considered.
     * @throws NodeNotFoundException
     * @throws NodeNotFoundException
     */
    public static ProbNet removeSuperValueNodes(ProbNet sourceProbNet, EvidenceCase evidence,
            boolean keepComponents, boolean leaveImplicitSum, Variable utilityVariableToKeep) {
        ProbNet network = sourceProbNet.copy();
        List<Node> utilityNodes = network.getNodes(NodeType.UTILITY);
        for (Node utilityNode : utilityNodes) {
            Variable utilityVariable = utilityNode.getVariable();
            if ((isSuperValueNode(utilityNode) && utilityVariableToKeep == null)
                    || utilityVariable == utilityVariableToKeep) {
                TablePotential potential = getUtilityFunction(utilityNode, evidence);
                List<Node> parents = network.getParents(utilityNode);
                // remove links between supervalue nodes and their utility
                // parents
                for (Node parent : parents) {
                    if (parent.getNodeType() == NodeType.UTILITY) {
                        network.removeLink(parent.getVariable(), utilityVariable, true);
                    }
                }
                // add links between of new potential of supervalue nodes
                for (Variable variable : potential.getVariables()) {
                    try {
                        network.addLink(variable, utilityVariable, true);
                    } catch (NodeNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                // sets the new potential
                List<Potential> newPotentials = new ArrayList<Potential>();
                newPotentials.add(potential);
                network.getNode(utilityVariable).setPotentials(newPotentials);
            }
        }
        if (!keepComponents) {
            List<Variable> nodesToKeep;
            if (utilityVariableToKeep == null) {
                if (leaveImplicitSum) {
                    // Get the nodes such as there is an implicit sum
                    // between them
                    nodesToKeep = getUtilityNodesToKeepImplicitSum(sourceProbNet);
                } else {
                    nodesToKeep = getTerminalUtilityVariables(sourceProbNet);
                }
            } else {
                nodesToKeep = new ArrayList<Variable>();
                nodesToKeep.add(utilityVariableToKeep);
            }
            for (Node utilityNode : utilityNodes) {
                if (!nodesToKeep.contains(utilityNode.getVariable())) {
                    network.removeNode(utilityNode);
                }
            }
        }
        return network;
    }
    
    public static ProbNet removeSuperValueNodes(ProbNet sourceProbNet, EvidenceCase evidence) {
        return removeSuperValueNodes(sourceProbNet, evidence, false, false, null);
    }

    /**
     * Assumes the structure of super value verifies that there are no more than
     * one path between two utility nodes.
     * 
     * @param sourceProbNet
     * @return A list of utility nodes that must be kept when we want to have a
     *         set of utility nodes with an implicit sum
     */
    private static List<Variable> getUtilityNodesToKeepImplicitSum(ProbNet sourceProbNet) {
        List<Variable> nodesToKeep = getTerminalUtilityVariables(sourceProbNet);
        while (thereAreSumNodesInTheList(sourceProbNet, nodesToKeep)) {
            removeASumNode(sourceProbNet, nodesToKeep);
        }
        return nodesToKeep;
    }

    /**
     * @param sourceProbNet
     * @param nodesToKeep
     * @return true if there are some sum node in the list 'nodesToKeep'
     */
    private static boolean thereAreSumNodesInTheList(ProbNet sourceProbNet,
            List<Variable> nodesToKeep) {
        boolean thereAre = false;
        for (int i = 0; (i < nodesToKeep.size()) && !thereAre; i++) {
            Variable auxVar = nodesToKeep.get(i);
            thereAre = (isSumSuperValueNode(sourceProbNet, auxVar));
        }
        return thereAre;
    }

    /**
     * @param sourceProbNet
     * @param nodesToKeep
     *            Removes a sum node of the list and add its parents to the list
     */
    private static void removeASumNode(ProbNet sourceProbNet, List<Variable> nodesToKeep) {
        boolean removed = false;
        for (int i = 0; (i < nodesToKeep.size()) && !removed; i++) {
            Variable auxVar = nodesToKeep.get(i);
            removed = (isSumSuperValueNode(sourceProbNet, auxVar));
            if (removed) {
                nodesToKeep.remove(auxVar);
                List<Node> parentNodes = sourceProbNet.getParents(sourceProbNet.getNode(auxVar));
                nodesToKeep.addAll(ProbNet.getVariables(parentNodes));
            }
        }
    }

    /**
     * Returns whether the node <code>utilityNode</code> is a supervalue node
     * 
     * @param utilityNode the node to test
     * @return true if the node is a supervalue node, false otherwise
     */
    public static boolean isSuperValueNode(Node utilityNode) {
    	boolean found = false;
    	int i = 0;
        List<Node> parents = utilityNode.getParents();
        while (i < parents.size() && !found) {
            found = parents.get(i).getNodeType() == NodeType.UTILITY;
            ++i;
        }
        return found;
    }
}
