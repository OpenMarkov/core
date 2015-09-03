package org.openmarkov.core.inference;

import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.PosteriorValues;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author manolo
 * Tests class for models that contain decisions. Different subclasses share that they have to test the MEU and the strategy
 *
 */
public abstract class InferencePropagationTaskDecTest extends InferenceTaskTest {

	public abstract PosteriorValues buildInferenceTask(ProbNet probNet, List<Variable> variablesOfInterest,
								   EvidenceCase preResolutionEvidence, EvidenceCase postResolutionEvidence)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException;


	public void checkUtilityPotential(
			Map<Variable, TablePotential> aPrioriProbabilities,
			Variable variableU, double u) {
		TablePotential U = aPrioriProbabilities.get(variableU);
		checkUtility(U, u);

	}
	/**
	 * @param aPosterioriUtils
	 * @param variables
	 * @param expectedUtils
	 * Checks the posterior utilities of a list of utility nodes.
	 * The utilities are ordered according the order in 'variables'.
	 * */
	protected void checkUtilities(
			HashMap<Variable, TablePotential> aPosterioriUtils,
			ArrayList<Variable> variables, double[] expectedUtils) {

		int size = variables.size();

		for (int i=0;i<size;i++){
			checkUtilityPotential(aPosterioriUtils,variables.get(i),expectedUtils[i]);
		}

	}



	/**
	 * @param x
	 * @param v
	 * Checks if the values of the potential 'x' are equal to 'v' and if the number
	 * of values in 'x' is 1.
	 */
	protected void checkUtility(TablePotential x, double v) {

		assertEquals(1, x.getTableSize());
		assertEquals(v, x.values[0], maxError);

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
