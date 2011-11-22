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
		ProbNet prunedProbNet = probNet.copy();
		HashSet<Variable> variablesOfInterest2 = 
				new HashSet<Variable>(variablesOfInterest);
		HashSet<Variable> variablesOfEvidence2 = 
				new HashSet<Variable>(evidence.getVariables());
		prunedProbNet = removeBarrenNodes(prunedProbNet, 
				variablesOfInterest2, variablesOfEvidence2);
		prunedProbNet = removeUnreachableNodes(prunedProbNet, 
				variablesOfInterest2, variablesOfEvidence2);
		return prunedProbNet;
	}
	
	/**Projects the evidence in the <code>probNet</code> potentials and remove
	 * evidence variables
	 * @param probNet. <code>ProbNet</code>
	 * @param evidence. <code>EvidenceCase</code>
	 * @throws NotEnoughMemoryException */
	public static void projectEvidence(ProbNet probNet, 
			EvidenceCase evidence) throws NotEnoughMemoryException {
		ArrayList<Variable> variables = evidence.getVariables();
		for (Variable variable : variables) {
			ArrayList<Potential> potentials = probNet.getPotentials(variable);
			for (Potential potential : potentials) {
				probNet.removePotential(potential);
				try {
					Potential newPotential = 
							potential.tableProject(evidence, null).get(0);
					if (newPotential.getNumVariables() > 0) {
						boolean containVariables = true;
						for (Variable potentialVariable : 
								newPotential.getVariables()) {
							containVariables &= 
									(probNet.getProbNode(potentialVariable) 
											!= null);
						}
						if (containVariables) {
							probNet.addPotential(newPotential);
						}
					}
				} catch (NonProjectablePotentialException e) {
					e.printStackTrace(); // Unreachable code
				} catch (WrongCriterionException e) {
					e.printStackTrace(); // Unreachable code
				}
			}
			probNet.removeProbNode(probNet.getProbNode(variable));
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
	private static ProbNet removeBarrenNodes(ProbNet prunedProbNet, 
			Collection<Variable> variablesOfInterest, 
			HashSet<Variable> variablesOfEvidence) {
		ArrayList<ProbNode> barrenNodes = new ArrayList<ProbNode>();
		// Get nodes without parents
		boolean foundBarrenNodes = false;
		do {
			ArrayList<ProbNode> probNodes = prunedProbNet.getProbNodes();
			for (ProbNode probNode : probNodes) {
				Node node = probNode.getNode();
				if (node.getNumChildren() == 0) {
					Variable variable = probNode.getVariable();
					if (!variablesOfInterest.contains(variable) && 
							!variablesOfEvidence.contains(variable)) {
						barrenNodes.add(probNode);
					}
				}
			}
			foundBarrenNodes = barrenNodes.size() > 0;
			if (foundBarrenNodes) {
				for (ProbNode probNode : barrenNodes) {
					prunedProbNet.removeProbNode(probNode);
				}
				barrenNodes.clear();
			}
		} while (foundBarrenNodes);
		return prunedProbNet;
	}

	private static ProbNet removeUnreachableNodes(ProbNet prunedProbNet, 
			Collection<Variable> variablesOfInterest, 
			HashSet<Variable> variablesOfEvidence) {
		// Gets nodes of interest and adds nodes connected to them
		Stack<Node> nodesToExplore = new Stack<Node>();
		HashSet<Node> nodesToKeep = new HashSet<Node>();

		// Store nodes of variablesOfInterest in nodesToKeep
		for (Variable variable : variablesOfInterest) {
			Node node = prunedProbNet.getProbNode(variable).getNode();
			nodesToKeep.add(node);
		}		

		// Add neighbors of variablesOfInterest as nodesToKeep and store them in
		// nodesToExplore
		@SuppressWarnings("unchecked")
		HashSet<Node> nodesToKeepClon = (HashSet<Node>)nodesToKeep.clone();
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
				getEvidenceNodes(prunedProbNet, variablesOfEvidence);
		HashSet<Node> evidenceAndAncestors = getAncestors(hashEvidenceNodes);
		
		// For each interest node, finds connected nodes via valid paths.
		while (!nodesToExplore.empty()) {
			Node node = nodesToExplore.pop();
			
			// Find head to head connected nodes: X->Y<-Z and
			// Y is evidence or Y has a descendent that is evidence
			if (evidenceAndAncestors.contains(node)) {
				ArrayList<Node> parents = node.getParents();
				int parentsSize = parents.size();
				boolean interestI, interestJ;
				for (int i = 0; i < parentsSize - 1; i++) {
					Node parentI = parents.get(i);
					interestI = nodesToKeep.contains(parentI);
					for (int j = 1; j < parentsSize; j++) {
						Node parentJ = parents.get(j);
						interestJ = nodesToKeep.contains(parentJ);
						if (interestI && !interestJ) {
							nodesToExplore.push(parentJ);
							nodesToKeep.add(parentJ);
						} else if (!interestI && interestJ) {
							nodesToExplore.push(parentI);
							nodesToKeep.add(parentI);
							interestI = true;
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
							nodesToExplore.push(parent);
							nodesToKeep.add(parent);
						} else if (interestParent && !interestChild) {
							nodesToExplore.push(child);
							nodesToKeep.add(child);
							interestChild = true;
						}
					}
					// X<-Y->Z
					for (int j = i + 1; j < numChildren; j++) {
						Node child2 = children.get(j);
						boolean interestChild2 = nodesToKeep.contains(child2);
						if (interestChild2 && !interestChild) {
							nodesToExplore.push(child);
							nodesToKeep.add(child);
							interestChild = true;
						} else if (interestChild && !interestChild2) {
							nodesToExplore.push(child2);
							nodesToKeep.add(child2);
						}
					}
				}
			}
		}

		// remove nodes that are not in nodesToKeep in prunedProbNet
		ArrayList<Node> prunedProbNetNodes = ProbNet
				.getNodesOfProbNodes(prunedProbNet.getProbNodes());
		for (Node node : prunedProbNetNodes) {
			if (!nodesToKeep.contains(node)) {
				prunedProbNet.removeProbNode((ProbNode) node.getObject());
			}
		}
		
		return prunedProbNet;
	}
	
	
	private static HashSet<Node> getEvidenceNodes(ProbNet prunedProbNet, 
			Collection<Variable> variablesOfEvidence) {
		HashSet<Node> hashEvidenceNodes = new HashSet<Node>();
		for (Variable variable : variablesOfEvidence) {
			ProbNode evidenceProbNode = prunedProbNet.getProbNode(variable);
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
	private static HashSet<Node> getAncestors(Collection<Node> nodes) {
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
