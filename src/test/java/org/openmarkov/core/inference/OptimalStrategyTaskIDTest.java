/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.UnexpectedInferenceException;

public abstract class OptimalStrategyTaskIDTest extends OptimalStrategyTaskDecTest {

/*	protected void testOptimalStrategy(ProbNet net,Intervention expectedStrategy) throws IncompatibleEvidenceException, UnexpectedInferenceException{
		OptimalStrategy algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(net);
		testScenariosIntervention(net,algorithm);
	}

	*//**
	 * Checks that the Intervention (optimal strategy) obtained from the evaluation optimal is not null
	 * and that it is consistent with the Cooper policy network (CPN) built using the policies obtained
	 * from the method getOptimizedPolicies
	 * @param net
	 * @param algorithm
	 * @throws IncompatibleEvidenceException
	 * @throws UnexpectedInferenceException
	 *//*
	private void testScenariosIntervention(ProbNet net,
										   OptimalStrategy algorithm) throws IncompatibleEvidenceException, UnexpectedInferenceException {
		Intervention interv = null;
		try {
			interv = algorithm.getOptimalIntervention();
		} catch (IncompatibleEvidenceException | UnexpectedInferenceException e) {
			e.printStackTrace();
		}
		assertNotNull(interv);
		testIntervention(algorithm, interv, new EvidenceCase());


	}

	*//**
	 * Checks that the Intervention 'interv' rooted at the evidence scenario 'parentEvi' is consistent with the
	 * Cooper policy network (CPN) that has been obtained by the inference algorithm 'algorithm'
	 * The  method checks correctness and completeness:
	 * - Correctness: Every scenario in the intervention has non-zero probability in the CPN
	 * - Completeness: The Intervention covers all the non-zero probability states of the CPN
	 * @param algorithm
	 * @param interv
	 * @param parentEvi
	 * @throws IncompatibleEvidenceException
	 * @throws UnexpectedInferenceException
	 *//*
	private void testIntervention(OptimalStrategy algorithm, Intervention interv, EvidenceCase parentEvi)
			throws IncompatibleEvidenceException, UnexpectedInferenceException {

		if (interv != null) {
			interv.getRootVariable();
			List<TreeADDBranch> branches = interv.getBranches();
			Variable rootVariable = interv.getRootVariable();
			algorithm.setPostResolutionEvidence(parentEvi);
			List<Variable> interestVariables = new ArrayList<>();
			interestVariables.add(rootVariable);
			//TablePotential probs = algorithm.getProbsAndUtilities().get(rootVariable);
			TablePotential probs = algorithm.getProbability();
			if (branches != null) {
				// Check that the number of branches is equal to the non-zero
				// probability states
				assertEquals(getNumStatesBranches(branches), getNumProbsNotZero(probs));
				for (int i = 0; i < branches.size(); i++) {
					TreeADDBranch auxBranch = branches.get(i);
					Intervention auxInterventionBranch = Intervention.getInterventionBranch(auxBranch);
					for (State state : auxBranch.getStates()) {
						// Check that 'state' has non-zero probability in the
						// CPN
						assertTrue(probs.values[rootVariable.getStateIndex(state)] > 0);
						EvidenceCase newEvi = new EvidenceCase(parentEvi.getFindings());

						Finding finding = new Finding(rootVariable, state);
						try {
							newEvi.addFinding(finding);
						} catch (InvalidStateException e) {
							e.printStackTrace();
						}
						testIntervention(algorithm, auxInterventionBranch, newEvi);
					}
				}
			}
		}
	}

	*//**
	 * @param branches
	 * @return The total number of states in 'branches'
	 *//*
	private int getNumStatesBranches(List<TreeADDBranch> branches) {
		int numStates = 0;
		Set<State> states = new HashSet<State>();
		if (branches != null){
			for (int i = 0; i < branches.size(); i++) {
				TreeADDBranch auxBranch = branches.get(i);
				if (auxBranch != null){
					states.addAll(auxBranch.getStates());
				}
			}
		}
		numStates = states.size();
		return numStates;
	}

	*//**
	 * @param probs
	 * @return The number of values in the potential that are greater than zero
	 *//*
	private int getNumProbsNotZero(TablePotential probs) {
		int numNotZero = 0;
		double[] values = probs.values;
		for (double value : values) {
			if (value > 0.0) {
				numNotZero = numNotZero + 1;
			}
		}
		return numNotZero;
	}

	*//**
	 * @param id
	 * @param decision
	 * @param state
	 * @return An Intervention with the assignment 'decision = state'
	 *//*
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

	protected void testMEU(ProbNet diagram,double expectedMeu) throws IncompatibleEvidenceException, UnexpectedInferenceException{

		OptimalStrategy algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);


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

	@Test
	public void testIDOneDecision() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDOneDecision(), null);
	}

	@Test
	public void testIDPerfectKnowledge() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDPerfectKnowledge(), null);
	}

	@Test
	public void testIDPerfectKnowledgeCostTherapy() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDPerfectKnowledgeCostTherapy(), null);
	}

	@Test
	public void testIDNoKnowledge() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDNoKnowledge(), null);
	}

	@Test
	public void testIDTestAlways() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDTestAlways(), null);
	}

	@Test
	public void testIDDecideTest() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(getIDDecideTest(), null);
	}

	protected ProbNet getIDDecideTest() {
		return IDFactory.buildIDDecideTest();
	}

	@Test
	public void testIDDecideTestSymptom() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDDecideTestSymptom(), null);
	}

	@Test
	public void testIDQaleMediastinet() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDQaleMediastinet(), null);
	}

	@Test
	public void testIDMediastinetWithoutSV() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDMediastinetWithoutSV(), null);
	}

	@Test
	public void testIDMediastinetWithoutMediastinoscopy() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDMediastinetWithoutMediastinoscopy(), null);
	}

	@Test
	public void testIDMediastinet() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDMediastinet(), null);
	}

	@Test
	public void testIDArthronet() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDArthronet(), null);
	}

	@Test
	public void testIDRedundantChance() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDRedundantChance(), null);
	}

	@Test
	public void testIDTwoIndependentDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDTwoIndependentDecisions(), null);
	}

	@Test
	public void testIDConcatenateOrderTwoDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDConcatenateOrderTwoDecisions(), null);
	}

	@Test
	public void testIDThreeIndependentDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDThreeIndependentDecisions(), null);
	}

	@Test
	public void testIDStatesTies() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDStatesTies(), null);
	}

	@Test
	public void testIDStatesTiesPerfectKnowledge() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDStatesTiesPerfectKnowledge(), null);
	}

	@Test
	public void testIDConsecutiveDecisions() throws IncompatibleEvidenceException, UnexpectedInferenceException{
		testOptimalStrategy(IDFactory.buildIDConsecutiveDecisions(), null);
	}

	*//**
	 * @param network
	 * @return An InferenceAlgorithm for 'network'. If the network is not evaluable
	 * with the algorithm then the test calling this method is skipped.
	 * @throws UnexpectedInferenceException
	 * @throws IncompatibleEvidenceException
	 *//*
	protected OptimalStrategy buildInferenceTaskAndSkipTestIfNotEvaluable(
			ProbNet network) throws IncompatibleEvidenceException, UnexpectedInferenceException {
		boolean isEvaluable;
		OptimalStrategy task = null;

		//If the network is not evaluable then the test is skipped
		isEvaluable = true;
		try {
			task = buildInferenceTask(network, new EvidenceCase());
		} catch (NotEvaluableNetworkException e1) {
			isEvaluable = false;
		}
		assumeTrue(isEvaluable);
		return task;
	}*/

}