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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.openmarkov.core.dt.DecisionTreeBuilder;
import org.openmarkov.core.dt.DecisionTreeElement;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.NetsFactory.NamesNetworks;
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
	
	
	@Test
	public void testMEUBasicBatteryDAN() throws IncompatibleEvidenceException, UnexpectedInferenceException {	
		Set<ProbNet> dans;
		Map<NamesNetworks, Double> meu = meuDANFactory();
		dans = basicDANBattery();
		for (ProbNet dan:dans){
			InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(dan);
			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
		assertEquals(meu.get(NetsFactory.NamesNetworks.valueOf(dan.getName())),meuEvaluation, maxError );
		}
	}
		
		public Map<NetsFactory.NamesNetworks,Double> meuDANFactory(){
			Map<NetsFactory.NamesNetworks,Double> meu = new Hashtable<NetsFactory.NamesNetworks,Double>();
			meu.put(NetsFactory.NamesNetworks.ONE_CHANCE_DAN,90.2);
			meu.put(NetsFactory.NamesNetworks.BLIND_TREATMENT_DAN,90.2);
			meu.put(NetsFactory.NamesNetworks.PERFECT_INFORMATION_TREATMENT_DAN,97.2);
			meu.put(NetsFactory.NamesNetworks.DECIDE_TEST_DAN,94.312);
			meu.put(NetsFactory.NamesNetworks.TWO_TEST_DAN, 9.3324);
			meu.put(NetsFactory.NamesNetworks.DIABETES_DAN,9.8261);
			meu.put(NetsFactory.NamesNetworks.REACTOR_DAN,10.0627);
			meu.put(NetsFactory.NamesNetworks.DATING_DAN,9.4076);
			meu.put(NetsFactory.NamesNetworks.WOOER_DAN,7.73);
			
			return meu;
		}	
		
		/**
		 * @return A set of basic DANs for tests, including OneChance, BlindTreatment, DecideTest and Diabetes.
		 */
		public static Set<ProbNet> basicDANBattery(){
			Set<ProbNet> battery = new HashSet<>();
			try {
				battery.add(NetsFactory.buildOneChanceDAN());
				battery.add(NetsFactory.buildBlindTreatmentDAN());
				battery.add(NetsFactory.buildPerfectInformationTreatmentDAN());
				battery.add(NetsFactory.buildDecideTestDAN());
				
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			battery.add(NetsFactory.buildTwoTestDAN());
			battery.add(NetsFactory.buildDiabetesDAN());
			battery.add(NetsFactory.buildReactorDAN());
			
			battery.add(NetsFactory.buildWooerDAN());
			
			/*try {
				battery.add(NetsFactory.buildDatingDAN());
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}*/
			
			
			return battery;
		}
}
