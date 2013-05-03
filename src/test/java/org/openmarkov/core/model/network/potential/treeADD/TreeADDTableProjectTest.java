package org.openmarkov.core.model.network.potential.treeADD;



import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

public class TreeADDTableProjectTest {

	private Variable variableA;
	private Variable variableB;
	private Variable variableC;
	private State absent;
	private State present;
	private State mild;
	private State moderate;
	private State severe;
	private TreeADDPotential treeADD;
	private Object projectedPotential;
	private double[] projectedValues;

	@Before
    public void setUp() throws Exception {
		 // create variables
		variableA = new Variable("A", 4);
		variableB = new Variable("B", 2); 
		variableC = new Variable("C", 2); 
		
		// set variable states
		absent = new State("absent");
		present = new State("present");
		State [] states= {absent, present};
		
		mild = new State("mild");
		moderate = new State("moderate");
		severe = new State("severe");
		
		State [] statesA = {absent, mild, moderate, severe};

		variableC.setStates(states);
		variableB.setStates(states);
		variableA.setStates(statesA);
		
		ArrayList<Variable> variablesC = new ArrayList<Variable>();
		variablesC.add(variableC);
		double []tableC1 = {0.7, 0.3};
		TablePotential potentialC1 = new TablePotential(variablesC,PotentialRole.CONDITIONAL_PROBABILITY, tableC1);
		
		double []tableC2 = {0.8, 0.2};
		TablePotential potentialC2 = new TablePotential(variablesC,PotentialRole.CONDITIONAL_PROBABILITY, tableC2);
		
		double []tableC3 = {0.6, 0.4};
		TablePotential potentialC3 = new TablePotential(variablesC,PotentialRole.CONDITIONAL_PROBABILITY, tableC3);
		
		
		ArrayList<Variable> variablesCB = new ArrayList<Variable>();
		variablesCB.add(variableC);
		variablesCB.add(variableB);
		double []tableCB = {0.7, 0.3, 0.1, 0.9};
		TablePotential potentialCB = new TablePotential(variablesCB,PotentialRole.CONDITIONAL_PROBABILITY, tableCB);
		
		//Branches
		ArrayList<State> branchModerateStates = new ArrayList<State>();
		branchModerateStates.add(moderate);
		ArrayList<Variable> parentVariables = new ArrayList<Variable>();
		parentVariables.add(variableC);
		parentVariables.add(variableA);
		parentVariables.add(variableB);
		TreeADDBranch branchModerate = new  TreeADDBranch(branchModerateStates, variableA, potentialCB, parentVariables);
		
		ArrayList<State> branchSevereStates = new ArrayList<State>();
		branchSevereStates.add(severe);
		TreeADDBranch branchSevere = new TreeADDBranch(branchSevereStates, variableA, potentialC3, parentVariables);
		
		//Subtree
		ArrayList<State> branchAbsentStates = new ArrayList<State>();
		branchAbsentStates.add(absent);
		ArrayList<State> branchPresentStates = new ArrayList<State>();
		branchPresentStates.add(present);
		ArrayList<Variable> subVariables =  new ArrayList<Variable>();
		subVariables.add(variableC);
		subVariables.add(variableB);
		TreeADDBranch branchAbsent = new TreeADDBranch(branchAbsentStates, variableB, potentialC1, subVariables);
		TreeADDBranch branchPresent = new TreeADDBranch(branchPresentStates, variableB, potentialC2, subVariables);
		ArrayList<TreeADDBranch> subBranches = new ArrayList<TreeADDBranch>();
		subBranches.add(branchAbsent);
		subBranches.add(branchPresent);
		TreeADDPotential subtreeB = new TreeADDPotential(subVariables, variableB, 
				PotentialRole.CONDITIONAL_PROBABILITY, subBranches);
		
		ArrayList<State> branchAbsentMildStates = new ArrayList<State>();
		branchAbsentMildStates.add(absent);
		branchAbsentMildStates.add(mild);
		TreeADDBranch branchAbsentMild = new TreeADDBranch(branchAbsentMildStates, variableA, subtreeB, parentVariables);		
		
		ArrayList<TreeADDBranch> branches = new ArrayList<TreeADDBranch>();
		branches.add(branchAbsentMild);
		branches.add(branchModerate);
		branches.add(branchSevere);
		
		treeADD = new TreeADDPotential(parentVariables, variableA, PotentialRole.CONDITIONAL_PROBABILITY, branches);
		
		double []projectedValues = {0.7, 0.3, 0.8, 0.2, 0.7, 0.3, 0.8, 0.2, 0.7, 0.3, 0.1, 0.9, 0.6, 0.4, 0.6, 0.4};
		projectedPotential = new TablePotential(parentVariables, PotentialRole.CONDITIONAL_PROBABILITY, projectedValues);
		
	}
	@Test
	public void testTableProject() throws NonProjectablePotentialException, WrongCriterionException {
		TablePotential tablePotential = 
				treeADD.tableProject(null, null).get(0);
		List<Variable> variables = tablePotential.getVariables();
		assertEquals(3, variables.size());
		assertEquals(16, tablePotential.values.length);
		double []projectedValues = {0.7, 0.3, 0.8, 0.2, 0.7, 0.3, 0.8, 0.2, 0.7, 0.3, 0.1, 0.9, 0.6, 0.4, 0.6, 0.4};
		
		assertEquals(projectedValues[0], tablePotential.values[0], 0.1);
		assertEquals(projectedValues[1], tablePotential.values[1], 0.1);
		assertEquals(projectedValues[2], tablePotential.values[2], 0.1);
		assertEquals(projectedValues[3], tablePotential.values[3], 0.1);
		assertEquals(projectedValues[4], tablePotential.values[4], 0.1);
		assertEquals(projectedValues[5], tablePotential.values[5], 0.1);
		assertEquals(projectedValues[6], tablePotential.values[6], 0.1);
		assertEquals(projectedValues[7], tablePotential.values[7], 0.1);
		assertEquals(projectedValues[8], tablePotential.values[8], 0.1);
		assertEquals(projectedValues[9], tablePotential.values[9], 0.1);
		assertEquals(projectedValues[10], tablePotential.values[10], 0.1);
		assertEquals(projectedValues[11], tablePotential.values[11], 0.1);
		assertEquals(projectedValues[12], tablePotential.values[12], 0.1);
		assertEquals(projectedValues[13], tablePotential.values[13], 0.1);
		assertEquals(projectedValues[14], tablePotential.values[14], 0.1);
		assertEquals(projectedValues[15], tablePotential.values[15], 0.1);
		
    	
	}
}
