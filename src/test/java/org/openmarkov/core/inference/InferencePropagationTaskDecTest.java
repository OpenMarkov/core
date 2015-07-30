package org.openmarkov.core.inference;

import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.Task;
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

	public abstract Task buildInferenceTask(ProbNet probNet, List<Variable> variablesOfInterest,
								   EvidenceCase preResolutionEvidence, EvidenceCase postResolutionEvidence)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException;


	/**
	 * Checks that the Intervention (optimal strategy) obtained from the evaluation optimal is not null
	 * and that it is consistent with the Cooper policy network (CPN) built using the policies obtained
	 * from the method getOptimizedPolicies
	 * @param net
	 * @param algorithm
	 * @throws IncompatibleEvidenceException
	 * @throws UnexpectedInferenceException
	 */
	private void testScenariosIntervention(ProbNet net,
			Task algorithm) throws IncompatibleEvidenceException, UnexpectedInferenceException {
		Intervention interv = null;
		try {
			interv = algorithm.getOptimalStrategy();
		} catch (IncompatibleEvidenceException | UnexpectedInferenceException e) {
			e.printStackTrace();
		}
		assertNotNull(interv);
		testIntervention(algorithm,interv,new EvidenceCase());
		
		
	}

	/**
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
	 */
	//TODO: check this tests and enable it
	private void testIntervention(Task algorithm, Intervention interv, EvidenceCase parentEvi)
			throws IncompatibleEvidenceException, UnexpectedInferenceException {

		if (interv != null) {
			interv.getRootVariable();
			List<TreeADDBranch> branches = interv.getBranches();
			Variable rootVariable = interv.getRootVariable();
			algorithm.setPostResolutionEvidence(parentEvi);
			List<Variable> interestVariables = new ArrayList<>();
			interestVariables.add(rootVariable);
			TablePotential probs = null; //algorithm.getProbsAndUtilities().get(rootVariable);
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

	/**
	 * @param branches
	 * @return The total number of states in 'branches'
	 */
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

	/**
	 * @param probs
	 * @return The number of values in the potential that are greater than zero
	 */
	private int getNumProbsNotZero(TablePotential probs) {
		int numNotZero = 0;
		double[] values = probs.values;
		for (int i=0;i<values.length;i++){
			if (values[i]>0.0){
				numNotZero = numNotZero + 1;
			}
		}
		return numNotZero;
	}
	
	public void checkUtilityPotential(
			Map<Variable, TablePotential> aPrioriProbabilities,
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


}
