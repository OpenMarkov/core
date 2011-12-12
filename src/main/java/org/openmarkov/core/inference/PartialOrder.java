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
import java.util.HashSet;
import java.util.Stack;

import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;


/** Stores decision nodes and random variables in a list of sets. 
 *  Used by variable elimination algorithm.<p>
 *  Algorithm to get the partial order:
 *  <ol>
 *  <li>Gets decision in order:
 *  <ol type="a">
 *  <li>Gets a random node
 *  <li>Recursively gets all the parents of that node
 *  <li>If a Node has no parents:<ol type="i">
 *  <li>If it is a Decision node stores it in <code>decisions</code>
 *  <li>In any case removes Node
 *  </ol>
 *  <li>If there is at least one decision node left go to <code>a.</code>
 *  </ol>
 *  <li>For each decision in <code>decisions</code>:
 *  <ol type="a">
 *  <li>Gets the chance nodes predecessors of each decision and creates two
 *  <code>ArrayList</code>, one for the variable and the other one for the
 *  predecessors chance nodes.
 *  <li>Stores them in <code>order</code>
 *  </ol>
 *  <li>Remaining chance nodes are the last variables in <code>order</code>
 *  because they are no parents of decisions.
 *  </ol> */
public class PartialOrder {

	/** A partial order is a list of lists of variables. */
	private ArrayList<ArrayList<Variable>> order;
    
    /** Builds the list.
     * @param id. <code>ProbNet</code> */
    public PartialOrder(ProbNet id) {
        order = null;
    }

    /** @return <code>ArrayList</code> of <code>ArrayList</code> of
     *   <code>Variables</code>. */
    public static ArrayList<ArrayList<Variable>> getPartialOrder(ProbNet id) {
    	ArrayList<ArrayList<Variable>> order = calculatePartialOrder(id);
        return order;
    }
    
    private static ArrayList<ArrayList<Variable>> calculatePartialOrder(
    		ProbNet id) {
    	ProbNet idCopy = id.copy(); // Copy influence diagram

    	// Get decisions (only) in elimination order
    	int numDecisions = idCopy.getNumNodes(NodeType.DECISION);
    	Stack<Variable> decisions = new Stack<Variable>();
    	do {
        	ArrayList<ProbNode> probNodes = idCopy.getProbNodes();
    		for (ProbNode probNode : probNodes) {
    			if (probNode.getNode().getNumChildren() == 0) {
    				if (probNode.getNodeType() == NodeType.DECISION) {
    					decisions.push(probNode.getVariable());
    					numDecisions--;
    				}
					idCopy.removeProbNode(probNode);
    			}
    		}
    	} while (numDecisions > 0);
    	
    	// Create elimination order adding chance nodes 
    	ArrayList<ArrayList<Variable>> order = 
    		new ArrayList<ArrayList<Variable>>(numDecisions * 2 + 1);
    	ArrayList<ProbNode> chanceProbNodes = id.getProbNodes(NodeType.CHANCE);
    	HashSet<Variable> chanceVariables = new HashSet<Variable>();
    	for (ProbNode chanceProbNode : chanceProbNodes) {
    		chanceVariables.add(chanceProbNode.getVariable());
    	}
    	while (!decisions.empty()) {
    		Variable decision = decisions.pop();
    		ProbNode decisionProbNode = id.getProbNode(decision);
    		ArrayList<Node> decisionNodeParents = 
    			decisionProbNode.getNode().getParents();
    		// Get ProbNodes of the decision parents
    		ArrayList<ProbNode> decisionProbNodeParents = 
    			new ArrayList<ProbNode>(decisionNodeParents.size());
    		for (Node node : decisionNodeParents) {
    			ProbNode probNode = (ProbNode)node.getObject();
    			if (probNode.getNodeType() != NodeType.DECISION) {
    				if (chanceVariables.contains(probNode.getVariable())) {
    					decisionProbNodeParents.add(probNode);
    					chanceVariables.remove(probNode.getVariable());
    				}
    			}
    		}
    		// Add parents and decision
    		int numParents = decisionProbNodeParents.size();
    		if (numParents > 0) {
        		ArrayList<Variable> decisionVariableParents = 
        			new ArrayList<Variable>(numParents);
        		for (ProbNode parent : decisionProbNodeParents) {
        			decisionVariableParents.add(parent.getVariable());
        		}
    			order.add(decisionVariableParents);
    		}
    		// Get ArrayList of decision (only one element)
    		ArrayList<Variable> oneDecision = new ArrayList<Variable>(1);
    		oneDecision.add(decision);
    		order.add(oneDecision);
    	}
    	ArrayList<Variable> remainingVariables = 
    		new ArrayList<Variable>(chanceVariables.size());
		for (Variable remainingVariable : chanceVariables) {
			remainingVariables.add(remainingVariable);
		}
    	order.add(remainingVariables);
    	return order;
    }
    
	/** @return A <code>String</code> with an array of arrays. */
    public String toString() {
    	StringBuffer buffer = new StringBuffer();
    	int numArrays = order.size();
    	for (int i = 0; i < numArrays; i++) {
    		ArrayList<Variable> array = order.get(i);
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

}
