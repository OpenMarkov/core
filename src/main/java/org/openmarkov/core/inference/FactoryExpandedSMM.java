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

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

public class FactoryExpandedSMM {

	// Attributes
	/** Horizontal separation in pixels between slices. */
	private double coordinateXOffset;

	/** Vertical separation in pixels between slices. */
	private final double coordinateYOffset = 0;
	
	private ProbNet probNet;

	/** Set of probNodes that will be cloned in each slice. */
	private ArrayList<ProbNode> generatedNodes;
	
	/** Each <ArrayList<ProbNode> contains the nodes of a time slice */
	private ArrayList<ArrayList<ProbNode>> classifiedNodes;
	
	// Constructor
	/** @param conciseNet. <code>ProbNet</code>
	 * @param numSlices. <code>int</code>
	 * @param simulationIndexVariable. <code>Variable</code>
	 * @param coordinateXOffset. <code>int</code>
	 * @throws NotEnoughMemoryException */
	public FactoryExpandedSMM(ProbNet conciseNet, int numSlices, 
			Variable simulationIndexVariable, double coordinateXOffset) 
	throws NotEnoughMemoryException {
		this.coordinateXOffset = coordinateXOffset;
		
		
		//probNet must be the original network and expandedNetwork the probNet espanded numSlices times
		probNet = conciseNet.copy();
		  //TODO get decisionCriteria from the probNet
		
		//adaptProbNetForCE();
		
		if (simulationIndexVariable != null) {
			sampleProbNet(simulationIndexVariable);
		}
		
		// if some of the slices of the concise net misses a node present
		// in previous slices, adds the node to that slice
		makeNetCompact();
		
		// expands the net
		while (classifiedNodes.size() < numSlices) {
			generateNextSlice();
		}

	}
	
	
	/**
	 * Adapts the concise network for performing cost-effectiveness analysis.
	 */
	public void adaptProbNetForCE(){
		//TODO take decisions criteria from the network
		  probNet.setDecisionCriteria(new String[]{"cost", "effectiveness"});
			//make all utility nodes of the expanded probNet child of decision criteria
			  ArrayList<ProbNode> utilityNodes = probNet.getProbNodes(NodeType.UTILITY);
			  ProbNode decisionCriteria = new ProbNode(probNet, probNet.getDecisionCriteriaVariable(), NodeType.DECISION);
			  probNet.addProbNode(decisionCriteria);
			  for (int i = 0; i < utilityNodes.size(); i++) {
				  Potential utility = utilityNodes.get(i).getPotentials().get(0);
				  probNet.addLink(decisionCriteria, utilityNodes.get(i), true);
				  ArrayList<Variable> treeVariables = utility.getVariables();
				  treeVariables.add(decisionCriteria.getVariable());
				  
				  String iUtilityDecisionCriteriaName = utilityNodes.get(i).getVariable().getDecisionCriteria().getString();
				  boolean hasDecisionCriteria = false;
				  String otherDecisionCriteria = null;
				  TreeADDPotential treeADDPotential = null;
				  if (iUtilityDecisionCriteriaName.equals("cost")){
					  hasDecisionCriteria = true;
					  otherDecisionCriteria = "effectiveness";
				  }
				  else if (iUtilityDecisionCriteriaName.equals("effectiveness")){
					  hasDecisionCriteria = true;
					  otherDecisionCriteria = "cost";
				  }
				  if (hasDecisionCriteria){
					  treeADDPotential = constructTreeADDForCE(decisionCriteria,treeVariables,utility,utilityNodes.get(i),iUtilityDecisionCriteriaName,otherDecisionCriteria);
				  }
				  ArrayList<Potential> potentials = new ArrayList<>();
				  potentials.add(treeADDPotential);
				 utilityNodes.get(i).setPotentials(potentials);
			  }
			
		
	}
	
	/**
	 * @param numSlices
	 * @param network
	 * @param costDiscount Percentage of discount. The utility function in instant time t will be:
	 * U(t) = U(t-1)/(1+discount/100.0)  
	 * @param adaptForCE
	 * @return An expanded network built from a SMM. It adapts the network to Cost-Effectiveness analysis is 
	 * adaptForCE is true.
	 */
	public static ProbNet constructExpandedNetwork(int numSlices, ProbNet network, double costDiscount, double effectivenessDiscount, boolean adaptForCE) {
		FactoryExpandedSMM expandedNetFactory = null;
		InferenceOptions inferenceOptions;
		
		try {
			expandedNetFactory = new FactoryExpandedSMM(network, numSlices, null, 200.0);
			inferenceOptions = new InferenceOptions(network, null);
			if (adaptForCE){
				expandedNetFactory.adaptProbNetForCE();
			}
			expandedNetFactory.applyDiscountToUtilityNodes(costDiscount, effectivenessDiscount, inferenceOptions);
		} catch (NotEnoughMemoryException e) {
			e.printStackTrace();
		}
		ProbNet expandedNetwork = expandedNetFactory.getExtendedNet();
		return expandedNetwork;
	}
	
	
	/**
	 * @param decisionCriteria
	 * @param treeVariables
	 * @param utility
	 * @param utilProbNode
	 * @param decisionCriteriaName
	 * @param otherDecisionCriteriaName
	 * @return A TreeADD for the utility potential where the branch of the criteria of the node is the old utility table, and the branch of the other criteria is 0.
	 */
	public TreeADDPotential constructTreeADDForCE(ProbNode decisionCriteria, ArrayList<Variable> treeVariables, Potential utility, ProbNode utilProbNode, String decisionCriteriaName, String otherDecisionCriteriaName){
		
		TreeADDPotential treeADDPotential = new TreeADDPotential(treeVariables, probNet.getDecisionCriteriaVariable(),
				  utility.getPotentialRole(), utility.getUtilityVariable());
		
		  ArrayList<Variable> variables = new ArrayList<>();
		  variables.add(decisionCriteria.getVariable());
		 
		  for (int j = 0; j < treeADDPotential.getBranches().size(); j++) {
			  TreeADDBranch jBranch = treeADDPotential.getBranches().get(j);
			String jBranchName = jBranch.getBranchStates().get(0).getName();
			
			if (jBranchName.equalsIgnoreCase(decisionCriteriaName)) {
				  jBranch.setPotential(utility);
			  } else if (jBranchName.equalsIgnoreCase(otherDecisionCriteriaName)) {
				  //zero potential
				  jBranch.setPotential(new UniformPotential(utility.getVariables(), PotentialRole.UTILITY, utilProbNode.getVariable()));
			  }
		  }
		  return treeADDPotential;
	}

	// Methods
	/** @param simulationIndexVariable. <code>Variable</code>
	 * @throws NotEnoughMemoryException */
	private void sampleProbNet(Variable simulationIndexVariable) 
			throws NotEnoughMemoryException {
		for (ProbNode probNode : probNet.getProbNodes()) {
			probNode.samplePotentials(simulationIndexVariable);
		}
	}

	/** When invoking this method, probNet is a copy of the concise net. We add
	 * new nodes, links, and potentials to make it a compact net. */
	private void makeNetCompact() {
		classifiedNodes = classifyNodes(probNet, probNet.getVariables());

		// generate the new nodes of the compact net
		ArrayList<ProbNode> generatingNodes = new ArrayList<ProbNode>();
		generatedNodes = new ArrayList<ProbNode>();

		for (int slice = 0; slice < classifiedNodes.size()-1; slice++) {
			ArrayList<ProbNode> generatedNodesInThisSlice = 
				new ArrayList<ProbNode>(classifiedNodes.get(slice).size());
			for (ProbNode generatingProbNode : classifiedNodes.get(slice)) {
				Variable generatingVariable = generatingProbNode.getVariable();
				int newSliceIndex = generatingVariable.getTimeSlice() + 1;
				String nameOfNewVariable = generatingVariable.getBaseName() + 
							" [" + newSliceIndex + "]";
				if ( !probNet.containsVariable(nameOfNewVariable) ) {
					ProbNode newProbNode =
						probNet.addShiftedProbNode(generatingProbNode, 1,
							coordinateXOffset, coordinateYOffset );
					generatingNodes.add(generatingProbNode);
					generatedNodes.add(newProbNode);
					generatedNodesInThisSlice.add(newProbNode);
				}
			}
			for (ProbNode probNode : generatedNodesInThisSlice) {
				classifiedNodes.get(probNode.getVariable().getTimeSlice()).
					add(probNode);
			}
		}
		
		// assign potentials to the new nodes of the compact net
		ProbNode generatingNode, generatedNode;
		for (int i = 0; i < generatedNodes.size(); i++) {
			generatingNode = generatingNodes.get(i);
			generatedNode = generatedNodes.get(i);
			expandPotentialAndLinks(generatingNode, generatedNode, 1);
		}
	}

	/** Assigns nodes to slices in a collection of slices. Each slice is a
	 * collection of nodes.
	 * @return <code>ArrayList</code> of <code>ArrayList</code> of 
	 *  <code>ProbNode</code> */
	public static ArrayList<ArrayList<ProbNode>> classifyNodes(ProbNet probNet,
			ArrayList<Variable> variables) {
		ArrayList<ArrayList<ProbNode>> classifiedNodes;
		int firstSliceIndex = Integer.MAX_VALUE;
		int lastSliceIndex = Integer.MIN_VALUE;

		// find the indexes of the first and last slice
		int timeSlice;
		for (Variable variable : variables) {
			if (variable.isTemporal()) {
				timeSlice = variable.getTimeSlice();
				if ( timeSlice < firstSliceIndex ) {
					firstSliceIndex = timeSlice;
				}
				if ( timeSlice > lastSliceIndex ) {
					lastSliceIndex = timeSlice;
				}
			}
		}
		
		int numSlices = lastSliceIndex - firstSliceIndex + 1;
		
		// initializes the variable classifiedNodes
		classifiedNodes = new ArrayList<ArrayList<ProbNode>>(numSlices);
		for (int slice = 0; slice < numSlices; slice++) {
			classifiedNodes.add(new ArrayList<ProbNode>());
		}

		// assigns each node to its slice 
		Variable variable;
		for (ProbNode node : probNet.getProbNodes()) {
			variable = node.getVariable();
			if (variable.isTemporal()) {
				classifiedNodes.get(variable.getTimeSlice()).add(node);
			} 
			
		}
		
		return classifiedNodes;
	}
	
	public ProbNet getExtendedNet(){
		return probNet;
	}
	
	
	/**
	 * @param costDiscount
	 * @param inferenceOptions 
	 * @throws NotEnoughMemoryException
	 * It applies the discount to each utility potential
	 */
	public void applyDiscountToUtilityNodes(double costDiscount, double effectivenessDiscount, InferenceOptions inferenceOptions) throws NotEnoughMemoryException{
		// apply discount rate for all temporal utility nodes in the expanded network
		  ArrayList<ProbNode> utilityExpandedNodes = probNet.getProbNodes(NodeType.UTILITY);
		  for (int i = 0; i < utilityExpandedNodes.size(); i++) {
			  ProbNode iUtilityProbNode = utilityExpandedNodes.get(i);
			int timeSlice = iUtilityProbNode.getVariable().getTimeSlice();
			double discount = iUtilityProbNode.getVariable().getDecisionCriteria().getString() == "cost" ?  costDiscount : effectivenessDiscount;
			if (iUtilityProbNode.getVariable().isTemporal() && timeSlice > 0) {
				  double discountRate = 1.0 / (Math.pow((1.0 + (discount/100.0)), timeSlice));
				  //project TreeADD original potential to a table
				 try {
					 TablePotential projectedPotential = null;
					 Potential potentialToBeProjected;
					Potential potential = iUtilityProbNode.getPotentials().get(0);
					if (potential instanceof SameAsPrevious) {
						ArrayList<Variable> variables = potential.getVariables();
						Variable utilityVariable = potential.getUtilityVariable();
						potentialToBeProjected = (((SameAsPrevious)potential).getOriginalPotential()).copy();
						potentialToBeProjected.setVariables(variables);
						
						potentialToBeProjected.setUtilityVariable(utilityVariable);
					 } else {
						potentialToBeProjected = (potential);
					 }
					 projectedPotential = potentialToBeProjected.tableProject(new EvidenceCase(), inferenceOptions).get(0);
//					 projectedPotential.setOriginalVariables(potentialToBeProjected.getVariables());
					double[] valuesProjectedPotential = projectedPotential.getValues();
					for (int j = 0; j < valuesProjectedPotential.length; j++) {
						valuesProjectedPotential[j] = valuesProjectedPotential[j] * (discountRate);
					}
					ArrayList<Potential> potentials = new ArrayList<>();
					potentials.add(projectedPotential);
					iUtilityProbNode.setPotentials(potentials);
				} catch (NonProjectablePotentialException | WrongCriterionException e) {					
					e.printStackTrace();
				} 
				  //utilityExpandedNodes.get(i).getPotentials().get(0).get
			  }
		  }
	}
	
	/** 
	 * @precondition extendedNet in this class must be a compact net */
	private void generateNextSlice() {

		ArrayList<ProbNode> lastSliceNodes = 
			classifiedNodes.get(classifiedNodes.size()-1);
		ArrayList<ProbNode> newSliceNodes = new ArrayList<ProbNode>();

		// generates the new nodes
		for (ProbNode generatingProbNode : lastSliceNodes) {
			ProbNode newProbNode =
				probNet.addShiftedProbNode(generatingProbNode, 1,
					coordinateXOffset, coordinateYOffset );
			newSliceNodes.add(newProbNode);
		}
		// generates new slices
		// assign potentials to the new nodes
		ProbNode generatingNode, generatedNode;
		for (int i = 0; i < lastSliceNodes.size(); i++) {
			generatingNode = lastSliceNodes.get(i);
			generatedNode = newSliceNodes.get(i);
			expandPotentialAndLinks(generatingNode, generatedNode, 1);
		}
		
		classifiedNodes.add(newSliceNodes);
	}		

	/** TODO documentar
	 * oldNode is a node in the last slice of the compact net
	 * TODO We are assuming that there is only one potential per node. Revise */
	private void expandPotentialAndLinks(ProbNode oldNode, ProbNode newNode, int timeDifference) {
		Potential oldPotential = oldNode.getPotentials().get(0);
		Potential newPotential = null;
		if (oldPotential.getPotentialType() == PotentialType.CYCLE_LENGTH_SHIFT) {
			newPotential = new CycleLengthShift(oldPotential.getShiftedVariables(probNet,
					timeDifference));
			/*
			 * if (oldPotential.getPotentialRole() == PotentialRole.UTILITY) {
			 * newPotential
			 * .setUtilityVariable(oldPotential.getUtilityVariable()); }
			 */
		} else {
			int timeDifferenceWithNew;
			Potential referencePotentialForNewPotential;
			if (oldPotential.getPotentialType() == PotentialType.SAME_AS_PREVIOUS) {
				 Potential originalPotential = ((SameAsPrevious) oldPotential)
						.getOriginalPotential();
				// Sets time difference respect to the original potential
				Variable firstOriginalVariable = null;
				PotentialRole potentialRole = originalPotential.getPotentialRole();
				switch (potentialRole) {
				case CONDITIONAL_PROBABILITY:
					firstOriginalVariable = originalPotential.getVariables().get(0);
					break;
				case UTILITY:
					firstOriginalVariable = originalPotential.getUtilityVariable();
					break;
				}
				Variable newVariable = newNode.getVariable();
				timeDifferenceWithNew = newVariable.getTimeSlice() - firstOriginalVariable.getTimeSlice();
				referencePotentialForNewPotential = originalPotential;
			} else {
				referencePotentialForNewPotential = oldPotential;
				timeDifferenceWithNew = timeDifference;
			}
			 
			try {
				newPotential = new SameAsPrevious(referencePotentialForNewPotential, probNet, timeDifferenceWithNew);
				/*if (referencePotentialForNewPotential.getPotentialRole() == PotentialRole.UTILITY) {
					newPotential.setUtilityVariable(referencePotentialForNewPotential.getUtilityVariable());
				}*/
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		newNode.addPotential(newPotential);
		newPotential.createDirectedLinks(probNet);
	}

}
