/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network;

import java.util.HashMap;

import org.openmarkov.core.exception.NoPropagationCanBeDoneException;
import org.openmarkov.core.exception.NoPropagationOnInfluenceDiagramsException;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;



/**
 * This class holds the information about and evidence case and
 * the resulting individual probabilities.
 * 
 * @author asaez 
 * @version 1.0
 */
public class Propagation {
	
	/**
	 * The network that this propagation is about.
	 */
	private ProbNet probNet;
	
	/**
	 * The evidence case that triggers this propagation.
	 */
	private EvidenceCase evidenceCase;
	
	/**
	 * The individual probabilities resulting of this propagation.
	 */
	private HashMap<Variable, Potential> individualProbabilities = null;
	
	
	/**
	 * Creates a new Propagation.
	 * 
	 * @param probNet
	 * 			  network to which this propagation is associated 
	 * @param evidenceCase
	 *            evidence case that triggers the propagation.
	 * @throws NoPropagationOnInfluenceDiagramsException 
	 * @throws NoPropagationCanBeDoneException 
	 */
	public Propagation(ProbNet probNet, EvidenceCase evidenceCase) throws NoPropagationCanBeDoneException, NoPropagationOnInfluenceDiagramsException {
		this.probNet = probNet;
		this.evidenceCase = evidenceCase;
		doPropagation();
	}

	/**
	 * Returns the individual probabilities resulting of propagation.
	 * 
	 * @return individual probabilities resulting of propagation.
	 */	
	public HashMap<Variable, Potential> getIndividualProbabilities() {
		return individualProbabilities;
	}
	
	/**
	 * This method does the propagation, obtaining the individual 
	 * probabilities corresponding to the evidence case provided.
	 * @throws NoPropagationCanBeDoneException 
	 * @throws NoPropagationOnInfluenceDiagramsException 
	 */	
	private void doPropagation() throws NoPropagationCanBeDoneException, NoPropagationOnInfluenceDiagramsException {
		//Currently it's fixed to use VarEliminationBN algorithm in case of a Bayesian Network.
		//In a future it should be dependent on the user election.
		//In case of an Influence Diagram it should use an appropriate algorithm (maybe VarEliminationID)
		boolean hasBNConstraint = probNet.getNetworkType () instanceof BayesianNetworkType;
		boolean hasIDConstraint = probNet.getNetworkType () instanceof InfluenceDiagramType;
		if (hasBNConstraint) {
//			Evaluation algorithm = new VarEliminationBN(probNet, probNet.getPNESupport());
//			individualProbabilities = algorithm.individualProbabilities(evidenceCase);
		} else if (hasIDConstraint) {
			//TODO
			throw new NoPropagationOnInfluenceDiagramsException();
			//TODO: Show this message higher up
			/*JOptionPane.showMessageDialog(null, "ERROR\nThis Network is an ID\n\n" +
					"Propagation cannot be done in Influence Diagrams by the moment", 
					"Error - IDConstraint in this Network", JOptionPane.ERROR_MESSAGE);*/
			
		} else {
			//TODO
			throw new NoPropagationCanBeDoneException(probNet.getConstraints());
		}
	}

}
