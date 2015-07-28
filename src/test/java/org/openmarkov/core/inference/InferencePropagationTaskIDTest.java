package org.openmarkov.core.inference;

import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.Task;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.factory.IDFactory;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public abstract class InferencePropagationTaskIDTest extends InferencePropagationTaskDecTest {

	protected ProbNet iD_UniformDiagnosisProblem;
	protected ProbNet iD_DecisionTestProblemWithSV;

//	protected ProbNet getIDDecideTest() {
//		return IDFactory.buildIDDecideTest();
//	}

//	protected void testMEUAndStrategy(ProbNet net,double expectedMEU,Intervention expectedStrategy) throws IncompatibleEvidenceException, UnexpectedInferenceException{
//		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(net);
//		Double meuEvaluation = algorithm.getGlobalUtility().values[0];
//		assertEquals(expectedMEU,meuEvaluation, maxError);
//
//		// TODO No se que hace esto. Documentar
//		// testScenariosIntervention(net,algorithm);
//	}

	//@Test
	public void testEvaluationSimpleIDWithoutDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.createSimpleIDWithoutDecisions(),83.7);
	}

	protected void testMEU(ProbNet diagram,double expectedMeu) throws IncompatibleEvidenceException, UnexpectedInferenceException{
		// TODO: review
		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram, null, null, null);


		// test max expected utility
		Double meuEvaluation = null;
		try {
			meuEvaluation = algorithm.getGlobalUtility().values[0];
		} catch (IncompatibleEvidenceException | UnexpectedInferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(expectedMeu, meuEvaluation, maxError);


	}

	private Intervention getOptimalStrategy(ProbNet id) throws IncompatibleEvidenceException, UnexpectedInferenceException {
		Intervention strategy = null;
		// TODO: review
		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(id, null, null, null);
		try {
			strategy = algorithm.getOptimalStrategy();
		} catch (IncompatibleEvidenceException | UnexpectedInferenceException e) {
			e.printStackTrace();
		}
		return strategy;
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
	 * @throws UnexpectedInferenceException
	 * @throws IncompatibleEvidenceException
	 */
	public void testEvaluationIDDecisionTestProblem(ProbNet diagram)
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
		Variable variableX;
		Variable variableY;
		Variable variableT;
		Variable variableD;
		Variable variableU1;
		Variable variableU2;

		//TODO: revise
		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram, null,null,null);

		try {
			// test max expected utility

			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
			assertEquals(96.006, meuEvaluation, maxError);

			// Test optimal policy
			variableT = getVariableAndAssertNotNull(diagram,"T");
			variableD = getVariableAndAssertNotNull(diagram,"D");


			Intervention optimalStrategy = algorithm.getOptimalStrategy();

			Potential policyT = algorithm.getOptimizedPolicy(variableT);
			Potential policyD = algorithm.getOptimizedPolicy(variableD);
			assertNotNull(policyT);
			assertNotNull(policyD);

			// Test the size of the domain of the policy of T
			assertTrue(checkPolicy(getTablePotential(policyT), variableT, 0));

			// Test the size of the domain of the policy of D
			assertTrue(checkPolicy(getTablePotential(policyD), variableD, 2));

			// Test the a priori case
			Map<Variable, TablePotential> aPrioriProbabilities = algorithm
					.getProbsAndUtilities();
			// Read the variables
			variableX = getVariableAndAssertNotNull(diagram,"X");
			variableY = getVariableAndAssertNotNull(diagram,"Y");
			variableU1 = getVariableAndAssertNotNull(diagram,"U1");
			variableU2 = getVariableAndAssertNotNull(diagram,"U2");



			//euPotT
			TablePotential euPotT = constructExpectedUtilitiesPolicyTDecisionTestProblem(variableT);
			assertTrue(areEqualPotentials(euPotT,(TablePotential) algorithm.getExpectedUtilities(variableT)));

			//euPotT
			TablePotential euPotD = constructExpectedUtilitiesPolicyDDecisionTestProblem(variableT,variableY,variableD);
			assertTrue(areEqualPotentials(euPotD,(TablePotential) algorithm.getExpectedUtilities(variableD)));

			checkProbabilityPotential(aPrioriProbabilities, variableX, 0.07);
			checkProbabilityPotential(aPrioriProbabilities, variableY, 0.0916,
					0.9084);
			checkProbabilityPotential(aPrioriProbabilities, variableD, 0.0916);
			checkProbabilityPotential(aPrioriProbabilities, variableT, 1.0);
			checkUtilityPotential(aPrioriProbabilities, variableU1, 98.006);
			checkUtilityPotential(aPrioriProbabilities, variableU2, -2.0);


		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}

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
	 * @throws UnexpectedInferenceException
	 * @throws IncompatibleEvidenceException
	 */
	@Test
	public void testPosteriorProbsAndUtilsIDDecisionTestProblem()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
		ProbNet network = IDFactory.buildIDDecideTest();

		Task algorithm;
		EvidenceCase evi;

		evi = new EvidenceCase();
		try {
			evi.addFinding(network, IDFactory.therapyName, "no");
		} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network,
				Collections.singletonList(network.getVariable(IDFactory.therapyName)),new EvidenceCase(),evi);

		checkPosteriorProbsAndUtilitiesDecideTest(algorithm,
				network, IDFactory.therapyName, "no", 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
				9.895843174303259, 0.0,-0.2);

		evi = new EvidenceCase();
		try {
			evi.addFinding(network, IDFactory.testResultName, "negative");
		} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network,
				Collections.singletonList(network.getVariable(IDFactory.testResultName)),new EvidenceCase(),evi);

		checkPosteriorProbsAndUtilitiesDecideTest(algorithm,
				network, IDFactory.testResultName, "negative", 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
				9.895843174303259, 0.0,-0.2);

		evi = new EvidenceCase();
		try {
			evi.addFinding(network, IDFactory.decTestName, "yes");
		} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network,
				Collections.singletonList(network.getVariable(IDFactory.decTestName)),new EvidenceCase(),evi);

		checkPosteriorProbsAndUtilitiesDecideTest(algorithm,
				network, IDFactory.decTestName, "yes", 1.0, 0.1532, 0.8468, 0.1532, 0.14, 9.6312,
				-0.0383,-0.2);



		evi = new EvidenceCase();
		try {
			evi.addFinding(network, IDFactory.decTestName, "yes");
			evi.addFinding(network, IDFactory.testResultName, "positive");
		} catch (InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network,
				Arrays.asList(network.getVariable(IDFactory.decTestName),network.getVariable(IDFactory.testResultName)),
				new EvidenceCase(),evi);

		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(
				algorithm, network, 1.0, 1.0, 0.0, 1.0,
				0.8316, 8.1684, -0.25,-0.2);

		try {
			evi.addFinding(network, IDFactory.diseaseName, "present");
		} catch (InvalidStateException | IncompatibleEvidenceException e) {
			printExceptionAndFailIfImplemented(e);
		}

	}
	
	protected void checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(Task algorithm, ProbNet diagram,
																	   double t,
																	   double y1, double y2,
																	   double d, double x,
																	   double uHealthState, double uCostOfTherapy,
																	   double uCostOfTest) {
		Variable variableX = null;
		Variable variableY = null;
		Variable variableT = null;
		Variable variableD = null;
		Variable variableU1 = null;
		Variable variableU2 = null;
		Variable variableU3 = null;

		try {
			variableT = diagram.getVariable(IDFactory.decTestName);
			variableD = diagram.getVariable(IDFactory.therapyName);
			variableX = diagram.getVariable(IDFactory.diseaseName);
			variableY = diagram.getVariable(IDFactory.testResultName);
			variableU1 = diagram.getVariable(IDFactory.healthStateName);
			variableU2 = diagram.getVariable(IDFactory.therapyCostName);
			variableU3 = diagram.getVariable(IDFactory.testCostName);
			
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}

		try {
			//algorithm.setPostResolutionEvidence(evi);
			Map<Variable, TablePotential> aPosterioriProbabilities = null;
			try {
				aPosterioriProbabilities = algorithm.getProbsAndUtilities();
			} catch (UnexpectedInferenceException e) {
				
				e.printStackTrace();
			}
			
			//checkProbabilityPotential(aPosterioriProbabilities, variableX, x);
			//checkProbabilityPotential(aPosterioriProbabilities, variableY, y1,
			//		y2);
			checkProbabilityPotential(aPosterioriProbabilities, variableD, d);
			//checkProbabilityPotential(aPosterioriProbabilities, variableT, t);
			//checkUtilityPotential(aPosterioriProbabilities, variableU1, uHealthState);
			//checkUtilityPotential(aPosterioriProbabilities, variableU2, uCostOfTherapy);
			//checkUtilityPotential(aPosterioriProbabilities, variableU3, uCostOfTest);
		} catch (IncompatibleEvidenceException e) {
			printExceptionAndFailIfImplemented(e);
		}

	}
	


	protected void checkPosteriorProbsAndUtilitiesDecideTest(Task algorithm, ProbNet diagram,
															 String nameVariable, String state,
															 double t, double y1, double y2,
															 double d, double x, double uHealthState,
															 double uCostOfTherapy,double uCostOfTest) {
//		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm, diagram, evi, t, y1, y2, d, x,
//				uHealthState, uCostOfTherapy, uCostOfTest);
		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(algorithm, diagram, t, y1, y2, d, x,
				uHealthState, uCostOfTherapy, uCostOfTest);
	}
	
// TODO Fix the test. Can not perform any operation in a network = null.	
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
//	@Test
	public void testEvaluationIDUniformDiagnosisProblem()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
		ProbNet diagram;

		diagram = iD_UniformDiagnosisProblem;
		//TODO: revise
		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram, null, null, null);
		try {
			// test max expected utility
			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
			assertEquals(10.0, meuEvaluation, maxError);

			// Test optimal policy
			Variable D = diagram.getVariable("D");

			Potential policy = algorithm.getOptimizedPolicy(D);
			assertNotNull(policy);

			// Test the size of the domain of the policy
			List<Variable> domainPolicy = policy.getVariables();
			domainPolicy.remove(D);
			assertEquals(1, domainPolicy.size());

			// Test the optimal choice of the policy
			double[] truePolicy = { 0.5, 0.5, 0.5, 0.5 };

			assertTrue(areEquals(getTablePotential(policy).getValues(), truePolicy));
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}


	//TODO The "diagram" is "null". The test is wrong.
//	@Test
	public void testPreAndPostResolutionEvidenceIDDecisionTestProblem() throws NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException{
		ProbNet diagram = iD_DecisionTestProblemWithSV;
		//TODO: revise
		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram,null,null,null);

		//TODO Test combination of pre and post resolution findings.
		try {
			//Variable variableT = diagram.getVariable("T");
			//Variable variableD = diagram.getVariable("D");

			EvidenceCase preResolutionEvidence = new EvidenceCase();
			preResolutionEvidence.addFinding(diagram,IDFactory.diseaseName,"present");
			algorithm.setPreResolutionEvidence(preResolutionEvidence);
			// test max expected utility
			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
			assertEquals(80.0, meuEvaluation, maxError);

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
	//@Test
	public void testAPrioriProbabilitiesIDTestAlways()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
		ProbNet diagram;

		diagram = IDFactory.buildIDTestAlways();

		//TODO: revise "a priori" ???
		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram, null, null, null);

		Variable variableX = diagram.getVariable(IDFactory.diseaseName);
		assertNotNull(variableX);
		Variable variableY = diagram.getVariable(IDFactory.testResultName);
		assertNotNull(variableY);
		Variable variableD = diagram.getVariable(IDFactory.therapyName);
		assertNotNull(variableD);

		// A priori probabilities
		try {
			Map<Variable, TablePotential> aPrioriProbabilities;
			aPrioriProbabilities = algorithm.getProbsAndUtilities();
			// test potential probabilities
			checkProbabilityPotential(aPrioriProbabilities,variableX,0.14);
			checkProbabilityPotential(aPrioriProbabilities,variableY,0.1532,0.8468);
			checkProbabilityPotential(aPrioriProbabilities, variableD, 0.1532);
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}
	
	protected Intervention getStrategyDiagnosisProblem(
			ProbNet id,
			String resultTestName,
			String decisionName,
			String positiveResult,
			String negativeResult,
			String yesTherapy,
			String noTherapy) throws InvalidStateException{
		Intervention interv;
		List<Variable> vars = new ArrayList<>();
		List<State> states = new ArrayList<>();
		Variable dec = null;
		Variable resultTest = null;
		String statesResultTestNames[] = new String[2];
		String statesTherapyNames[]=new String[2];
		
		statesResultTestNames[0]=positiveResult;
		statesResultTestNames[1]=negativeResult;
		statesTherapyNames[0]=yesTherapy;
		statesTherapyNames[1]=noTherapy;
		try {
			dec = id.getVariable(decisionName);
			resultTest = id.getVariable(resultTestName);
		} catch (NodeNotFoundException e1) {
			e1.printStackTrace();
		}
		vars.add(dec);
		vars.add(resultTest);	
		List<State> statesRoot = new ArrayList<>();
		for (String nameState:statesResultTestNames){
				statesRoot.add(resultTest.getState(nameState));
		}
		
		List<Intervention> interventionsChildren;
		
		interventionsChildren = new ArrayList<>();
		for (String nameState:statesTherapyNames){
			interventionsChildren.add(createSimpleIntervention(id,decisionName,nameState));
		}
			
		
		interv = new Intervention(resultTest, statesRoot, interventionsChildren);
				
		return interv;
	}


	/**
	 * @param network
	 * @return An InferenceAlgorithm for 'network'. If the network is not evaluable
	 * with the algorithm then the test calling this method is skipped.
	 * @throws UnexpectedInferenceException
	 * @throws IncompatibleEvidenceException
	 */
	protected Task buildInferenceTaskAndSkipTestIfNotEvaluable(ProbNet network, List<Variable>  variablesOfInterest,
															   EvidenceCase preResolutionEvidence,
															   EvidenceCase postResolutionEvidence)
			throws IncompatibleEvidenceException, UnexpectedInferenceException {
		boolean isEvaluable;
		Task task = null;

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
