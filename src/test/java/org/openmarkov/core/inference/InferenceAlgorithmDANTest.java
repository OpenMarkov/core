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

import org.junit.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.factory.DANFactory;
import org.openmarkov.core.model.network.factory.IDFactory;
import org.openmarkov.core.model.network.potential.Intervention;

/**
 * @author manolo
 *
 */
public class InferenceAlgorithmDANTest extends InferenceAlgorithmDecTest {
	

	public InferenceAlgorithmDANTest() {
		maxError = 0.0001;
	}

			

		@Override
		public InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet)
				throws NotEvaluableNetworkException {
			return null;
		}


		@Override
		protected void testMEUAndStrategy(ProbNet net, double expectedMEU,
				Intervention expectedStrategy)
				throws IncompatibleEvidenceException,
				UnexpectedInferenceException {
			InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(net);
			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
			assertEquals(expectedMEU,meuEvaluation, maxError);
		}

		
		//@Test
		public void testDANOneChance() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildOneChanceDAN(),90.2,null);	
		}
		
		//@Test
		public void testDANBlindTreatment() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildBlindTreatmentDAN(),90.2,null);	
		}
		
		//@Test
		public void testDANPerfectInformationTreatment() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildDANPerfectKnowledge(),9.72,null);	
		}
		
		//@Test
		public void testDANDecideTest() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildDecideTestDAN(),94.312,null);	
		}
		
		//@Test
		public void testDANTwoTest() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildTwoTestDAN(),9.3324,null);	
		}
		
		//@Test
		public void testDANDiabetes() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildDiabetesDAN(),9.8261,null);	
		}
		
		//@Test
		public void testDANDecideTreatmentRestricted() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildDecideTreatmentRestrictedDAN(),88.6,null);	
		}
		
		//@Test
		public void testDANReactor() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildReactorDAN(),10.0627,null);	
		}
		
		//@Test
		public void testDANDatingBranchAcceptSimplified() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildDatingBranchAcceptSimplifiedDAN(),9.88,null);	
		}
		
		//@Test
		public void testDANDatingAcceptNo() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildDatingAcceptNoDAN(),9.4076,null);	
		}
		
		//@Test
		public void testDANDating() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildDatingDAN(),9.4076,null);	
		}
		
		//@Test
		public void testDANWooer() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildWooerDAN(),7.73,null);	
		}
		
		//@Test
		public void testDANUserCarBuyer() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildUsedCarBuyer(),32.96,null);	
		}
		
		//@Test
		public void testDANNTest3Tests() throws IncompatibleEvidenceException, UnexpectedInferenceException, NodeNotFoundException{
			testMEUAndStrategy(DANFactory.buildNTestsDAN(3),9.80657,null);	
		}

}
