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

import org.openmarkov.core.exception.WrongGraphStructureException;
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
    
    public ArrayList<ArrayList<Variable>> getOrder() {
		return order;
	}

	public void setOrder(ArrayList<ArrayList<Variable>> order) {
		this.order = order;
	}

	/** Builds the list.
     * @param id. <code>ProbNet</code> */
    public PartialOrder(ProbNet id) throws WrongGraphStructureException {
       calculatePartialOrder(id);
    }

     /**
     * @param id
     * @return <code>ArrayList</code> of <code>ArrayList</code> of
     *   <code>Variables</code>.
     */
    private void calculatePartialOrder(
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
    	order = 
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
    	
    }
    
    
    /** @param originalID influence diagram. <code>ProbNet</code> 
     * @return */
/*	private void calculatePartialOrder(ProbNet originalID) 
			throws WrongGraphStructureException {
		order = new ArrayList<ArrayList<Variable>>();
		ProbNet idCopy = originalID.copy();//The copy will be destroyed
		ArrayList<Variable> decisionVariables = 
			idCopy.getVariables(NodeType.DECISION);
		ArrayList<Variable> chanceVariables = 
			idCopy.getVariables(NodeType.CHANCE);

		// Build an array of arrays containing the parents
		// of each decision
		ArrayList<ArrayList<Variable>> parentsOfDecisions = 	
			getParentsOfDecisions(idCopy, decisionVariables);
		
		// Gets the unobservable variables, i.e., the variables that are not
		// a parent of any decision
		ArrayList<Variable> unobservableVariables = 
			getUnobservableVariables(chanceVariables, parentsOfDecisions);
		
		// Iteratively remove from the influence diagram
		// chance nodes without parents or without
		// children, and decision nodes without parents,
		// and add them and their potential to this MarkovDecisionNetwork.
		int numVariablesToRemove = chanceVariables.size() 
			+ decisionVariables.size();
		for (int i = 0; i < numVariablesToRemove; i++) {
			// gets the node
			ProbNode node = getNextNodeToDelete(idCopy);
			NodeType nodeType = node.getNodeType();
			Variable variable = node.getVariable();
			// adds the node to this MarkovDecisionNetwork
			
			//mluque: I will probably have to uncomment the next line
			//addVariable(variable, nodeType);
			// adds decision nodes and its parents to partialOrder
			if (nodeType == NodeType.DECISION) {
				int index = decisionVariables.indexOf(variable);
				if (parentsOfDecisions.get(index).size() > 0) {
					order.add(parentsOfDecisions.get(index));
				}
				ArrayList<Variable> oneDecisionArray = 
					new ArrayList<Variable>();
				oneDecisionArray.add(variable);
				order.add(oneDecisionArray);
			}
			idCopy.removeProbNode(node); // remove from influenceDiagram
		}
		if (unobservableVariables.size() > 0) {
			order.add(unobservableVariables);
		}
		
		
		
		// adds the potentials to this MarkovDecisionNetwork, 
		// which entails adding links among the nodes
		ArrayList<Potential> potentials = originalID.getPotentials();
		for (Potential potential : potentials) {
			addPotential(potential);
		}
		
		// Add restriction applied in Markov networks: only undirected links.
		try {
            addConstraint (new OnlyUndirectedLinks (), true);
		} catch (ConstraintViolationException e) {
			logger.fatal (e);
		}
	}*/
	
	/** If there is a chance node without parents or without children,
	 *   returns that node. Otherwise, if there is just one decision node 
	 *   without nodes, returns that node. Otherwise, i.e., if there are more
	 *   than one decision nodes without parents, throws an exception.
	 * @param influenceDiagram <code>InfluenceDiagram</code>
	 * @return A <code>ProbNode</code>
	 *   without parents or without children.<p>
	 *   It first tries to get a chance node;  
	 * @argCondition The influence diagram contains at least one chance or 
	 *   decision node
	 * @throws <code>WrongGraphStructureException</code> */
	private ProbNode getNextNodeToDelete(ProbNet influenceDiagram) 
			throws WrongGraphStructureException {
		// looks for a chance node
		ArrayList<ProbNode> chanceNodes = 
			influenceDiagram.getProbNodes(NodeType.CHANCE);
		for (ProbNode probNode : chanceNodes) {
			Node node = probNode.getNode();
			if ((node.getNumChildren() == 0) || (node.getNumParents() == 0)) {
				return probNode;
			}
		}
		// looks for a decision node
		ArrayList<ProbNode> decisionNodes = 
			influenceDiagram.getProbNodes(NodeType.DECISION);
		ProbNode decisionNode = null;
		for (ProbNode probNode : decisionNodes) {
			if (probNode.getNode().getNumParents() == 0) {
				if (decisionNode != null) {
					//More than two decision without parents
					throw new WrongGraphStructureException(
						"No partial order for decision nodes in this " +
						"influence diagram");
				}
				decisionNode = probNode;
			}
		}
		return decisionNode;
	}

	
	/** @param chanceVariables <code>ArrayList</code> of <code>Variable</code>
	 * @param parentsVariables <code>ArrayList</code> of <code>ArrayList</code>
	 *   of <code>Variable</code>
	 * @return An <code>ArrayList</code> of <code>Variable</code> that are not
	 *   parents of any decision. */
	private ArrayList<Variable> getUnobservableVariables(
			ArrayList<Variable> chanceVariables, 
			ArrayList<ArrayList<Variable>> parentsVariables) {
		ArrayList<Variable> unobservableVariables = 
			new ArrayList<Variable>(chanceVariables);
		for (ArrayList<Variable> variables : parentsVariables) {
			unobservableVariables.removeAll(variables);
		}
		return unobservableVariables;
	}
	
	/*	*//** @return An <code>ArrayList</code> of <code>ArrayList</code> of 
	 *   <code>Variable</code>. It contains the parents of each decision. The 
	 *   <code>ArrayList</code> nested in the i-th position contains the parents
	 * of the i-th decision node.
	 * @param influenceDiagram <code>InfluenceDiagram</code>.
	 * @param decisionVariables <code>ArrayList</code> of <code>Variable</code>
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	private ArrayList<ArrayList<Variable>> getParentsOfDecisions(
			ProbNet influenceDiagram, ArrayList<Variable> decisionVariables) {
		ArrayList<ArrayList<Variable>> parentsVariables = 
			new ArrayList<ArrayList<Variable>>();
		for(Variable decision : decisionVariables) {
			// Remove decision nodes from node parents
			ProbNode probNode =influenceDiagram.getProbNode(decision);
			ArrayList<Node> nodeParents = probNode.getNode().getParents();
			ArrayList<Node> nodeParentCloned = 
				(ArrayList<Node>)nodeParents.clone();
			for (Node node : nodeParentCloned) {
				if (((ProbNode)node.getObject()).getNodeType() == 
					    NodeType.DECISION) {
					nodeParents.remove(node);
				}
			}
			ArrayList<Variable> parentVariables = 
				influenceDiagram.getVariables(nodeParents);
			parentsVariables.add(parentVariables);
		}
		return parentsVariables;
	}


	/**
	 * @param order
	 * @param queryVariables
	 * @param evidenceVariables 
	 * @param conditioningVariables 
	 * @return An order that has been pruned by eliminating the variables that are not in 'variables
	 */
	public ArrayList<ArrayList<Variable>> projectPartialOrder(ArrayList<Variable> queryVariables, ArrayList<Variable> evidenceVariables, ArrayList<Variable> conditioningVariables) {
		ArrayList<ArrayList<Variable>> newOrder;
		ArrayList<ArrayList<Variable>> newOrder2;
		//Remove variables
		newOrder = new ArrayList<ArrayList<Variable>>();
		for (int i=0;i<order.size();i++){
			ArrayList<Variable> auxArray=order.get(i);
			ArrayList<Variable> cloneAuxArray;
			cloneAuxArray = (ArrayList<Variable>) auxArray.clone();
			for (Variable auxVar:auxArray){
				if ((queryVariables.contains(auxVar)
						||evidenceVariables.contains(auxVar)
						||conditioningVariables.contains(auxVar))){
					cloneAuxArray.remove(auxVar);
				}
			}
			newOrder.add(cloneAuxArray);
			
		}
		//Copy the non empty array lists
		newOrder2 = new ArrayList<ArrayList<Variable>>();
		
		for (ArrayList<Variable> auxArray:newOrder){
			if (auxArray.size()>0){
				newOrder2.add(auxArray);
			}
		}
		return newOrder2;
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

	public int getNumVariables() {
		int num = 0;
		
		if (order!=null){
			for (ArrayList<Variable> auxArray:order){
				if (auxArray!=null){
					num = num + auxArray.size();
				}
			}
		}
		else{
			num = 0;
		}
		return num;
	}

}
