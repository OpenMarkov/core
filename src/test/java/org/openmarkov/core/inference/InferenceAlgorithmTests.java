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
import static org.junit.Assume.assumeTrue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.EvidenceCase;
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

	private ProbNet iD_DiagnosisProblem;
	private ProbNet iD_UniformDiagnosisProblem;
	private ProbNet iD_DecisionTestProblemWithoutSV;
	private ProbNet iD_DecisionTestProblemWithSV;

 
	@Before
	public void setUp() throws Exception {

		bN_X = NetsFactory.createBN_X(1.0);
		bN_XY = NetsFactory.createBN_XY(prevalence,
				sensitivity, specificity);
		bN_ABC = NetsFactory.createBN_ABC();
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
	
	
 
	
	
	/**
	 * @param probNet
	 * @return
	 * @throws NotEvaluableNetworkException
	 * Builds an InferenceAlgorithm object with 'probNet'.
	 * This method must be implemented by each inference test class.
	 */
	public abstract InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet) throws NotEvaluableNetworkException;
	
	/**
	 * @throws ProbNodeNotFoundException
	 * Tests the a priori probabilities obtained in the network bN_ABC
	 */
	@Test
	public void testAPrioriProbabilitiesBN_ABC()
			throws ProbNodeNotFoundException {
		InferenceAlgorithm elimination1;
		ProbNet network;
		
		network = bN_ABC;
			
		elimination1 = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
		
		Variable variableA = getVariableAndAssertNotNull(network,"A"); 
		Variable variableB = getVariableAndAssertNotNull(network,"B");
		Variable variableC = getVariableAndAssertNotNull(network,"C");

		try {
			HashMap<Variable, TablePotential> aPrioriProbabilities;
			aPrioriProbabilities = elimination1.getProbsAndUtilities();
			checkProbabilityPotential(aPrioriProbabilities,variableA,0.8);
			checkProbabilityPotential(aPrioriProbabilities,variableB,0.14);
			checkProbabilityPotential(aPrioriProbabilities,variableC,0.2784);
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	@Test
	public void testAPosterioriProbabilitiesBN_XY() throws Exception {
		ProbNet network;
		double probPositiveX;
		double probNegativeX;
		
		network = bN_XY;
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(bN_XY);
		
		
		Variable variableX = getVariableAndAssertNotNull(network,"X");
		Variable variableY = getVariableAndAssertNotNull(network,"Y");
		
		// Test when Y = positive
		EvidenceCase evidence1 = new EvidenceCase();
		evidence1.addFinding(network, "Y", "positive");
		HashMap<Variable, TablePotential> yPositiveProbabilities;
		algorithm.setPostResolutionEvidence(evidence1);

		probPositiveX = prevalence * sensitivity;
		probNegativeX = (1-prevalence)*(1-specificity);
		double alpha = probPositiveX + probNegativeX;
		probPositiveX = probPositiveX / alpha; 
		try {
			yPositiveProbabilities = algorithm.getProbsAndUtilities();
			checkProbabilityPotential(yPositiveProbabilities,variableX,0.9796034);
			checkProbabilityPotential(yPositiveProbabilities,variableY,1.0);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}


	/**
	 * @param network
	 * @param variableName
	 * @return The variable in 'network' whose name is 'variableName'. It also checks whether the variable is not null.
	 * @throws ProbNodeNotFoundException	 
	 */
	private Variable getVariableAndAssertNotNull(ProbNet network, String variableName) throws ProbNodeNotFoundException {
		Variable variable;
		
		variable = network.getVariable(variableName);
		assertNotNull(variable);
		return variable;
	}





	/**
	 * @param network
	 * @return An InferenceAlgorithm for 'network'. If the network is not evaluable
	 * with the algorithm then the test calling this method is skipped.
	 */
	private InferenceAlgorithm buildInferenceAlgorithmAndSkipTestIfNotEvaluable(
			ProbNet network) {
		boolean isEvaluable;
		InferenceAlgorithm algorithm = null;
		
		//If the network is not evaluable then the test is skipped
		isEvaluable = true;
		try {
			algorithm = buildInferenceAlgorithm(network);
		} catch (NotEvaluableNetworkException e1) {
			isEvaluable = false;
		}
		assumeTrue(isEvaluable);
		return algorithm;
	}


	/**
	 * @throws IncompatibleEvidenceException
	 * Tests if the inference on a network with a deterministic variable throws IncompatibleEvidenceException
	 * if there is evidence on the state whose probability is 0.
	 */
	@Test (expected = IncompatibleEvidenceException.class)
	public void testIncompatibleEvidenceBN_X() throws IncompatibleEvidenceException {
		ProbNet network;
				
		network = bN_X;
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
				
		// Test when X = absent, which is incompatible evidence
		EvidenceCase evidence = new EvidenceCase();
		try {
			evidence.addFinding(network, "X", "absent");
		} catch (ProbNodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			//e.printStackTrace();
		} 
		
		algorithm.setPostResolutionEvidence(evidence);
		
		try {
			algorithm.getProbsAndUtilities();
		} catch (NotEnoughMemoryException | UnexpectedInferenceException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @throws IncompatibleEvidenceException
	 * Tests if the inference on the network XY with the probability of Y=absent equal to 0.0
	 * for any state in X, throws IncompatibleEvidenceException if the evidence is Y=absent.
	 */

	@Test (expected = IncompatibleEvidenceException.class)
	public void testIncompatibleEvidenceBN_XY() throws IncompatibleEvidenceException {
		ProbNet network = null;
				
		try {
			network = NetsFactory.createBN_XY(0.5,1.0,0.0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
				
		// Test when Y = absent, which is incompatible evidence
		EvidenceCase evidence = new EvidenceCase();
		try {
			evidence.addFinding(network, "Y", "negative");
		} catch (ProbNodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			//e.printStackTrace();
		} 
		
		algorithm.setPostResolutionEvidence(evidence);
		
		try {
			algorithm.getProbsAndUtilities();
		} catch (NotEnoughMemoryException | UnexpectedInferenceException e) {
			e.printStackTrace();
		}

	}



	/**
	 * @throws Exception
	 * Tests the a priori probabilities in the network 'bN_XY'
	 */
	@Test
	public void testAPrioriProbabilitiesBN_XY() throws Exception {
		ProbNet network;
		double probPositiveY;

		
		network = bN_XY;
		InferenceAlgorithm elimination1 = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);

		// A priori probabilities
		try {
			HashMap<Variable, TablePotential> aPrioriProbabilities;
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
			System.err.println(e.getMessage());
		}
	}
	
	@Test
		public void testAPosterioriProbabilitiesBN_ABC() throws Exception {
		ProbNet network;
		
		network = bN_ABC;
	
		InferenceAlgorithm algorithm1 = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
			Variable variableA = getVariableAndAssertNotNull(network,"A");
			Variable variableB = getVariableAndAssertNotNull(network,"B");
			Variable variableC = getVariableAndAssertNotNull(network,"C");

			// A = absent
			EvidenceCase evidence1 = new EvidenceCase();
			evidence1.addFinding(network, "A", "absent");
			HashMap<Variable, TablePotential> aAbsentProbabilities;
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
			HashMap<Variable, TablePotential> aPresentProbabilities;
			InferenceAlgorithm algorithm2 = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(bN_XY);
			algorithm2.setPostResolutionEvidence(evidence2);
			try {
				aPresentProbabilities = algorithm2.getProbsAndUtilities();
				checkProbabilityPotential(aPresentProbabilities,variableA,1.0);
				checkProbabilityPotential(aPresentProbabilities,variableB,0.1);
				checkProbabilityPotential(aPresentProbabilities,variableC,0.146);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
	
	

	/**
	 * @throws Exception
	 * Tests the introduction of pre and post-resolution evidence in bN_ABC.
	 */
	@Test
	public void testPreAndPostResolutionAPosterioriProbabilitiesBN_ABC() throws Exception {
		ProbNet network;
		
		network = bN_ABC;
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
		Variable variableA = getVariableAndAssertNotNull(network,"A");
		Variable variableB = getVariableAndAssertNotNull(network,"B");
		Variable variableC = getVariableAndAssertNotNull(network,"C");

		// Test when A = absent
		EvidenceCase evidence1 = new EvidenceCase();
		evidence1.addFinding(network, "A", "absent");
		HashMap<Variable, TablePotential> aAbsentProbabilities;
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
		ArrayList<Variable> variablesOfInterest = new ArrayList<Variable>();
		variablesOfInterest = new ArrayList<Variable>();
		variablesOfInterest.add(variableC);
		try {
			aAbsentProbabilities = algorithm.getProbsAndUtilities(variablesOfInterest);
			checkProbabilityPotential(aAbsentProbabilities,variableC,0.71);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
		
		@Test
		public void testAPosterioriProbabilitiesBN_XYZ() throws Exception {
			ProbNet network;
			InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(bN_XYZ);
			
			network = bN_XYZ;
			Variable variableX = getVariableAndAssertNotNull(network,"X");
			Variable variableY = getVariableAndAssertNotNull(network,"Y");
			Variable variableZ = getVariableAndAssertNotNull(network,"Z");

			// Test when Y = positive
			EvidenceCase evidence1 = new EvidenceCase();
			evidence1.addFinding(bN_XYZ, "Y", "positive");
			HashMap<Variable, TablePotential> yPositiveProbabilities;
			algorithm.setPostResolutionEvidence(evidence1);
			try {
				yPositiveProbabilities = algorithm.getProbsAndUtilities();
				checkProbabilityPotential(yPositiveProbabilities,variableX,0.9796034);
				checkProbabilityPotential(yPositiveProbabilities,variableY,1.0);
				checkProbabilityPotential(yPositiveProbabilities,variableZ,0.86);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		}

		

	/**
	 * Test for diagnosis problem
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NotEnoughMemoryException
	 * @throws ProbNodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	@Test
	public void testEvaluationIDDiagnosisProblem()
			throws NotEnoughMemoryException, FileNotFoundException,
			IOException, ParserException, ProbNodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		ProbNet network;
		
		network = iD_DiagnosisProblem;
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(iD_DiagnosisProblem);
		
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
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("Exception in testTrivial1");
		}
	}

	/**
	 * Test for diagnosis problem
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NotEnoughMemoryException
	 * @throws ProbNodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	@Test
	public void testAPrioriProbabilitiesID_DiagnosisProblem()
			throws NotEnoughMemoryException, FileNotFoundException,
			IOException, ParserException, ProbNodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		ProbNet diagram;
		
		diagram = iD_DiagnosisProblem;
				
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(iD_DiagnosisProblem);
		
		Variable variableX = diagram.getVariable("X");
		assertNotNull(variableX);
		Variable variableY = diagram.getVariable("Y");
		assertNotNull(variableY);
		Variable variableD = diagram.getVariable("D");
		assertNotNull(variableD);

		// A priori probabilities
		try {
			HashMap<Variable, TablePotential> aPrioriProbabilities;
			aPrioriProbabilities = algorithm.getProbsAndUtilities();
			// test potential probabilities
			checkProbabilityPotential(aPrioriProbabilities,variableX,0.07);
			checkProbabilityPotential(aPrioriProbabilities,variableY,0.0916);
			checkProbabilityPotential(aPrioriProbabilities,variableD,0.0916);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Test for diagnosis problem
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NotEnoughMemoryException
	 * @throws ProbNodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	public void testEvaluationIDDecisionTestProblem(ProbNet diagram)
			throws NotEnoughMemoryException, FileNotFoundException,
			IOException, ParserException, ProbNodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		Variable variableX = null;
		Variable variableY = null;
		Variable variableT = null;
		Variable variableD = null;
		Variable variableU1 = null;
		Variable variableU2 = null;
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);

		try {
			// test max expected utility
			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
			assertEquals(96.006, meuEvaluation, maxError);

			// Test optimal policy
			variableT = getVariableAndAssertNotNull(diagram,"T");
			variableD = getVariableAndAssertNotNull(diagram,"D");
			HashMap<Variable, TablePotential> optimalStrategy = algorithm.getOptimizedPolicies();
			TablePotential policyT = optimalStrategy.get(variableT);
			TablePotential policyD = optimalStrategy.get(variableD);
			assertNotNull(policyT);
			assertNotNull(policyD);

			// Test the size of the domain of the policy of T
			assertTrue(checkPolicy(policyT, variableT, 0));

			// Test the size of the domain of the policy of D
			assertTrue(checkPolicy(policyD, variableD, 2));

			// Test the a priori case
			HashMap<Variable, TablePotential> aPrioriProbabilities = algorithm
					.getProbsAndUtilities();
			// Read the variables
			variableX = getVariableAndAssertNotNull(diagram,"X");
			variableY = getVariableAndAssertNotNull(diagram,"Y");
			variableU1 = getVariableAndAssertNotNull(diagram,"U1");
			variableU2 = getVariableAndAssertNotNull(diagram,"U2");

			checkProbabilityPotential(aPrioriProbabilities, variableX, 0.07);
			checkProbabilityPotential(aPrioriProbabilities, variableY, 0.0916,
					0.9084);
			checkProbabilityPotential(aPrioriProbabilities, variableD, 0.0916);
			checkProbabilityPotential(aPrioriProbabilities, variableT, 1.0);
			checkUtilityPotential(aPrioriProbabilities, variableU1, 98.006);
			checkUtilityPotential(aPrioriProbabilities, variableU2, -2.0);


		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("Exception in testTrivial1");
		}

	}

	/**
	 * Test for diagnosis problem
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NotEnoughMemoryException
	 * @throws ProbNodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	@Test
	public void testEvaluationIDDecisionTestProblemWithoutSV()
			throws NotEnoughMemoryException, FileNotFoundException,
			IOException, ParserException, ProbNodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {

		testEvaluationIDDecisionTestProblem(iD_DecisionTestProblemWithoutSV);

	}

	/**
	 * Test for diagnosis problem
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NotEnoughMemoryException
	 * @throws ProbNodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	@Test
	public void testEvaluationIDDecisionTestProblemWithSV()
			throws NotEnoughMemoryException, FileNotFoundException,
			IOException, ParserException, ProbNodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {

		testEvaluationIDDecisionTestProblem(iD_DecisionTestProblemWithSV);

	}

	/**
	 * Test for diagnosis problem
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NotEnoughMemoryException
	 * @throws ProbNodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	@Test
	public void testPosteriorProbsAndUtilsIDDecisionTestProblem()
			throws NotEnoughMemoryException, FileNotFoundException,
			IOException, ParserException, ProbNodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(iD_DecisionTestProblemWithoutSV);

		
		checkPosteriorProbsAndUtilitiesDecisionTestProblem(algorithm,
				iD_DecisionTestProblemWithoutSV, "D", "no", 1.0, 0.0, 1.0, 0.0, 0.0069352708058,
				99.51453104359314, -2.0);
		checkPosteriorProbsAndUtilitiesDecisionTestProblem(algorithm,
				iD_DecisionTestProblemWithoutSV, "Y", "negative", 1.0, 0.0, 1.0, 0.0, 0.0069352708058,
				99.51453104359314, -2.0);
		checkPosteriorProbsAndUtilitiesDecisionTestProblem(algorithm,
				iD_DecisionTestProblemWithoutSV, "T", "yes", 1.0, 0.0916, 0.9084, 0.0916, 0.07, 98.006,
				-2.0);

		EvidenceCase evi = new EvidenceCase();
		try {
			evi.addFinding(iD_DecisionTestProblemWithoutSV, "T", "yes");
			evi.addFinding(iD_DecisionTestProblemWithoutSV, "Y", "positive");
		} catch (InvalidStateException e) {
			e.printStackTrace();
		} catch (IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		checkPosteriorProbsAndUtilitiesEvidenceDecisionTestProblem(
				algorithm, iD_DecisionTestProblemWithoutSV, evi, 1.0, 1.0, 0.0, 1.0,
				0.6954148, 83.045851, -2.0);
		try {
			evi.addFinding(iD_DecisionTestProblemWithoutSV, "X", "present");

		} catch (InvalidStateException e) {
			e.printStackTrace();
		} catch (IncompatibleEvidenceException e) {
			e.printStackTrace();
		}

		// checkPosteriorProbsAndUtilitiesEvidenceDecisionTestProblem(variableElimination,diagram,evi,1.0,1.0,0.0,1.0,1.0,80.0,-2.0);

	}

	private void checkPosteriorProbsAndUtilitiesDecisionTestProblem(
			InferenceAlgorithm algorithm, ProbNet diagram,
			String nameVariable, String state, double t, double y1, double y2,
			double d, double x, double u1, double u2) {
		EvidenceCase evi;
		evi = new EvidenceCase();
		try {
			evi.addFinding(diagram, nameVariable, state);
		} catch (ProbNodeNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidStateException e) {
			e.printStackTrace();
		} catch (IncompatibleEvidenceException e) {
			e.printStackTrace();
		}
		checkPosteriorProbsAndUtilitiesEvidenceDecisionTestProblem(
				algorithm, diagram, evi, t, y1, y2, d, x, u1, u2);

	}

	private void checkPosteriorProbsAndUtilitiesEvidenceDecisionTestProblem(
			InferenceAlgorithm algorithm, ProbNet diagram,
			EvidenceCase evi, double t, double y1, double y2, double d,
			double x, double u1, double u2) {

		Variable variableX = null;
		Variable variableY = null;
		Variable variableT = null;
		Variable variableD = null;
		Variable variableU1 = null;
		Variable variableU2 = null;

		try {
			variableT = diagram.getVariable("T");
			variableD = diagram.getVariable("D");

			variableX = diagram.getVariable("X");

			variableY = diagram.getVariable("Y");

			variableU1 = diagram.getVariable("U1");

			variableU2 = diagram.getVariable("U2");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("Exception in testTrivial1");
		}

		try {
			algorithm.setPostResolutionEvidence(evi);
			HashMap<Variable, TablePotential> aPosterioriProbabilities = null;
			try {
				try {
					aPosterioriProbabilities = algorithm.getProbsAndUtilities();
				} catch (UnexpectedInferenceException e) {
					
					e.printStackTrace();
				}
			} catch (NotEnoughMemoryException e) {
				e.printStackTrace();
			}
			
			checkProbabilityPotential(aPosterioriProbabilities, variableX, x);
			checkProbabilityPotential(aPosterioriProbabilities, variableY, y1,
					y2);
			checkProbabilityPotential(aPosterioriProbabilities, variableD, d);
			checkProbabilityPotential(aPosterioriProbabilities, variableT, t);
			checkUtilityPotential(aPosterioriProbabilities, variableU1, u1);
			checkUtilityPotential(aPosterioriProbabilities, variableU2, u2);
		} catch (IncompatibleEvidenceException e) {
			//
			e.printStackTrace();
		}

	}

	private void checkUtilityPotential(
			HashMap<Variable, TablePotential> aPrioriProbabilities,
			Variable variableU, double u) {
		TablePotential U = (TablePotential) aPrioriProbabilities.get(variableU);
		checkUtility(U, u);

	}

	
	/**
	 * Test for diagnosis problem
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NotEnoughMemoryException
	 * @throws ProbNodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	//@Test
/*	public void testEvaluationIDDecisionTestProblem()
			throws NotEnoughMemoryException, FileNotFoundException,
			IOException, ParserException, ProbNodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {

		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(iD_DecisionTestProblemWithoutSV);
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
	
	private boolean checkPolicy(TablePotential policy, Variable d, int numVar) {
		ArrayList<Variable> domainPolicy = policy.getVariables();
		domainPolicy.remove(d);
		return (numVar == domainPolicy.size());
	}

	/**
	 * Test for diagnosis problem
	 * 
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NotEnoughMemoryException
	 * @throws ProbNodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	@Test
	public void testEvaluationIDUniformDiagnosisProblem()
			throws NotEnoughMemoryException, FileNotFoundException,
			IOException, ParserException, ProbNodeNotFoundException,
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
			HashMap<Variable, TablePotential> optimalStrategy = algorithm
					.getOptimizedPolicies();
			TablePotential policy = optimalStrategy.get(D);
			assertNotNull(policy);

			// Test the size of the domain of the policy
			ArrayList<Variable> domainPolicy = policy.getVariables();
			domainPolicy.remove(D);
			assertEquals(1, domainPolicy.size());

			// Test the optimal choice of the policy
			double[] truePolicy = { 0.5, 0.5, 0.5, 0.5 };

			assertTrue(areEquals(policy.getValues(), truePolicy));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("Exception in testTrivial1");
		}
	}
	
	@Test
	public void testPreAndPostResolutionEvidenceIDDecisionTestProblem() throws NotEvaluableNetworkException{
		ProbNet diagram = iD_DecisionTestProblemWithSV;
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
		
		//TODO Test combination of pre and post resolution findings.
		try {
			//Variable variableT = diagram.getVariable("T");
			//Variable variableD = diagram.getVariable("D");
			
			EvidenceCase preResolutionEvidence = new EvidenceCase();
			preResolutionEvidence.addFinding(diagram,"X","present");
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
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("Exception in testTrivial1");
		}
	}

		
		

	
	/**
	 * @param probabilities
	 * @param variableX
	 * @param x
	 * Checks if the probability potential of 'variableX' in 'probabilities' is equal
	 * to x[0],..,x[n], where 'n' is the number of states of 'variableX'
	 */
	protected void checkProbabilityPotential(
			HashMap<Variable, TablePotential> probabilities,
			Variable variableX, double... x) {
		TablePotential X = (TablePotential) probabilities.get(variableX);
		checkProbabilities(X, x);

	}
	
	/**
	 * @param pot
	 * @param values
	 * Checks if the values of 'pot' (except the last one) is equal to 'values' and if the sum of
	 * the probabilities in 'pot' is 1.0.
	 */
	protected void checkProbabilities(TablePotential pot, double... values) {

		double[] potValues = pot.values;
		int potValuesLength = potValues.length;
		assertEquals(values.length + 1, potValuesLength);
		double sum;
		sum = 0.0;
		for (int i = 0; i < potValuesLength - 1; i++) {
			double expected = values[i];
			assertEquals(expected, potValues[i], maxError);
			sum = sum + expected;
		}
		assertEquals(1.0 - sum, potValues[potValuesLength - 1], maxError);
	}

	/**
	 * @param x
	 * @param v
	 * Checks if the values of the potential 'x' are equal to 'v' and if the number
	 * of values in 'x' is 1. 
	 */
	protected void checkUtility(TablePotential x, double v) {

		assertEquals(1, x.getTableSize());
		assertEquals(x.values[0], v, maxError);

	}
	
	protected boolean areEquals(double[] v1, double[] v2) {
		boolean areEquals = true;
		int v1length;
		int i;

		v1length = v1.length;

		areEquals = (v1length == v2.length);
		i = 0;
		while (areEquals && i < v1length) {
			areEquals = Math.abs(v1[i] - v2[i]) < maxError;
			i = i + 1;
		}
		return areEquals;

	}


}
