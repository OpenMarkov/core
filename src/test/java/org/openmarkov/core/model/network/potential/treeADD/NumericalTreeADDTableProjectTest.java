package org.openmarkov.core.model.network.potential.treeADD;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

public class NumericalTreeADDTableProjectTest {
	private TreeADDPotential tree;
	private Variable age;
	@Before
    public void setUp() throws Exception {
		State dead = new State("dead");
		State alive = new State("alive");
		State [] states= {dead, alive};
		Variable previousState = new Variable("state0", 2);
		previousState.setStates(states);
		Variable currentState = new Variable("state1", 2);
		currentState.setStates(states);
		age = new Variable("Age0", true, 0.0, 10.0, true, 0.01);
		
		List<Variable> treeVariables = new ArrayList<>();
		treeVariables.add(currentState);
		treeVariables.add(previousState);
		treeVariables.add(age);
		
		//subtree 
		//branch 1
		Threshold min1 = new Threshold(0, false);
		Threshold max1 = new Threshold(5, true);
		
		List<Variable> table1Variables = new ArrayList<>();
		table1Variables.add(currentState);
		double []tableBranch1 = {1.0, 0.0};
		TablePotential subTablePotential1 = new TablePotential(table1Variables, 
				PotentialRole.CONDITIONAL_PROBABILITY,tableBranch1); 
		List<Variable> subParentVariables = new ArrayList<>();
		subParentVariables.add(currentState);
		subParentVariables.add(age);
		
		TreeADDBranch subBranch1 = new TreeADDBranch(min1,max1, age, subTablePotential1, subParentVariables);
		
		//branch 2
		Threshold min2 = new Threshold(5, true);
		Threshold max2 = new Threshold(10, false);
		
		List<Variable> table2Variables = new ArrayList<>();
		table2Variables.add(currentState);
		double []tableBranch2 = {0.5, 0.5};
		TablePotential subTablePotential2 = new TablePotential(table2Variables, 
				PotentialRole.CONDITIONAL_PROBABILITY,tableBranch2); 
		TreeADDBranch subBranch2 = new TreeADDBranch(min2,max2, age, subTablePotential2, subParentVariables);
		
		List<TreeADDBranch> subBranches = new ArrayList<>();
		subBranches.add(subBranch1);
		subBranches.add(subBranch2);
	
		TreeADDPotential subTree = new TreeADDPotential(subParentVariables, age, PotentialRole.CONDITIONAL_PROBABILITY, subBranches);
		
		//tree
		List<Variable> parentVariables = new ArrayList<>();
		parentVariables.add(previousState);
		parentVariables.add(currentState);
		parentVariables.add(age);
		
		List<State> states1 = new ArrayList<>();
		states1.add(dead);
		double []table1 = {0.0, 1.0};
		TablePotential tablePotential1 = new TablePotential(table2Variables, PotentialRole.CONDITIONAL_PROBABILITY, table1);
		
		TreeADDBranch branch1 = new TreeADDBranch(states1, previousState, tablePotential1, parentVariables); 
		
		List<State> states2 = new ArrayList<>();
		states2.add(alive);
		
		TreeADDBranch branch2 = new TreeADDBranch(states2, previousState, subTree, parentVariables); 
		
		List<Variable> variables = new ArrayList<>();
		variables.add(currentState);
		variables.add(previousState);
		variables.add(age);
		List<TreeADDBranch> branches = new ArrayList<>();
		branches.add(branch1);
		branches.add(branch2);
		tree = new TreeADDPotential(variables, previousState, PotentialRole.CONDITIONAL_PROBABILITY, branches);
		
	}
	
	@Test
	public void testTableProject() throws NonProjectablePotentialException, WrongCriterionException {
	    List<Finding> findings = new ArrayList<>();
		Finding value = new Finding(age, 0.5);
		findings.add(value);
		EvidenceCase evidenceCase = new EvidenceCase(findings);
		
		TablePotential tablePotential = 
				tree.tableProject(evidenceCase, null).get(0);
		List<Variable> variables = tablePotential.getVariables();
		assertEquals(2, variables.size());
		assertEquals(4, tablePotential.values.length);
		double []projectedValues = {0.0, 1.0, 1.0, 0.0};
		
		assertEquals(projectedValues[0], tablePotential.values[0], 0.1);
		assertEquals(projectedValues[1], tablePotential.values[1], 0.1);
		assertEquals(projectedValues[2], tablePotential.values[2], 0.1);
		assertEquals(projectedValues[3], tablePotential.values[3], 0.1);
		
	}
	
	@Test
	public void testTablePorjectNumericalTop()
			throws NonProjectablePotentialException, WrongCriterionException {
		//Evidence
		ProbNet probNet = NetsFactory.createSemiMarkovOnlyChanceNet();
		List<Finding> findings = new ArrayList<>();
		try {
			findings.add(new Finding(probNet.getVariable("Duration [0]"), 1.0));
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EvidenceCase evidence = new EvidenceCase(findings);
		
		TablePotential tablePotential1;
		try {
			tablePotential1 = probNet.getNode("State [1]").getPotentials().get(0).tableProject(evidence, null).get(0);
			List<Variable> variables = tablePotential1.getVariables();
			assertEquals(2, variables.size());
			assertEquals(4, tablePotential1.values.length);
			assertEquals(0.5, tablePotential1.values[0], 0.1);
			assertEquals(0.5, tablePotential1.values[1], 0.1);
			assertEquals(0.0, tablePotential1.values[2], 0.1);
			assertEquals(1.0, tablePotential1.values[3], 0.1);
		} catch (NodeNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		List<Finding> findings2 = new ArrayList<>();
		try {
			findings2.add(new Finding(probNet.getVariable("Duration [0]"), 2.0));
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EvidenceCase evidence2 = new EvidenceCase(findings2);
		
		TablePotential tablePotential2;
		try {
			tablePotential2 = probNet.getNode("State [1]").getPotentials().get(0).tableProject(evidence2, null).get(0);
			List<Variable> variables2 = tablePotential2.getVariables();
			assertEquals(2, variables2.size());
			assertEquals(4, tablePotential2.values.length);
			assertEquals(0.5, tablePotential2.values[0], 0.1);
			assertEquals(0.5, tablePotential2.values[1], 0.1);
			assertEquals(0.0, tablePotential2.values[2], 0.1);
			assertEquals(1.0, tablePotential2.values[3], 0.1);
		} catch (NodeNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		List<Finding> findings3 = new ArrayList<>();
		try {
			findings3.add(new Finding(probNet.getVariable("Duration [0]"), 2.0));
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EvidenceCase evidence3 = new EvidenceCase(findings3);
		
        TablePotential tablePotential3;
        try
        {
            tablePotential3 = probNet.getNode ("State [1]").getPotentials ().get (0).tableProject (evidence3,
                                                                                                       null).get (0);
            List<Variable> variables3 = tablePotential3.getVariables ();
            assertEquals (2, variables3.size ());
            assertEquals (4, tablePotential3.values.length);
            assertEquals (0.5, tablePotential3.values[0], 0.1);
            assertEquals (0.5, tablePotential3.values[1], 0.1);
            assertEquals (0.0, tablePotential3.values[2], 0.1);
            assertEquals (1.0, tablePotential3.values[3], 0.1);
        }
        catch (NodeNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }
}
