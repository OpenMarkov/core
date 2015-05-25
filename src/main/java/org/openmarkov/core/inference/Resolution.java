/*
 * Copyright 2015 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.annotation.InferenceManager;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.*;

public class Resolution {

    //TODO: the following two enumerates have to be here for pre-processing and eventually be removed from VE
    /**
     * This type is used when invoking method 'performInference'. Purpose of the
     * inference: STRATEGY_AND_MEU indicates the inference performed is aimed to
     * calculate the strategy and the global utility (maximum expected utility)
     * POSTERIOR_PROB indicates that the inference's objective is calculating
     * some posterior probability POSTERIOR_UTIL indicates that the inference's
     * objective is calculating some posterior utility EXPECTED_UTIL indicates
     * the inference performed is aimed to calculate the expected utilities of a
     * policy
     */
    public enum InferencePurpose {
        STRATEGY_AND_MEU, POSTERIOR_PROB, POSTERIOR_UTIL, EXPECTED_UTIL
    }

    /**
     * Indicates the state of the InferenceAlgorithm object. PRERESOLUTION
     * indicates that the network has been edited and is prepared for any query.
     * POSTRESOLUTION indicates that the policy of every decision in network has
     * been calculated, and therefore queries about posterior probabilities and
     * utilities are accepted.
     *
     */
    public enum InferenceState {
        PRERESOLUTION, POSTRESOLUTION
    }

    /**
     * Variables that will not be eliminated during the inference, and therefore all the results
     * contain these variables in the domain.
     */
    protected List<Variable> conditioningVariables;

    InferenceState inferenceState;
    InferencePurpose inferencePurpose;

    // ** OUTPUTS OF THE RESOLUTION PHASE **//
    private HashMap<Variable, Potential> strategy;
    private TablePotential globalUtility;

    // TODO: should this be removed from VE?
    /**
     * Evidence introduced before the network is resolved.
     * In influence diagrams this is Ezawa's evidence.
     */
    private EvidenceCase preResolutionEvidence;

    private InferenceManager inferenceManager;
    private InferenceAlgorithm inferenceAlgorithm;

    /**
     * @return The pre-resolution evidence
     */
    public EvidenceCase getPreResolutionEvidence() {
        return preResolutionEvidence;
    }

    /**
     * @param preResolutionEvidence The pre-resolution evidence to set
     */
    public void setPreResolutionEvidence(EvidenceCase preResolutionEvidence) {
        this.preResolutionEvidence = preResolutionEvidence;
    }


    public Resolution() {
        inferenceManager = new InferenceManager();
    }

    /**
     *
     * @param probNet the probNet that the user wants to resolve
     * @return a TablePotential that corresponds to the one that was calculated by the getGlobalUtility function
     * of the VariableElimination algorith,
     */
    public TablePotential performResolution (ProbNet probNet) {

        setConditioningVariables(new ArrayList<Variable>());

        List<TablePotential> projectedTablePotentials = null;
        boolean isPurposeStrategyAndMEU = inferencePurpose == InferencePurpose.STRATEGY_AND_MEU;
        inferenceState = InferenceState.PRERESOLUTION;
        inferencePurpose = InferencePurpose.STRATEGY_AND_MEU;
        TablePotential posteriorUtility = null;
        TablePotential posteriorProbOrUtil = null;
        ProbNet preProcessedProbNet = null;
        MarkovDecisionNetwork markovNetworkInference = null;

        //TODO: the following variables are only valid for the task Resolution (getGlobalUtility)
        List<Variable> queryVariables = new ArrayList<>();
        EvidenceCase evidence = new EvidenceCase(getPreResolutionEvidence());
        List<Variable> informationalPredecessors = null;
        //

        try {
            inferenceAlgorithm = inferenceManager.getDefaultInferenceAlgorithm(probNet);
            if (inferenceAlgorithm != null) {

                try {
                    preProcessedProbNet = ResolutionPreprocessing(probNet,inferencePurpose);
                } catch (IncompatibleEvidenceException e) {
                    e.printStackTrace();
                }

                try {
                    projectedTablePotentials = preProcessedProbNet.tableProjectPotentials(evidence);
                } catch (NonProjectablePotentialException | WrongCriterionException e1) {
                    try {
                        throw new IncompatibleEvidenceException("Unexpected inference exception :"
                                + e1.getMessage());
                    } catch (IncompatibleEvidenceException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    markovNetworkInference = createMarkovDecisionNetwork(preProcessedProbNet,projectedTablePotentials,inferencePurpose);
                } catch (UnexpectedInferenceException e) {
                    e.printStackTrace();
                } catch (IncompatibleEvidenceException e) {
                    e.printStackTrace();
                }

                // Conditioning variables
                List<Variable> conditioningVariables = getConditioningVariables();

                // Build list of variables to eliminate
                List<Variable> variablesToEliminate = preProcessedProbNet.getChanceAndDecisionVariables();
                if (inferencePurpose == InferencePurpose.EXPECTED_UTIL) {
                    Variable decisionVariable = queryVariables.get(0);
                    variablesToEliminate.removeAll(informationalPredecessors);
                    variablesToEliminate.remove(decisionVariable);
                } else {
                    variablesToEliminate.removeAll(queryVariables);
                }

                List<Variable> orderOfDecisions = (isPurposeStrategyAndMEU) ? markovNetworkInference
                        .getPartialOrder().getAnAdmissibleOrderOfDecisions() : new ArrayList<Variable>();
                posteriorUtility = null; /*inferenceAlgorithm.performVariableEliminationScheme(inferencePurpose, queryVariables, evidence,
                        conditioningVariables, variablesToEliminate, markovNetworkInference, true, orderOfDecisions);*/
                posteriorProbOrUtil = calculatePosteriorProbOrUtil(inferencePurpose, markovNetworkInference, posteriorUtility,
                        queryVariables,orderOfDecisions,conditioningVariables);




            }
        } catch (NotEvaluableNetworkException |
                IncompatibleEvidenceException e) {//|
                //UnexpectedInferenceException e) {
            e.printStackTrace();

        }

        return posteriorProbOrUtil;
    }

    private ProbNet ResolutionPreprocessing (ProbNet probNet,
                                             InferencePurpose inferencePurpose) throws IncompatibleEvidenceException {

        //TODO: the following variables are only valid for the task Resolution (getGlobalUtility)
        List<Variable> queryVariables = new ArrayList<>();
        EvidenceCase evidence = new EvidenceCase(getPreResolutionEvidence());
        List<Variable> informationalPredecessors = null;
        //

        ProbNet preProcessedProbNet = null;


        boolean isPurposeStrategyAndMEU = inferencePurpose == InferencePurpose.STRATEGY_AND_MEU;
        if (inferencePurpose == InferencePurpose.EXPECTED_UTIL) {
            informationalPredecessors =
                    ProbNetOperations.getInformationalPredecessors(probNet, queryVariables.get(0));
        }else if (isPurposeStrategyAndMEU) {
            strategy = new HashMap<Variable, Potential>();
        }

        if (inferenceState == InferenceState.PRERESOLUTION) {
            preProcessedProbNet = constructCooperPolicyNetwork(probNet, null, null, null);
        } else {
            // POSTRESOLUTION:
            List<Variable> utilityVariables = (inferencePurpose == InferencePurpose.POSTERIOR_UTIL) ? queryVariables
                    : null;

            probNet = constructCooperPolicyNetwork(probNet, inferencePurpose, informationalPredecessors,
                    utilityVariables);
            // TODO Study how to prune the influence diagram when we want to
            // calculate a posterior utility of a utility node.

            List<Variable> queryVariablesForPrune = (inferencePurpose != InferencePurpose.EXPECTED_UTIL) ? queryVariables
                    : null;

            preProcessedProbNet = (inferencePurpose == InferencePurpose.POSTERIOR_PROB) ? ProbNetOperations
                    .getPruned(probNet, queryVariablesForPrune, evidence) : probNet;
        }

        removePotentialsWithPrunedVariables(preProcessedProbNet);

        if (inferencePurpose == InferencePurpose.POSTERIOR_PROB) {
            removeUniformPotentials(preProcessedProbNet);
        }

        return preProcessedProbNet;
    }

    //TODO: when post-resolution preprocessing is removed from VE, this method should be removed from that class
    protected ProbNet constructCooperPolicyNetwork(ProbNet network,
                                                   InferencePurpose purpose,
                                                   List<Variable> informationalPredecessors,
                                                   List<Variable> utilityVariables) {
        ProbNet cooperNet = null;
        boolean includeUtilities;
        Variable utilityVariable = null;

        if (hasOnlyChanceNodes(network)) {
            // In Bayesian networks we do nothing: use the original network
            cooperNet = network;
        } else {
            if ((inferenceState == InferenceState.PRERESOLUTION)
                    || (purpose == InferencePurpose.EXPECTED_UTIL)) {
                includeUtilities = true;
            } else {// POSTRESOLUTION
                includeUtilities = utilityVariables != null && !utilityVariables.isEmpty();
                if (includeUtilities && utilityVariables.size() == 1) {
                    utilityVariable = utilityVariables.get(0);
                }
            }
            if (includeUtilities) {
                cooperNet = BasicOperations.removeSuperValueNodes(network, null, false, true,
                        utilityVariable);
            } else {
                cooperNet = BasicOperations.removeUtilityNodes(network);
            }
            replaceDecisionsByChanceNodesWithPolicies(cooperNet, informationalPredecessors);
        }
        return cooperNet;
    }

    //TODO: this method exists also in VE. Should be moved to ProbNet class or ProbNetOperations class
    /**
     * @param network
     * @return True if the network has only chance nodes.
     */
    private static boolean hasOnlyChanceNodes(ProbNet network) {

        return network.hasConstraint(OnlyChanceNodes.class);
    }

    //TODO: this method exists also in VE. Should be removed, eventually, from that class
    /**
     * @param probNet
     *            Replaces decision nodes in 'probNet' by chance nodes by using
     *            the corresponding policies. In PRERESOLUTION phase only
     *            imposed policies are used. In POSTRESOLUTION phase both
     *            imposed and calculated policies are used. Decision nodes in
     *            'informationalPredecessors' are not changed.
     * @param informationalPredecessors
     */
    private void replaceDecisionsByChanceNodesWithPolicies(ProbNet probNet, List<Variable> informationalPredecessors) {
        // Change decision nodes by chance nodes whose probability potential
        // is given by the corresponding policy
        List<Node> decisions = probNet.getNodes(NodeType.DECISION);
        for (Node decision : decisions) {
            Variable varDecision = decision.getVariable();

            if ((informationalPredecessors == null) || (!informationalPredecessors.contains(varDecision))) {

                Potential policy = (inferenceState == InferenceState.PRERESOLUTION) ? getImposedPolicy(varDecision, probNet)
                        : getCurrentPolicy(varDecision, probNet);
                if (policy != null) {
                    List<Node> childrenOfDecision = probNet.getNode(varDecision).getChildren();
                    // Remove decision
                    probNet.removeNode(decision);
                    // Create a chance node for the same variable
                    Node decisionNode = probNet.addNode(varDecision, NodeType.CHANCE);

                    // Add the links to the children (chance) of decision node
                    for (Node child : childrenOfDecision) {
                        NodeType type = child.getNodeType();
                        if (type == NodeType.CHANCE || type == NodeType.UTILITY) {
                            try {
                                probNet.addLink(varDecision, child.getVariable(), true);
                            } catch (NodeNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Incoming Links for the variable
                    List<Variable> domainPolicy = policy.getVariables();
                    domainPolicy.remove(varDecision);
                    for (Variable varInDomain : domainPolicy) {
                        try {
                            probNet.addLink(varInDomain, varDecision, true);
                        } catch (NodeNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    List<Potential> potentials = decisionNode.getPotentials();
                    if (potentials != null) {
                        for (Potential potential : potentials) {
                            decisionNode.removePotential(potential);
                        }
                    }

                    // Potential probability for the variable
                    probNet.addPotential(policy);
                }
            }
        }
    }

    //TODO: this method exists also in VE. Should it be removed, eventually, from that class?
    /**
     * @param decision
     * @return The imposed policy of the decision
     */
    protected Potential getImposedPolicy(Variable decision, ProbNet probNet) {
        Potential policy = null;

        Node decisionNode = probNet.getNode(decision);
        if (decisionNode==null){
            policy = null;
        }
        else{
            List<Potential> potentials = decisionNode.getPotentials();
            if ((potentials == null)||(potentials.size()==0)){
                policy = null;
            }
            else{
                policy = potentials.get(0);
            }
        }
        return policy;
    }

    //TODO: this method exists also in VE. Should it be removed, eventually, from that class?
    /**
     * @param decision
     * @return the policy existing for the decision. It can be (1) an imposed
     *         policy, or (2) a policy calculated when the network was
     *         evaluated.
     */
    private Potential getCurrentPolicy(Variable decision, ProbNet probNet) {
        Potential policy = getImposedPolicy(decision,probNet);
        if (policy == null) {
            policy = strategy.get(decision);
        }
        return policy;
    }

    //TODO: this method exists also in VE. Should be removed, eventually, from that class
    /**
     * Removes those potentials that contain a variable that does not appear in
     * the network after being pruned.
     *
     * @param prunedProbNet
     */
    private void removePotentialsWithPrunedVariables(ProbNet prunedProbNet) {

        List<Potential> potentials = prunedProbNet.getPotentials();

        for (Potential potential : potentials) {
            List<Variable> potentialVariables = potential.getVariables();
            boolean removePotential = false;
            for (int j = 0; j < potentialVariables.size() && !removePotential; j++) {
                removePotential = !prunedProbNet.containsVariable(potentialVariables.get(j));
            }
            if (removePotential) {
                prunedProbNet.removePotential(potential);
            }
        }
    }

    //TODO: this method exists also in VE. Should be removed, eventually, from that class
    /**
     * @param prunedProbNet
     *            Eliminates Uniform potentials, because they are unnecessary
     *            for the inference
     */
    private void removeUniformPotentials(ProbNet prunedProbNet) {
        for (Node node : prunedProbNet.getNodes()) {
            for (Potential potential : node.getPotentials()) {
                if (potential instanceof UniformPotential
                        && potential.getPotentialRole() != PotentialRole.UTILITY) {
                    node.removePotential(potential);
                }
            }
        }
    }

    //TODO: this method exists also in VE. Should be removed, eventually, from that class
    /**
     * @param prunedProbNet
     * @param projectedTablePotentials
     * @param purpose
     * @return
     * @throws UnexpectedInferenceException
     * @throws IncompatibleEvidenceException
     */
    protected MarkovDecisionNetwork createMarkovDecisionNetwork(
            ProbNet prunedProbNet,
            List<TablePotential> projectedTablePotentials,
            InferencePurpose purpose) throws UnexpectedInferenceException, IncompatibleEvidenceException {
        // Remove constant potentials
        Set<TablePotential> constantPotentials = removeConstantPotentials(projectedTablePotentials, purpose);

        MarkovDecisionNetwork markovNetworkInference;
        // Build equivalent Markov network with the TablePotentials obtained when projecting
        // the potentials of the ProbNet according to the evidence
        try {
            markovNetworkInference = new MarkovDecisionNetwork(prunedProbNet, projectedTablePotentials);
        } catch (WrongGraphStructureException e) {
            throw new UnexpectedInferenceException(e.getMessage());
        }

        markovNetworkInference.setConstantPotentials(constantPotentials);
        return markovNetworkInference;
    }

    //TODO: this method exists also in VE. Should it be removed, eventually, from that class?
    protected TablePotential calculatePosteriorProbOrUtil(InferencePurpose purpose,
                                                          MarkovDecisionNetwork markovNetworkInference, TablePotential posteriorUtility,
                                                          List<Variable> queryVariables, List<Variable> orderOfDecisions, List<Variable> conditioningVariables)
            throws IncompatibleEvidenceException {
        TablePotential posteriorProbOrUtil = null;

        if (inferenceState == InferenceState.POSTRESOLUTION && purpose == InferencePurpose.POSTERIOR_PROB) {
            List<? extends Potential> remainingPotentials = markovNetworkInference.getPotentials();
            TablePotential multipliedPotential = (TablePotential) DiscretePotentialOperations
                    .multiplyAndEliminate((List<TablePotential>) remainingPotentials,
                            new ArrayList<Variable>());
            PotentialRole role = (conditioningVariables.isEmpty())? PotentialRole.JOINT_PROBABILITY : PotentialRole.CONDITIONAL_PROBABILITY;
            multipliedPotential.setPotentialRole(role);
            // If resulting potential is a conditional probability, make sure query variables are first in the potential
            if(role == PotentialRole.CONDITIONAL_PROBABILITY && multipliedPotential.getNumVariables()>1)
            {
                List<Variable> variableOrder = new ArrayList<>(queryVariables);
                for(Variable potentialVariable : multipliedPotential.getVariables())
                {
                    if(!variableOrder.contains(potentialVariable))
                        variableOrder.add(potentialVariable);
                }
                multipliedPotential = DiscretePotentialOperations.reorder(multipliedPotential, variableOrder);
            }
            try {
                posteriorProbOrUtil = DiscretePotentialOperations.normalize(multipliedPotential);
            } catch (NormalizeNullVectorException e) {
                throw new IncompatibleEvidenceException("Incompatible Evidence");
            }
        }

        switch (purpose) {
            case POSTERIOR_UTIL:
            case STRATEGY_AND_MEU:
            case EXPECTED_UTIL:
                List<TablePotential> utilityPotentials = new ArrayList<>();
                for (Potential pot : markovNetworkInference.getPotentialsByRole(PotentialRole.UTILITY)) {
                    utilityPotentials.add((TablePotential) pot);
                }

                if (posteriorUtility != null) {
                    utilityPotentials.add(posteriorUtility);
                }
                if (purpose == InferencePurpose.STRATEGY_AND_MEU) {
                    utilityPotentials = orderPotentialsByPartialOrder(utilityPotentials, orderOfDecisions);
                }
                posteriorProbOrUtil = DiscretePotentialOperations.sum(utilityPotentials);
                if (purpose == InferencePurpose.EXPECTED_UTIL) {
                    posteriorProbOrUtil = putDecisionVariableFirst(posteriorProbOrUtil, queryVariables.get(0));
                }
                break;
            default:
                break;
        }
        return posteriorProbOrUtil;
    }

    //TODO: this method exists also in VE. Should it be removed, eventually, from that class?
    /**
     * @return The conditioning variables
     */
    public List<Variable> getConditioningVariables() {
        return conditioningVariables;
    }

    //TODO: this method exists also in VE. Should it be removed, eventually, from that class?
    /**
     * @param conditioningVariables The conditioning variables to set
     */
    public void setConditioningVariables(List<Variable> conditioningVariables) {
        this.conditioningVariables = conditioningVariables;
    }

    //TODO: this method exists also in VE. Should be removed, eventually, from that class
    /**
     * Removes constant potentials (only have one value in attribute values)
     *
     * @param projectedTablePotentials
     * @param purpose
     * @return A list of constant potentials removed from
     *         projectedTablePotentials
     * @throws IncompatibleEvidenceException
     */
    protected Set<TablePotential> removeConstantPotentials(
            Collection<TablePotential> projectedTablePotentials, InferencePurpose purpose)
            throws IncompatibleEvidenceException {
        boolean addToConstantPotentials;
        boolean includeInMDN;
        Set<TablePotential> constantPotentials = new HashSet<TablePotential>();
        List<TablePotential> toRemove = new ArrayList<TablePotential>();
        for (TablePotential potential : projectedTablePotentials) {
            addToConstantPotentials = checkIfAddToConstantPotentials(potential);
            includeInMDN = checkIfIncludeInMarkovDecisionNetwork(potential);
            if (addToConstantPotentials) {
                constantPotentials.add(potential);
            }
            if (!includeInMDN) {
                toRemove.add(potential);
            }
        }
        for (TablePotential potential : toRemove) {
            projectedTablePotentials.remove(potential);
        }
        return constantPotentials;
    }

    //TODO: this method exists also in VE. Should it be removed, eventually, from that class?
    /**
     * @param potential
     * @return True if and only if the potential has to be added to the list of
     *         constant potentials. It also detects 0 in probability potentials
     *         and then throws IncompatibleEvidenceException
     * @throws IncompatibleEvidenceException
     */
    public static boolean checkIfAddToConstantPotentials(TablePotential potential)
            throws IncompatibleEvidenceException {

        if (potential != null && potential.getPotentialRole() != PotentialRole.UTILITY
                && potential.getVariables().size() == 0 && potential.values[0] == 0.0) {
            throw new IncompatibleEvidenceException("Incompatible Evidence");
        }
        return potential != null && potential.getVariables().size() == 0;
    }

    //TODO: this method exists also in VE. Should it be removed, eventually, from that class?
    /**
     * @param potential
     * @return True if and only if the potential has to be added to the list of
     *         constant potentials. It also detects 0 in probability potentials
     *         and then throws IncompatibleEvidenceException
     * @throws IncompatibleEvidenceException
     */
    public static boolean checkIfIncludeInMarkovDecisionNetwork(TablePotential potential) {

        return ((potential != null) && (potential.getVariables().size() > 0));
    }

    //TODO: this method exists also in VE. Should it be removed, eventually, from that class?
    public static List<TablePotential> orderPotentialsByPartialOrder(
            List<TablePotential> utilityPotentialsVariable,List<Variable> decisionsUntilEnd) {
        List<TablePotential> newList = new ArrayList<>();
        Set<TablePotential> inputPotentials = new HashSet<>();

        for (Potential auxPot:utilityPotentialsVariable){
            inputPotentials.add((TablePotential) auxPot);
        }

        Set<TablePotential> withoutInterv = new HashSet<>();
        //Remove from inputPotentials the potentials without Interventions
        for (TablePotential auxPot: inputPotentials){
            if (!withInterventions(auxPot)){
                withoutInterv.add(auxPot);
            }
        }


        for (Variable dec: decisionsUntilEnd){
            Set<TablePotential> potsWithDecInIntervention;
            potsWithDecInIntervention = getPotentialsWithDecisionInIntervention(dec,inputPotentials);
            inputPotentials.removeAll(potsWithDecInIntervention);
            newList.addAll(potsWithDecInIntervention);
        }

        inputPotentials.removeAll(withoutInterv);
        newList.addAll(withoutInterv);

        newList.addAll(inputPotentials);
        return newList;

    }

    //TODO: this method exists also in VE. Should be removed, eventually, from that class
    private static Set<TablePotential> getPotentialsWithDecisionInIntervention(Variable dec,
                                                                               Set<TablePotential> inputPotentials) {
        Set<TablePotential> potsWithDecInIntervention = new HashSet<>();
        for (TablePotential auxPot : inputPotentials) {
            if (hasDecisionInIntervention(dec, auxPot)) {
                potsWithDecInIntervention.add(auxPot);
            }
        }
        return potsWithDecInIntervention;
    }

    //TODO: this method exists also in VE. Should be removed, eventually, from that class
    private static boolean hasDecisionInIntervention(Variable dec, TablePotential auxPot) {
        return withInterventions(auxPot) && auxPot.interventions[0].hasInterventionForDecision(dec);
    }

    //TODO: this method exists also in VE. Maybe it can be extracted to someplace more general in the code
    private static boolean withInterventions(TablePotential auxPot) {
        Intervention[] auxInterv = auxPot.interventions;
        return auxInterv != null && auxInterv.length > 0 && auxInterv[0]!=null;
    }

    //TODO: this method exists also in VE. Maybe it can be extracted to someplace more general in the code
    /**
     * @param potential
     * @param variable
     * @return A new potential equivalent to the input potential, where the
     *         'decision' appears in the first position
     */
    private TablePotential putDecisionVariableFirst(TablePotential potential,
                                                    Variable decision) {
        TablePotential newPotential = potential;

        List<Variable> variables = potential.getVariables();
        if (variables.indexOf(decision) != 0) {
            List<Variable> reorderedVariables = new ArrayList<>(variables);
            Collections.swap(reorderedVariables, 0, variables.indexOf(decision));
            newPotential = DiscretePotentialOperations.reorder(potential, reorderedVariables);
        }
        return newPotential;
    }

    private ProbNet preprocessProbnet(ProbNet probNet, EvidenceCase evidence){
        ProbNet copyProbNet = probNet.deepCopy();
        ProbNet expandedProbNet = copyProbNet;

        if(!probNet.hasConstraint(OnlyAtemporalVariables.class)) {
            expandedProbNet = TemporalNetOperations.expandNetwork(copyProbNet);
            try {
                evidence.extendEvidence(expandedProbNet);
            } catch (IncompatibleEvidenceException e) {
                e.printStackTrace();
            } catch (InvalidStateException e) {
                e.printStackTrace();
            } catch (WrongCriterionException e) {
                e.printStackTrace();
            }
        }

        // Convert numeric variables
        expandedProbNet = ProbNetOperations.convertNumericalVariablesToFS(expandedProbNet, evidence);

        if(!probNet.hasConstraint(OnlyAtemporalVariables.class)) {
            TemporalNetOperations.applyDiscountToUtilityNodes(expandedProbNet);
            TemporalNetOperations.applyTransitionTime(expandedProbNet);
        }

        if(probNet.getInferenceOptions().getMultiCriteriaOptions().getMulticriteriaType() == MulticriteriaOptions.Type.UNICRITERION){
            UtilityOperations.transformToUnicriterion(expandedProbNet);
        }


        return expandedProbNet;
    }
}