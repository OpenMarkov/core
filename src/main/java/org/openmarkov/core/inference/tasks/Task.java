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
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

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

    /**
     * @param decision
     * @return The imposed policy of the decision
     */
    protected Potential getPolicy(Variable decision) {
        Potential policy;

        Node decisionNode = probNet.getNode(decision);
        if (decisionNode == null){
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
    /**
     * @param decision
     * @return True if the decision has an imposed policy.
     */
    public boolean hasImposedPolicy(Variable decision){
        return (getPolicy(decision)!=null);
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

    /**
     * @param probNet
     *            Replaces decision nodes in 'probNet' by chance nodes by using
     *            the corresponding policies. In PRERESOLUTION phase only
     *            imposed policies are used. In POSTRESOLUTION phase both
     *            imposed and calculated policies are used. Decision nodes in
     *            'informationalPredecessors' are not changed.
     * @param informationalPredecessors
     */
    protected void replaceDecisionsByChanceNodesWithPolicies(ProbNet probNet, List<Variable> informationalPredecessors) {
        // Change decision nodes by chance nodes whose probability potential
        // is given by the corresponding policy
        List<Node> decisions = probNet.getNodes(NodeType.DECISION);
        for (Node decision : decisions) {
            Variable varDecision = decision.getVariable();

            if ((informationalPredecessors == null) || (!informationalPredecessors.contains(varDecision))) {

                Potential policy = getPolicy(varDecision);

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

}
