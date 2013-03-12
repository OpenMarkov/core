/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
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

public class FactoryExpandedMPAD
{
    // Attributes
    /** Vertical separation in pixels between slices. */
    private final double         VERTICAL_OFFSET = 0;
    
    /** Horizontal separation between slices */
    private final double         MARGIN_BETWEEN_SLICES = 150;
    
    private ProbNet              probNet;
    /** Set of probNodes that will be cloned in each slice. */
    private List<ProbNode>       generatedNodes;
    /** Each <ArrayList<ProbNode> contains the nodes of a time slice */
    private List<List<ProbNode>> classifiedNodes;

    // Constructor
    /**
     * @param conciseNet. <code>ProbNet</code>
     * @param numSlices. <code>int</code>
     * @param simulationIndexVariable. <code>Variable</code>
     * @param coordinateXOffset. <code>int</code>
     */
    public FactoryExpandedMPAD (ProbNet conciseNet,
                               int numSlices,
                               Variable simulationIndexVariable)
    {
        // probNet must be the original network and
        // expandedNetwork the probNet expanded numSlices times
        probNet = conciseNet.copy ();
        if (simulationIndexVariable != null)
        {
            sampleProbNet (simulationIndexVariable);
        }
        // if some of the slices of the concise net miss a node present
        // in previous slices, adds the node to that slice
        compactNetwork ();
        // expands the net
        while (classifiedNodes.size () < numSlices)
        {
            generateNextSlice ();
        }
    }

    /**
     * Adapts the concise network for performing cost-effectiveness analysis.
     * Adds decisionCriteria node to the network and makes all utility nodes children of it
     */
    public void adaptProbNetForCE ()
    {
        List<String> decisionCriteriaNames = new ArrayList<>();
        for (int i = 0; i < probNet.getDecisionCriteria ().size (); i++)
        {
            String decisionCriterion = probNet.getDecisionCriteria ().get (i).getString ();
            if (decisionCriterion.equalsIgnoreCase ("cost") || decisionCriterion.equalsIgnoreCase ("effectiveness"))
            {
                decisionCriteriaNames.add (decisionCriterion);
            }
        }
        if (decisionCriteriaNames.size ()!=2)
        {
            // TODO propagate exception
            // throw new
            // Exception("For cost effectiveness analysis performance network's decision criteria must be cost and effectiveness");
        }
        probNet.setDecisionCriteria (decisionCriteriaNames);
        // make all utility nodes of the expanded probNet children of the decision criteria node
        ProbNode decisionCriteriaNode = new ProbNode (probNet, probNet.getDecisionCriteriaVariable (), NodeType.DECISION);
        probNet.addProbNode (decisionCriteriaNode);
        for (ProbNode utilityNode : probNet.getProbNodes (NodeType.UTILITY))
        {
            Potential utilityPotential = utilityNode.getPotentials ().get (0);
            probNet.addLink (decisionCriteriaNode, utilityNode, true);
            List<Variable> treeVariables = utilityPotential.getVariables ();
            treeVariables.add (decisionCriteriaNode.getVariable ());
            String iUtilityDecisionCriteriaName = utilityNode.getVariable ().getDecisionCriteria ().getString ();
            boolean hasDecisionCriterion = false;
            String otherDecisionCriterion = null;
            TreeADDPotential treeADDPotential = null;
            if (iUtilityDecisionCriteriaName.equals ("cost"))
            {
                hasDecisionCriterion = true;
                otherDecisionCriterion = "effectiveness";
            }
            else if (iUtilityDecisionCriteriaName.equals ("effectiveness"))
            {
                hasDecisionCriterion = true;
                otherDecisionCriterion = "cost";
            }
            if (hasDecisionCriterion)
            {
                treeADDPotential = constructTreeADDForCE (decisionCriteriaNode, treeVariables,
                                                          utilityPotential, utilityNode,
                                                          iUtilityDecisionCriteriaName,
                                                          otherDecisionCriterion);
                utilityNode.setPotential (treeADDPotential);
            }
        }
    }

    /**
     * @param numSlices
     * @param network
     * @param costDiscount Percentage of discount. The utility function in
     *            instant time t will be: U(t) = U(t-1)/(1+discount/100.0)
     * @param adaptForCE
     * @return An expanded network built from a MPAD. It adapts the network to
     *         Cost-Effectiveness analysis if adaptForCE is true.
     */
    public static ProbNet constructExpandedNetwork (int numSlices,
                                                    ProbNet network,
                                                    double costDiscount,
                                                    double effectivenessDiscount,
                                                    boolean adaptForCE)
    {
        FactoryExpandedMPAD expandedNetFactory = null;
        InferenceOptions inferenceOptions;
        expandedNetFactory = new FactoryExpandedMPAD (network, numSlices, null);
        inferenceOptions = new InferenceOptions (network, null);
        if (adaptForCE)
        {
            expandedNetFactory.adaptProbNetForCE ();
        }
        expandedNetFactory.applyDiscountToUtilityNodes (costDiscount, effectivenessDiscount,
                                                        inferenceOptions, null);
        ProbNet expandedNetwork = expandedNetFactory.getExtendedNetwork ();
        return expandedNetwork;
    }

    /**
     * @param initialAge
     * @param finalAge
     * @param probNet
     * @param costDiscount
     * @param effectivenessDiscount
     * @param cycleLength
     * @return An expanded network built from a MPAD when the network has
     *         evidence of initial age of the patient. It adapts the network to
     *         Cost-Effectiveness analysis if adaptForCE is true.
     */
    public static ProbNet constructExpandedNetAge (int numSlices,
                                                   ProbNet probNet,
                                                   double costDiscount,
                                                   double effectivenessDiscount,
                                                   double cycleLength,
                                                   boolean adaptForCE)
    {
        EvidenceCase evidenceCase = new EvidenceCase ();
        ProbNet expandedNetwork = null;
        FactoryExpandedMPAD expandedNetFactory = new FactoryExpandedMPAD (probNet, numSlices, null);
        InferenceOptions inferenceOptions = new InferenceOptions (probNet, null);
        if (!evidenceCase.getFindings ().isEmpty ())
        {
            try
            {
                evidenceCase.extendEvidence (expandedNetFactory.getExtendedNetwork (), cycleLength);
            }
            catch (IncompatibleEvidenceException | InvalidStateException | WrongCriterionException e)
            {
                e.printStackTrace ();
            }
        }
        expandedNetFactory.applyDiscountToUtilityNodes (costDiscount, effectivenessDiscount,
                                                        inferenceOptions, null);
        if (adaptForCE)
        {
            expandedNetFactory.adaptProbNetForCE ();
        }
        expandedNetwork = expandedNetFactory.getExtendedNetwork ();
        return expandedNetwork;
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
    public TreeADDPotential constructTreeADDForCE (ProbNode decisionCriteria,
                                                   List<Variable> treeVariables,
                                                   Potential utility,
                                                   ProbNode utilProbNode,
                                                   String decisionCriteriaName,
                                                   String otherDecisionCriteriaName)
    {
        TreeADDPotential treeADDPotential = new TreeADDPotential (
                                                                  treeVariables,
                                                                  probNet.getDecisionCriteriaVariable (),
                                                                  utility.getPotentialRole (),
                                                                  utility.getUtilityVariable ());
        List<Variable> variables = new ArrayList<> ();
        variables.add (decisionCriteria.getVariable ());
        for (int j = 0; j < treeADDPotential.getBranches ().size (); j++)
        {
            TreeADDBranch jBranch = treeADDPotential.getBranches ().get (j);
            String jBranchName = jBranch.getBranchStates ().get (0).getName ();
            if (jBranchName.equalsIgnoreCase (decisionCriteriaName))
            {
                jBranch.setPotential (utility);
            }
            else if (jBranchName.equalsIgnoreCase (otherDecisionCriteriaName))
            {
                // zero potential
                jBranch.setPotential (new UniformPotential (utility.getVariables (),
                                                            PotentialRole.UTILITY,
                                                            utilProbNode.getVariable ()));
            }
        }
        return treeADDPotential;
    }

    /**
     * when checkbox zero cycle is unselected utility nodes which decision
     * criteria is cost or effectiveness must be removed from the network this
     * approach is in order to take into account changes taking place at the
     * beginning of the cycle not at the end
     * @return
     */
    public void pruneZeroCycleUtilities ()
    {
        List<ProbNode> utilityExpandedNodes = probNet.getProbNodes (NodeType.UTILITY);
        for (int i = 0; i < utilityExpandedNodes.size (); i++)
        {
            ProbNode iUtilityProbNode = utilityExpandedNodes.get (i);
            int timeSlice = iUtilityProbNode.getVariable ().getTimeSlice ();
            if (iUtilityProbNode.getVariable ().isTemporal ()
                && timeSlice == 0
                && (iUtilityProbNode.getVariable ().getDecisionCriteria ().getString ().equalsIgnoreCase ("cost") || iUtilityProbNode.getVariable ().getDecisionCriteria ().getString ().equalsIgnoreCase ("effectiveness")))
            { // decision criteria of utility nodes must be cost or
              // effectiveness
                probNet.removeProbNode (iUtilityProbNode);
            }
        }
    }

    // Methods
    /**
     * @param simulationIndexVariable. <code>Variable</code>
     * @throws NotEnoughMemoryException
     */
    private void sampleProbNet (Variable simulationIndexVariable)
    {
        for (ProbNode probNode : probNet.getProbNodes ())
        {
            probNode.samplePotentials (simulationIndexVariable);
        }
    }

    /**
     * When invoking this method, probNet is a copy of the concise net. We add
     * new nodes, links, and potentials to make it a compact net.
     */
    private void compactNetwork ()
    {
        classifiedNodes = classifyNodes (probNet, probNet.getVariables ());
        // generate the new nodes of the compact net
        List<ProbNode> generatingNodes = new ArrayList<ProbNode> ();
        generatedNodes = new ArrayList<ProbNode> ();
        for (int slice = 0; slice < classifiedNodes.size () - 1; slice++)
        {
            double sliceWidth = getSliceWidth (classifiedNodes.get (slice));
            List<ProbNode> generatedNodesInThisSlice = new ArrayList<ProbNode> (classifiedNodes.get (slice).size ());
            for (ProbNode generatingProbNode : classifiedNodes.get (slice))
            {
                if (!probNet.containsShiftedVariable (generatingProbNode.getVariable (), 1))
                {
                    ProbNode newProbNode = probNet.addShiftedProbNode (generatingProbNode, 1,
                                                                       sliceWidth + MARGIN_BETWEEN_SLICES, 
                                                                       VERTICAL_OFFSET);
                    generatingNodes.add (generatingProbNode);
                    generatedNodes.add (newProbNode);
                    generatedNodesInThisSlice.add (newProbNode);
                }
            }
            for (ProbNode probNode : generatedNodesInThisSlice)
            {
                classifiedNodes.get (probNode.getVariable ().getTimeSlice ()).add (probNode);
            }
        }
        // assign potentials to the new nodes of the compact net
        ProbNode generatingNode, generatedNode;
        for (int i = 0; i < generatedNodes.size (); i++)
        {
            generatingNode = generatingNodes.get (i);
            generatedNode = generatedNodes.get (i);
            expandPotentialAndLinks (generatingNode, generatedNode, 1);
        }
    }
    
    /**
     * Assigns nodes to slices in a collection of slices. Each slice is a
     * collection of nodes.
     * @return <code>List</code> of <code>List</code> of
     *         <code>ProbNode</code>
     */
    public static List<List<ProbNode>> classifyNodes (ProbNet probNet, List<Variable> variables)
    {
        List<List<ProbNode>> classifiedNodes;
        int firstSliceIndex = Integer.MAX_VALUE;
        int lastSliceIndex = Integer.MIN_VALUE;
        // find the indexes of the first and last slice
        int timeSlice;
        for (Variable variable : variables)
        {
            if (variable.isTemporal ())
            {
                timeSlice = variable.getTimeSlice ();
                if (timeSlice < firstSliceIndex)
                {
                    firstSliceIndex = timeSlice;
                }
                if (timeSlice > lastSliceIndex)
                {
                    lastSliceIndex = timeSlice;
                }
            }
        }
        int numSlices = lastSliceIndex - firstSliceIndex + 1;
        // initializes the variable classifiedNodes
        classifiedNodes = new ArrayList<> (numSlices);
        for (int slice = 0; slice < numSlices; slice++)
        {
            classifiedNodes.add (new ArrayList<ProbNode> ());
        }
        // assigns each node to its slice
        Variable variable;
        for (ProbNode node : probNet.getProbNodes ())
        {
            variable = node.getVariable ();
            if (variable.isTemporal ())
            {
                classifiedNodes.get (variable.getTimeSlice ()).add (node);
            }
        }
        return classifiedNodes;
    }

    /**
     * projects the evidence for all nodes in the expanded network calling for
     * each potential within the network to the method tableProject
     * @param evidence
     */
    public void projectEvidence (EvidenceCase evidence)
    {
        for (ProbNode probNode : probNet.getProbNodes ())
        {
            ArrayList<Potential> potentials = new ArrayList<> ();
            InferenceOptions io = new InferenceOptions (probNet, null);
            if (probNode.getNodeType () != NodeType.DECISION)
            {
                try
                {
                    if (!probNode.getPotentials ().get (0).tableProject (evidence, io).isEmpty ())
                    {
                        potentials.add (probNode.getPotentials ().get (0).tableProject (evidence, io).get (0));
                        probNode.setPotentials (potentials);
                    }
                }
                catch (NonProjectablePotentialException | WrongCriterionException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace ();
                }
            }
        }
    }

    public ProbNet getExtendedNetwork ()
    {
        return probNet;
    }

    /**
     * @param costDiscount
     * @param inferenceOptions
     * @throws NotEnoughMemoryException It applies the discount to each utility
     *             potential
     */
    public void applyDiscountToUtilityNodes (double costDiscount,
                                             double effectivenessDiscount,
                                             InferenceOptions inferenceOptions,
                                             EvidenceCase evidence)
    {
        try
        {
            probNet = BasicOperations.removeSuperValueNodes (probNet, evidence, false, true, null);
        }
        catch (NodeNotFoundException | ProbNodeNotFoundException e1)
        {
            e1.printStackTrace();
        }
        // apply discount rate for all temporal utility nodes in the expanded
        // network
        List<ProbNode> utilityExpandedNodes = probNet.getProbNodes (NodeType.UTILITY);
        for (int i = 0; i < utilityExpandedNodes.size (); i++)
        {
            ProbNode iUtilityProbNode = utilityExpandedNodes.get (i);
            int timeSlice = iUtilityProbNode.getVariable ().getTimeSlice ();
            double discount = iUtilityProbNode.getVariable ().getDecisionCriteria ().getString ().equalsIgnoreCase ("cost") ? costDiscount
                                                                                                                      : effectivenessDiscount;
            if(iUtilityProbNode.getVariable ().isTemporal ())
            {
                // slice 0 must be projected to eliminate continuous variables in trees
                if (timeSlice == 0)
                {
                    Potential potentialToBeProjected = iUtilityProbNode.getPotentials ().get (0);
                    if(potentialToBeProjected instanceof TreeADDPotential)
                    {
                        TablePotential projectedPotential = null;
                        try
                        {
                            projectedPotential = potentialToBeProjected.tableProject (evidence,
                                                                                      inferenceOptions).get (0);
                        }
                        catch (NonProjectablePotentialException | WrongCriterionException e)
                        {
                            e.printStackTrace ();
                        }
                        iUtilityProbNode.setPotential (projectedPotential);
                    }
                }
                else //timeSlice > 0
                {
                    double discountRate = 1.0 / (Math.pow ((1.0 + (discount / 100.0)), timeSlice));
                    // project TreeADD original potential to a table
                    try
                    {
                        TablePotential projectedPotential = null;
                        Potential potentialToBeProjected;
                        Potential potential = iUtilityProbNode.getPotentials ().get (0);
                        if (potential instanceof SameAsPrevious)
                        {
                            List<Variable> variables = potential.getVariables ();
                            Variable utilityVariable = potential.getUtilityVariable ();
                            potentialToBeProjected = (((SameAsPrevious) potential).getOriginalPotential ()).copy ();
                            potentialToBeProjected.setVariables (variables);
                            potentialToBeProjected.setUtilityVariable (utilityVariable);
                        }
                        else
                        {
                            potentialToBeProjected = (potential);
                        }
                        projectedPotential = potentialToBeProjected.tableProject (evidence,
                                                                                  inferenceOptions).get (0);
                        // projectedPotential.setOriginalVariables(potentialToBeProjected.getVariables());
                        double[] valuesProjectedPotential = projectedPotential.getValues ();
                        for (int j = 0; j < valuesProjectedPotential.length; j++)
                        {
                            valuesProjectedPotential[j] = valuesProjectedPotential[j] * (discountRate);
                        }
                        iUtilityProbNode.setPotential (projectedPotential);
                    }
                    catch (NonProjectablePotentialException | WrongCriterionException e)
                    {
                        e.printStackTrace ();
                    }
                    // utilityExpandedNodes.get(i).getPotentials().get(0).get
                }
            }
        }
    }

    /**
     * @precondition extendedNet in this class must be a compact net
     */
    private void generateNextSlice ()
    {
        List<ProbNode> lastSliceNodes = classifiedNodes.get (classifiedNodes.size () - 1);
        List<ProbNode> newSliceNodes = new ArrayList<ProbNode> ();
        // generates the new nodes
        double sliceWidth = getSliceWidth(lastSliceNodes);
        for (ProbNode generatingProbNode : lastSliceNodes)
        {
            ProbNode newProbNode = probNet.addShiftedProbNode (generatingProbNode, 1,
                                                               sliceWidth + MARGIN_BETWEEN_SLICES, 
                                                               VERTICAL_OFFSET);
            newSliceNodes.add (newProbNode);
        }
        // generates new slices
        // assign potentials to the new nodes
        ProbNode generatingNode, generatedNode;
        for (int i = 0; i < lastSliceNodes.size (); i++)
        {
            generatingNode = lastSliceNodes.get (i);
            generatedNode = newSliceNodes.get (i);
            expandPotentialAndLinks (generatingNode, generatedNode, 1);
        }
        classifiedNodes.add (newSliceNodes);
    }

    /**
     * TODO documentar oldNode is a node in the last slice of the compact net
     * TODO We are assuming that there is only one potential per node. Revise
     */
    private void expandPotentialAndLinks (ProbNode oldNode, ProbNode newNode, int timeDifference)
    {
        Potential oldPotential = oldNode.getPotentials ().get (0);
        Potential newPotential = null;
        if (oldPotential.getPotentialType () == PotentialType.CYCLE_LENGTH_SHIFT)
        {
            newPotential = new CycleLengthShift (oldPotential.getShiftedVariables (probNet,
                                                                                   timeDifference));
        }
        else
        {
            int timeDifferenceWithNew;
            Potential referencePotentialForNewPotential = null;
            if (oldPotential.getPotentialType () == PotentialType.SAME_AS_PREVIOUS)
            {
                Potential originalPotential = ((SameAsPrevious) oldPotential).getOriginalPotential ();
                // Sets time difference respect to the original potential
                Variable firstOriginalVariable = null;
                PotentialRole potentialRole = originalPotential.getPotentialRole ();
                switch (potentialRole)
                {
                    case CONDITIONAL_PROBABILITY :
                        firstOriginalVariable = originalPotential.getVariables ().get (0);
                        break;
                    case UTILITY :
                        firstOriginalVariable = originalPotential.getUtilityVariable ();
                        break;
                    default :
                        break;
                }
                Variable newVariable = newNode.getVariable ();
                timeDifferenceWithNew = newVariable.getTimeSlice ()
                                        - firstOriginalVariable.getTimeSlice ();
                referencePotentialForNewPotential = originalPotential;
            }
            else
            {
                referencePotentialForNewPotential = oldPotential;
                timeDifferenceWithNew = timeDifference;
            }
            try
            {
                newPotential = new SameAsPrevious (referencePotentialForNewPotential, probNet,
                                                   timeDifferenceWithNew);
            }
            catch (NodeNotFoundException e)
            {
                e.printStackTrace ();
            }
        }
        newNode.addPotential (newPotential);
        newPotential.createDirectedLinks (probNet);
    }

    /**
     * Removes all that nodes that has evidence, this means that numerical and
     * CycleLegthShift ones disappears with evidence the are not necessary in
     * the network for the inference algorithm anymore
     * @param evidence
     */
    public ProbNet prepareExpandedNetworkToInference (EvidenceCase evidence)
    {
        ProbNet prunedProbNet = probNet.copy ();
        List<Finding> findings = evidence.getFindings ();
        for (int i = 0; i < findings.size (); i++)
        {
            prunedProbNet.removeProbNode (prunedProbNet.getProbNode (findings.get (i).getVariable ()));
        }
        return prunedProbNet;
    }
    
    private double getSliceWidth (List<ProbNode> nodes)
    {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = 0.0;
        for(ProbNode probNode: nodes)
        {
           if(probNode.getNode ().getCoordinateX () > maxX)
           {
               maxX = probNode.getNode ().getCoordinateX ();
           }
           if(probNode.getNode ().getCoordinateX () < minX)
           {
               minX = probNode.getNode ().getCoordinateX ();
           }
        }
        return maxX-minX;
    }    
}
