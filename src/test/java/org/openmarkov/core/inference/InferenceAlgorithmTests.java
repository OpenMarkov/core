/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.inference;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TablePotentialTest;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;



/** @author mluque */
/** @author ibermejo */
@Ignore
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

	//protected ProbNet iD_DiagnosisProblem;
	protected ProbNet iD_UniformDiagnosisProblem;
	protected ProbNet iD_DecisionTestProblemWithoutSV;
	protected ProbNet iD_DecisionTestProblemWithSV;

	protected ProbNet bN_Asia;

 
	@Before
	public void setUp() throws Exception {

		bN_X = NetsFactory.createBN_X(1.0);
		bN_XY = NetsFactory.createBN_XY(prevalence,
				sensitivity, specificity);
		bN_ABC = NetsFactory.createBN_ABC();
		bN_XYZ = NetsFactory.createBN_XYZ(prevalence,
				sensitivity, specificity, 0.86, 0.89);
		bN_Asia = NetsFactory.createBN_Asia();
		/*iD_DiagnosisProblem = NetsFactory
				.createInfluenceDiagramDiagnosisProblem();*/
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
		
		network = NetsFactory.createBN_ABC();
			
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
			fail();
		}
	}
	
	/**
	 * @throws ProbNodeNotFoundException
	 * Tests the a priori probabilities obtained in the network bN_ABC
	 */
	@Test
	public void testAPrioriProbabilitiesBN_Asia()
			throws ProbNodeNotFoundException {
					
		String namesVariables[]={"A","B","T","L","TOrC","X","D","S"};
		double expectedProbs[] = {0.01,0.45,0.0104,0.055,0.064828,0.11029004,0.3974534,0.5};
		
		checkVariablesAndProbabilities(bN_Asia,namesVariables,null, null, expectedProbs);
			
	}
	
	
	/**
	 * @param namesVariables
	 * @param expectedProbs
	 * Performs a complete propagation and checks the probabilities obtained
	 */
	private void checkVariablesAndProbabilities(ProbNet network,String[] namesVariables,EvidenceCase preResolutionEvidence,
			EvidenceCase postResolutionEvidence,
			double[] expectedProbs) {
		
		InferenceAlgorithm elimination = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
		
		ArrayList<Variable> variables = new ArrayList<Variable>();
		
		for (int i=0;i<namesVariables.length;i++){
			Variable auxVar = null;
			try {
				auxVar = getVariableAndAssertNotNull(network,namesVariables[i]);
			} catch (ProbNodeNotFoundException e) {
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
			HashMap<Variable, TablePotential> aPosterioriProbs;
			aPosterioriProbs = elimination.getProbsAndUtilities();
			checkProbabilities(aPosterioriProbs,variables,expectedProbs);
		
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
			
		}
	}

	/**
	 * @param e
	 */
	@SuppressWarnings("restriction")
	protected void printExceptionAndFailIfImplemented(Exception e) {
		if (e.getClass()!=NotImplementedException.class){
			System.err.println(e.getMessage());
			fail();
		}
	}





	/**
	 * @throws ProbNodeNotFoundException
	 * Tests the a priori probabilities obtained in the network bN_ABC
	 */
	@Test
	public void testAPosterioriProbabilitiesBN_Asia()
			throws ProbNodeNotFoundException {
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
	
	
	/**
	 * @param aPosterioriProbs
	 * @param variables
	 * @param expectedProbs
	 * The probabilities in expectedProbs are given as independent numbers for each variable:
	 * It means that, if n is the number of states of a variable, then n-1 probabilities are given
	 * for it in 'expectedProbs'. And the values are ordered according the order in 'variables'.
	 * In the particular case when all variables are binary then 'variables' and 'expectedProbs' have
	 * the same size.
	 * */
	protected void checkProbabilities(
			HashMap<Variable, TablePotential> aPosterioriProbs,
			ArrayList<Variable> variables, double[] expectedProbs) {
		
			int size = variables.size();
						
			int indexBaseProbs = 0;
			for (int i=0;i<size;i++){
				double auxExpectedProbs[];
				Variable auxVar = variables.get(i);
				int numStates = auxVar.getNumStates();
				int numProbsAux;
				numProbsAux = numStates - 1;
				auxExpectedProbs = new double[numProbsAux];
				for (int j=0;j<numProbsAux;j++){
					auxExpectedProbs[j] = expectedProbs[indexBaseProbs+j];
				}
				checkProbabilityPotential(aPosterioriProbs,variables.get(i),auxExpectedProbs);
				indexBaseProbs = indexBaseProbs + numProbsAux;
			}
		
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
			printExceptionAndFailIfImplemented(e);
		}

	}


	/**
	 * @param network
	 * @param variableName
	 * @return The variable in 'network' whose name is 'variableName'. It also checks whether the variable is not null.
	 * @throws ProbNodeNotFoundException	 
	 */
	public static Variable getVariableAndAssertNotNull(ProbNet network, String variableName) throws ProbNodeNotFoundException {
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
	//@Test (expected = IncompatibleEvidenceException.class)
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
			network = NetsFactory.createBN_XY(0.5,1.0,0.0);
		} catch (Exception e1) {
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
			printExceptionAndFailIfImplemented(e);
		}

	}

	/**
	 * @throws IncompatibleEvidenceException
	 * Tests if the inference on the network Asia throws IncompatibleEvidenceException
	 * if the evidence is (T=absent,L=absent,TOrC=yes).
	 */

	@Test (expected = IncompatibleEvidenceException.class)
	public void testIncompatibleEvidenceBN_Asia() throws IncompatibleEvidenceException {
		ProbNet network = null;
				
		try {
			network = bN_Asia;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
				
		// Test when Y = absent, which is incompatible evidence
		EvidenceCase evidence = new EvidenceCase();
		try {
			evidence.addFinding(network, "T", "absent");
			evidence.addFinding(network, "L", "absent");
			evidence.addFinding(network, "TOrC", "yes");
		} catch (ProbNodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			//e.printStackTrace();
		} 
		
		algorithm.setPostResolutionEvidence(evidence);
		
		try {
			algorithm.getProbsAndUtilities();
		} catch (NotEnoughMemoryException | UnexpectedInferenceException e) {
			printExceptionAndFailIfImplemented(e);
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
			printExceptionAndFailIfImplemented(e);
		}
	}
	
	/**
	 * @throws Exception
	 * Tests the a priori joint probability in the network 'bN_XY'
	 */
	@Test
	public void testAPrioriJointProbabilityBN_XY() throws Exception {
		ProbNet network;
		TablePotential expectedPot;
	
		network = bN_XY;
		InferenceAlgorithm elimination1 = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);

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
     * @throws ProbNodeNotFoundException
     * Tests the a priori joint probabilities obtained in the network Asia
     * @throws UnexpectedInferenceException 
     * @throws IncompatibleEvidenceException 
     * @throws NotEnoughMemoryException 
     */
    @Test
    public void testAPosterioriJointProbabilitiesBN_Asia ()
        throws ProbNodeNotFoundException,
        NotEnoughMemoryException,
        IncompatibleEvidenceException,
        UnexpectedInferenceException
    {
        ProbNet network = bN_Asia;
        int numIter = 10;
        InferenceAlgorithm inferenceAlgorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
        for (int i = 0; i < numIter; i++)
        {
            EvidenceCase evidence = new EvidenceCase ();
            try
            {
                // T=absent, TOrC=yes
                evidence.addFinding (network, "T", "absent");
                evidence.addFinding (network, "TOrC", "yes");
            }
            catch (InvalidStateException | IncompatibleEvidenceException e1)
            {
                e1.printStackTrace ();
            }
            double expectedProbs[] = {0.00959984, 0.572727, 0.0, 1.0, 1.0, 0.98, 0.85727273, 0.90909091};
            
            inferenceAlgorithm.setPreResolutionEvidence (evidence);
            ArrayList<Variable> variables = new ArrayList<> ();
            variables.add (network.getVariable ("T"));
            variables.add (network.getVariable ("TOrC"));
            variables.add (network.getVariable ("L"));
            
            TablePotential jointProbability = inferenceAlgorithm.getJointProbability (variables);
            
            Assert.assertEquals (variables.size (), jointProbability.getVariables ().size ());
            for(int j=0; j < expectedProbs.length; ++j)
            {
                Assert.assertEquals (expectedProbs[j], jointProbability.values[j], maxError);
            }
        }
    }
	
	/**
	 * @throws Exception
	 * Tests the a priori joint probability in the network 'bN_ABC'
	 */
	@Test
	public void testAPrioriJointProbabilityBN_ABC() throws Exception {
		ProbNet network;
		TablePotential expectedPot;
	
		network = bN_ABC;
		InferenceAlgorithm elimination1 = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);

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
			InferenceAlgorithm algorithm2 = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
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
			printExceptionAndFailIfImplemented(e);
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
				printExceptionAndFailIfImplemented(e);
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
		
		network = NetsFactory.createInfluenceDiagramDiagnosisProblem();
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
		
		try {
			// test max expected utility
			Double meuEvaluation = algorithm.getGlobalUtility().values[0];
			assertEquals(96.006, meuEvaluation, maxError);

			// Test optimal policy
			Variable D = network.getVariable("D");
			HashMap<Variable, Potential> optimalStrategy = algorithm.getOptimizedPolicies();
			Potential policy = optimalStrategy.get(D);
			assertNotNull(policy);

			// Test the size of the domain of the policy
			ArrayList<Variable> domainPolicy = policy.getVariables();
			domainPolicy.remove(D);
			assertEquals(1, domainPolicy.size());
			
			// Test the optimal choice of the policy
			double[] truePolicy = { 1.0, 0.0, 0.0, 1.0 };
			assertTrue(areEquals(getTablePotential(policy).getValues(), truePolicy));
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
		}
	}

	
	
	private TablePotential getTablePotential(Potential potential){
		TablePotential table=null;
		 try {
			 table = potential.tableProject(null, null).get(0);
		} catch (NotEnoughMemoryException | NonProjectablePotentialException
				| WrongCriterionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return table;
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
	 *//*
	@Test
	public void testConditioningVariablesEvaluationIDDiagnosisProblem()
			throws NotEnoughMemoryException, FileNotFoundException,
			IOException, ParserException, ProbNodeNotFoundException,
			ConstraintViolationException, NotEvaluableNetworkException {
		ProbNet network;
		
		network = iD_DiagnosisProblem;
		
		//Decision criteria variable
		String dCStates[]= {"Health","Money"};
		Variable variableDC = new Variable("DC",dCStates);

		network.getPotentials(network.getVariable("U")).get(0).addVariable(variableDC);
		
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
			printExceptionAndFail(e);
		}
	}
*/
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
		
		diagram = NetsFactory.createInfluenceDiagramDiagnosisProblem();
				
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
		
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
			printExceptionAndFailIfImplemented(e);
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
			HashMap<Variable, Potential> optimalStrategy = algorithm.getOptimizedPolicies();
			Potential policyT = optimalStrategy.get(variableT);
			Potential policyD = optimalStrategy.get(variableD);
			assertNotNull(policyT);
			assertNotNull(policyD);

			// Test the size of the domain of the policy of T
			assertTrue(checkPolicy(getTablePotential(policyT), variableT, 0));

			// Test the size of the domain of the policy of D
			assertTrue(checkPolicy(getTablePotential(policyD), variableD, 2));

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
			printExceptionAndFailIfImplemented(e);
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

		} catch (InvalidStateException | IncompatibleEvidenceException e) {
			
			printExceptionAndFailIfImplemented(e);
		}	

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
			printExceptionAndFailIfImplemented(e);
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
			printExceptionAndFailIfImplemented(e);
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
			HashMap<Variable, Potential> optimalStrategy = algorithm
					.getOptimizedPolicies();
			Potential policy = optimalStrategy.get(D);
			assertNotNull(policy);

			// Test the size of the domain of the policy
			ArrayList<Variable> domainPolicy = policy.getVariables();
			domainPolicy.remove(D);
			assertEquals(1, domainPolicy.size());

			// Test the optimal choice of the policy
			double[] truePolicy = { 0.5, 0.5, 0.5, 0.5 };

			assertTrue(areEquals(getTablePotential(policy).getValues(), truePolicy));
		} catch (Exception e) {
			printExceptionAndFailIfImplemented(e);
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
			printExceptionAndFailIfImplemented(e);
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
			double actual = potValues[i];
			assertEquals(expected, actual, maxError);
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
