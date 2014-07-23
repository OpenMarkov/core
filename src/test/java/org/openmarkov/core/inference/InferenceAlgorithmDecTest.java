package org.openmarkov.core.inference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.factory.IDFactory;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

/**
 * @author manolo
 * Tests class for models that contain decisions. Different subclasses share that they have to test the MEU and the strategy
 *
 */
public abstract class InferenceAlgorithmDecTest extends InferenceAlgorithmTest {
	
	

	
	
	
	protected void testMEUAndStrategy(ProbNet net,double expectedMEU,Intervention expectedStrategy) throws IncompatibleEvidenceException, UnexpectedInferenceException{
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(net);
		Double meuEvaluation = algorithm.getGlobalUtility().values[0];
		assertEquals(expectedMEU,meuEvaluation, maxError);
		//testScenariosIntervention(net,algorithm);
	}
	
	
	

	private void testScenariosIntervention(ProbNet net,
			InferenceAlgorithm algorithm) throws IncompatibleEvidenceException, UnexpectedInferenceException {
		Intervention interv = null;
		try {
			interv = algorithm.getOptimalStrategy();
		} catch (IncompatibleEvidenceException | UnexpectedInferenceException e) {
			e.printStackTrace();
		}
		
		testLeavesScenarios(algorithm,interv,new EvidenceCase());
		
		
	}




	private void testLeavesScenarios(InferenceAlgorithm algorithm,
			Intervention interv, EvidenceCase parentEvi)
			throws IncompatibleEvidenceException, UnexpectedInferenceException {

		if (interv != null) {
			interv.getRootVariable();
			List<TreeADDBranch> branches = interv.getBranches();
			if (branches != null) {
				for (int i = 0; i < branches.size(); i++) {
					TreeADDBranch auxBranch = branches.get(i);
					Intervention auxInterventionBranch = Intervention
							.getInterventionBranch(auxBranch);
					for (State state : auxBranch.getStates()) {
						EvidenceCase newEvi = new EvidenceCase(
								parentEvi.getFindings());
						Finding finding = new Finding(interv.getRootVariable(),
								state);
						try {
							newEvi.addFinding(finding);
						} catch (InvalidStateException e) {
							e.printStackTrace();
						}
						algorithm.setPostResolutionEvidence(newEvi);
						algorithm.getProbsAndUtilities();
						testLeavesScenarios(algorithm, auxInterventionBranch,
								newEvi);
					}
				}
			}
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
	public void testEvaluationIDDecisionTestProblem(ProbNet diagram)
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
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
			HashMap<Variable, TablePotential> aPrioriProbabilities = algorithm
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

	private TablePotential constructExpectedUtilitiesPolicyTDecisionTestProblem(Variable variableT) {
		
	TablePotential pot;
	
	ArrayList<Variable> variables;
	variables = new ArrayList<Variable>();
	variables.add(variableT);
			
	pot = new TablePotential(variables,PotentialRole.UTILITY);
	double values[]={96.006,95.1};
	pot.setValues(values);
	return pot;
	}


	


	private TablePotential constructExpectedUtilitiesPolicyDDecisionTestProblem(Variable variableT,
			Variable variableY, Variable variableD) {
		TablePotential pot;
		
		ArrayList<Variable> variables;
		variables = new ArrayList<Variable>();
		variables.add(variableT);
		variables.add(variableY);
		variables.add(variableD);
				
		pot = new TablePotential(variables,PotentialRole.UTILITY);
		double values[]={81.04585153,0.0,87.93064729,0.0,-2.0,89.3,49.3209607,0.0,97.51453104,0.0,-2.0,95.1};
		pot.setValues(values);
		return pot;
	}









	

	

	public void checkUtilityPotential(
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
	 * @throws NodeNotFoundException
	 * @throws ConstraintViolationException
	 * @throws NotEvaluableNetworkException
	 */
	//@Test
/*	public void testEvaluationIDDecisionTestProblem()
			throws FileNotFoundException,
			IOException, ParserException, NodeNotFoundException,
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
	    List<Variable> domainPolicy = policy.getVariables();
		domainPolicy.remove(d);
		return (numVar == domainPolicy.size());
	}


	
	
	//@Test
	public void testEvaluationSimpleIDWithoutDecisions(){
		
		testMEUDiagram(IDFactory.createSimpleIDWithoutDecisions(),83.7);
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
		
		interv = new Intervention(dec, states, new ArrayList<Intervention>(), null);
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

	
	
	
	private Intervention getOptimalStrategy(ProbNet id) {
		Intervention strategy = null;
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(id);
		try {
			strategy = algorithm.getOptimalStrategy();
		} catch (IncompatibleEvidenceException | UnexpectedInferenceException e) {
			e.printStackTrace();
		}
		return strategy;
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
private void testMEUDiagram(ProbNet diagram,double expectedMeu){
		
		InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);

		
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
}
