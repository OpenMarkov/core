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
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

public abstract class InferenceAlgorithm
{
    /** This is a copy of the <code>ProbNet</code> received. */
    protected ProbNet                     probNet;
    /** For undo/redo operations. */
    protected PNESupport                  pNESupport;
    /**
     * <code>true</code> if the network is prepared for obtaining the marginal
     * probabilities.
     */
    // protected boolean compiled;
    /**
     * Indicates if the Bayesian network or the Cooper Policy Network has been
     * compiled. If it is true, then the posteriori probabilities and expected
     * utilities associated to each utility node have been computed.
     */
   // protected boolean                     hasBeenCompiled;
 //   protected StrategyUtilities           utilityTables;
 //   protected Hashtable<Variable, Double> expectedUtilities;
 //   protected Double                      globalExpectedUtility;
    
    //evidence is deprecated. It is only maintained during the debugging phase
    //of the inference
   /* protected EvidenceCase                evidence;
    
	public void setEvidence(EvidenceCase evidence) {
		this.evidence = evidence;
	}
*/
	/**
	 * Evidence introduced before the network is resolved. In influence diagrams this is
	 * Ezawa's evidence.
	 */
	private ArrayList<Finding> preResolutionEvidence;
	
	/**
	 * Evidence when the network has been resolved. In influence diagrams this is
	 * Luque and Diez's evidence.
	 */
	private ArrayList<Finding> postResolutionEvidence;
	
	public ArrayList<Finding> getPostResolutionEvidence() {
		return postResolutionEvidence;
	}

	public void setPostResolutionEvidence(ArrayList<Finding> postResolutionEvidence) {
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
    
  
    public ArrayList<Finding> getPreResolutionEvidence() {
		return preResolutionEvidence;
	}

	public void setPreResolutionEvidence(ArrayList<Finding> preResolutionEvidence) {
		this.preResolutionEvidence = preResolutionEvidence;
	}

	public ArrayList<Variable> getConditioningVariables() {
		return conditioningVariables;
	}

	public void setConditioningVariables(ArrayList<Variable> conditioningVariables) {
		this.conditioningVariables = conditioningVariables;
	}

	public ArrayList<TablePotential> getImposedPolicies() {
		return imposedPolicies;
	}


    

 
    // Constructor
    public InferenceAlgorithm (ProbNet probNet)
        throws NotEvaluableNetworkException
    {
        this.probNet = probNet;
        //evidence = new EvidenceCase();
        if (!isEvaluable (probNet))
        {
            throw new NotEvaluableNetworkException (probNet.toString ());
        }
    }

    public abstract boolean isEvaluable (ProbNet probNet);

      
    /**
     * @param imposedPolicies the imposedPolicies to set
     */
    public void setImposedPolicies (ArrayList<TablePotential> imposedPolicies)
    {
        this.imposedPolicies = imposedPolicies;
    }

    /**
     * This method calculates the probabilities of each variable of interest in
     * this form: P(a|evidence), P(b|evidence) ...
     * @param variablesOfInterest <code>ArrayList</code> of
     *            <code>Variable</code>s.
     * @param evidence <code>EvidenceCase</code>.
     * @return A <code>HashMap</code> with key = a variable and value = a
     *         potential
     * @throws CanNotDoEditException
     * @throws ConstraintViolationException
     * @throws DoEditException
     * @throws NotEvaluableNetworkException
     * @throws WrongCriterionException
     * @throws ProbNodeNotFoundException
     * @throws WrongGraphStructureException
     * @throws IncompatibleEvidenceException 
     */
/*    public abstract HashMap<Variable, Potential> getIndividualProbabilities (ArrayList<Variable> variablesOfInterest)
        throws NotEnoughMemoryException,
        NormalizeNullVectorException,
        DoEditException,
        ConstraintViolationException,
        CanNotDoEditException,
        NotEvaluableNetworkException,
        NonProjectablePotentialException,
        WrongCriterionException,
        WrongGraphStructureException,
        ProbNodeNotFoundException, IncompatibleEvidenceException;;*/

    /**
     * This method calculates the probabilities for all the variables in this
     * form: P(a|evidence), P(b|evidence) ...
     * @param evidence <code>EvidenceCase</code>.
     * @return A <code>HashMap</code> with key = a variable and value = a
     *         potential
     * @throws CanNotDoEditException
     * @throws ConstraintViolationException
     * @throws DoEditException
     * @throws NotEvaluableNetworkException
     * @throws NonProjectablePotentialException
     * @throws WrongCriterionException
     * @throws ProbNodeNotFoundException
     * @throws WrongGraphStructureException
     * @throws IncompatibleEvidenceException 
     */
/*    public abstract HashMap<Variable, Potential> getIndividualProbabilities ()
        throws NotEnoughMemoryException,
        NormalizeNullVectorException,
        DoEditException,
        ConstraintViolationException,
        CanNotDoEditException,
        NotEvaluableNetworkException,
        NonProjectablePotentialException,
        WrongCriterionException,
        WrongGraphStructureException,
        ProbNodeNotFoundException,
        IncompatibleEvidenceException;;;*/

 /*   public abstract StrategyUtilities getUtilityTables ()
        throws NotEnoughMemoryException,
        WrongGraphStructureException,
        ConstraintViolationException,
        CanNotDoEditException,
        DoEditException,
        NonProjectablePotentialException,
        WrongCriterionException,
        ProbNodeNotFoundException;

    public Hashtable<Variable, Double> getExpectedUtilities ()
        throws NotEnoughMemoryException,
        WrongGraphStructureException,
        ConstraintViolationException,
        CanNotDoEditException,
        DoEditException,
        NonProjectablePotentialException,
        WrongCriterionException,
        NotEvaluableNetworkException,
        ProbNodeNotFoundException,
        IncompatibleEvidenceException,
        NormalizeNullVectorException
    {
        // TODO Auto-generated method stub
        return null;
    }*/

 /*   public Double getGlobalExpectedUtility ()
        throws NotEnoughMemoryException,
        WrongGraphStructureException,
        ConstraintViolationException,
        CanNotDoEditException,
        DoEditException,
        NonProjectablePotentialException,
        WrongCriterionException,
        ProbNodeNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }*/
    
    /**
     * @return The optimal policy for the decisions not having imposed policies.
     * The domain of each policy also includes the conditioning variables.
     */
    public abstract HashMap<Variable,TablePotential> getStrategy () throws
	NotEnoughMemoryException,
	IncompatibleEvidenceException,
    NormalizeNullVectorException;
    
    
    private void resolve() {
		// TODO Auto-generated method stub
		
	}

	/**
     * @return The global expected utility of the influence diagram. It is a potential
     * defined over the conditioning variables.
     */
    public abstract TablePotential getGlobalUtility() throws
	NotEnoughMemoryException,
	IncompatibleEvidenceException,
    NormalizeNullVectorException;;
    
    
    public abstract HashMap<Variable,TablePotential> getProbsAndUtilities() throws
    	NotEnoughMemoryException,
    	IncompatibleEvidenceException,
        NormalizeNullVectorException;
    
    public abstract HashMap<Variable,TablePotential> getProbsAndUtilities(ArrayList<Variable> variablesOfInterest) throws
	NotEnoughMemoryException,
	IncompatibleEvidenceException,
    NormalizeNullVectorException;
    
    public abstract TablePotential getJointProbability(ArrayList<Variable> variables)throws
	NotEnoughMemoryException,
	IncompatibleEvidenceException,
    NormalizeNullVectorException;
    
    
   

    
    
    
    protected TablePotential getImposedPolicy(Variable decision){
    	TablePotential policyDecision = null;
    	TablePotential iPolicy;
    	boolean foundPolicy;
    	
    	foundPolicy = false;
    	if (imposedPolicies!=null){
    	for (int i=0;i<imposedPolicies.size()&&!foundPolicy;i++){
    		iPolicy = imposedPolicies.get(i);
    		if (iPolicy!=null){
    			foundPolicy = iPolicy.getVariable(0)==decision;
    			if (foundPolicy){
    				policyDecision = iPolicy;
    			}
    		}
    	}
    	}
    	return policyDecision;
    }
    
    public boolean hasImposedPolicy(Variable decision){
    	return (getImposedPolicy(decision)!=null);
    }
    
    
    

    
    
    
}
