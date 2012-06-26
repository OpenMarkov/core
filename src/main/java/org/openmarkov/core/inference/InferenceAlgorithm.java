/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import java.util.ArrayList;
import java.util.HashMap;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author mluque
 * @author marias
 * @author fjdiez
 *
 */
public abstract class InferenceAlgorithm
{
    /** This is a copy of the <code>ProbNet</code> received. */
    protected ProbNet                     probNet;
    /** For undo/redo operations. */
    protected PNESupport                  pNESupport;
   
  
	/**
	 * Evidence introduced before the network is resolved. In influence diagrams this is
	 * Ezawa's evidence.
	 */
	private EvidenceCase preResolutionEvidence;
	
	/**
	 * Evidence when the network has been resolved. In influence diagrams this is
	 * Luque and Diez's evidence.
	 */
	private EvidenceCase postResolutionEvidence;
	
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
     * Policies set by the user. The optimal policy would only be calculated for the decisions
     * without imposed policies.
     * Each policy is stochastic, which implies it is a probability potential whose domain
     * contains the decision.
     */
    private ArrayList<TablePotential> imposedPolicies;
    
    /**
     * Variables that will not be eliminated during the inference, and therefore all the results
     * contain these variables in the domain.
     */
    private ArrayList<Variable> conditioningVariables;
    
  
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

	/**
	 * @return The conditioning variables
	 */
	public ArrayList<Variable> getConditioningVariables() {
		return conditioningVariables;
	}

	/**
	 * @param conditioningVariables The conditioning variables to set
	 */
	public void setConditioningVariables(ArrayList<Variable> conditioningVariables) {
		this.conditioningVariables = conditioningVariables;
	}

	/**
	 * @return The imposed policies
	 */
	public ArrayList<TablePotential> getImposedPolicies() {
		return imposedPolicies;
	}
  
    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public InferenceAlgorithm (ProbNet probNet)
        throws NotEvaluableNetworkException
    {
        this.probNet = probNet;
        if (!isEvaluable (probNet))
        {
            throw new NotEvaluableNetworkException (probNet.toString ());
        }
    }

    /**
     * @param probNet
     * @return True if the network can be evaluated.
     */
    public abstract boolean isEvaluable (ProbNet probNet);

      
    /**
     * @param imposedPolicies the imposedPolicies to set
     */
    public void setImposedPolicies (ArrayList<TablePotential> imposedPolicies)
    {
        this.imposedPolicies = imposedPolicies;
    }

    
    /**
     * @return The optimal policy for the decisions that do not have any imposed policy.
     * The domain of each policy also includes the decision and the conditioning variables.
     */
    public abstract HashMap<Variable,TablePotential> getOptimizedPolicies () throws
	NotEnoughMemoryException,
	IncompatibleEvidenceException,
    NormalizeNullVectorException;
    
    
  
	/**
     * @return The global expected utility of the influence diagram. It is a potential
     * defined over the conditioning variables.
     */
    public abstract TablePotential getGlobalUtility() throws
	NotEnoughMemoryException,
	IncompatibleEvidenceException,
    NormalizeNullVectorException;;
    
    
    /**
     * @return The posterior probabilities and utilities of the network.
     * @throws NotEnoughMemoryException
     * @throws IncompatibleEvidenceException
     * @throws NormalizeNullVectorException
     */
    public abstract HashMap<Variable,TablePotential> getProbsAndUtilities() throws
    	NotEnoughMemoryException,
    	IncompatibleEvidenceException,
        NormalizeNullVectorException;
    
   
    /**
     * @param variablesOfInterest
     * @return The posterior probabilities and utilities of the network.
     * @throws NotEnoughMemoryException
     * @throws IncompatibleEvidenceException
     * @throws NormalizeNullVectorException
     */
    public abstract HashMap<Variable,TablePotential> getProbsAndUtilities(ArrayList<Variable> variablesOfInterest) throws
	NotEnoughMemoryException,
	IncompatibleEvidenceException,
    NormalizeNullVectorException;
    
    /**
     * @param variables
     * @return The joint probability of a list of variables
     * @throws NotEnoughMemoryException
     * @throws IncompatibleEvidenceException
     * @throws NormalizeNullVectorException
     */
    public abstract TablePotential getJointProbability(ArrayList<Variable> variables)throws
	NotEnoughMemoryException,
	IncompatibleEvidenceException,
    NormalizeNullVectorException;
    
  
	/**
	 * @param decision
	 * @return The imposed policy of the decision
	 */
	protected TablePotential getImposedPolicy(Variable decision) {
		TablePotential policyDecision = null;
		TablePotential iPolicy;
		boolean foundPolicy;

		foundPolicy = false;
		if (imposedPolicies != null) {
			for (int i = 0; i < imposedPolicies.size() && !foundPolicy; i++) {
				iPolicy = imposedPolicies.get(i);
				if (iPolicy != null) {
					foundPolicy = iPolicy.getVariable(0) == decision;
					if (foundPolicy) {
						policyDecision = iPolicy;
					}
				}
			}
		}
		return policyDecision;
	}
    
    /**
     * @param decision
     * @return True if the decision has an imposed policy.
     */
    public boolean hasImposedPolicy(Variable decision){
    	return (getImposedPolicy(decision)!=null);
    }
        
    
}
