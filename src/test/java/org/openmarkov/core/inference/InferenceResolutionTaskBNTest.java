/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.junit.Ignore;
import org.junit.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.Resolution;
import org.openmarkov.core.inference.tasks.Task;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.factory.BNFactory;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TablePotentialTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/** @author mluque */
/** @author ibermejo */
@Ignore
public abstract class InferenceResolutionTaskBNTest extends InferenceTaskTest {



	/** Default values used in the test for medical problems **/
	static double prevalence = 0.91;
	static double sensitivity = 0.95;
	static double specificity = 0.8;

	/**
	 * Bayesian network with one node (X)
	 */
	ProbNet bN_X;


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



	protected ProbNet bN_Asia;


	public InferenceResolutionTaskBNTest() {
		maxError = 1E-6;

		try {
			bN_X = BNFactory.createBN_X(1.0);
			bN_XY = BNFactory.createBN_XY(prevalence,
					sensitivity, specificity);
			bN_ABC = BNFactory.createBN_ABC();
			bN_XYZ = BNFactory.createBN_XYZ(prevalence,
					sensitivity, specificity, 0.86, 0.89);
			bN_Asia = BNFactory.createBN_Asia();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}



	/**
	 * @throws NodeNotFoundException
	 * Tests the a priori probabilities obtained in the network bN_ABC
	 * @throws UnexpectedInferenceException 
	 * @throws IncompatibleEvidenceException 
	 */
	//@Test
	public void testAPrioriProbabilitiesBN_ABC()
			throws NodeNotFoundException, IncompatibleEvidenceException, UnexpectedInferenceException {
		Task elimination1;
		ProbNet network;

		network = BNFactory.createBN_ABC();

		elimination1 = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

		Variable variableA = getVariableAndAssertNotNull(network,"A"); 
		Variable variableB = getVariableAndAssertNotNull(network,"B");
		Variable variableC = getVariableAndAssertNotNull(network,"C");

		try {
			Map<Variable, TablePotential> aPrioriProbabilities;
			aPrioriProbabilities = elimination1.getProbsAndUtilities();
			checkProbabilityPotential(aPrioriProbabilities,variableA,0.8);
			checkProbabilityPotential(aPrioriProbabilities,variableB,0.14);
			checkProbabilityPotential(aPrioriProbabilities,variableC,0.2784);

		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}

	/**
	 * @throws NodeNotFoundException
	 * Tests the a priori probabilities obtained in the network bN_ABC
	 * @throws UnexpectedInferenceException 
	 * @throws IncompatibleEvidenceException 
	 */
	//@Test
	public void testAPrioriProbabilitiesBN_Asia()
			throws NodeNotFoundException, IncompatibleEvidenceException, UnexpectedInferenceException {

		String namesVariables[]={"A","B","T","L","TOrC","X","D","S"};
		double expectedProbs[] = {0.01,0.45,0.0104,0.055,0.064828,0.11029004,0.3974534,0.5};

		checkVariablesAndProbabilities(bN_Asia,namesVariables,null, null, expectedProbs);

	}


	/**
	 * @param namesVariables
	 * @param expectedProbs
	 * Performs a complete propagation and checks the probabilities obtained
	 * @throws UnexpectedInferenceException 
	 * @throws IncompatibleEvidenceException 
	 */
	private void checkVariablesAndProbabilities(ProbNet network,String[] namesVariables,EvidenceCase preResolutionEvidence,
			EvidenceCase postResolutionEvidence,
			double[] expectedProbs) throws IncompatibleEvidenceException, UnexpectedInferenceException {

		Task elimination = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

		ArrayList<Variable> variables = new ArrayList<Variable>();

		for (int i=0;i<namesVariables.length;i++){
			Variable auxVar = null;
			try {
				auxVar = getVariableAndAssertNotNull(network,namesVariables[i]);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			variables.add(auxVar);
		}

		if (preResolutionEvidence!=null){
			elimination.setPreResolutionEvidence(preResolutionEvidence);
		}
		if (postResolutionEvidence!=null){
			elimination.setPostResolutionEvidence(postResolutionEvidence);
		}

		try {
			Map<Variable, TablePotential> aPosterioriProbs;
			aPosterioriProbs = elimination.getProbsAndUtilities();
			checkProbabilities(aPosterioriProbs,variables,expectedProbs);

		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);

		}
	}





	/**
	 * @throws NodeNotFoundException
	 * Tests the a priori probabilities obtained in the network bN_ABC
	 * @throws UnexpectedInferenceException 
	 * @throws IncompatibleEvidenceException 
	 */
	//@Test
	public void testAPosterioriProbabilitiesBN_Asia()
			throws NodeNotFoundException, IncompatibleEvidenceException, UnexpectedInferenceException {
		ProbNet network;
		int numIter=10;

		for (int i=0;i<numIter; i++){

			network = bN_Asia;

			String namesVariables[]={"A","B","T","L","TOrC","X","D","S"};

			EvidenceCase evidence1 = new EvidenceCase();
			try {
				evidence1.addFinding(network, "T", "absent");
				evidence1.addFinding(network, "TOrC", "yes");
			} catch (InvalidStateException | IncompatibleEvidenceException e1) {
				e1.printStackTrace();
			}

			//A=yes, B=present, T=present, L=present, TOrC=yes, X=yes, D=yes, S=yes
			double expectedProbs[] = {0.00959984, 0.572727, 0.0, 1.0, 1.0, 0.98, 0.85727273, 0.90909091};


			checkVariablesAndProbabilities(network,namesVariables,null, evidence1, expectedProbs);
		}
	}

	//@Test
	public void testAPosterioriProbabilitiesBN_XY() throws Exception {
		ProbNet network;
		double probPositiveX;
		double probNegativeX;

		network = bN_XY;

		Task resolution = buildInferenceTaskAndSkipTestIfNotEvaluable(bN_XY);


		Variable variableX = getVariableAndAssertNotNull(network,"X");
		Variable variableY = getVariableAndAssertNotNull(network,"Y");

		// Test when Y = positive
		EvidenceCase evidence1 = new EvidenceCase();
		evidence1.addFinding(network, "Y", "positive");
		Map<Variable, TablePotential> yPositiveProbabilities;
		resolution.setPostResolutionEvidence(evidence1);

		probPositiveX = prevalence * sensitivity;
		probNegativeX = (1-prevalence)*(1-specificity);
		double alpha = probPositiveX + probNegativeX;
		probPositiveX = probPositiveX / alpha; 
		try {
			yPositiveProbabilities = resolution.getProbsAndUtilities();
			checkProbabilityPotential(yPositiveProbabilities,variableX,0.9796034);
			checkProbabilityPotential(yPositiveProbabilities,variableY,1.0);
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}

	}


	/**
	 * @param network
	 * @param variableName
	 * @return The variable in 'network' whose name is 'variableName'. It also checks whether the variable is not null.
	 * @throws NodeNotFoundException	 
	 */
	public static Variable getVariableAndAssertNotNull(ProbNet network, String variableName) throws NodeNotFoundException {
		Variable variable;

		variable = network.getVariable(variableName);
		assertNotNull(variable);
		return variable;
	}



	/**
	 * @throws IncompatibleEvidenceException
	 * Tests if the inference on a network with a deterministic variable throws IncompatibleEvidenceException
	 * if there is evidence on the state whose probability is 0.
	 * @throws UnexpectedInferenceException 
	 */
	//@Test (expected = IncompatibleEvidenceException.class)
	public void testIncompatibleEvidenceBN_X() throws IncompatibleEvidenceException, UnexpectedInferenceException {
		ProbNet network;

		network = bN_X;

		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

		// Test when X = absent, which is incompatible evidence
		EvidenceCase evidence = new EvidenceCase();
		try {
			evidence.addFinding(network, "X", "absent");
		} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			//e.printStackTrace();
		} 

		algorithm.setPostResolutionEvidence(evidence);

		try {
			algorithm.getProbsAndUtilities();
		} catch (UnexpectedInferenceException e) {
			printExceptionAndFailIfImplemented(e);
		}

	}

	/**
	 * @throws IncompatibleEvidenceException
	 * Tests if the inference on the network XY with the probability of Y=absent equal to 0.0
	 * for any state in X, throws IncompatibleEvidenceException if the evidence is Y=absent.
	 */

	//@Test (expected = IncompatibleEvidenceException.class)
	public void testIncompatibleEvidenceBN_XY() throws IncompatibleEvidenceException {
		ProbNet network = null;

		try {
			network = BNFactory.createBN_XY(0.5,1.0,0.0);
			Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

			// Test when Y = absent, which is incompatible evidence
			EvidenceCase evidence = new EvidenceCase();
			try {
				evidence.addFinding(network, "Y", "negative");
			} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
				System.err.println(e.getMessage());
				e.printStackTrace(System.err);
			} 

			algorithm.setPostResolutionEvidence(evidence);

			try {
				algorithm.getProbsAndUtilities();
			} catch (UnexpectedInferenceException e) {
				System.err.println(e.getMessage());
				e.printStackTrace(System.err);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}


	}

	/**
	 * @throws IncompatibleEvidenceException
	 * Tests if the inference on the network Asia throws IncompatibleEvidenceException
	 * if the evidence is (T=absent,L=absent,TOrC=yes).
	 * @throws UnexpectedInferenceException 
	 */

	//@Test (expected = IncompatibleEvidenceException.class)
	public void testIncompatibleEvidenceBN_Asia() throws IncompatibleEvidenceException, UnexpectedInferenceException {
		ProbNet network = null;

		try {
			network = bN_Asia;
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

		// Test when Y = absent, which is incompatible evidence
		EvidenceCase evidence = new EvidenceCase();
		try {
			evidence.addFinding(network, "T", "absent");
			evidence.addFinding(network, "L", "absent");
			evidence.addFinding(network, "TOrC", "yes");
		} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			//e.printStackTrace();
		} 

		algorithm.setPostResolutionEvidence(evidence);

		try {
			algorithm.getProbsAndUtilities();
		} catch (UnexpectedInferenceException e) {
			printExceptionAndFailIfImplemented(e);
		}

	}


	/**
	 * @throws Exception
	 * Tests the a priori probabilities in the network 'bN_XY'
	 */
	//@Test
	public void testAPrioriProbabilitiesBN_XY() throws Exception {
		ProbNet network;
		double probPositiveY;


		network = bN_XY;
		Task elimination1 = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

		// A priori probabilities
		try {
			Map<Variable, TablePotential> aPrioriProbabilities;
			aPrioriProbabilities = elimination1.getProbsAndUtilities();
			// Read the variables
			Variable variableX = getVariableAndAssertNotNull(network,"X");
			Variable variableY = getVariableAndAssertNotNull(network,"Y");

			// test potential probabilities
			checkProbabilityPotential(aPrioriProbabilities,variableX,prevalence);
			probPositiveY = prevalence * sensitivity + (1.0 - prevalence)
					* (1.0 - specificity);
			checkProbabilityPotential(aPrioriProbabilities,variableY,probPositiveY);
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}

	/**
	 * @throws Exception
	 * Tests the a priori joint probability in the network 'bN_XY'
	 */
	//@Test
	public void testAPrioriJointProbabilityBN_XY() throws Exception {
		ProbNet network;
		TablePotential expectedPot;

		network = bN_XY;
		Task elimination1 = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

		// A priori probabilities
		try {
			// Read the variables
			Variable variableX = getVariableAndAssertNotNull(network,"X");
			Variable variableY = getVariableAndAssertNotNull(network,"Y");
			ArrayList<Variable> variables;
			variables = new ArrayList<>();
			variables.add(variableX);
			variables.add(variableY);
			TablePotential jointProbability = elimination1.getJointProbability(variables);
			expectedPot = new TablePotential(variables,PotentialRole.JOINT_PROBABILITY);			
			double []expectedValues = {0.8645,0.018,0.0455,0.072};
			expectedPot.setValues(expectedValues);
			TablePotentialTest.checkEqualPotentials(jointProbability,expectedPot, maxError);
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}

	/**
	 * @throws NodeNotFoundException
	 * Tests the a priori joint probabilities obtained in the network Asia
	 * @throws UnexpectedInferenceException 
	 * @throws IncompatibleEvidenceException 
	 */
	//@Test
	public void testAPosterioriJointProbabilitiesBN_Asia ()
			throws NodeNotFoundException,
			IncompatibleEvidenceException,
			UnexpectedInferenceException
	{
		ProbNet network = bN_Asia;
		TablePotential expectedPot;
		Task Task = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

		EvidenceCase evidence = new EvidenceCase ();
		try
		{
			// Read the variables
			Variable variableT = getVariableAndAssertNotNull(network,"T");
			Variable variableTOrC = getVariableAndAssertNotNull(network,"TOrC");
			Variable variableL = getVariableAndAssertNotNull(network,"L");

			// T=absent, TOrC=yes
			evidence.addFinding (network, "T", "absent");
			evidence.addFinding (network, "TOrC", "yes");
			Task.setPreResolutionEvidence (evidence);
			ArrayList<Variable> variables = new ArrayList<> ();
			variables.add (variableTOrC);
			variables.add (variableL);
			variables.add (variableT);
			expectedPot = new TablePotential(variables,PotentialRole.JOINT_PROBABILITY);            
			double[] expectedValues = {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0};
			expectedPot.setValues(expectedValues);
			TablePotential jointProbability = Task.getJointProbability (variables);
			TablePotentialTest.checkEqualPotentials(jointProbability,expectedPot,maxError);
		}
		catch (InvalidStateException | IncompatibleEvidenceException e1)
		{
			e1.printStackTrace ();
		}
	}

	/**
	 * @throws Exception
	 * Tests the a priori joint probability in the network 'bN_ABC'
	 */
	//@Test
	public void testAPrioriJointProbabilityBN_ABC() throws Exception {
		ProbNet network;
		TablePotential expectedPot;

		network = bN_ABC;
		Task elimination1 = buildInferenceTaskAndSkipTestIfNotEvaluable(network);

		// A priori probabilities
		try {
			// Read the variables
			Variable variableA = getVariableAndAssertNotNull(network,"A");
			Variable variableB = getVariableAndAssertNotNull(network,"B");
			Variable variableC = getVariableAndAssertNotNull(network,"C");
			ArrayList<Variable> variables;
			variables = new ArrayList<>();
			variables.add(variableC);
			variables.add(variableB);
			variables.add(variableA);
			TablePotential jointProbability = elimination1.getJointProbability(variables);
			expectedPot = new TablePotential(variables,PotentialRole.JOINT_PROBABILITY);			
			double []expectedValues = {0.0016,0.0784,0.1152,0.6048,0.0426,0.0174,0.119,0.021};
			expectedPot.setValues(expectedValues);
			TablePotentialTest.checkEqualPotentials(jointProbability,expectedPot,maxError);
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}




	//@Test
	public void testAPosterioriProbabilitiesBN_ABC() throws Exception {
		ProbNet network;

		network = bN_ABC;

		Task algorithm1 = buildInferenceTaskAndSkipTestIfNotEvaluable(network);
		Variable variableA = getVariableAndAssertNotNull(network,"A");
		Variable variableB = getVariableAndAssertNotNull(network,"B");
		Variable variableC = getVariableAndAssertNotNull(network,"C");

		// A = absent
		EvidenceCase evidence1 = new EvidenceCase();
		evidence1.addFinding(network, "A", "absent");
		Map<Variable, TablePotential> aAbsentProbabilities;
		algorithm1.setPostResolutionEvidence(evidence1);
		try {
			aAbsentProbabilities = algorithm1.getProbsAndUtilities();
			checkProbabilityPotential(aAbsentProbabilities,variableA,0.0);
			checkProbabilityPotential(aAbsentProbabilities,variableB,0.3);
			checkProbabilityPotential(aAbsentProbabilities,variableC,0.808);
		} catch (Exception e) {
			fail("testAPosterioriProbabilitiesBN_ABC().\nException: "
					+ e.getMessage());
		}

		// A = present
		EvidenceCase evidence2 = new EvidenceCase();
		evidence2.addFinding(network, "A", "present");
		Map<Variable, TablePotential> aPresentProbabilities;
		Task algorithm2 = buildInferenceTaskAndSkipTestIfNotEvaluable(network);
		algorithm2.setPostResolutionEvidence(evidence2);
		try {
			aPresentProbabilities = algorithm2.getProbsAndUtilities();
			checkProbabilityPotential(aPresentProbabilities,variableA,1.0);
			checkProbabilityPotential(aPresentProbabilities,variableB,0.1);
			checkProbabilityPotential(aPresentProbabilities,variableC,0.146);
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}




	/**
	 * @throws Exception
	 * Tests the introduction of pre and post-resolution evidence in bN_ABC.
	 */
	//@Test
	public void testPreAndPostResolutionAPosterioriProbabilitiesBN_ABC() throws Exception {
		ProbNet network;

		network = bN_ABC;

		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network);
		Variable variableA = getVariableAndAssertNotNull(network,"A");
		Variable variableB = getVariableAndAssertNotNull(network,"B");
		Variable variableC = getVariableAndAssertNotNull(network,"C");

		// Test when A = absent
		EvidenceCase evidence1 = new EvidenceCase();
		evidence1.addFinding(network, "A", "absent");
		Map<Variable, TablePotential> aAbsentProbabilities;
		algorithm.setPreResolutionEvidence(evidence1);
		try {
			aAbsentProbabilities = algorithm.getProbsAndUtilities();
			checkProbabilityPotential(aAbsentProbabilities,variableA,0.0);
			checkProbabilityPotential(aAbsentProbabilities,variableB,0.3);
			checkProbabilityPotential(aAbsentProbabilities,variableC,0.808);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		//Test when B = present
		EvidenceCase postEvidence = new EvidenceCase();
		postEvidence.addFinding(bN_ABC, "B", "present");
		algorithm.setPostResolutionEvidence(postEvidence);
		try {
			aAbsentProbabilities = algorithm.getProbsAndUtilities();
			checkProbabilityPotential(aAbsentProbabilities,variableA,0.0);
			checkProbabilityPotential(aAbsentProbabilities,variableB,1.0);
			checkProbabilityPotential(aAbsentProbabilities,variableC,0.71);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		//Test when B = present and invoke getProbsAndUtilities for variable C
		List<Variable> variablesOfInterest = new ArrayList<Variable>();
		variablesOfInterest = new ArrayList<Variable>();
		variablesOfInterest.add(variableC);
		try {
			aAbsentProbabilities = algorithm.getProbsAndUtilities(variablesOfInterest);
			checkProbabilityPotential(aAbsentProbabilities,variableC,0.71);
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}

	//@Test
	public void testAPosterioriProbabilitiesBN_XYZ() throws Exception {
		ProbNet network;
		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(bN_XYZ);

		network = bN_XYZ;
		Variable variableX = getVariableAndAssertNotNull(network,"X");
		Variable variableY = getVariableAndAssertNotNull(network,"Y");
		Variable variableZ = getVariableAndAssertNotNull(network,"Z");

		// Test when Y = positive
		EvidenceCase evidence1 = new EvidenceCase();
		evidence1.addFinding(bN_XYZ, "Y", "positive");
		Map<Variable, TablePotential> yPositiveProbabilities;
		algorithm.setPostResolutionEvidence(evidence1);
		try {
			yPositiveProbabilities = algorithm.getProbsAndUtilities();
			checkProbabilityPotential(yPositiveProbabilities,variableX,0.9796034);
			checkProbabilityPotential(yPositiveProbabilities,variableY,1.0);
			checkProbabilityPotential(yPositiveProbabilities,variableZ,0.86);
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
	 *//*
	@Test
	public void testConditioningVariablesEvaluationIDDiagnosisProblem()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		ProbNet network;

		network = iD_DiagnosisProblem;

		//Decision criteria variable
		String dCStates[]= {"Health","Money"};
		Variable variableDC = new Variable("DC",dCStates);

		network.getPotentials(network.getVariable("U")).get(0).addVariable(variableDC);

		Task algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(iD_DiagnosisProblem);

		try {
			// test max expected utility
			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
			assertEquals(96.006, meuEvaluation, maxError);

			// Test optimal policy
			Variable D = network.getVariable("D");
			HashMap<Variable, TablePotential> optimalStrategy = algorithm.getOptimizedPolicies();
			TablePotential policy = optimalStrategy.get(D);
			assertNotNull(policy);

			// Test the size of the domain of the policy
			ArrayList<Variable> domainPolicy = policy.getVariables();
			domainPolicy.remove(D);
			assertEquals(1, domainPolicy.size());

			// Test the optimal choice of the policy
			double[] truePolicy = { 1.0, 0.0, 0.0, 1.0 };
			assertTrue(areEquals(policy.getValues(), truePolicy));
		} catch (Exception e) {
			printExceptionAndFail(e);
		}
	}
	  */

}