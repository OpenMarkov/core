package org.openmarkov.core.inference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import static org.openmarkov.core.model.network.factory.IDFactory.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.factory.IDFactory;
import org.openmarkov.core.model.network.potential.TablePotential;

public abstract class InferencePropagationTaskIDTest  extends InferencePropagationTaskDecTest {

	protected ProbNet iD_DecisionTestProblemWithSV;

	/**
	 * Test for diagnosis problem
	 *
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 * @throws UnexpectedInferenceException
	 * @throws IncompatibleEvidenceException
	 */
	@Ignore("Old tests with an AssertionError") @Test
	public void testPosteriorProbsAndUtilsIDDecisionTestProblem()
			throws IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
		ProbNet network = buildIDDecideTest();

		Propagation algorithm;
		EvidenceCase preResolutionEvidence;
		EvidenceCase postResolutionEvidence;


		List<Variable> variablesOfInterest = Arrays.asList(network.getVariable(decTestName),
				network.getVariable(therapyName),
				network.getVariable(diseaseName),
				network.getVariable(testResultName),
				network.getVariable(healthStateName),
				network.getVariable(therapyCostName),
				network.getVariable(testCostName));

		preResolutionEvidence = new EvidenceCase();

		//Without post-resolution evidence
		postResolutionEvidence = new EvidenceCase();

		algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network,variablesOfInterest,preResolutionEvidence,postResolutionEvidence);

		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm,network, 1.0, 0.1532, 0.8468, 0.1532, 0.14,
				9.6312, -0.0383, -0.2);

		// Post-resolution evidence: therapy = no
		postResolutionEvidence = new EvidenceCase();
		try {
			postResolutionEvidence.addFinding(network, therapyName, "no");
		} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network,variablesOfInterest,preResolutionEvidence,postResolutionEvidence);

		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm,network, 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
				9.895843174303259, 0.0, -0.2);

		// Post-resolution evidence: result of test = negative
		postResolutionEvidence = new EvidenceCase();
		try {
			postResolutionEvidence.addFinding(network, IDFactory.testResultName, "negative");
		} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network,
				variablesOfInterest,preResolutionEvidence,postResolutionEvidence);

		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm,network, 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
				9.895843174303259, 0.0, -0.2);

		// Post-resolution evidence: do test? = yes
		postResolutionEvidence = new EvidenceCase();
		try {
			postResolutionEvidence.addFinding(network, IDFactory.decTestName, "yes");
		} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network,variablesOfInterest,
				preResolutionEvidence,postResolutionEvidence);

		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm,network, 1.0, 0.1532, 0.8468, 0.1532, 0.14, 9.6312,
				-0.0383,-0.2);

		// Post-resolution evidence: do test? = yes & result of test = positive
		postResolutionEvidence = new EvidenceCase();
		try {
			postResolutionEvidence.addFinding(network, IDFactory.decTestName, "yes");
			postResolutionEvidence.addFinding(network, IDFactory.testResultName, "positive");
		} catch (InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network,variablesOfInterest,
				preResolutionEvidence,postResolutionEvidence);

		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm, network, 1.0, 1.0, 0.0, 1.0, 0.8316, 8.1684, -0.25,-0.2);

		try {
			postResolutionEvidence.addFinding(network, IDFactory.diseaseName, "present");
		} catch (InvalidStateException | IncompatibleEvidenceException e) {
			printExceptionAndFailIfImplemented(e);
		}

	}

	protected void checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(
			Propagation algorithm, ProbNet diagram, double t, double y1, double y2, double d,
			double x, double  uHealthState,  double uCostOfTherapy, double uCostOfTest)
			throws NotEvaluableNetworkException {

		Variable variableX = null;
		Variable variableY = null;
		Variable variableT = null;
		Variable variableD = null;
		Variable variableU1 = null;
		Variable variableU2 = null;
		Variable variableU3 = null;

		try {
			variableT = diagram.getVariable(decTestName);
			variableD = diagram.getVariable(therapyName);
			variableX = diagram.getVariable(diseaseName);
			variableY = diagram.getVariable(testResultName);
			variableU1 = diagram.getVariable(healthStateName);
			variableU2 = diagram.getVariable(therapyCostName);
			variableU3 = diagram.getVariable(testCostName);

		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}

		try {
			HashMap<Variable, TablePotential> aPosterioriProbabilities = null;
			try {
				aPosterioriProbabilities = algorithm.getPosteriorValues();
			} catch (UnexpectedInferenceException e) {
				e.printStackTrace();
			}

			checkProbabilityPotential(aPosterioriProbabilities, variableX, x);
			checkProbabilityPotential(aPosterioriProbabilities, variableY, y1,
					y2);
			checkProbabilityPotential(aPosterioriProbabilities, variableD, d);
			checkProbabilityPotential(aPosterioriProbabilities, variableT, t);
			checkUtilityPotential(aPosterioriProbabilities, variableU1, uHealthState);
			checkUtilityPotential(aPosterioriProbabilities, variableU2, uCostOfTherapy);
			checkUtilityPotential(aPosterioriProbabilities, variableU3, uCostOfTest);
		} catch (IncompatibleEvidenceException e) {
			printExceptionAndFailIfImplemented(e);
		}

	}

	//@Test
	//TODO Review the minor error in test
	//TODO: has this test sense here
	public void testPreAndPostResolutionEvidenceIDDecisionTestProblem() throws NotEvaluableNetworkException, UnexpectedInferenceException, IncompatibleEvidenceException {
		ProbNet diagram = iD_DecisionTestProblemWithSV;

		Propagation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram,null,null,null);

		//TODO Test combination of pre and post resolution findings.
		try {
			//Variable variableT = diagram.getVariable("T");
			//Variable variableD = diagram.getVariable("D");

			EvidenceCase preResolutionEvidence = new EvidenceCase();
			preResolutionEvidence.addFinding(diagram, diseaseName,"present");
			algorithm.setPreResolutionEvidence(preResolutionEvidence);

			// Test optimal policy
			
		/*	HashMap<Variable, TablePotential> optimalStrategy = variableElimination
					.getOptimizedPolicies();
			TablePotential policyT = optimalStrategy.get(variableT);
			TablePotential policyD = optimalStrategy.get(variableD);
			assertNotNull(policyT);
			assertNotNull(policyD);

			// Test the size of the domain of the policy of T
			assertTrue(checkPolicy(policyT, variableT, 0));

			// Test the size of the domain of the policy of D
			assertTrue(checkPolicy(policyD, variableD, 2));

			// Test the a priori case
			HashMap<Variable, TablePotential> aPrioriProbabilities = variableElimination
					.getProbsAndUtilities();*/
			// Read the variables
		/*	variableX = diagram.getVariable("X");
			assertNotNull(variableX);
			variableY = diagram.getVariable("Y");
			assertNotNull(variableY);
			variableU1 = diagram.getVariable("U1");
			assertNotNull(variableU1);
			variableU2 = diagram.getVariable("U2");
			assertNotNull(variableU2);

			checkProbabilityPotential(aPrioriProbabilities, variableX, 0.07);
			checkProbabilityPotential(aPrioriProbabilities, variableY, 0.0916,
					0.9084);
			checkProbabilityPotential(aPrioriProbabilities, variableD, 0.0916);
			checkProbabilityPotential(aPrioriProbabilities, variableT, 1.0);
			checkUtilityPotential(aPrioriProbabilities, variableU1, 98.006);
			checkUtilityPotential(aPrioriProbabilities, variableU2, -2.0);*/

			// Test the expected utilities of the policy
			// StrategyUtilities strategyUtilities =
			// variableElimination.getUtilityTables();
			// TablePotential policyUtilities =
			// strategyUtilities.getUtilities(D);

			// Test the optimal choice of the policy
			// double[] truePolicy = {1.0, 0.0, 0.0, 1.0};

			// assertTrue(areEquals(policy.getValues(),truePolicy));
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}





	/**
	 * @param network
	 * @return A Task for 'network'. If the network is not evaluable
	 * with the algorithm then the test calling this method is skipped.
	 * @throws UnexpectedInferenceException
	 * @throws IncompatibleEvidenceException
	 */
	protected Propagation buildInferenceTaskAndSkipTestIfNotEvaluable(ProbNet network, List<Variable>  variablesOfInterest,
															   EvidenceCase preResolutionEvidence,
															   EvidenceCase postResolutionEvidence)
			throws IncompatibleEvidenceException, UnexpectedInferenceException {
		boolean isEvaluable;
		Propagation task = null;

		//If the network is not evaluable then the test is skipped
		isEvaluable = true;
		try {
			task = buildInferenceTask(network, variablesOfInterest, preResolutionEvidence, postResolutionEvidence);
		} catch (NotEvaluableNetworkException e1) {
			isEvaluable = false;
		}
		assumeTrue(isEvaluable);
		return task;
	}

	protected void setUp() throws Exception {
		// TODO Auto-generated method stub

	}

}