package org.openmarkov.core.inference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;

public abstract class Inference {
	
	protected EvidenceCase evidence;
	
	protected ArrayList<Potential> imposedPolicies;

	private ProbNet probNet;
		
	protected boolean hasInferenceBeenPerformed;
	
	/**
	 * Indicates if the Cooper Policy Network has been compiled. If it is true, then the posteriori probabilities
	 * and expected utilities associated to each utility node have been computed.
	 */
	protected boolean hasCooperPolicyNetworkBeenCompiled;
	
	private utilityTables;
	
	private globalExpectedUtility;
	
	// Constructor
		public Inference(ProbNet probNet) 
		throws NotEvaluableNetworkException {
			this.probNet = probNet;
			for (PNConstraint constraint : getRequiredConstraints()) {
				if (!constraint.checkProbNet(probNet)) {
					throw new NotEvaluableNetworkException(constraint.toString());
				}
			}
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
	 * @throws WrongCriterionException */
	public abstract HashMap<Variable, Potential> getIndividualProbabilities(
			ArrayList<Variable> variablesOfInterest) 
			throws NotEnoughMemoryException, NormalizeNullVectorException,
			DoEditException, ConstraintViolationException,
			CanNotDoEditException, NotEvaluableNetworkException, 
			NonProjectablePotentialException, WrongCriterionException;;
	
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
	 * @throws WrongCriterionException */
	public HashMap<Variable, Potential> getIndividualProbabilities() 
			throws NotEnoughMemoryException, NormalizeNullVectorException,
			DoEditException, ConstraintViolationException, 
			CanNotDoEditException, NotEvaluableNetworkException, 
			NonProjectablePotentialException, WrongCriterionException
			 {
				ArrayList<Variable> variablesOfInterest =
					probNet.getChanceAndDecisionVariables();
				return getIndividualProbabilities(variablesOfInterest);
			};
			
	/** This method must be overriden in the child classes */
	public abstract Collection<PNConstraint> getRequiredConstraints();
	
	public abstract getUtilityTables();
	
	
}
