package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.heuristic.HeuristicFactory;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.ArrayList;
import java.util.List;

/** This class represents a user action related to inference. */
public abstract class Task {

    /** This is a copy of the <code>ProbNet</code> received. */
    protected ProbNet probNet;

    /** For undo/redo operations. */
    protected PNESupport pNESupport;

    protected boolean isTemporal;


    /** Elimination heuristic factory **/
    protected HeuristicFactory heuristicFactory;

    /**
     * Evidence introduced before the network is resolved.
     * In influence diagrams this is Ezawa's evidence.
     */
    private EvidenceCase preResolutionEvidence;

    /**
     * Evidence when the network has been resolved.
     * In influence diagrams this is Luque and Diez's evidence.
     */
    private EvidenceCase postResolutionEvidence;

    /**
     * Policies set by the user. The optimal policy would only be calculated for the decisions
     * without imposed policies.
     * Each policy is stochastic, which implies it is a probability potential whose domain
     * contains the decision.
     */
    // private List<TablePotential> imposedPolicies;

    /**
     * Variables that will not be eliminated during the inference, and therefore all the results
     * contain these variables in the domain.
     */
    protected List<Variable> conditioningVariables;

    /**
     * @param probNet The network used in the inference
     * @throws UnexpectedInferenceException 
     * @throws NotEvaluableNetworkException
     */
    public Task (ProbNet probNet) throws NotEvaluableNetworkException {
        this.probNet = probNet.copy();
        preResolutionEvidence = new EvidenceCase();
        postResolutionEvidence = new EvidenceCase();
//        //checkEvaluability(probNet);
//        if (!isEvaluable (probNet)) {
//            throw new NotEvaluableNetworkException(probNet.toString());
//        }
    }

    /**
     * @return The post-resolution evidence.
     */
    public EvidenceCase getPostResolutionEvidence() {
        return postResolutionEvidence;
    }

    /**
     * If <code>postResolutionEvidence == null</code>, creates an empty one.
     * @param postResolutionEvidence <code>EvidenceCase</code>
     */
    public void setPostResolutionEvidence(EvidenceCase postResolutionEvidence) {
        this.postResolutionEvidence = new EvidenceCase(postResolutionEvidence);
    }

    /**
     * @return The pre-resolution evidence
     */
    public EvidenceCase getPreResolutionEvidence() {
        return preResolutionEvidence;
    }

    /**
     * If <code>preResolutionEvidence == null</code>, creates an empty one.
     * @param preResolutionEvidence The pre-resolution evidence to set. <code>EvidenceCase</code>
     */
    public void setPreResolutionEvidence(EvidenceCase preResolutionEvidence) {
        this.preResolutionEvidence = new EvidenceCase(preResolutionEvidence);
    }

    public EvidenceCase getJoinResolutionEvidence() throws IncompatibleEvidenceException {
        EvidenceCase evidence = new EvidenceCase(preResolutionEvidence);
        try {
            evidence.addFindings(postResolutionEvidence.getFindings());
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        return evidence;
    }

    /**
     * @return The conditioning variables
     */
    public List<Variable> getConditioningVariables() {
        return conditioningVariables;
    }

    /**
     * @param conditioningVariables The conditioning variables to set
     */
    public void setConditioningVariables(List<Variable> conditioningVariables) {
    	if (conditioningVariables != null) {
    		this.conditioningVariables = conditioningVariables;
    	} else {
    		this.conditioningVariables = new ArrayList<>();
    	}
    }

    /**
     * @return The imposed policies
     *//*
	protected ArrayList<TablePotential> getImposedPolicies() {
		return imposedPolicies;
	}*/

    public void setHeuristicFactory(HeuristicFactory heuristicFactory) {
        this.heuristicFactory = heuristicFactory;
    }

    public void checkNetworkConsistency (ProbNet probNet) throws NotEvaluableNetworkException {
        boolean isApplicable;

        List<NetworkType> networkTypes = initializeNetworkTypesApplicable();

        isApplicable = false;
        NetworkType networkType = probNet.getNetworkType();
        // Check that there is a network type applicable equal to type of probNet
        for (int i = 0; (i < networkTypes.size()) && !isApplicable; i++) {
            isApplicable = networkType == networkTypes.get(i);
        }

        if (!isApplicable) {
            throw new NotEvaluableNetworkException("Network type " + networkType.toString() + "is not evaluable.");
        } else {
            // Check that the probNet satisfies the specific constraints of the algorithm
            List<PNConstraint> additionalConstraints = initializeAdditionalConstraints();

            for (PNConstraint pnConstraint : additionalConstraints) {
                if (!pnConstraint.checkProbNet(probNet)) {
                    throw new NotEvaluableNetworkException("Constraint " + pnConstraint.toString()
                            + " is not satisfied by the network.");
                }
            }
        }
    }

    protected abstract List<PNConstraint> initializeAdditionalConstraints();

    protected abstract List<NetworkType> initializeNetworkTypesApplicable();

    public boolean checkEvidenceConsistency() {return true;}
    
    public boolean checkPoliciesConsistency() {return true;}
}
