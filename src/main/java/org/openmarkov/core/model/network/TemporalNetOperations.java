package org.openmarkov.core.model.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmarkov.core.exception.ImposedPoliciesException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.TablePotential;

public class TemporalNetOperations {

	// Attributes
	/** Vertical separation in pixels between slices. */
	private static final double VERTICAL_OFFSET = 0;

	/** Horizontal separation between slices */
	private static final double MARGIN_BETWEEN_SLICES = 150;
	
	/**
	 * When invoking this method, probNet is a copy of the concise net. We add
	 * new nodes, links, and potentials to make it a compact net.
	 * If some of the slices of the concise net miss a node present in previous slices,
	 *  adds the node to that slice
	 */
	public static List<List<Node>> compactNetwork(ProbNet probNet) {
		List<List<Node>> classifiedNodes = classifyNodesbySlices(probNet, probNet.getVariables());
		// generate the new nodes of the compact net
		List<Node> generatingNodes = new ArrayList<Node>();
		List<Node> generatedNodes = new ArrayList<Node>();
		for (int slice = 0; slice < classifiedNodes.size() - 1; slice++) {
			double sliceWidth = getSliceWidth(classifiedNodes.get(slice));
			List<Node> generatedNodesInThisSlice = new ArrayList<Node>(classifiedNodes.get(
					slice).size());
			for (Node generatingNode : classifiedNodes.get(slice)) {
				if (!probNet.containsShiftedVariable(generatingNode.getVariable(), 1)) {
					Node newNode = probNet.addShiftedNode(generatingNode, 1,
							sliceWidth + MARGIN_BETWEEN_SLICES, VERTICAL_OFFSET);
					generatingNodes.add(generatingNode);
					generatedNodes.add(newNode);
					generatedNodesInThisSlice.add(newNode);
				}else
				{
					// Replace all SameAsPrevious potentials
					try {
						Variable variable = probNet.getShiftedVariable(generatingNode.getVariable(), 1);
						Node node = probNet.getNode(variable);
						if(!node.getPotentials().isEmpty() && node.getPotentials().get(0) instanceof SameAsPrevious)
						{
							Potential newPotential = ((SameAsPrevious)node.getPotentials().get(0)).getOriginalPotential(probNet).copy();
							newPotential.shift(probNet, variable.getTimeSlice() - newPotential.getConditionedVariable().getTimeSlice());
							node.setPotential(newPotential);
						}
							
					} catch (NodeNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			for (Node node : generatedNodesInThisSlice) {
				classifiedNodes.get(node.getVariable().getTimeSlice()).add(node);
			}
		}
		// assign potentials to the new nodes of the compact net
		Node generatingNode, generatedNode;
		for (int i = 0; i < generatedNodes.size(); i++) {
			generatingNode = generatingNodes.get(i);
			generatedNode = generatedNodes.get(i);
			try {
				expandPotentialAndLinks(probNet, generatingNode, generatedNode, 1);
			} catch (NodeNotFoundException e) {
				// If we get here is because we have not generated the nodes as
				// we should
				e.printStackTrace();
			}
		}
		return classifiedNodes;
	}
	
	public static ProbNet expandNetwork(ProbNet probNet, int numSlices) {
		ProbNet expandedNet = probNet.copy(); 
		List<List<Node>> classifiedNodes = compactNetwork(expandedNet);
		while (classifiedNodes.size() <= numSlices) {
			generateNextSlice(expandedNet, classifiedNodes);
		}
		return expandedNet;
	}
	
	
	public static Map<Variable, TablePotential> traceTemporalEvolution(ProbNet expandedNetwork, 
			InferenceAlgorithm inferenceAlgorithm,
			Variable variableOfInterest)
			throws ImposedPoliciesException {

		Map<Variable, TablePotential> probsAndUtilities = null;
		String baseName = variableOfInterest.getBaseName();
		List<Variable> variablesOfInterest = new ArrayList<>();
		List<Node> expandedProbNetNodes = expandedNetwork.getNodes();
		for (Node node : expandedProbNetNodes) {
			if (node.getVariable().getBaseName().equals(baseName)) {
				variablesOfInterest.add(node.getVariable());
			}
		}

		try {
			probsAndUtilities = inferenceAlgorithm.getProbsAndUtilities(variablesOfInterest);
		} catch (IncompatibleEvidenceException | UnexpectedInferenceException e) {
			e.printStackTrace();
		}
		return probsAndUtilities;
	}	

	/**
	 * Assigns nodes to slices in a collection of slices. Each slice is a
	 * collection of nodes.
	 * 
	 * @return <code>List</code> of <code>List</code> of <code>Node</code>
	 */
	private static List<List<Node>> classifyNodesbySlices(ProbNet probNet,
			List<Variable> variables) {
		List<List<Node>> classifiedNodes;
		int firstSliceIndex = Integer.MAX_VALUE;
		int lastSliceIndex = Integer.MIN_VALUE;
		// find the indexes of the first and last slice
		int timeSlice;
		for (Variable variable : variables) {
			if (variable.isTemporal()) {
				timeSlice = variable.getTimeSlice();
				if (timeSlice < firstSliceIndex) {
					firstSliceIndex = timeSlice;
				}
				if (timeSlice > lastSliceIndex) {
					lastSliceIndex = timeSlice;
				}
			}
		}
		int numSlices = lastSliceIndex - firstSliceIndex + 1;
		// initializes the variable classifiedNodes
		classifiedNodes = new ArrayList<>(numSlices);
		for (int slice = 0; slice < numSlices; slice++) {
			classifiedNodes.add(new ArrayList<Node>());
		}
		// assigns each node to its slice
		Variable variable;
		for (Node node : probNet.getNodes()) {
			variable = node.getVariable();
			if (variable.isTemporal()) {
				classifiedNodes.get(variable.getTimeSlice()).add(node);
			}
		}
		return classifiedNodes;
	}

	/**
	 * @precondition extendedNet in this class must be a compact net
	 */
	private static void generateNextSlice(ProbNet probNet, List<List<Node>> classifiedNodes) {
		List<Node> lastSliceNodes = classifiedNodes.get(classifiedNodes.size() - 1);
		List<Node> newSliceNodes = new ArrayList<Node>();
		// generates the new nodes
		double sliceWidth = getSliceWidth(lastSliceNodes);
		for (Node generatingNode : lastSliceNodes) {
			Node newNode = probNet.addShiftedNode(generatingNode, 1, sliceWidth
					+ MARGIN_BETWEEN_SLICES, VERTICAL_OFFSET);
			newSliceNodes.add(newNode);
		}
		// generates new slices
		// assign potentials to the new nodes
		Node generatingNode, generatedNode;
		for (int i = 0; i < lastSliceNodes.size(); i++) {
			generatingNode = lastSliceNodes.get(i);
			generatedNode = newSliceNodes.get(i);
			try {
				expandPotentialAndLinks(probNet, generatingNode, generatedNode, 1);
			} catch (NodeNotFoundException e) {
				// If we get here is because we have not generated the nodes as
				// we should
				e.printStackTrace();
			}
		}
		classifiedNodes.add(newSliceNodes);
	}

	/**
	 * TODO document: oldNode is a node in the last slice of the compact net
	 * TODO We are assuming that there is only one potential per node. Revise
	 * 
	 * @throws NodeNotFoundException
	 */
	private static void expandPotentialAndLinks(ProbNet probNet, Node oldNode, Node newNode, int timeDifference)
			throws NodeNotFoundException {
		Potential oldPotential = oldNode.getPotentials().get(0);
		Potential newPotential = null;
		if (oldPotential instanceof CycleLengthShift) {
			newPotential = new CycleLengthShift(oldPotential.getShiftedVariables(probNet,
					timeDifference));
		} else {
			newPotential = oldPotential.copy();
			newPotential.shift(probNet, timeDifference);
		}
		newNode.addPotential(newPotential);
		newPotential.createDirectedLinks(probNet);
	}

	private static double getSliceWidth(List<Node> nodes) {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = 0.0;
		for (Node node : nodes) {
			if (node.getCoordinateX() > maxX) {
				maxX = node.getCoordinateX();
			}
			if (node.getCoordinateX() < minX) {
				minX = node.getCoordinateX();
			}
		}
		return maxX - minX;
	}	
}
