/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.openmarkov.core.exception.WrongGraphStructureException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;

/**
 * Stores decision nodes and random variables in a list of sets. Used by
 * variable elimination algorithm.
 * <p>
 * Algorithm to get the partial order:
 * <ol>
 * <li>Gets decision in order:
 * <ol type="a">
 * <li>Gets a random node
 * <li>Recursively gets all the parents of that node
 * <li>If a Node has no parents:
 * <ol type="i">
 * <li>If it is a Decision node stores it in <code>decisions</code>
 * <li>In any case removes Node
 * </ol>
 * <li>If there is at least one decision node left go to <code>a.</code>
 * </ol>
 * <li>For each decision in <code>decisions</code>:
 * <ol type="a">
 * <li>Gets the chance nodes predecessors of each decision and creates two
 * <code>ArrayList</code>, one for the variable and the other one for the
 * predecessors chance nodes.
 * <li>Stores them in <code>order</code>
 * </ol>
 * <li>Remaining chance nodes are the last variables in <code>order</code>
 * because they are no parents of decisions.
 * </ol>
 * @author Manuel Arias
 */
public class PartialOrder {

    /** A partial order is a list of lists of variables. */
    private List<List<Variable>> order;
    
    ProbNet diagram;

    /**
     * @return <code>List</code> of <code>List</code> of <code>Variable</code>s 
     */
    public List<List<Variable>> getOrder() {
    	List<List<Variable>> copyOfOrder = new ArrayList<List<Variable>>();
    	for (List<Variable> list : order) {
    		copyOfOrder.add(new ArrayList<Variable>(list));
    	}
        return copyOfOrder;
    }

    public void setOrder(List<List<Variable>> order) {
        this.order = order;
    }

    /**
     * Builds the list.
     * @param probNet. <code>ProbNet</code>
     */
    public PartialOrder(ProbNet probNet) throws WrongGraphStructureException {
        calculatePartialOrder(probNet);
        diagram = probNet;
    }
    
    public PartialOrder() throws WrongGraphStructureException {
    }

    /**
     * @param probNet
     * @return <code>ArrayList</code> of <code>ArrayList</code> of
     *         <code>Variables</code>.
     */
    private void calculatePartialOrder(ProbNet probNet) {
        ProbNet idCopy = probNet.copy(); // Copy influence diagram

        // Get decisions (only) in elimination order
        int numDecisions = idCopy.getNumNodes(NodeType.DECISION);
        Stack<Variable> decisions = new Stack<Variable>();
        do {
            List<Node> nodes = idCopy.getNodes();
            for (Node node : nodes) {
                if (idCopy.getNumChildren(node) == 0) {
                    if (node.getNodeType() == NodeType.DECISION) {
                        decisions.push(node.getVariable());
                        numDecisions--;
                    }
                    idCopy.removeNode(node);
                }
            }
        } while (numDecisions > 0);

        // Create elimination order adding chance nodes
        order = new ArrayList<>(numDecisions * 2 + 1);
        List<Node> chanceNodes = probNet.getNodes(NodeType.CHANCE);
        HashSet<Variable> chanceVariables = new HashSet<Variable>();
        for (Node chanceNode : chanceNodes) {
            chanceVariables.add(chanceNode.getVariable());
        }
        while (!decisions.empty()) {
            Variable decision = decisions.pop();
            Node decisionNode = probNet.getNode(decision);
            // Get nodes of the decision parents
            List<Node> parentDecisionNodes = new ArrayList<Node>();
            for (Node parent : probNet.getParents(decisionNode)) {
                if (parent.getNodeType() != NodeType.DECISION) {
                    if (chanceVariables.contains(parent.getVariable())) {
                        parentDecisionNodes.add(parent);
                        chanceVariables.remove(parent.getVariable());
                    }
                }
            }
            // Add parents and decision
            int numParents = parentDecisionNodes.size();
            if (numParents > 0) {
                List<Variable> decisionVariableParents = new ArrayList<Variable>(numParents);
                for (Node parent : parentDecisionNodes) {
                    decisionVariableParents.add(parent.getVariable());
                }
                order.add(decisionVariableParents);
            }
            // Add decision variable
            order.add(Arrays.asList(decision));
        }
        List<Variable> remainingVariables = new ArrayList<Variable>(chanceVariables.size());
        for (Variable remainingVariable : chanceVariables) {
            remainingVariables.add(remainingVariable);
        }
        if (remainingVariables.size() > 0) {
        	order.add(remainingVariables);
        }

    }
    
    
    public List<Variable> getAnAdmissibleOrderOfDecisions(){
    	List<Variable> decisions = new ArrayList<>();
    	
    	for (List<Variable> variablesSet:order){
    		if (variablesSet.size()>0 && diagram.getNode(variablesSet.get(0)).getNodeType()==NodeType.DECISION){
    			decisions.addAll(variablesSet);
    		}
    	}
    	return decisions;
    }

    /**
     * @param order
     * @param queryVariables
     * @param evidenceVariables
     * @param conditioningVariables
     * @param variablesToEliminate 
     * @return An order that has been pruned by eliminating the variables that
     *         are in queryVariables or in evidenceVariables or in conditioningVariables or not in variablesToEliminate
     */
    public List<List<Variable>> projectPartialOrder(List<Variable> queryVariables,
            List<Variable> evidenceVariables, List<Variable> conditioningVariables, List<Variable> variablesToEliminate) {
        List<List<Variable>> newOrder;
        List<List<Variable>> newOrder2;
        // Remove variables
        newOrder = new ArrayList<>();
        for (int i = 0; i < order.size(); i++) {
            List<Variable> auxArray = order.get(i);
            List<Variable> cloneAuxArray;
            cloneAuxArray = new ArrayList<Variable>(auxArray);
            for (Variable auxVar : auxArray) {
                if (queryVariables.contains(auxVar) || evidenceVariables.contains(auxVar) || conditioningVariables
                        .contains(auxVar) || !variablesToEliminate.contains(auxVar)) {
                    cloneAuxArray.remove(auxVar);
                }
            }
            newOrder.add(cloneAuxArray);

        }
        // Copy the non empty array lists
        newOrder2 = new ArrayList<>();

        for (List<Variable> auxArray : newOrder) {
            if (auxArray.size() > 0) {
                newOrder2.add(auxArray);
            }
        }
        return newOrder2;
    }

    /** @return A <code>String</code> with an array of arrays. */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        int numArrays = order.size();
        for (int i = 0; i < numArrays; i++) {
            List<Variable> array = order.get(i);
            int arraySize = array.size();
            if (arraySize > 1) {
                buffer.append("{");
            }
            for (int j = 0; j < arraySize; j++) {
                buffer.append(array.get(j));
                if (j < arraySize - 1) {
                    buffer.append(", ");
                }
            }
            if (arraySize > 1) {
                buffer.append("}");
            }
            if (i < numArrays - 1) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }

    public int getNumVariables() {
        int num = 0;

        if (order != null) {
            for (List<Variable> auxArray : order) {
                if (auxArray != null) {
                    num = num + auxArray.size();
                }
            }
        } else {
            num = 0;
        }
        return num;
    }

}
