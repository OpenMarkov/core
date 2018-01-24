/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.tasks.Task;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.NoSuperValueNode;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

/**
 * @author mluque
 * @author marias
 * @author fjdiez
 */
public abstract class InferenceAlgorithm implements Task {

	/**
	 * This is a copy of the <code>ProbNet</code> received.
	 */
	protected ProbNet probNet;
	/**
	 * For undo/redo operations.
	 * TODO - Check if this is necessary
	 */
	protected PNESupport pNESupport;

	/**
	 * Evidence introduced before the network is resolved.
	 * In influence diagrams this is Ezawa's evidence.
	 */
	private EvidenceCase preResolutionEvidence;

	/**
	 * Variables that will not be eliminated during the inference, and therefore all the results
	 * contain these variables in the domain.
	 */
	protected List<Variable> conditioningVariables;

	/**
	 * @param probNet The network used in the inference
	 * @throws NotEvaluableNetworkException
	 */
	public InferenceAlgorithm(ProbNet probNet) throws NotEvaluableNetworkException {
		this.probNet = probNet.copy();
		this.preResolutionEvidence = new EvidenceCase();
		this.conditioningVariables = new ArrayList<>();
		checkEvaluability();
		checkConsistency();
	}

	/**
	 * Checks the network and constraints applicability
	 * @throws NotEvaluableNetworkException
	 */
	public void checkEvaluability() throws NotEvaluableNetworkException {
		checkNetworkApplicability();
		checkConstraintsApplicability();
	}

	/**
	 * Checks the network and evidence consistency
	 * @throws NotEvaluableNetworkException
	 */
	private void checkConsistency() throws NotEvaluableNetworkException {
		checkNetworkConsistency();
		checkEvidenceConsistency();
	}

	/**
	 * Checks network consistency
	 * @throws NotEvaluableNetworkException
	 * TODO - Implement that method
	 */
	private void checkNetworkConsistency() throws NotEvaluableNetworkException {

	}

	/**
	 * Checks evidence consistency
	 * @throws NotEvaluableNetworkException
	 * TODO - Implement that method
	 */
	private void checkEvidenceConsistency() throws NotEvaluableNetworkException {

	}

	/**
	 * Check if the network type can be evaluated by the algorithm
	 * @throws NotEvaluableNetworkException
	 */
	private void checkNetworkApplicability() throws NotEvaluableNetworkException {
		boolean isApplicable;

		List<NetworkType> networkTypes = getPossibleNetworkTypes();

		isApplicable = false;
		NetworkType networkType = probNet.getNetworkType();
		// Check that there is a network type applicable equal to type of probNet
		for (int i = 0; (i < networkTypes.size()) && !isApplicable; i++) {
			isApplicable = networkType == networkTypes.get(i);
		}

		if (!isApplicable) {
			throw new NotEvaluableNetworkException(
					"This algorithm cannot evaluate this network because" + "the network is of type " + networkType
							.toString() + ".");
		}
	}

	/**
	 * List of networks that the algorithm can evaluate
	 * @return
	 */
	protected abstract List<NetworkType> getPossibleNetworkTypes();

	/**
	 * Check if the network satisfies all the constraints that requires the algorithm
	 * @throws NotEvaluableNetworkException
	 * TODO - Remove additional constraints
	 */
	private void checkConstraintsApplicability() throws NotEvaluableNetworkException {
		// Check that the probNet satisfies the specific constraints of the algorithm
		List<PNConstraint> additionalConstraints = getAdditionalConstraints();

		List<PNConstraint> notEvaluableConstraints = new ArrayList<>();
		for (PNConstraint pnConstraint : additionalConstraints) {
			if (!pnConstraint.checkProbNet(probNet)) {
				if (pnConstraint.getClass().equals(NoSuperValueNode.class)) {
					throw new NotEvaluableNetworkException("Evaluation of supervalue nodes is temporarily disabled.");
				}
				notEvaluableConstraints.add(pnConstraint);
			}
		}

		if (notEvaluableConstraints.size() != 0) {
			String notEvaluableMessage = "This algorithm cannot evaluate this network because the network does "
					+ "not satisfy the following constraints:\n";
			for (PNConstraint pnConstraint : notEvaluableConstraints) {
				notEvaluableMessage += pnConstraint.toString() + "\n";
			}
			throw new NotEvaluableNetworkException(notEvaluableMessage);
		}
	
	}

	/**
	 * List of additional constraints that network must satisfy in order to be evaluated by the algorithm
	 * @return
	 */
	protected abstract List<PNConstraint> getAdditionalConstraints();

	/**
	 * @return The pre-resolution evidence
	 */
	public EvidenceCase getPreResolutionEvidence() {
		return this.preResolutionEvidence;
	}

	/**
	 * @param preResolutionEvidence The pre-resolution evidence to set
	 */
	public void setPreResolutionEvidence(EvidenceCase preResolutionEvidence) {
		this.preResolutionEvidence = new EvidenceCase(preResolutionEvidence);
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
	 * @param decision
	 * @return The imposed policy of the decision
	 */
	//	protected Potential getImposedPolicy(Variable decision) {
	//		Potential policy = null;
	//
	//		Node decisionNode = probNet.getNode(decision);
	//		if (decisionNode == null){
	//			policy = null;
	//		}
	//		else{
	//		    List<Potential> potentials = decisionNode.getPotentials();
	//			if ((potentials == null)||(potentials.size()==0)){
	//				policy = null;
	//			}
	//			else{
	//				policy = potentials.get(0);
	//			}
	//		}
	//		return policy;
	//	}
	/**
	 * @param decision
	 * @return True if the decision has an imposed policy.
	 */
	//    public boolean hasImposedPolicy(Variable decision){
	//    	return (getImposedPolicy(decision)!=null);
	//    }

}