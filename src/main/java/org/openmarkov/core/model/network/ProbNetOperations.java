/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.potential.Potential;

/** This class performs prune on <code>ProbNet</code>  */
public class ProbNetOperations {

	// Methods
	/** Performs prune operation in these steps:
	 * <ol>
	 * <li> Copy the received <code>ProbNet</code>.
	 * <li> Remove barren nodes from the copied <code>ProbNet</code>.
	 * <li> Remove unreachable nodes from <code>variablesOfInterest</code> given
	 *  the <code>variablesOfEvidence</code>.
	 * </ol>
	 * @return <code>ProbNet</code>. Evidence variables are removed in serial 
	 * connections */
	public static ProbNet getPruned(ProbNet probNet, 
			Collection<Variable> variablesOfInterest,
			EvidenceCase evidence) {
        ProbNet prunedProbNet = probNet.copy ();
        HashSet<Variable> variablesOfInterest2 = new HashSet<Variable> (variablesOfInterest);
        HashSet<Variable> variablesOfEvidence2 = new HashSet<Variable> (evidence.getVariables ());
        prunedProbNet = removeBarrenNodes (prunedProbNet, variablesOfInterest2,
                                           variablesOfEvidence2);
        prunedProbNet = removeUnreachableNodes (prunedProbNet, variablesOfInterest2,
                                                variablesOfEvidence2);
        return prunedProbNet;
	}
	
	/**Projects the evidence in the <code>probNet</code> potentials and remove
	 * evidence variables
	 * @param probNet. <code>ProbNet</code>
	 * @param evidence. <code>EvidenceCase</code>
	 * @throws NotEnoughMemoryException */
    public static void projectEvidence (ProbNet probNet, EvidenceCase evidence)
        throws NotEnoughMemoryException
    {
        ArrayList<Variable> variables = evidence.getVariables ();
        for (Variable variable : variables)
        {
            ArrayList<Potential> potentials = probNet.getPotentials (variable);
            for (Potential potential : potentials)
            {
                probNet.removePotential (potential);
                try
                {
                    for (Potential newPotential : potential.tableProject (evidence, null))
                    {
                        if (newPotential.getNumVariables () > 0)
                        {
                            boolean containVariables = true;
                            for (Variable potentialVariable : newPotential.getVariables ())
                            {
                                containVariables &= (probNet.getProbNode (potentialVariable) != null);
                            }
                            if (containVariables)
                            {
                                probNet.addPotential (newPotential);
                            }
                        }
                    }
                }
                catch (NonProjectablePotentialException e)
                {
                    e.printStackTrace (); // Unreachable code
                }
                catch (WrongCriterionException e)
                {
                    e.printStackTrace (); // Unreachable code
                }
            }
            probNet.removeProbNode (probNet.getProbNode (variable));
        }
    }

	/** Remove nodes that:<ol>
	 * <li> Are not included in <code>variablesOfInterest</code>
	 * <li> Are not included in <code>variablesOfEvidence</code>
	 * <li> Have no children or all its children are barren nodes.
	 * </ol>
	 * @param variablesOfEvidence2 
	 * @param variablesOfInterest2 
	 * @param prunedProbNet. <code>ProbNet</code>
	 * @return <code>ProbNet</code> without barren nodes. */
    public static ProbNet removeBarrenNodes (ProbNet prunedProbNet,
                                              Collection<Variable> variablesOfInterest,
                                              HashSet<Variable> variablesOfEvidence)
    {
        ArrayList<ProbNode> barrenNodes = new ArrayList<ProbNode> ();
        boolean foundBarrenNodes = false;
        // TODO Instead of looping through the whole network each time, examine
        // the parents of each barren node removed to see if they have become
        // barren nodes
        do
        {
            // Collect barren nodes
            ArrayList<ProbNode> probNodes = prunedProbNet.getProbNodes ();
            for (ProbNode probNode : probNodes)
            {
                Node node = probNode.getNode ();
                if (node.getNumChildren () == 0)
                {
                    Variable variable = probNode.getVariable ();
                    if (!variablesOfInterest.contains (variable)
                        && !variablesOfEvidence.contains (variable))
                    {
                        barrenNodes.add (probNode);
                    }
                }
            }
            foundBarrenNodes = barrenNodes.size () > 0;
            if (foundBarrenNodes)
            {
                // Remove barren nodes
                for (ProbNode probNode : barrenNodes)
                {
                    prunedProbNet.removeProbNode (probNode);
                }
                barrenNodes.clear ();
            }
        }
        while (foundBarrenNodes);
        return prunedProbNet;
    }

    /**
     * Removes the nodes that are not connected to the variables of interest by
     * any path
     * @param probNet
     * @param variablesOfInterest
     * @param variablesOfEvidence
     * @return
     */
	public static ProbNet removeUnreachableNodes(ProbNet probNet, 
			Collection<Variable> variablesOfInterest, 
			HashSet<Variable> variablesOfEvidence) {
		// Gets nodes of interest and adds nodes connected to them
		UniqueStack<Node> nodesToExplore = new UniqueStack<Node>();
		HashSet<Node> nodesToKeep = new HashSet<Node>();
	
		// Store nodes of variablesOfInterest in nodesToKeep
		for (Variable variable : variablesOfInterest) {
			Node node = probNet.getProbNode(variable).getNode();
			nodesToKeep.add(node);
		}		
	
		// Add neighbors of variablesOfInterest as nodesToKeep and store them in
		// nodesToExplore
		HashSet<Node> nodesToKeepClon = new HashSet<Node>(nodesToKeep);
		for (Node node : nodesToKeepClon) {
			ArrayList<Node> neighbors = node.getNeighbors();
			for (Node neighbor : neighbors) {
				if (!nodesToKeep.contains(neighbor)) {
					nodesToKeep.add(neighbor);
					nodesToExplore.push(neighbor);
				}
			}
		}
		
		// Store evidence nodes and probNodes in collections
		HashSet<Node> hashEvidenceNodes = 
				getEvidenceNodes(probNet, variablesOfEvidence);
		HashSet<Node> evidenceAndAncestors = getNodesAndAncestors(hashEvidenceNodes);
		
		// For each interest node, finds connected nodes via valid paths.
		while (!nodesToExplore.empty()) {
			Node node = nodesToExplore.pop();
			
			// Find head to head connected nodes: X->Y<-Z and
			// Y is evidence or Y has a descendent that is evidence
			if (evidenceAndAncestors.contains(node)) {
				ArrayList<Node> parents = node.getParents();
				int parentsSize = parents.size();
				boolean toKeepI, toKeepJ;
				for (int i = 0; i < parentsSize - 1; i++) {
					Node parentI = parents.get(i);
					toKeepI = nodesToKeep.contains(parentI);
					for (int j = i+1; j < parentsSize; j++) {
						Node parentJ = parents.get(j);
						toKeepJ = nodesToKeep.contains(parentJ);
						if (toKeepI && !toKeepJ) {
							pushInExploreAndAddToKeep(parentJ,nodesToExplore,nodesToKeep);
						} else if (!toKeepI && toKeepJ) {
							pushInExploreAndAddToKeep(parentI,nodesToExplore,nodesToKeep);
							toKeepI = true;
						}
					}
				}
			}
			
			// Find not head to head connected nodes:
			// X->Y->Z, X<-Y<-Z and X<-Y->Z
			if (!hashEvidenceNodes.contains(node)) {
				ArrayList<Node> children = node.getChildren();
				ArrayList<Node> parents = node.getParents();
				int numChildren = children.size();
				for (int i = 0; i < numChildren; i++) {
					Node child = children.get(i);
					boolean interestChild = nodesToKeep.contains(child);
					// X->Y->Z and X<-Y<-Z
					for (Node parent : parents) {
						boolean interestParent = nodesToKeep.contains(parent);
						if (interestChild && !interestParent) {
							pushInExploreAndAddToKeep(parent,nodesToExplore,nodesToKeep);							
						} else if (interestParent && !interestChild) {
							pushInExploreAndAddToKeep(child,nodesToExplore,nodesToKeep);							
							interestChild = true;
						}
					}
					// X<-Y->Z
					for (int j = i + 1; j < numChildren; j++) {
						Node child2 = children.get(j);
						boolean interestChild2 = nodesToKeep.contains(child2);
						if (interestChild2 && !interestChild) {
							pushInExploreAndAddToKeep(child,nodesToExplore,nodesToKeep);
							interestChild = true;
						} else if (interestChild && !interestChild2) {
							pushInExploreAndAddToKeep(child2,nodesToExplore,nodesToKeep);							
						}
					}
				}
			}
		}
		
		
		// remove nodes that are not in nodesToKeep in prunedProbNet
		ArrayList<Node> prunedProbNetNodes = ProbNet
				.getNodesOfProbNodes(probNet.getProbNodes());
		for (Node node : prunedProbNetNodes) {
			if (!nodesToKeep.contains(node)) {
				probNet.removeProbNode((ProbNode) node.getObject());
			}
		}
		
		return probNet;
	}
	
	private static void pushInExploreAndAddToKeep(Node node, UniqueStack<Node> nodesToExplore, 
			HashSet<Node> nodesToKeep){
		nodesToExplore.push(node);
		nodesToKeep.add(node);
	}
	
	
	private static HashSet<Node> getEvidenceNodes(ProbNet probNet, 
			Collection<Variable> variablesOfEvidence) {
		HashSet<Node> hashEvidenceNodes = new HashSet<Node>();
		for (Variable variable : variablesOfEvidence) {
			ProbNode evidenceProbNode = probNet.getProbNode(variable);
			if (evidenceProbNode != null) {
				Node evidenceNode = evidenceProbNode.getNode();
				hashEvidenceNodes.add(evidenceNode);
			}
		}
		return hashEvidenceNodes;
	}

	/**
	 * @param nodes
	 *            . <code>ArrayList</code> of <code>Node</code>.
	 * @return <code>nodes</code> and its ancestors. <code>ArrayList</code> of
	 *         <code>Node</code>.
	 */
	private static HashSet<Node> getNodesAndAncestors(Collection<Node> nodes) {
		HashSet<Node> ancestors = new HashSet<Node>(nodes);

		Stack<Node> noExploredNodes = new Stack<Node>();
		noExploredNodes.addAll(nodes);

		while (!noExploredNodes.empty()) {
			Node node = noExploredNodes.pop();
			ArrayList<Node> parents = node.getParents();
			for (Node parent : parents) {
				if (ancestors.add(parent)) {
					noExploredNodes.push(parent);
				}
			}
		}
		return ancestors;
	}


	
}
