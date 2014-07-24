package org.openmarkov.core.inference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.factory.IDFactory;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

public abstract class InferenceAlgorithmIDTest  extends InferenceAlgorithmDecTest {

	//protected ProbNet iD_DiagnosisProblem;
	protected ProbNet iD_UniformDiagnosisProblem;
	protected ProbNet iD_DecisionTestProblemWithoutSV;
	protected ProbNet iD_DecisionTestProblemWithSV;
	
	
	
	public InferenceAlgorithmIDTest () {
	/*iD_DiagnosisProblem = NetsFactory
	.createInfluenceDiagramDiagnosisProblem();*/
/*iD_UniformDiagnosisProblem = IDFactory
	.createUniformInfluenceDiagramDiagnosisProblem();
iD_DecisionTestProblemWithoutSV = IDFactory
	.createInfluenceDiagramDecisionTestProblemWithoutSV(0.07, 0.91,
			0.97);
iD_DecisionTestProblemWithSV = IDFactory
	.createInfluenceDiagramDecisionTestProblem(0.07, 0.91, 0.97);*/

	}

	@Test
	public void testIDOneDecision() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDOneDecision(),87.4,null);	
	}
	
	@Test
	public void testIDPerfectKnowledge() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDPerfectKnowledge(),9.72,null);	
	}
	
	@Test
	public void testIDPerfectKnowledgeCostTherapy() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDPerfectKnowledgeCostTherapy(),9.685,null);	
	}
	
	@Test
	public void testIDNoKnowledge() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDNoKnowledge(),9.02,null);	
	}
	
	@Test
	public void testIDTestAlways() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDTestAlways(),9.3929,null);	
	}
	
	@Test
	public void testIDDecideTest() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDDecideTest(),9.3929,null);	
	}
	
	@Test
	public void testIDDecideTestSymptom() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDDecideTestSymptom(),9.9143,null);	
	}
	
	@Test
	public void testIDQaleMediastinet() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDQaleMediastinet(),2.1154194051058286,null);	
	}
	
	

	@Test
	public void testIDMediastinetWithoutSV() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDMediastinetWithoutSV(),1.4709741803092176,null);	
	}
	
	@Test
	public void testIDMediastinetWithoutMediastinoscopy() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDMediastinetWithoutMediastinoscopy(),1.5209741803092172,null);	
	}
	
	
	@Test
	public void testIDMediastinet() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDMediastinet(),1.4709741803092176,null);	
	}
	
	@Test
	public void testIDArthronet() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEUAndStrategy(IDFactory.buildIDArthronet(),0.4960714549037456,null);	
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
	@Test
	public void testPosteriorProbsAndUtilsIDDecisionTestProblem()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		ProbNet network = IDFactory.buildIDDecideTest();
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
		
		checkPosteriorProbsAndUtilitiesDecideTest(algorithm,
				network, IDFactory.therapyName, "no", 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
				9.895843174303259, 0.0,-0.2);
		checkPosteriorProbsAndUtilitiesDecideTest(algorithm,
				network, IDFactory.testResultName, "negative", 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
				9.895843174303259, 0.0,-0.2);
		checkPosteriorProbsAndUtilitiesDecideTest(algorithm,
				network, IDFactory.decTestName, "yes", 1.0, 0.1532, 0.8468, 0.1532, 0.14, 9.6312,
				-0.0383,-0.2);

		EvidenceCase evi = new EvidenceCase();
		try {
			evi.addFinding(network, IDFactory.decTestName, "yes");
			evi.addFinding(network, IDFactory.testResultName, "positive");
		} catch (InvalidStateException e) {
			e.printStackTrace();
		} catch (IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(
				algorithm, network, evi, 1.0, 1.0, 0.0, 1.0,
				0.8316, 8.1684, -0.25,-0.2);
		try {
			evi.addFinding(network,  IDFactory.diseaseName, "present");

		} catch (InvalidStateException | IncompatibleEvidenceException e) {
			
			printExceptionAndFailIfImplemented(e);
		}	

	}
	
	protected void checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(
			InferenceAlgorithm algorithm, ProbNet diagram,
			EvidenceCase evi, double t, double y1, double y2, double d,
			double x, double  uHealthState,  double uCostOfTherapy, double uCostOfTest) {

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
			algorithm.setPostResolutionEvidence(evi);
			HashMap<Variable, TablePotential> aPosterioriProbabilities = null;
			try {
				aPosterioriProbabilities = algorithm.getProbsAndUtilities();
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
	


	protected void checkPosteriorProbsAndUtilitiesDecideTest(
			InferenceAlgorithm algorithm, ProbNet diagram,
			String nameVariable, String state, double t, double y1, double y2,
			double d, double x, double uHealthState, double uCostOfTherapy,double uCostOfTest) {
		EvidenceCase evi;
		evi = new EvidenceCase();
		try {
			evi.addFinding(diagram, nameVariable, state);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidStateException e) {
			e.printStackTrace();
		} catch (IncompatibleEvidenceException e) {
			e.printStackTrace();
		}
		checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(
				algorithm, diagram, evi, t, y1, y2, d, x, uHealthState, uCostOfTherapy, uCostOfTest);

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
	public void testEvaluationIDUniformDiagnosisProblem()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		ProbNet diagram;

		diagram = iD_UniformDiagnosisProblem;

		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
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
	

	//@Test
	//TODO Review the minor error in test
	public void testPreAndPostResolutionEvidenceIDDecisionTestProblem() throws NotEvaluableNetworkException{
		ProbNet diagram = iD_DecisionTestProblemWithSV;
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
		
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
	 */
/*	@Test
	public void testEvaluationIDDiagnosisProblem()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		ProbNet network;
		
		network = IDFactory.createInfluenceDiagramDiagnosisProblem();
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
		
		try {
			// test max expected utility
			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
			assertEquals(96.006, meuEvaluation, maxError);

			// Test optimal policy
			Variable D = network.getVariable("D");
			
			Potential policy = algorithm.getOptimizedPolicy(D);
			assertNotNull(policy);

			// Test the size of the domain of the policy
			List<Variable> domainPolicy = policy.getVariables();
			domainPolicy.remove(D);
			assertEquals(1, domainPolicy.size());
			
			Intervention optimalStrategy = algorithm.getOptimalStrategy();
			assertTrue(optimalStrategy.equals(getExpectedStrategyDiagnosisProblem(network,
					"Y","D","positive","negative","yes","no")));
			int i = 0;
			// Test the optimal choice of the policy
			double[] truePolicy = { 1.0, 0.0, 0.0, 1.0 };
			//TODO Uncomment next line to check policy
			//assertTrue(areEquals(getTablePotential(policy).getValues(), truePolicy));
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}*/
	
	
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
	@Test
	public void testAPrioriProbabilitiesIDTestAlways()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		ProbNet diagram;
		
		diagram = IDFactory.buildIDTestAlways();
				
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
		
		Variable variableX = diagram.getVariable(IDFactory.diseaseName);
		assertNotNull(variableX);
		Variable variableY = diagram.getVariable(IDFactory.testResultName);
		assertNotNull(variableY);
		Variable variableD = diagram.getVariable(IDFactory.therapyName);
		assertNotNull(variableD);

		// A priori probabilities
		try {
			HashMap<Variable, TablePotential> aPrioriProbabilities;
			aPrioriProbabilities = algorithm.getProbsAndUtilities();
			// test potential probabilities
			checkProbabilityPotential(aPrioriProbabilities,variableX,0.14);
			checkProbabilityPotential(aPrioriProbabilities,variableY,0.1532,0.8468);
			checkProbabilityPotential(aPrioriProbabilities,variableD,0.1532);
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}

	
	
	protected Intervention getStrategyDiagnosisProblem(ProbNet id,
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

	
}
