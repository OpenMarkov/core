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
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.exception.WrongGraphStructureException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.type.NetworkType;

public abstract class Inference {
	
	protected EvidenceCase evidence;
	
	protected ArrayList<Potential> imposedPolicies;

	/** This is a copy of the <code>ProbNet</code> received. */
	protected ProbNet probNet;
	
	/** For undo/redo operations. */
	protected PNESupport pNESupport;
	
	
	/** Set of network types where the algorithm can be applied. */
	private ArrayList<NetworkType> networkTypesApplicable;
	
	/** Set of additional constraints that the ProbNet must satisfy in conjunction with the constraints
	 * typical of the network types. Its initial value must be null, instead 
	 * of an empty ArrayList<PNCconstraint>, so that
	 * the method getRequiredConstraints of its child classes detect when this
	 * property has not been initialized */
	private ArrayList<PNConstraint> additionalConstraints;
		
	/** <code>true</code> if the network is prepared for obtaining the marginal probabilities. */
	protected boolean compiled;
	
	/**
	 * Indicates if the Bayesian network or the Cooper Policy Network has been compiled. If it is true, then the posteriori probabilities
	 * and expected utilities associated to each utility node have been computed.
	 */
	protected boolean hasBeenCompiled;
	
	protected StrategyUtilities utilityTables;
	
	protected Hashtable<Variable,Double> expectedUtilities;
	
	protected Double globalExpectedUtility;
	
	/**
	 * @param globalExpectedUtility the globalExpectedUtility to set
	 */
	public void setGlobalExpectedUtility(Double globalExpectedUtility) {
		this.globalExpectedUtility = globalExpectedUtility;
	}


		protected abstract ArrayList<NetworkType> initializeNetworkTypesApplicable();


		protected abstract ArrayList<PNConstraint> initializeAdditionalConstraints();


		// Constructor
		public Inference(ProbNet probNet) 
		throws NotEvaluableNetworkException {
			
			boolean isNetworkTypeApplicable;
			NetworkType probNetNetworkType;
			
			this.probNet = probNet;
				
			probNetNetworkType = probNet.getNetworkType();
			
			//Check the type of network
			isNetworkTypeApplicable = false;
			ArrayList<NetworkType> networkTypesApplicable2 = getNetworkTypesApplicable();
			for (int iType=0;iType<networkTypesApplicable2.size()&&!isNetworkTypeApplicable;iType++){
				NetworkType auxNetworkType = networkTypesApplicable2.get(iType);
				isNetworkTypeApplicable = (probNetNetworkType == auxNetworkType);
			}
			
			if (!isNetworkTypeApplicable){
				throw new NotEvaluableNetworkException(probNetNetworkType.toString());
			}
			
			//Check the additional constraints
			for (PNConstraint constraint : getAdditionalConstraints()) {
				if (!constraint.checkProbNet(probNet)) {
					throw new NotEvaluableNetworkException(constraint.toString());
				}
			}
			evidence = new EvidenceCase();
		}


	protected final ArrayList<NetworkType> getNetworkTypesApplicable() {
		
		if (networkTypesApplicable==null){
			networkTypesApplicable = initializeNetworkTypesApplicable();
		}
		
		return networkTypesApplicable;
	}


	/**
	 * @return the evidence
	 */
	public EvidenceCase getEvidence() {
		return evidence;
	}

	/**
	 * @param evidence the evidence to set
	 */
	public void setEvidence(EvidenceCase evidence) {
		this.evidence = evidence;
	}
	
	/**
	 * @return the imposedPolicies
	 */
	public ArrayList<Potential> getImposedPolicies() {
		return imposedPolicies;
	}

	/**
	 * @param imposedPolicies the imposedPolicies to set
	 */
	public void setImposedPolicies(ArrayList<Potential> imposedPolicies) {
		this.imposedPolicies = imposedPolicies;
	}
	

	/** This method calculates the probabilities of each variable of interest in
	 *   this form: P(a|evidence), P(b|evidence) ...
	 * @param variablesOfInterest <code>ArrayList</code> of 
	 *   <code>Variable</code>s.
	 * @param evidence <code>EvidenceCase</code>.
	 * @return A <code>HashMap</code> with key = a variable and value = 
	 *   a potential 
	 * @throws CanNotDoEditException 
	 * @throws ConstraintViolationException 
	 * @throws DoEditException 
	 * @throws NotEvaluableNetworkException 
	 * @throws WrongCriterionException 
	 * @throws ProbNodeNotFoundException 
	 * @throws WrongGraphStructureException */
	public abstract HashMap<Variable, Potential> getIndividualProbabilities(
			ArrayList<Variable> variablesOfInterest) 
			throws NotEnoughMemoryException, NormalizeNullVectorException,
			DoEditException, ConstraintViolationException,
			CanNotDoEditException, NotEvaluableNetworkException, 
			NonProjectablePotentialException, WrongCriterionException, WrongGraphStructureException, ProbNodeNotFoundException;;
	
	/** This method calculates the probabilities for all the variables in
	 *   this form: P(a|evidence), P(b|evidence) ...
	 * @param evidence <code>EvidenceCase</code>.
	 * @return A <code>HashMap</code> with key = a variable and value =
	 *   a potential 
	 * @throws CanNotDoEditException 
	 * @throws ConstraintViolationException 
	 * @throws DoEditException 
	 * @throws NotEvaluableNetworkException 
	 * @throws NonProjectablePotentialException 
	 * @throws WrongCriterionException 
	 * @throws ProbNodeNotFoundException 
	 * @throws WrongGraphStructureException */
	public HashMap<Variable, Potential> getIndividualProbabilities() 
			throws NotEnoughMemoryException, NormalizeNullVectorException,
			DoEditException, ConstraintViolationException, 
			CanNotDoEditException, NotEvaluableNetworkException, 
			NonProjectablePotentialException, WrongCriterionException, WrongGraphStructureException, ProbNodeNotFoundException
			 {
				ArrayList<Variable> variablesOfInterest =
					probNet.getChanceAndDecisionVariables();
				return getIndividualProbabilities(variablesOfInterest);
			};
			
	/** This method must be overriden in the child classes */
	protected final Collection<PNConstraint> getAdditionalConstraints(){
			
			if (additionalConstraints==null){
				additionalConstraints = initializeAdditionalConstraints();
			}
			
			return additionalConstraints;
		}
	
	public abstract StrategyUtilities getUtilityTables() throws NotEnoughMemoryException, WrongGraphStructureException, ConstraintViolationException, CanNotDoEditException, DoEditException, NonProjectablePotentialException, WrongCriterionException, ProbNodeNotFoundException;


	public Hashtable<Variable, Double> getExpectedUtilities()
			throws NotEnoughMemoryException, WrongGraphStructureException,
			ConstraintViolationException, CanNotDoEditException,
			DoEditException, NonProjectablePotentialException,
			WrongCriterionException, NotEvaluableNetworkException,
			ProbNodeNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getGlobalExpectedUtility() throws NotEnoughMemoryException,
			WrongGraphStructureException, ConstraintViolationException,
			CanNotDoEditException, DoEditException,
			NonProjectablePotentialException, WrongCriterionException, ProbNodeNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
