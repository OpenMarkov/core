/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.inference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import junit.framework.Assert;

import org.junit.Test;
import org.openmarkov.core.dt.DecisionTreeBuilder;
import org.openmarkov.core.dt.DecisionTreeElement;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.ProbNet;

/**
 * @author manolo
 *
 */
public abstract class InferenceAlgorithmDANTests {
	
	private double maxError = 0.0001;



	/**
	 * @param probNet
	 * @return
	 * @throws NotEvaluableNetworkException
	 * Builds an InferenceAlgorithm object with 'probNet'.
	 * This method must be implemented by each inference test class.
	 */
	public abstract InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet) throws NotEvaluableNetworkException;

	
	/**
	 * @param network
	 * @return An InferenceAlgorithm for 'network'. If the network is not evaluable
	 * with the algorithm then the test calling this method is skipped.
	 */
	private InferenceAlgorithm buildInferenceAlgorithmAndSkipTestIfNotEvaluable(
			ProbNet network) {
		boolean isEvaluable;
		InferenceAlgorithm algorithm = null;
		
		//If the network is not evaluable then the test is skipped
		isEvaluable = true;
		try {
			algorithm = buildInferenceAlgorithm(network);
		} catch (NotEvaluableNetworkException e1) {
			isEvaluable = false;
		}
		assumeTrue(isEvaluable);
		return algorithm;
	}

	
	
	//@Test
	public void testDecideTestDAN() throws IncompatibleEvidenceException, UnexpectedInferenceException {	
		ProbNet network = null;
		try {
			network = NetsFactory.buildDecideTestDAN();
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
		Double meuEvaluation = algorithm.getGlobalUtility().values[0];
		assertEquals(94.312, meuEvaluation, maxError );
	}
	
	@Test
	public void testOneChanceDAN() throws IncompatibleEvidenceException, UnexpectedInferenceException {	
		ProbNet network = null;
		try {
			network = NetsFactory.buildOneChanceDAN();
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
		Double meuEvaluation = algorithm.getGlobalUtility().values[0];
		assertEquals(90.2,meuEvaluation, maxError );
	}
	
	//@Test
	public void testBlindTreatmentDAN() throws IncompatibleEvidenceException, UnexpectedInferenceException {	
		ProbNet network = null;
		try {
			network = NetsFactory.buildBlindTreatmentDAN();
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
		Double meuEvaluation = algorithm.getGlobalUtility().values[0];
		assertEquals(90.2,meuEvaluation, maxError );
	}
}
