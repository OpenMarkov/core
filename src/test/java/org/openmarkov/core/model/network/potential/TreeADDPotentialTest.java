package org.openmarkov.core.model.network.potential;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.LabelledLink;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.treeadd.BranchData;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.SimpleMarkovModelType;

public class TreeADDPotentialTest {

	private ProbNet probNet;
	
	private PNConstraint networkConstraint;
	
	private Variable variableA;
	
	private Variable variableB;
	
	private State absent;
	
	private State present;
	
	private PotentialRole role;
	
	private NodeType nodeType;
	
	private TablePotential potentialvaluesA;
	
	private TablePotential potentialBA;
	
	private TablePotential potentialBA0;
	
	private TablePotential potentialBA1;
	
	private TreeADDPotential treeADD;
	
	private Graph graph; 
	
	private Node nodeA;
	
	private Node nodeBA0;
	
	private Node nodeBA1;
	
	private ArrayList<Variable> listA;
	
	private ArrayList<Variable> listB;
	
	private ArrayList<Variable> listBA;
	
	private ArrayList<Variable> variablesBA;
	
	private ArrayList<Potential> leaves;
	
	private Variable startVariable;
	
	private Node startNode;
	
	private BranchData branchData0;
	
	private BranchData branchData1;
	
	private Node branchNode0;
		
	private Node branchNode1;
	
	private LabelledLink labelledlink0;
	
	private LabelledLink labelledlink1;
	
	@Before
    public void setUp() throws Exception {
		
		
		 // create variables
		variableA = new Variable("A", 2);
		variableB = new Variable("B", 2); 
		
		// set variable states
		absent = new State("absent");
		present = new State("present");
		State [] states= {absent, present};
				
		variableA.setStates(states);
		variableB.setStates(states);
		
		// create table potential P(a)
		listA = new ArrayList<Variable>(1);
		double [] tableA ={0.9, 0.1};
		listA.add(variableA);
		role = PotentialRole.CONDITIONAL_PROBABILITY;
		potentialvaluesA = new TablePotential(listA, role, tableA);
		
		// create subpotentials (leaves of the treeADD)
		listB = new ArrayList<Variable>(1);
		double [] tableBA0 ={1.0, 0.0};
		double [] tableBA1 ={0.9, 0.1};
		listB.add(variableB);
		potentialBA0= new TablePotential(listB, role, tableBA0);// leaf potential
		potentialBA1= new TablePotential(listB, role, tableBA1);// leaf potential
		
				
		// create treeADD potential P(b|a)
		listBA = new ArrayList<Variable>(2);
		listBA.add(variableB);
		listBA.add(variableA);
		
		// create graph
		graph = new Graph();
		graph.makeLinksExplicit(true);
		
		nodeA = new Node(graph, variableA);// object stored variable A
		nodeBA0 = new Node(graph, potentialBA0);// object stored table potential
		nodeBA1 = new Node(graph, potentialBA1);// object stored table potential
		
		//create branches
		startVariable = variableA;
		startNode = nodeA;
		branchNode0 = nodeBA0;
		branchNode1 = nodeBA1;
		branchData0= new BranchData(startVariable);
		branchData1= new BranchData(startVariable);
		branchData0.add(absent);
		branchData1.add(present);
		
		// Append the new 'states' branch to the tree
		labelledlink0 = new LabelledLink (startNode, branchNode0, true, branchData0);
		labelledlink1 = new LabelledLink (startNode, branchNode1, true, branchData1);
		
		// create treeADD
		treeADD = new TreeADDPotential(listBA, graph, role);
		treeADD.setConditionedVariable(variableB);
		
		//leaves of the tree
		leaves = new ArrayList<Potential>(2);
		leaves.add(potentialBA0);
		leaves.add(potentialBA1);
		
		
		treeADD.setPotentials(leaves);
		
		//SMMContraint Simple Markov Model
		probNet = new ProbNet(SimpleMarkovModelType.getUniqueInstance());
		
		nodeType = NodeType.CHANCE;
		
		probNet.addVariable(variableA, nodeType);
		probNet.addVariable(variableB, nodeType);
		probNet.addLink(variableA, variableB, true);
		probNet.addPotential(potentialvaluesA);
		probNet.addPotential(treeADD);
		
		
		ArrayList<Potential> potentials = probNet.getPotentials();
		for (Potential potential : potentials) {
			if (potential.getPotentialType() == PotentialType.TREE_ADD) {
				this.treeADD = (TreeADDPotential) potential;
				variableB = potential.getVariable(0);
				variableA = potential.getVariable(1);
			}
		}
	}
	
	@Test
	public void testTableProject() 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
			WrongCriterionException, InvalidStateException, 
			IncompatibleEvidenceException {
								
		//InferenceOptions options = new InferenceOptions(probNet, variableB);
		TablePotential tablePotential = 
				treeADD.tableProject(null, null).get(0);
		ArrayList<Variable> variables = tablePotential.getVariables();
		assertEquals(2, variables.size());
		assertEquals(1.0, tablePotential.values[0]);
		assertEquals(0.0, tablePotential.values[1]);
		assertEquals(0.9, tablePotential.values[2]);
		assertEquals(0.1, tablePotential.values[3]);
		
		Finding bFinding = new Finding(variableB, 0);
		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(bFinding);
		tablePotential = 
			treeADD.tableProject(evidence, null).get(0);
		variables = tablePotential.getVariables();
		assertEquals(1, variables.size());
		assertEquals(1.0, tablePotential.values[0]);
		assertEquals(0.9, tablePotential.values[1]);

		Finding aFinding = new Finding(variableA, 1);
		evidence = new EvidenceCase();
		evidence.addFinding(aFinding);
		tablePotential = 
			treeADD.tableProject(evidence, null).get(0);
		variables = tablePotential.getVariables();
		assertEquals(1, variables.size());
		assertEquals(0.9, tablePotential.values[0]);
		assertEquals(0.1, tablePotential.values[1]);
	}

	@Test
	public void testShift() {
		// TODO
	}

	@Test
	public void testGetInducedFindings() {
		// TODO
	}

}
