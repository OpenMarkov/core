/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

public class MPADFactory {
    // Attributes
    /** Vertical separation in pixels between slices. */
    private final double VERTICAL_OFFSET = 0;

    /** Horizontal separation between slices */
    private final double MARGIN_BETWEEN_SLICES = 150;

    private ProbNet probNet;
    /** Set of probNodes that will be cloned in each slice. */
    private List<ProbNode> generatedNodes;
    /** Each <ArrayList<ProbNode> contains the nodes of a time slice */
    private List<List<ProbNode>> classifiedNodes;

    private int numSlices;

    // Constructor
    /**
     * @param conciseNet
     *            . <code>ProbNet</code>
     * @param numSlices
     *            . <code>int</code>
     * @param simulationIndexVariable
     *            . <code>Variable</code>
     * @param coordinateXOffset
     *            . <code>int</code>
     */
    public MPADFactory(ProbNet conciseNet, int numSlices) {
        // probNet must be the original network and
        // expandedNetwork the probNet expanded numSlices times
        probNet = conciseNet.copy();
        // if some of the slices of the concise net miss a node present
        // in previous slices, adds the node to that slice
        compactNetwork();
        // expands the net
        this.numSlices = numSlices;
        while (classifiedNodes.size() <= numSlices) {
            generateNextSlice();
        }
    }

    // Methods
    /**
     * Adapts the concise network for performing cost-effectiveness analysis.
     * Adds decisionCriteria node to the network and makes all utility nodes
     * children of it
     */
    public void adaptMPADforCE(int numSlices, double costDiscountRate,
            double effectivenessDiscountRate, EvidenceCase evidence, TransitionTime transitionTime) {
        // Extend evidence
        extendEvidence(probNet, evidence);

        // Project evidence on temporal nodes
        InferenceOptions inferenceOptions = new InferenceOptions(probNet, null);
        projectTemporalEvidence(inferenceOptions, evidence);
        
        if (transitionTime == TransitionTime.BEGINNING) {
            pruneZeroCycleUtilities();
        } else if (transitionTime == TransitionTime.END) {
            // Prune last cycle utilities
            pruneLastCycleUtilities();
        } else {
            // Half zero and last cycle utilities
            applyHalfCycleCorrection();
        }
        // Remove super value nodes
        probNet = BasicOperations.removeSuperValueNodes(probNet, evidence, false, false, null);
        
        applyDiscountToUtilityNodes(costDiscountRate, effectivenessDiscountRate);
        
        List<String> decisionCriteriaNames = new ArrayList<>();
        for (int i = 0; i < probNet.getDecisionCriteria().size(); i++) {
            String decisionCriterion = probNet.getDecisionCriteria().get(i).getString();
            if (decisionCriterion.equalsIgnoreCase("cost")
                    || decisionCriterion.equalsIgnoreCase("effectiveness")) {
                decisionCriteriaNames.add(decisionCriterion);
            }
        }
        if (decisionCriteriaNames.size() != 2) {
            // TODO propagate exception
            // throw new
            // Exception("For cost effectiveness analysis performance network's decision criteria must be cost and effectiveness");
        }
        probNet.setDecisionCriteria(decisionCriteriaNames);
        // make all utility nodes of the expanded probNet children of the
        // decision criteria node
        ProbNode decisionCriteriaNode = new ProbNode(probNet,
                probNet.getDecisionCriteriaVariable(), NodeType.DECISION);
        probNet.addProbNode(decisionCriteriaNode);
        for (ProbNode utilityNode : BasicOperations.getTerminalUtilityNodes(probNet)) {
            Potential utilityPotential = utilityNode.getPotentials().get(0);
            probNet.addLink(decisionCriteriaNode, utilityNode, true);
            List<Variable> treeVariables = utilityPotential.getVariables();
            treeVariables.add(decisionCriteriaNode.getVariable());
            String utilityDecisionCriteriaName = utilityNode.getVariable().getDecisionCriteria()
                    .getString();
            boolean hasDecisionCriterion = false;
            String otherDecisionCriterion = null;
            TreeADDPotential treeADDPotential = null;
            if (utilityDecisionCriteriaName.equals("cost")) {
                hasDecisionCriterion = true;
                otherDecisionCriterion = "effectiveness";
            } else if (utilityDecisionCriteriaName.equals("effectiveness")) {
                hasDecisionCriterion = true;
                otherDecisionCriterion = "cost";
            }
            if (hasDecisionCriterion) {
                treeADDPotential = constructTreeADDForCE(decisionCriteriaNode, treeVariables,
                        utilityPotential, utilityNode, utilityDecisionCriteriaName,
                        otherDecisionCriterion);
                utilityNode.setPotential(treeADDPotential);
            }
        }
    
    }

    public ProbNet getExtendedNetwork() {
        return probNet;
    }      

    /**
     * @param decisionCriteria
     * @param treeVariables
     * @param utility
     * @param utilProbNode
     * @param decisionCriteriaName
     * @param otherDecisionCriteriaName
     * @return A TreeADD for the utility potential where the branch of the
     *         criteria of the node is the old utility table, and the branch of
     *         the other criteria is 0.
     */
    private TreeADDPotential constructTreeADDForCE(ProbNode decisionCriteria,
            List<Variable> treeVariables, Potential utility, ProbNode utilProbNode,
            String decisionCriteriaName, String otherDecisionCriteriaName) {
        TreeADDPotential treeADDPotential = new TreeADDPotential(treeVariables,
                probNet.getDecisionCriteriaVariable(), utility.getPotentialRole(),
                utility.getUtilityVariable());
        List<Variable> variables = new ArrayList<>();
        variables.add(decisionCriteria.getVariable());
        for (int j = 0; j < treeADDPotential.getBranches().size(); j++) {
            TreeADDBranch jBranch = treeADDPotential.getBranches().get(j);
            String jBranchName = jBranch.getBranchStates().get(0).getName();
            if (jBranchName.equalsIgnoreCase(decisionCriteriaName)) {
                jBranch.setPotential(utility);
            } else if (jBranchName.equalsIgnoreCase(otherDecisionCriteriaName)) {
                // zero potential
                jBranch.setPotential(new UniformPotential(utility.getVariables(),
                        PotentialRole.UTILITY, utilProbNode.getVariable()));
            }
        }
        return treeADDPotential;
    }

    /**
     * when checkbox zero cycle is unselected utility nodes which decision
     * criteria is cost or effectiveness must be removed from the network this
     * approach is in order to take into account changes taking place at the
     * beginning of the cycle not at the end
     * 
     * @return
     */
    private void pruneZeroCycleUtilities() {
        List<ProbNode> utilityExpandedNodes = probNet.getProbNodes(NodeType.UTILITY);
        for (int i = 0; i < utilityExpandedNodes.size(); i++) {
            ProbNode iUtilityProbNode = utilityExpandedNodes.get(i);
            int timeSlice = iUtilityProbNode.getVariable().getTimeSlice();
            String decisionCriterion = iUtilityProbNode.getVariable().getDecisionCriteria().getString();
            if (iUtilityProbNode.getVariable().isTemporal()
                    && timeSlice == 0
                    // decision criteria of utility nodes must be cost or
                    // effectiveness
                    && (decisionCriterion.equalsIgnoreCase("cost") || 
                            decisionCriterion.equalsIgnoreCase("effectiveness"))) {
                probNet.removeProbNode(iUtilityProbNode);
            }
        }
    }

    private void pruneLastCycleUtilities() {
        List<ProbNode> utilityExpandedNodes = probNet.getProbNodes(NodeType.UTILITY);
        for (int i = 0; i < utilityExpandedNodes.size(); i++) {
            ProbNode iUtilityProbNode = utilityExpandedNodes.get(i);
            int timeSlice = iUtilityProbNode.getVariable().getTimeSlice();
            String decisionCriterion = iUtilityProbNode.getVariable().getDecisionCriteria().getString();
            if (iUtilityProbNode.getVariable().isTemporal() && timeSlice == numSlices
            // decision criteria of utility nodes must be cost or
            // effectiveness
                    && (decisionCriterion.equalsIgnoreCase("cost") || 
                            decisionCriterion.equalsIgnoreCase("effectiveness"))) {
                probNet.removeProbNode(iUtilityProbNode);
            }
        }
    }

//    /**
//     * Divide by two the utilities of the zero and last cycle
//     */
//    private void applyHalfCycleCorrection() {
//        List<ProbNode> utilityExpandedNodes = probNet.getProbNodes(NodeType.UTILITY);
//        for (int i = 0; i < utilityExpandedNodes.size(); i++) {
//            ProbNode utilityProbNode = utilityExpandedNodes.get(i);
//            int timeSlice = utilityProbNode.getVariable().getTimeSlice();
//            StringWithProperties decisionCriteria = utilityProbNode.getVariable()
//                    .getDecisionCriteria();
//            if (utilityProbNode.getVariable().isTemporal()) {
//                if ((timeSlice == numSlices || timeSlice == 0)
//                        && decisionCriteria.getString().equalsIgnoreCase("effectiveness")) {
//                    for (Potential utilityPotential : utilityProbNode.getPotentials()) {
//                        // We can safely assume they are all table potentials,
//                        // as evidence has already been projected
//                        double[] values = ((TablePotential) utilityPotential).values;
//                        for (int j = 0; j < values.length; ++j) {
//                            values[j] /= 2;
//                        }
//                    }
//                } else if (decisionCriteria.getString().equalsIgnoreCase("cost") && timeSlice == 0) {
//                    probNet.removeProbNode(utilityProbNode);
//                }
//            }
//        }
//    }

    /**
     * Divide by two the utilities of the zero and last cycle
     */
    private void applyHalfCycleCorrection() {
        for (ProbNode utilityProbNode : probNet.getProbNodes(NodeType.UTILITY)) {
            Variable utilityVariable = utilityProbNode.getVariable();
            int timeSlice = utilityVariable.getTimeSlice();
            StringWithProperties decisionCriteria = utilityVariable.getDecisionCriteria();
            if (utilityVariable.isTemporal()) {
                if (decisionCriteria.getString().equalsIgnoreCase("effectiveness")) {
                    
                    for (Potential utilityPotential : utilityProbNode.getPotentials()) {
                        // We can safely assume they are all table potentials,
                        // as evidence has already been projected
                        double[] values = ((TablePotential) utilityPotential).values;
                        for (int j = 0; j < values.length; ++j) {
                            values[j] /= 2;
                        }
                    }

                    // Create a sum super-value whose parents are this node and the one in the
                    // next time slice.
                    if(timeSlice < numSlices)
                    {
                        Variable nextSliceVariable = probNet.getShiftedVariable(utilityVariable, 1);
                        ProbNode nextSliceNode = probNet.getProbNode(nextSliceVariable);
                        
                        Variable halfCycleUtility = new Variable(nextSliceVariable.getBaseName()+ " HC");
                        halfCycleUtility.setTimeSlice(nextSliceVariable.getTimeSlice());
                        halfCycleUtility.setDecisionCriteria(new StringWithProperties("effectiveness"));
                        ProbNode halfCycleNode = probNet.addProbNode(halfCycleUtility, NodeType.UTILITY);
                        halfCycleNode.getNode().setCoordinateX(nextSliceNode.getNode().getCoordinateX());
                        halfCycleNode.getNode().setCoordinateY(nextSliceNode.getNode().getCoordinateY() + 100);
                        probNet.addLink(utilityProbNode, halfCycleNode, true);
                        probNet.addLink(nextSliceNode, halfCycleNode, true);
                        SumPotential halfCyclePotential = new SumPotential(Arrays.asList(utilityVariable,
                                nextSliceVariable), PotentialRole.UTILITY, halfCycleUtility);
                        halfCycleNode.setPotential(halfCyclePotential);
                    }
                } else if (decisionCriteria.getString().equalsIgnoreCase("cost") && timeSlice == 0) {
                    probNet.removeProbNode(utilityProbNode);
                }
            }
        }
    }

    
    /**
     * When invoking this method, probNet is a copy of the concise net. We add
     * new nodes, links, and potentials to make it a compact net.
     */
    private void compactNetwork() {
        classifiedNodes = classifyNodesbySlices(probNet, probNet.getVariables());
        // generate the new nodes of the compact net
        List<ProbNode> generatingNodes = new ArrayList<ProbNode>();
        generatedNodes = new ArrayList<ProbNode>();
        for (int slice = 0; slice < classifiedNodes.size() - 1; slice++) {
            double sliceWidth = getSliceWidth(classifiedNodes.get(slice));
            List<ProbNode> generatedNodesInThisSlice = new ArrayList<ProbNode>(classifiedNodes.get(
                    slice).size());
            for (ProbNode generatingProbNode : classifiedNodes.get(slice)) {
                if (!probNet.containsShiftedVariable(generatingProbNode.getVariable(), 1)) {
                    ProbNode newProbNode = probNet.addShiftedProbNode(generatingProbNode, 1,
                            sliceWidth + MARGIN_BETWEEN_SLICES, VERTICAL_OFFSET);
                    generatingNodes.add(generatingProbNode);
                    generatedNodes.add(newProbNode);
                    generatedNodesInThisSlice.add(newProbNode);
                }
            }
            for (ProbNode probNode : generatedNodesInThisSlice) {
                classifiedNodes.get(probNode.getVariable().getTimeSlice()).add(probNode);
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

    /**
     * Assigns nodes to slices in a collection of slices. Each slice is a
     * collection of nodes.
     * 
     * @return <code>List</code> of <code>List</code> of <code>ProbNode</code>
     */
    private static List<List<ProbNode>> classifyNodesbySlices(ProbNet probNet, List<Variable> variables) {
        List<List<ProbNode>> classifiedNodes;
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
  
    /**
     * Projects temporal evidence
     * @param inferenceOptions
     * @param evidence
     */
    private void projectTemporalEvidence(InferenceOptions inferenceOptions, EvidenceCase evidence) {
        List<ProbNode> utilityExpandedNodes = probNet.getProbNodes(NodeType.UTILITY);
        for (ProbNode utilityProbNode : utilityExpandedNodes) {
            Variable utilityVariable = utilityProbNode.getVariable();

            if (utilityVariable.isTemporal()) {
                try {
                    List<Potential> projectedPotentials = new ArrayList<>();
                    Potential potentialToBeProjected = null;
                    List<Potential> potentials = utilityProbNode.getPotentials();
                    for(Potential potential: potentials)
                    {
                        if (potential instanceof SameAsPrevious) {
                            List<Variable> variables = potential.getVariables();
                            potentialToBeProjected = (((SameAsPrevious) potential).getOriginalPotential()).copy();
                            potentialToBeProjected.setVariables(variables);
                            potentialToBeProjected.setUtilityVariable(potential.getUtilityVariable());
                        } else {
                            potentialToBeProjected = potential;
                        }
                        List<TablePotential> projectedTablePotentials = potentialToBeProjected.tableProject(evidence, inferenceOptions);
                        projectedPotentials.addAll(projectedTablePotentials);
                    }
                    utilityProbNode.setPotentials(projectedPotentials);
                    
                } catch (NonProjectablePotentialException | WrongCriterionException e) {
                    e.printStackTrace();
                }
           }
        }
    }

    /**
     * @param costDiscount
     * @param inferenceOptions
     * @throws NotEnoughMemoryException
     *             It applies the discount to each utility potential
     */
    private void applyDiscountToUtilityNodes(double costDiscount, double effectivenessDiscount) {

        // apply discount rate for all temporal utility nodes in the expanded
        // network
        List<ProbNode> utilityExpandedNodes = probNet.getProbNodes(NodeType.UTILITY);
        for (ProbNode utilityProbNode : utilityExpandedNodes) {
            Variable utilityVariable = utilityProbNode.getVariable();

            if (utilityVariable.isTemporal()) {
                TablePotential projectedPotential = (TablePotential) utilityProbNode
                        .getPotentials().get(0);
                int timeSlice = utilityVariable.getTimeSlice();
                double discount = utilityVariable.getDecisionCriteria().getString()
                        .equalsIgnoreCase("cost") ? costDiscount : effectivenessDiscount;

                double discountRate = 1.0 / (Math.pow((1.0 + (discount / 100.0)), timeSlice));
                double[] projectedPotentialValues = projectedPotential.getValues();
                for (int j = 0; j < projectedPotentialValues.length; j++) {
                    projectedPotentialValues[j] = projectedPotentialValues[j] * (discountRate);
                }
            }
        }
    }

    /**
     * @precondition extendedNet in this class must be a compact net
     */
    private void generateNextSlice() {
        List<ProbNode> lastSliceNodes = classifiedNodes.get(classifiedNodes.size() - 1);
        List<ProbNode> newSliceNodes = new ArrayList<ProbNode>();
        // generates the new nodes
        double sliceWidth = getSliceWidth(lastSliceNodes);
        for (ProbNode generatingProbNode : lastSliceNodes) {
            ProbNode newProbNode = probNet.addShiftedProbNode(generatingProbNode, 1, sliceWidth
                    + MARGIN_BETWEEN_SLICES, VERTICAL_OFFSET);
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

    /**
     * TODO document: oldNode is a node in the last slice of the compact net
     * TODO We are assuming that there is only one potential per node. Revise
     */
    private void expandPotentialAndLinks(ProbNode oldNode, ProbNode newNode, int timeDifference) {
        Potential oldPotential = oldNode.getPotentials().get(0);
        Potential newPotential = null;
        if (oldPotential.getPotentialType() == PotentialType.CYCLE_LENGTH_SHIFT) {
            newPotential = new CycleLengthShift(oldPotential.getShiftedVariables(probNet,
                    timeDifference));
        } else {
            int timeDifferenceWithNew;
            Potential referencePotentialForNewPotential = null;
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
                default:
                    break;
                }
                Variable newVariable = newNode.getVariable();
                timeDifferenceWithNew = newVariable.getTimeSlice()
                        - firstOriginalVariable.getTimeSlice();
                referencePotentialForNewPotential = originalPotential;
            } else {
                referencePotentialForNewPotential = oldPotential;
                timeDifferenceWithNew = timeDifference;
            }
            try {
                newPotential = new SameAsPrevious(referencePotentialForNewPotential, probNet,
                        timeDifferenceWithNew);
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
        }
        newNode.addPotential(newPotential);
        newPotential.createDirectedLinks(probNet);
    }

    private double getSliceWidth(List<ProbNode> nodes) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = 0.0;
        for (ProbNode probNode : nodes) {
            if (probNode.getNode().getCoordinateX() > maxX) {
                maxX = probNode.getNode().getCoordinateX();
            }
            if (probNode.getNode().getCoordinateX() < minX) {
                minX = probNode.getNode().getCoordinateX();
            }
        }
        return maxX - minX;
    }
    
    private void extendEvidence(ProbNet extendedNetwork, EvidenceCase evidence) {
        try {
            evidence.extendEvidence(extendedNetwork, 1);
        } catch (IncompatibleEvidenceException | InvalidStateException | WrongCriterionException e) {
            e.printStackTrace();
        }
    }    
}
