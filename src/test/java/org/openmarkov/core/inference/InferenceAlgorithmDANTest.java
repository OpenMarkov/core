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
import org.openmarkov.core.model.network.factory.DANFactory;
import org.openmarkov.core.model.network.factory.NetsFactory;
import org.openmarkov.core.model.network.factory.NetsFactory.NamesNetworks;
import org.openmarkov.core.model.network.ProbNet;

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
		public void testMEUAndStrategyBattery() throws IncompatibleEvidenceException,
				UnexpectedInferenceException {
			//TODO Adapt to the new variable-elimination based inference implementation
			/*try {
				testMEUAndStrategy(DANFactory.buildOneChanceDAN(),90.2,null);
				testMEUAndStrategy(DANFactory.buildBlindTreatmentDAN(),90.2,null);
				testMEUAndStrategy(DANFactory.buildPerfectInformationTreatmentDAN(),97.2,null);
				testMEUAndStrategy(DANFactory.buildDecideTestDAN(),94.312,null);
				testMEUAndStrategy(DANFactory.buildTwoTestDAN(),9.3324,null);
				testMEUAndStrategy(DANFactory.buildDiabetesDAN(),9.8261,null);
				testMEUAndStrategy(DANFactory.buildDecideTreatmentRestrictedDAN(),88.6,null);
				testMEUAndStrategy(DANFactory.buildReactorDAN(),10.0627,null);
				testMEUAndStrategy(DANFactory.buildDatingBranchAcceptSimplifiedDAN(),9.88,null);
				testMEUAndStrategy(DANFactory.buildDatingAcceptNoDAN(),9.4076,null);
				testMEUAndStrategy(DANFactory.buildDatingDAN(),9.4076,null);
				testMEUAndStrategy(DANFactory.buildWooerDAN(),7.73,null);
				testMEUAndStrategy(DANFactory.buildUsedCarBuyer(),32.96,null);				
				testMEUAndStrategy(DANFactory.buildNTestsDAN(3),9.80657,null);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}*/
			
			
		}




		


}
