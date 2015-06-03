/*
 * Copyright 2013 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.dt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.inference.BasicOperations;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

public class DecisionTreeBuilder
{
    public static DecisionTreeElement buildDecisionTree (ProbNet probNet)
    {
        DecisionTreeElement root = null;
        if (probNet.getNetworkType () instanceof InfluenceDiagramType)
        {
            root = buildDecisionTreeFromID (probNet);
        }
        else if (probNet.getNetworkType () instanceof DecisionAnalysisNetworkType)
        {
            root =  new DecisionTreeBranch (probNet);
            ((DecisionTreeBranch)root).setChild((DecisionTreeNode)buildDecisionTreeFromDAN (probNet, probNet));
        }
        return root;
    }

    /**
     * Builds a decision tree from a decision analysis network
     * @param probNet
     * @return
     */    
    private static DecisionTreeElement buildDecisionTreeFromDAN (ProbNet originalProbNet, ProbNet probNet)
    {
        DecisionTreeElement root = null;
        try{
            List<Node> alwaysObservedVariables = getAlwaysObservedVariablesWithoutObservableParents(probNet);
            if(!alwaysObservedVariables.isEmpty ()) // Always observed variables 
            {
                // Get first node in the list
                Node alwaysObservedNode = alwaysObservedVariables.get (0); 
                Variable alwaysObservedVariable = alwaysObservedNode.getVariable ();
                
                DecisionTreeNode treeNode = new DecisionTreeNode (alwaysObservedNode);
                for (State state : alwaysObservedVariable.getStates ())
                {
                    DecisionTreeBranch treeBranch = new DecisionTreeBranch (originalProbNet,
                                                                            originalProbNet.getVariable (alwaysObservedVariable.getName ()), 
                                                                            state);
                    treeNode.addChild (treeBranch);
                    ProbNet restrictedProbNet = applyRestrictionsAndReveal(probNet, alwaysObservedNode, state, originalProbNet);
                    treeBranch.setChild ((DecisionTreeNode)buildDecisionTreeFromDAN (originalProbNet, restrictedProbNet));
                }         
                root = treeNode;
            }else{
            	List<Node> parentlessDecisions = getNextDecisions (probNet);
            	if(!parentlessDecisions.isEmpty ()) // Parentless decision nodes
            	{
	                if(parentlessDecisions.size () == 1)
	                {
	                    Node decisionNode = parentlessDecisions.iterator().next ();
	                    DecisionTreeNode treeNode = new DecisionTreeNode (decisionNode);
	                    Variable decisionVariable = decisionNode.getVariable ();
	                    for (State state : decisionVariable.getStates ())
	                    {
	                        DecisionTreeBranch treeBranch = new DecisionTreeBranch (originalProbNet,
	                                                                                originalProbNet.getVariable (decisionVariable.getName ()),
	                                                                                state);
	                        treeNode.addChild (treeBranch);
	                        ProbNet restrictedProbNet = applyRestrictionsAndReveal (probNet, decisionNode, state, originalProbNet);
	                        treeBranch.setChild ((DecisionTreeNode) buildDecisionTreeFromDAN (originalProbNet, restrictedProbNet));
	                    }          
	                    root = treeNode;
	                }else // If more than one parentless decision, introduce metadecision
	                {
	                    Variable orderDecisionVariable = new  Variable("OD");
	                    State[] states = new State[parentlessDecisions.size ()];
	                    int i =0;
	                    for(Node parentlessDecision : parentlessDecisions)
	                    {
	                        states[i++] = new State (parentlessDecision.getName ());
	                    }
	                    orderDecisionVariable.setStates (states);
	                    Node orderDecisionNode = new Node (probNet, orderDecisionVariable, NodeType.DECISION);
	                    DecisionTreeNode treeNode = new DecisionTreeNode (orderDecisionNode);
	                    i= 0;
	                    for (State metaState : orderDecisionVariable.getStates ())
	                    {
	                        DecisionTreeBranch treeBranch = new DecisionTreeBranch (originalProbNet,
	                                                                                orderDecisionVariable, 
	                                                                                metaState);
	                        treeNode.addChild (treeBranch);
	                        Node parentlessDecisionNode = parentlessDecisions.get (i);
	                        Variable parentlessDecisionVariable = parentlessDecisionNode.getVariable ();
	                        DecisionTreeNode decisionTreeNode = new DecisionTreeNode (parentlessDecisionNode);
	                        treeBranch.setChild (decisionTreeNode);
	                        for (State state : parentlessDecisionVariable.getStates ())
	                        {
	                            DecisionTreeBranch subTreeBranch = new DecisionTreeBranch (originalProbNet,
	                                                                                       originalProbNet.getVariable (parentlessDecisionVariable.getName ()), 
	                                                                                    state);
	                            decisionTreeNode.addChild (subTreeBranch);
	                            ProbNet restrictedProbNet = applyRestrictionsAndReveal (probNet, parentlessDecisionNode, state, originalProbNet);
	                            subTreeBranch.setChild ((DecisionTreeNode)buildDecisionTreeFromDAN (originalProbNet, restrictedProbNet));
	                        }          
	                        ++i;
	                        
	                    }
	                    root = treeNode;
	                }
	            }else {
	                List<Node> neverObservedNodes = getNeverObservedVariables (probNet);
	            	if(!neverObservedNodes.isEmpty ()) // Never observed variables
	            	{
		                ProbNet dtProbNet = probNet.copy ();
		                Node neverObservedNode = neverObservedNodes.get (0);
		                Variable neverObservedVariable = neverObservedNode.getVariable ();
		                DecisionTreeNode treeNode = new DecisionTreeNode (neverObservedNode);
		                dtProbNet.removeNode (dtProbNet.getNode (neverObservedVariable));
		                for (State state : neverObservedVariable.getStates ())
		                {
		                    DecisionTreeBranch treeBranch = new DecisionTreeBranch (originalProbNet,
		                                                                            originalProbNet.getVariable (neverObservedVariable.getName ()), 
		                                                                            state);
		                    treeNode.addChild (treeBranch);
		                    treeBranch.setChild ((DecisionTreeNode)buildDecisionTreeFromDAN (originalProbNet, dtProbNet));
		                }
		                
		                root = treeNode;
		            }else // Utility nodes
		            {
		                ProbNet dtProbNet = probNet.copy ();
		                Node svNode = getSuperValueNode (dtProbNet);
		                root = addUtilityNodes (svNode);
		            }
		        }
            }
        }catch(NodeNotFoundException ignoreException)
        {
            ignoreException.printStackTrace ();
        }
        
        return root;
    }
    
    public static List<Node> getAlwaysObservedVariablesWithoutObservableParents(ProbNet probNet) {
    	List<Node> alwaysObservedVariables = ProbNetOperations.getAlwaysObservedVariables (probNet);
    	List<Node> filteredVariables = new ArrayList<>();
    	for(Node alwaysObservedVariable : alwaysObservedVariables)
    	{
    		boolean noObservedParent = true;
    		int i = 0;
    		while(i<alwaysObservedVariables.size() && noObservedParent)
    		{
    			if(!alwaysObservedVariable.equals(alwaysObservedVariables.get(i)))
    				noObservedParent = !probNet.isParent(alwaysObservedVariables.get(i), alwaysObservedVariable);
    			++i;
    		}
    		if(noObservedParent)
    			filteredVariables.add(alwaysObservedVariable);
    	}
		return filteredVariables;
	}
    
	public static List<Node> getNextDecisions(ProbNet probNet) {
			
			List<Node> decisionNodes = ProbNetOperations.getParentlessDecisions(probNet);
			// Check if the nodes revealed by a decision node are the subset of another
			// In that case we don't need to consider them as valid orders
			List<Set<Node>> revealedNodes = new ArrayList<>();
			for (Node node : decisionNodes) {
				Set<Node> revealedByDecision = new HashSet<>();
				for (Link<Node> link : node.getLinks()) {
					if (link.getNode1().equals(node) && link.hasRevealingConditions()) {
						revealedByDecision.add((Node) link.getNode2());
					}
				}
				revealedNodes.add(revealedByDecision);
			}
			List<Node> dominatedDecisions = new ArrayList<>();
			for (int i=0; i<decisionNodes.size(); ++i) {
				Node nodeA = decisionNodes.get(i);
				for (int j=0; j<decisionNodes.size(); ++j) {
					Node nodeB = decisionNodes.get(j);
					if (nodeA != nodeB
							// if both sets are equal, just mark one of the nodes as dominated
							&& !(revealedNodes.get(i).equals(revealedNodes.get(j)) && i < j)
							&& revealedNodes.get(i).containsAll(revealedNodes.get(j)))
						dominatedDecisions.add(nodeB);
				}
			}
			decisionNodes.removeAll(dominatedDecisions);
	
			return decisionNodes;
		}

	/**
     * Builds a decision tree from an influence diagram
     * @param probNet
     * @return
     */
    private static DecisionTreeElement buildDecisionTreeFromID (ProbNet probNet)
    {
        ProbNet dtProbNet = probNet.copy ();
        Node svNode = getSuperValueNode (dtProbNet);
        List<Variable> variables = getPartiallySortedVariables (dtProbNet);
        DecisionTreeElement root = new DecisionTreeBranch (dtProbNet);
        Stack<DecisionTreeElement> treeStack = new Stack<> ();
        treeStack.push (root);
        List<DecisionTreeBranch> leaves = new ArrayList<> ();
        // Build tree with decision & utility nodes
        while (!treeStack.isEmpty ())
        {
            DecisionTreeElement treeElement = treeStack.pop ();
            // If a node
            if (treeElement instanceof DecisionTreeNode)
            {
                // Get next variable in the list
                Variable variable = ((DecisionTreeNode) treeElement).getVariable ();
                for (State state : variable.getStates ())
                {
                    DecisionTreeBranch treeBranch = new DecisionTreeBranch (dtProbNet, 
                                                                            variable,
                                                                            state);
                    ((DecisionTreeNode) treeElement).addChild (treeBranch);
                    treeStack.push (treeBranch);
                }
            }
            // If a branch
            else if (treeElement instanceof DecisionTreeBranch)
            {
                Variable branchVariable = ((DecisionTreeBranch) treeElement).getBranchVariable ();
                Variable childVariable = null;
                // If this is the root 
                if (branchVariable == null)
                {
                    childVariable = variables.get (0);
                }
                // If this neither the root nor a leaf                
                else if (variables.indexOf (branchVariable) + 1 < variables.size ())
                {
                    childVariable = variables.get (variables.indexOf (branchVariable) + 1);
                }
                // If this is a leaf
                else
                {
                    leaves.add ((DecisionTreeBranch) treeElement);
                }
                if (childVariable != null)
                {
                    DecisionTreeNode child = new DecisionTreeNode (dtProbNet.getNode (childVariable));
                    ((DecisionTreeBranch) treeElement).setChild (child);
                    treeStack.push (child);
                }
            }
        }

        // Add utility trees at the tip of each leaf
        for (DecisionTreeBranch leaf : leaves)
        {
            leaf.setChild (addUtilityNodes (svNode));
        }
        return root;
    }

    /**
     * Looks for the super value node. If there is none, it creates it.
     * @param probNet
     * @return
     */
    private static Node getSuperValueNode (ProbNet probNet)
    {
        Node svNode = null;
        // Look for leaves
        List<Node> leaves = getUtilityLeaves (probNet);
        // if there is more than one leave, create a new super value node
        if (leaves.size () > 1)
        {
            Variable svVariable = new Variable ("Global Utility");
            svNode = probNet.addNode (svVariable, NodeType.UTILITY);
            List<Variable> leafVariables = new ArrayList<> (leaves.size ());
            for (Node leafNode : leaves)
            {
                leafVariables.add (leafNode.getVariable ());
            }
            svNode.addPotential (new SumPotential (leafVariables, PotentialRole.UTILITY));
            for (Node leaf : leaves)
            {
                probNet.addLink (leaf, svNode, true);
            }
        }
        else if (leaves.size () == 1)
        {
            svNode = leaves.get (0);
        }
        return svNode;
    }

    private static List<Node> getUtilityLeaves (ProbNet probNet)
    {
        List<Node> leaves = new ArrayList<> ();
        for (Node node : probNet.getNodes ())
        {
            if (node.getNodeType () == NodeType.UTILITY
                && probNet.getChildren (node).isEmpty ())
            {
                leaves.add (node);
            }
        }
        return leaves;
    }

    /**
     * Using PartialOrder generates a sorted plain list of decision and chance variables
     * @param probNet ProbNet
     * @return List<Variable>
     */
    private static List<Variable> getPartiallySortedVariables (ProbNet probNet)
    {
        List<Variable> variables;
        //PartialOrder partialOrder = null;
        List<List<Variable>> partialOrder = BasicOperations.calculatePartialOrder(probNet); //new PartialOrder (probNet);
        variables = new ArrayList<> (BasicOperations.getNumVariables(probNet)); //(partialOrder.getNumVariables ());
        for (List<Variable> variableSubList : partialOrder) //partialOrder.getOrder ())
        {
            variables.addAll (variableSubList);
        }
        return variables;
    }
    
    /**
     * Adds a utility tree at the tip of each leaf
     * @param svNode A super value node
     */
    private static DecisionTreeNode addUtilityNodes (Node svNode)
    {
        // Add utility nodes
        DecisionTreeNode svTreeNode = new DecisionTreeNode (svNode);
        ProbNet probNet = svNode.getProbNet();        
        Stack<DecisionTreeNode> utilityTreeStack = new Stack<> ();
        utilityTreeStack.push (svTreeNode);
        while (!utilityTreeStack.isEmpty ())
        {
            DecisionTreeNode utilityTreeNode = utilityTreeStack.pop ();
            Node utilityNode = probNet.getNode(utilityTreeNode.getVariable ());
            for (Node parentNode : utilityNode.getParents ())
            {
                if (parentNode.getNodeType () == NodeType.UTILITY)
                {
                    DecisionTreeNode treeNode = new DecisionTreeNode (parentNode);
                    utilityTreeNode.addChild (treeNode);
                    utilityTreeStack.push (treeNode);
                }
            }
        }
        return svTreeNode;
   }
    
    private static ProbNet applyRestrictionsAndReveal(ProbNet probNet, Node node, State state, ProbNet originalProbNet)
    {
        ProbNet probNetCopy = probNet.copy ();
        
        for (Link<Node> link : probNet.getLinks (node))
        {
            if(link.getNode1 ().equals (node)) // Our node is the source node
            {
                Node destinationNode = probNetCopy.getNode (link.getNode2 ().getVariable ());
                if(destinationNode.getNodeType () == NodeType.CHANCE)
                {
                    if (link.hasRevealingConditions ())
                    {
                        if (link.getRevealingStates ().contains (state))
                        {
                            destinationNode.setAlwaysObserved (true);
                        }
                    }
                }
                if(link.hasRestrictions ())
                {
                	State[] restrictedVariableStates = destinationNode.getVariable ().getStates ();
                    List<State> nonRestrictedStates = ProbNetOperations.getUnrestrictedStates(link, restrictedVariableStates, state);
                
                    if(nonRestrictedStates.isEmpty ())
                    {
                    	// Remove link between restricting node and restricted node
                    	probNetCopy.removeLink(link.getNode1().getVariable(), link.getNode2().getVariable(),true);
                        // Remove destination node and its descendants!
                        Stack<Node> disposableNodes = new Stack<> ();
                        disposableNodes.push (destinationNode);
                        while(!disposableNodes.isEmpty ())
                        {
                            Node disposableNode = disposableNodes.pop ();
                            // If it's a decision node, check if there is another 
                            // path to it from another decision
                            if(disposableNode.getNodeType() != NodeType.DECISION ||
                                    !ProbNetOperations.hasPredecessorDecision(disposableNode, probNetCopy))
                            {
                                for(Node descendant : probNetCopy.getChildren(disposableNode))
                                {
                                    disposableNodes.push(descendant);
                                }
                                probNetCopy.removeNode (disposableNode);
                            }
                        }
                        
//                    }else if(nonRestrictedStates.size () == 1) // Remove variables with a single variable
//                    {
//                        ProbNet probNetWithoutSingleStateVariable = probNetCopy.copy ();
//                        probNetWithoutSingleStateVariable.removeNode (probNetWithoutSingleStateVariable.getNode (destinationNode.getVariable ()));
//                        probNetCopy = applyRestrictionsAndReveal(probNetWithoutSingleStateVariable, destinationNode, nonRestrictedStates.get (0), originalProbNet);
                    }else if(nonRestrictedStates.size () < restrictedVariableStates.length)
                    {
                        // At least one of the states of the destination node is restricted.
                        // Make a copy of the variable and remove the restricted states
                        State[] unrestrictedStates = nonRestrictedStates.toArray (new State[0]);
                        Variable restrictedVariable = new Variable (destinationNode.getVariable ().getName (), unrestrictedStates);
                        restrictedVariable.setVariableType (destinationNode.getVariable ().getVariableType ());
                        destinationNode.setVariable(restrictedVariable);
                    }else
                    {
                        // No state restricted, leave destinationNode as it is 
                    }
                }
            }
         } 
        
        probNetCopy.removeNode (probNetCopy.getNode (node.getVariable ()));
        return probNetCopy;
    }

    public static List<Node> getNeverObservedVariables (ProbNet probNet)
    {
        List<Node> neverObservedVariables = new ArrayList<> ();
        for (Node node : probNet.getNodes (NodeType.CHANCE))
        {
            if (probNet.getParents (node).isEmpty ())
            {
                neverObservedVariables.add (node);
            }
        }
        return neverObservedVariables;    
    }
}
