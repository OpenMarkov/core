package org.openmarkov.core.inference;

import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.Resolution;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.factory.IDFactory;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.openmarkov.core.model.network.factory.IDFactory.buildIDTestAlways;
import static org.openmarkov.core.model.network.factory.IDFactory.therapyName;
import static org.openmarkov.core.model.network.factory.NetsFactory.diseaseName;
import static org.openmarkov.core.model.network.factory.NetsFactory.testResultName;

public abstract class InferenceResolutionTaskIDTest extends InferenceResolutionTaskDecTest {

	//protected ProbNet iD_DiagnosisProblem;
	protected ProbNet iD_UniformDiagnosisProblem;
	protected ProbNet iD_DecisionTestProblemWithoutSV;
	protected ProbNet iD_DecisionTestProblemWithSV;

	public InferenceResolutionTaskIDTest() {
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

	protected void testMEU(ProbNet net, double expectedMEU, Intervention expectedStrategy) throws IncompatibleEvidenceException, UnexpectedInferenceException{
		Resolution algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(net);
		Double meuEvaluation = algorithm.getUtility().values[0];
		assertEquals(expectedMEU,meuEvaluation, maxError);
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
	//TODO: check this test and enable it
	public void testEvaluationIDDecisionTestProblem(ProbNet diagram)
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
		Variable variableX = null;
		Variable variableY = null;
		Variable variableT = null;
		Variable variableD = null;
		Variable variableU1 = null;
		Variable variableU2 = null;

		Resolution algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);

		try {
			// test max expected utility

			Double meuEvaluation = algorithm.getUtility().values[0];
			assertEquals(96.006, meuEvaluation, maxError);

			// Test optimal policy
			variableT = getVariableAndAssertNotNull(diagram,"T");
			variableD = getVariableAndAssertNotNull(diagram,"D");

			Potential policyT = algorithm.getOptimalPolicy(variableT);
			Potential policyD = algorithm.getOptimalPolicy(variableD);
			assertNotNull(policyT);
			assertNotNull(policyD);

			// Test the size of the domain of the policy of T
			assertTrue(checkPolicy(getTablePotential(policyT), variableT, 0));

			// Test the size of the domain of the policy of D
			assertTrue(checkPolicy(getTablePotential(policyD), variableD, 2));

			// Test the a priori case
			Map<Variable, TablePotential> aPrioriProbabilities = null; //algorithm.getProbsAndUtilities();
			// Read the variables
			variableX = getVariableAndAssertNotNull(diagram,"X");
			variableY = getVariableAndAssertNotNull(diagram,"Y");
			variableU1 = getVariableAndAssertNotNull(diagram,"U1");
			variableU2 = getVariableAndAssertNotNull(diagram,"U2");



			//euPotT
			TablePotential euPotT = constructExpectedUtilitiesPolicyTDecisionTestProblem(variableT);
			//assertTrue(areEqualPotentials(euPotT,(TablePotential) algorithm.getExpectedUtilities(variableT)));

			//euPotT
			TablePotential euPotD = constructExpectedUtilitiesPolicyDDecisionTestProblem(variableT,variableY,variableD);
			//assertTrue(areEqualPotentials(euPotD,(TablePotential) algorithm.getExpectedUtilities(variableD)));

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

	//@Test
	public void testEvaluationSimpleIDWithoutDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{

		testMEU(IDFactory.createSimpleIDWithoutDecisions(), 83.7);
	}

	/**
	 * @param id
	 * @param decision
	 * @param state
	 * @return An Intervention with the assignment 'decision = state'
	 */
	protected Intervention createSimpleIntervention(ProbNet id, String decision, String state) {
		Intervention interv;
		List<Variable> vars = new ArrayList<>();
		List<State> states = new ArrayList<>();
		Variable dec = null;
		try {
			dec = id.getVariable(decision);
		} catch (NodeNotFoundException e1) {
			e1.printStackTrace();
		}
		vars.add(dec);

		interv = new Intervention(dec, states);
		interv.setRootVariable(dec);
		try {
			states.add(dec.getState(state));
		} catch (InvalidStateException e) {
			e.printStackTrace();
		}
		TreeADDBranch branch = new TreeADDBranch(states, dec, vars);
		interv.addBranch(branch);

		return interv;
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

	protected void testMEU(ProbNet diagram,double expectedMeu) throws IncompatibleEvidenceException, UnexpectedInferenceException{

		Resolution algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);


		// test max expected utility
		Double meuEvaluation = null;
		try {
			meuEvaluation = algorithm.getUtility().values[0];
		} catch (UnexpectedInferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(expectedMeu, meuEvaluation, maxError);


	}

	@Test
	public void testIDOneDecision() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDOneDecision(), 87.4, null);
	}
	
	@Test
	public void testIDPerfectKnowledge() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDPerfectKnowledge(), 9.72, null);
	}
	
	@Test
	public void testIDPerfectKnowledgeCostTherapy() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDPerfectKnowledgeCostTherapy(), 9.685, null);
	}
	
	@Test
	public void testIDNoKnowledge() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDNoKnowledge(), 9.02, null);
	}
	
	@Test
	public void testIDTestAlways() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDTestAlways(), 9.3929, null);
	}
	
	@Test
	public void testIDDecideTest() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(getIDDecideTest(), 9.3929, null);
	}
	
	protected ProbNet getIDDecideTest() {
		return IDFactory.buildIDDecideTest();
	}
	
	@Test
	public void testIDDecideTestSymptom() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDDecideTestSymptom(), 9.9143, null);
	}
	
	@Test
	public void testIDQaleMediastinet() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDQaleMediastinet(), 2.1154194051058286, null);
	}

	@Test
	public void testIDMediastinetWithoutSV() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDMediastinetWithoutSV(), 1.4709741803092176, null);
	}
	
	@Test
	public void testIDMediastinetWithoutMediastinoscopy() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDMediastinetWithoutMediastinoscopy(), 1.5209741803092172, null);
	}

	@Test
	public void testIDMediastinet() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDMediastinet(), 1.4709741803092176, null);
	}
	
	@Test
	public void testIDArthronet() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDArthronet(), 0.4960714549037456, null);
	}
	
	@Test
	public void testIDRedundantChance() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDRedundantChance(), 175.0, null);
	}

	@Test
	public void testIDTwoIndependentDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDTwoIndependentDecisions(), 4.0, null);
	}
	
	@Test
	public void testIDConcatenateOrderTwoDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDConcatenateOrderTwoDecisions(), 8.15, null);
	}
	
	@Test
	public void testIDThreeIndependentDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDThreeIndependentDecisions(), 37.63, null);
	}
	
	@Test
	public void testIDStatesTies() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDStatesTies(), 13.7, null);
	}
	
	@Test
	public void testIDStatesTiesPerfectKnowledge() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDStatesTiesPerfectKnowledge(), 1.5, null);
	}

	@Test
	public void testIDConsecutiveDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testMEU(IDFactory.buildIDConsecutiveDecisions(), 4.57501894, null);
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
	public void testOptimizedPolicyIDPerfectKnowledge()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
		ProbNet network;
		
		network = IDFactory.buildIDPerfectKnowledge();

		Resolution algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network);
		
		try {
			// Test optimal policy
			Variable D = network.getVariable("Therapy");
			
			Potential policy = algorithm.getOptimalPolicy(D);
			assertNotNull(policy);

			// Test the size of the domain of the policy
			List<Variable> domainPolicy = policy.getVariables();
			domainPolicy.remove(D);
			assertEquals(1, domainPolicy.size());
			
			// Test the optimal choice of the policy
			double[] truePolicy = { 1.0, 0.0, 0.0, 1.0 };
			assertTrue(areEquals(getTablePotential(policy).getValues(), truePolicy));
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
	//TODO: this test should be moved, shouldn't it?
	//@Test
	public void testExpectedUtilitiesIDPerfectKnowledge()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
		ProbNet network;
		
		network = IDFactory.buildIDPerfectKnowledge();

		Resolution algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

		try {
			// Test optimal policy
			Variable D = network.getVariable("Therapy");
			
			Potential utilities = null; //algorithm.getExpectedUtilities(D);
			assertNotNull(utilities);

			// Test the size of the domain of the utilities table
			assertEquals(2, utilities.getVariables().size());
			
			Variable X = network.getVariable("Disease");
			
			TablePotential expectedPotential = new TablePotential(Arrays.asList(X,D), PotentialRole.UTILITY);
			double [] values = {10.0,3.0,9.0,8.0};
			expectedPotential.setValues(values);
			
			assertTrue(areEqualPotentials(getTablePotential(utilities), expectedPotential));
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
	protected Resolution buildInferenceTaskAndSkipTestIfNotEvaluable(
			ProbNet network) throws IncompatibleEvidenceException, UnexpectedInferenceException {
		boolean isEvaluable;
		Resolution task = null;

		//If the network is not evaluable then the test is skipped
		isEvaluable = true;
		try {
			task = buildInferenceTask(network);
		} catch (NotEvaluableNetworkException e1) {
			isEvaluable = false;
		}
		assumeTrue(isEvaluable);
		return task;
	}

	protected void setUp() throws Exception {
		// TODO Auto-generated method stub

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
	//TODO: review how to refactor it
	public void testEvaluationIDUniformDiagnosisProblem()
			throws IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException,
			UnexpectedInferenceException, IncompatibleEvidenceException {
		ProbNet diagram;

		diagram = iD_UniformDiagnosisProblem;

		Resolution algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);
		try {
			// test max expected utility
			Double meuEvaluation = algorithm.getUtility().values[0];
			assertEquals(10.0, meuEvaluation, maxError);

			// Test optimal policy
			Variable D = diagram.getVariable("D");

			Potential policy = algorithm.getOptimalPolicy(D);
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
	//TODO: has this test sense here?
	public void testAPrioriProbabilitiesIDTestAlways()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException, UnexpectedInferenceException, IncompatibleEvidenceException {
		ProbNet diagram;

		diagram = buildIDTestAlways();

		Resolution algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);

		Variable variableX = diagram.getVariable(diseaseName);
		assertNotNull(variableX);
		Variable variableY = diagram.getVariable(testResultName);
		assertNotNull(variableY);
		Variable variableD = diagram.getVariable(therapyName);
		assertNotNull(variableD);

		// A priori probabilities
		try {
			HashMap<Variable, TablePotential> aPrioriProbabilities;
			aPrioriProbabilities = null; //(HashMap<Variable, TablePotential>) algorithm.getProbsAndUtilities();
			// test potential probabilities
			checkProbabilityPotential(aPrioriProbabilities,variableX,0.14);
			checkProbabilityPotential(aPrioriProbabilities,variableY,0.1532,0.8468);
			checkProbabilityPotential(aPrioriProbabilities, variableD, 0.1532);
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}
}
