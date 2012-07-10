/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.inference;
import static org.junit.Assert.*;


import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;



        
/** @author mluque */
/** @author ibermejo */
public abstract class InferenceAlgorithmTests {
	
	/**
	 * Maximum error allowed in tests. It could be modified by subclasses
	 * if it is necessary (for example, approximate inference methods).
	 */
	protected double maxError = 1E-6;

	static double prevalence = 0.91;

	static double sensitivity = 0.95;
	static double specificity = 0.8;
	
	
	/**
	 * Bayesian network with two nodes (X and Y) and a link X -> Y
	 */
	ProbNet bN_XY;
	
	/**
	 * Bayesian network with three nodes (A, B and C) and two links A -> B and A
	 * -> C
	 */
	ProbNet bN_ABC;
	
	/**
	 * Bayesian network with three nodes (X, Y and Z) and two links X -> Y and Y
	 * -> Z
	 */	 
	ProbNet bN_XYZ;

	private ProbNet iD_DiagnosisProblem;
	private ProbNet iD_UniformDiagnosisProblem;
	private ProbNet iD_DecisionTestProblemWithoutSV;
	private ProbNet iD_DecisionTestProblemWithSV;

 
	@Before
	public void setUp() throws Exception {

		bN_XY = NetsFactory.createBayesianNetworkXY(prevalence,
				sensitivity, specificity);
		bN_ABC = NetsFactory.createBayesianNetworkABC();
		bN_XYZ = NetsFactory.createBayesianNetworkXYZ(prevalence,
				sensitivity, specificity, 0.86, 0.89);
		iD_DiagnosisProblem = NetsFactory
				.createInfluenceDiagramDiagnosisProblem();
		iD_UniformDiagnosisProblem = NetsFactory
				.createUniformInfluenceDiagramDiagnosisProblem();
		iD_DecisionTestProblemWithoutSV = NetsFactory
				.createInfluenceDiagramDecisionTestProblemWithoutSV(0.07, 0.91,
						0.97);
		iD_DecisionTestProblemWithSV = NetsFactory
				.createInfluenceDiagramDecisionTestProblem(0.07, 0.91, 0.97);
		
		
	}
	
	
 
	
	
	public abstract InferenceAlgorithm buildInferenceObject(ProbNet probNet) throws NotEvaluableNetworkException;
	
	@Test
	public void testAPrioriProbabilitiesProbNetABC()
			throws NotEvaluableNetworkException, ProbNodeNotFoundException {
		// load a simple bayes net
		ProbNet network;
		network = bN_ABC;

		InferenceAlgorithm elimination1 = buildInferenceObject(network);
		Variable variableA = network.getVariable("A");
		assertNotNull(variableA);
		Variable variableB = network.getVariable("B");
		assertNotNull(variableB);
		Variable variableC = network.getVariable("C");
		assertNotNull(variableC);

		// A priori probabilities
		try {
			HashMap<Variable, TablePotential> aPrioriProbabilities;
			aPrioriProbabilities = elimination1.getProbsAndUtilities();
			// test potential probabilities
			TablePotential A = (TablePotential) aPrioriProbabilities
					.get(variableA);
			double[] valuesA = A.values;
			assertEquals(0.8, valuesA[0], maxError);
			assertEquals(0.2, valuesA[1], maxError);
			TablePotential B = (TablePotential) aPrioriProbabilities
					.get(variableB);
			double[] valuesB = B.values;
			assertEquals(0.14, valuesB[0], maxError);
			assertEquals(0.86, valuesB[1], maxError);
			TablePotential C = (TablePotential) aPrioriProbabilities
					.get(variableC);
			double[] valuesC = C.values;
			assertEquals(0.2784, valuesC[0], maxError);
			assertEquals(0.7216, valuesC[1], maxError);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testIndividualProbabilitiesEvidenceCase: a priori "
					+ "probabilities with probNetABC");
		}
	}


}
