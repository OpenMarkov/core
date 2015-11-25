package org.openmarkov.core.model.network.potential;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

public class InterventionTest {

	private Variable chanceVar0, chanceVar1, chanceVar2;
	private Variable decisionVar;
	private Intervention intervention0;
	private Intervention intervention1;
	private Intervention intervention2;
	private List<Intervention> interventions;
	private List<State> chanceStates;
	
	@Before
	public void setUp() throws Exception {
		chanceVar0 = new Variable("chance", "chance0", "chance1", "chance2");
		chanceVar1 = new Variable("chance", "chance0", "chance1", "chance2");
		chanceVar2 = new Variable("chance", "chance0", "chance1", "chance2");
		decisionVar = new Variable("decision", "decision0", "decision1", "decision2");
		intervention0 = new Intervention(chanceVar0);
		intervention1 = new Intervention(chanceVar1);
		intervention2 = new Intervention(chanceVar2);
		interventions = new ArrayList<>();
		chanceStates = new ArrayList<>();
	}

	@Test
	/** Test constructor with several interventions and states */
	public void testIntervention1() {
		TreeADDBranch branch0, branch1;
		List<TreeADDBranch> branches;
		
		interventions = Arrays.asList(intervention0, intervention1, intervention2);
		chanceStates = Arrays.asList(chanceVar0.getStates());
		
		// Test 1: All interventions are distinct.
		Intervention testIntervention0 = new Intervention(decisionVar, chanceStates, interventions);
		
		branches = testIntervention0.getBranches();
		assertEquals(3, branches.size());
		branch0 = testIntervention0.getBranch(chanceStates.get(0));
		assertEquals(branch0.getPotential(), intervention0);
		branch0 = testIntervention0.getBranch(chanceStates.get(1));
		assertEquals(branch0.getPotential(), intervention1);
		branch0 = testIntervention0.getBranch(chanceStates.get(2));
		assertEquals(branch0.getPotential(), intervention2);
		assertEquals(decisionVar, testIntervention0.getRootVariable());
		
		// Test 2: Two interventions are equal.
		interventions = Arrays.asList(intervention0, intervention1, intervention0);

		Intervention testIntervention1 = new Intervention(decisionVar, chanceStates, interventions);
		
		branches = testIntervention1.getBranches();
		assertEquals(2, branches.size());
		branch0 = testIntervention1.getBranch(chanceStates.get(0));
		assertEquals(branch0.getPotential(), intervention0);
		List<State> branchStates = branch0.getBranchStates();
		try {
			assertTrue(branchStates.contains(chanceVar0.getState("chance2")));
			assertTrue(branchStates.contains(chanceVar0.getState("chance0")));
		} catch (InvalidStateException e) {
			fail(e.getMessage());
		}
		branch1 = testIntervention1.getBranch(chanceStates.get(1));
		assertEquals(branch1.getPotential(), intervention1);
		assertEquals(decisionVar, testIntervention1.getRootVariable());
		
		// Test 3: All interventions are null. Intervention without branches
		interventions = Arrays.asList(null, null, null);
		Intervention testIntervention2 = new Intervention(decisionVar, chanceStates, interventions);
		
		branches = testIntervention2.getBranches();
		assertEquals(0, branches.size());
		assertEquals(decisionVar, testIntervention2.getRootVariable());
	}
	
	/**
	 * Simple test for equals
	 */
	@Test
	public void testEquals1() {
		// Create several interventions with small differences. All of them are different
		Intervention[] testInterventions0 = new Intervention[5];
		testInterventions0[0] = new Intervention(chanceVar0);
		
		testInterventions0[1] = new Intervention(chanceVar0);
		List<State> statesTestIntervention1 = null;
		try {
			statesTestIntervention1 = Arrays.asList(chanceVar0.getState("chance0"));
		} catch (InvalidStateException e) {
			fail(e.getMessage());
		}
		testInterventions0[1].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar0, intervention0, null));
		
		testInterventions0[2] = new Intervention(chanceVar0);
		List<State> statesTestIntervention2 = null;
		try {
			statesTestIntervention2 = Arrays.asList(chanceVar0.getState("chance1"));
		} catch (InvalidStateException e) {
			fail(e.getMessage());
		}
		testInterventions0[2].addBranch(new TreeADDBranch(statesTestIntervention2, chanceVar0, intervention0, null));
		
		testInterventions0[3] = new Intervention(chanceVar1);
		testInterventions0[3].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar1, intervention0, null));
		
		testInterventions0[4] = new Intervention(chanceVar0);
		testInterventions0[4].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar0, intervention1, null));
		
		// Test begins here
		for (int i = 0; i < testInterventions0.length - 1; i++) {
			for (int j = i + 1; j < testInterventions0.length; j++) {
				if (testInterventions0[i].equals(testInterventions0[j])) {
					System.out.println("Aqui");
				}
				assertFalse(testInterventions0[i].equals(testInterventions0[j]));
			}
		}
		
		// Create the same interventions with different objects
		Intervention[] testInterventions1 = new Intervention[5];
		testInterventions1[0] = new Intervention(chanceVar0);
		
		testInterventions1[1] = new Intervention(chanceVar0);
		testInterventions1[1].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar0, intervention0, null));
		
		testInterventions1[2] = new Intervention(chanceVar0);
		testInterventions1[2].addBranch(new TreeADDBranch(statesTestIntervention2, chanceVar0, intervention0, null));
		
		testInterventions1[3] = new Intervention(chanceVar1);
		testInterventions1[3].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar1, intervention0, null));
		
		testInterventions1[4] = new Intervention(chanceVar0);
		testInterventions1[4].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar0, intervention1, null));

		// Test begins here
		for (int i = 0; i < testInterventions1.length; i++) {
			if (!testInterventions0[i].equals(testInterventions1[i])) {
				System.out.println("Aqui");
			}
			assertTrue(testInterventions0[i].equals(testInterventions1[i]));
		}
	}
	
}
