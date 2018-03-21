/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.Evaluation;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author manolo
 * Tests class for models that contain decisions. Different subclasses share that they have to test the MEU and the strategy
 */
public abstract class InferenceResolutionTaskDecTest extends InferenceTaskTest {

	/**
	 * @param probNet
	 * @return
	 * @throws NotEvaluableNetworkException  Builds an InferenceAlgorithm object with 'probNet'.
	 *                                       This method must be implemented by each inference test class.
	 * @throws UnexpectedInferenceException
	 * @throws IncompatibleEvidenceException
	 */
	public abstract Evaluation buildInferenceTask(ProbNet probNet)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException;

	/**
	 * @param x
	 * @param v Checks if the values of the potential 'x' are equal to 'v' and if the number
	 *          of values in 'x' is 1.
	 */
	protected void checkUtility(TablePotential x, double v) {
		assertEquals(1, x.getTableSize());
		assertEquals(v, x.values[0], maxError);
	}

	public void checkUtilityPotential(Map<Variable, TablePotential> aPrioriProbabilities, Variable variableU,
			double u) {
		TablePotential U = (TablePotential) aPrioriProbabilities.get(variableU);
		checkUtility(U, u);
	}

	/**
	 * Test for diagnosis problem
	 *
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	//@Test
/*	public void testEvaluationIDDecisionTestProblem()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {

		Task algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(iD_DecisionTestProblemWithoutSV);
		try {
			// test max expected utility
			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
			assertEquals(96.006, meuEvaluation, maxError);

			// Test optimal policy
			Variable T = diagram.getVariable("T");
			Variable D = diagram.getVariable("D");
			HashMap<Variable, TablePotential> optimalStrategy = algorithm
					.getOptimizedPolicies();
			TablePotential policyT = optimalStrategy.get(T);
			TablePotential policyD = optimalStrategy.get(D);
			assertNotNull(policyT);
			assertNotNull(policyD);

			// Test the size of the domain of the policy of T
			assertTrue(checkPolicy(policyT, T, 0));

			// Test the size of the domain of the policy of D
			assertTrue(checkPolicy(policyD, D, 2));

			// Test the expected utilities of the policy
			// StrategyUtilities strategyUtilities =
			// variableElimination.getUtilityTables();
			// TablePotential policyUtilities =
			// strategyUtilities.getUtilities(D);

			// Test the optimal choice of the policy
			// double[] truePolicy = {1.0, 0.0, 0.0, 1.0};

			// assertTrue(areEquals(policy.getValues(),truePolicy));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("Exception in testTrivial1");
		}
	}
*/

}
