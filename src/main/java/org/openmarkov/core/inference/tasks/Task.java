package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.heuristic.HeuristicFactory;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.NoMixedParents;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<TablePotential> imposedPolicies;

    /**
     * Variables that will not be eliminated during the inference, and therefore all the results
     * contain these variables in the domain.
     */
    protected List<Variable> conditioningVariables;

    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public Task (ProbNet probNet)
            throws NotEvaluableNetworkException {
        this.probNet = probNet;
        preResolutionEvidence = new EvidenceCase();
        postResolutionEvidence = new EvidenceCase();
        if (!isEvaluable (probNet))
        {
            throw new NotEvaluableNetworkException (probNet.toString ());
        }
    }

    /**
     * @return The post-resolution evidence.
     */
    public EvidenceCase getPostResolutionEvidence() {
        return postResolutionEvidence;
    }

    /**
     * @param postResolutionEvidence
     */
    public void setPostResolutionEvidence(EvidenceCase postResolutionEvidence) {
        this.postResolutionEvidence = postResolutionEvidence;
    }

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
        this.conditioningVariables = conditioningVariables;
    }

    /**
     * @return The imposed policies
     *//*
	protected ArrayList<TablePotential> getImposedPolicies() {
		return imposedPolicies;
	}*/

    /**
     * @param probNet
     * @return True if the network can be evaluated.
     */
    public boolean isEvaluable (ProbNet probNet){
        boolean isEvaluable;

        isEvaluable = true;

        try {
            checkEvaluability(probNet);
        } catch (NotEvaluableNetworkException e) {
            isEvaluable = false;
        }
        return isEvaluable;

    }


    public void setHeuristicFactory(HeuristicFactory heuristicFactory) {
        this.heuristicFactory = heuristicFactory;
    }


    protected static void checkEvaluability (ProbNet probNet)
            throws NotEvaluableNetworkException
    {
    }

    /**
     * @return The optimal strategy
     * @throws UnexpectedInferenceException
     * @throws IncompatibleEvidenceException
     */
    public abstract Intervention getOptimalStrategy() throws IncompatibleEvidenceException, UnexpectedInferenceException;

    /**
     * @return The global expected utility of the influence diagram. It is a potential
     * defined over the conditioning variables.
     */
    public abstract TablePotential getUtility() throws
            IncompatibleEvidenceException,
            UnexpectedInferenceException;

    public abstract TablePotential getGlobalUtility() throws
            IncompatibleEvidenceException,
            UnexpectedInferenceException;

    /**
     * @return The global expected utility of the influence diagram. It is a potential
     * defined over the conditioning variables.
     */
    public abstract TablePotential getProbability() throws
            IncompatibleEvidenceException,
            UnexpectedInferenceException;

    public abstract HashMap<Variable, TablePotential> getPosteriorValues()
            throws IncompatibleEvidenceException, UnexpectedInferenceException;

    public abstract Potential getOptimizedPolicy(Variable decisionVariable)
            throws IncompatibleEvidenceException, UnexpectedInferenceException;

    public abstract boolean checkNetworkConsistency() throws NotEvaluableNetworkException;
    public abstract boolean checkEvidenceConsistency();
    public abstract boolean checkPoliciesConsistency();
}
