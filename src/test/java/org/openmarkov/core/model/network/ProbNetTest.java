/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network;

/** @author marias */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.constraint.ConstraintManager;
import org.openmarkov.core.model.network.constraint.MaxNumParents;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;


public class ProbNetTest {

	private ProbNet emptyProbNet;
	
	private ProbNet peque;
	
	/** ProbNet for test: Two chance nodes A --> B, one decision D, B --> D;
	 * one utility U, A --> U, D --> U. */
	private ProbNet simpleProbNet;
	
	private ProbNet pruebaInferencia;
	
	private Variable A;
	
	private Variable B; 
	
	private Variable D;
	
	private Variable variableA;
	
	private Variable variableB; 
	
	private Variable variableC;
	
	private Variable variableD;
	
	private Variable variableE; 
	
	private Variable variableF;
	
	private Variable variableG;
	
	private Variable variableH; 
	
	private Variable variableI;
	
	private Variable U;
	
	
	private ArrayList<Variable> aVariables;
	
	private ArrayList<Variable> abVariables;
	
	private ArrayList<Variable> adVariables;
	
	private ArrayList<Variable> variablesA;
	
	private ArrayList<Variable> variablesBA;
	
	private ArrayList<Variable> variablesCAB;
	
	private ArrayList<Variable> variablesCA;
	
	private ArrayList<Variable> variablesEBC;
	
	private ArrayList<Variable> variablesFE;
	
	private ArrayList<Variable> variablesGD;
	
	private ArrayList<Variable> variablesI;	
	
	private ArrayList<Variable> variablesDBI;
	
	private ArrayList<Variable> variablesba;
	
	private ArrayList<Variable> variablesAH;
	
	private ArrayList<Variable> variablesH;
	
	
	
	private TablePotential potentialvaluesA;
	
	private TablePotential potentialvaluesBA;
	
	private TablePotential potentialvaluesCAB;
	
	private TablePotential potentialvaluesCA;
	
	private TablePotential potentialvaluesEBC;
	
	private TablePotential potentialvaluesFE;
	
	private TablePotential potentialvaluesGD;
	
	private TablePotential potentialvaluesI;
	
	private TablePotential potentialvaluesDBI;
	
	private TablePotential potentialvaluesba;
	
	private TablePotential potentialvaluesAH;
	
	private TablePotential potentialvaluesH;
	
	
	
	private State absent;
	
	private State present;

	private TablePotential pA;
	
	private TablePotential pBA;
	
	private TablePotential pU;
	
	private EvidenceCase simpleEvidence;
	
	private PotentialRole role;
	
	// private Finding eB;
	
	private Finding eA;

	@Before
	public void setUp() throws Exception {
		emptyProbNet = new ProbNet();

		// create simpleProbNet
		// create variables
		A = new Variable("A", 2);
		B = new Variable("B", 2); 
		D = new Variable("D", 2);
		U = new Variable("U");
		// create Arrays of variables used in potentials
		aVariables = new ArrayList<Variable>(1);
		aVariables.add(A);
		abVariables = new ArrayList<Variable>(2);
		abVariables.add(B);
		abVariables.add(A);
		adVariables = new ArrayList<Variable>(2);
		adVariables.add(A);
		adVariables.add(D);
		// create potentials
		pA = new TablePotential(
				aVariables, PotentialRole.CONDITIONAL_PROBABILITY);
		pA.values[0] = 0.9;  pA.values[1] = 0.1;
		pBA = new TablePotential(
				abVariables, PotentialRole.CONDITIONAL_PROBABILITY);
		pBA.values[0] = 0.2; pBA.values[1] = 0.8; 
		pBA.values[2] = 0.9; pBA.values[3] = 0.1; 
		pU = new TablePotential(
				adVariables, PotentialRole.CONDITIONAL_PROBABILITY);
		pU.setUtilityVariable(U);
		pU.values[0] = 1; pU.values[1] = 2;
		pU.values[2] = 3; pU.values[3] = 4;
		simpleProbNet = new ProbNet();
		simpleProbNet.addConstraint(new NoCycle(), true);
		simpleProbNet.addConstraint(new OnlyDirectedLinks(), true);
		// add potentials and variables
		simpleProbNet.addPotential(pA); // add variable and potential
		simpleProbNet.addProbNode(D, NodeType.DECISION);
		simpleProbNet.addPotential(pU);
		simpleProbNet.addPotential(pBA);
		simpleProbNet.addLink(B, D, true);
		eA = new Finding(A, 0);
		// eB = new Finding(B, 1);
		simpleEvidence = new EvidenceCase();
		simpleEvidence.addFinding(eA);
	
		
		//probNet peque					
		//Variables
		String a = new String("A");
		String b = new String("B");
		String c = new String("C");
		
		//finite States variables
		variableA = new Variable(a,2);
		variableB = new Variable(b,2);
		variableC = new Variable(c,2);
			
				
		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");
			
		variableA.setAdditionalProperty(relevance,value);
		variableB.setAdditionalProperty(relevance,value);
		variableC.setAdditionalProperty(relevance,value);
		
		//Setting variable states
		absent = new State("ausente");
		present = new State("presente");
		State [] states= {absent, present};
		
		variableA.setStates(states);
		variableB.setStates(states);
		variableC.setStates(states); 
		
		//Potentials
		//PotentialType type = PotentialType.TABLE;
		role = PotentialRole.CONDITIONAL_PROBABILITY;
		
		//Potential A
		double [] tableA ={0.2, 0.8};
		
		variablesA = new ArrayList<Variable>();
		variablesA.add(variableA);
		
		potentialvaluesA= new TablePotential(variablesA,role, tableA);
		
		
		//Potential BA
		double [] tableBA ={0.7, 0.3, 0.9, 0.1};
		
		variablesBA = new ArrayList<Variable>();;
		variablesBA.add(variableB);
		variablesBA.add(variableA);
		
		
		potentialvaluesBA= new TablePotential(variablesBA,role,tableBA);
		
		//potencial CAB
		double [] tableCAB ={0.15, 0.29, 0.84, 0.98, 0.85, 0.71, 0.16, 0.02};
		
		variablesCAB = new ArrayList<Variable>();
		variablesCAB.add(variableC);
		variablesCAB.add(variableA);
		variablesCAB.add(variableB);
		
		
		potentialvaluesCAB= new TablePotential(variablesCAB,role,tableCAB);
		
		peque = new ProbNet();
		
		NodeType nodeType = NodeType.CHANCE;
		
		peque.addProbNode(variableA, nodeType);
		peque.addProbNode(variableB, nodeType);
		peque.addProbNode(variableC, nodeType);
		
		//Links throws NodeNotFoundException
		try {
			peque.addLink(variableA, variableB, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		try {
			peque.addLink(variableA, variableC, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		try {
			peque.addLink(variableB, variableC, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		
		
		peque.addPotential((Potential)potentialvaluesA);
		peque.addPotential((Potential)potentialvaluesBA);
		peque.addPotential((Potential)potentialvaluesCAB);
		
		
		//ProbNet pruebaInferencia
				
				//Variables
								
				//finite States variables
				variableA = new Variable(a,2);
				variableB = new Variable(b,2);
				variableC = new Variable(c,2);
				variableD = new Variable("D",2);
				variableE = new Variable("E",2);
				variableF = new Variable("F",2);
				variableG = new Variable("G",2);
				variableH = new Variable("H",2);
				variableI = new Variable("I",2);	
								
				variableA.setAdditionalProperty(relevance,value);
				variableB.setAdditionalProperty(relevance,value);
				variableC.setAdditionalProperty(relevance,value);
				variableD.setAdditionalProperty(relevance,value);
				variableE.setAdditionalProperty(relevance,value);
				variableF.setAdditionalProperty(relevance,value);
				variableG.setAdditionalProperty(relevance,value);
				variableH.setAdditionalProperty(relevance,value);
				variableI.setAdditionalProperty(relevance,value);
				
				variableA.setStates(states);
				variableB.setStates(states);
				variableC.setStates(states); 
				variableD.setStates(states);
				variableE.setStates(states);
				variableF.setStates(states); 
				variableG.setStates(states);
				variableH.setStates(states);
				variableI.setStates(states); 
				
				//Potentials
				//PotentialType type = PotentialType.TABLE;
				role = PotentialRole.CONDITIONAL_PROBABILITY;
				
				//Potential CA
				double [] tableCA ={0.81, 0.19, 0.98, 0.02};
				variablesCA = new ArrayList<Variable>();
				variablesCA.add(variableC);
				variablesCA.add(variableA);
				
				potentialvaluesCA= new TablePotential(variablesCA,role, tableCA);
				
				
				//Potential EBC
				double [] tableEBC ={0.02, 0.98, 0.68, 0.32, 0.24, 0.76, 0.79, 0.21};
				variablesEBC = new ArrayList<Variable>();
				variablesEBC.add(variableE);
				variablesEBC.add(variableB);
				variablesEBC.add(variableC);
				
				potentialvaluesEBC= new TablePotential(variablesEBC,role,tableEBC);
				
				//potentialFE
				double [] tableFE ={0.12, 0.88, 0.77, 0.23};
				variablesFE = new ArrayList<Variable>();
				variablesFE.add(variableF);
				variablesFE.add(variableE);
							
				
						potentialvaluesFE= new TablePotential(variablesFE,role,tableFE);
				
				//potentialGD
				double [] tableGD ={0.49, 0.51, 0.75, 0.25};				
				variablesGD = new ArrayList<Variable>();
				variablesGD.add(variableG);
				variablesGD.add(variableD);
							
				
						potentialvaluesGD= new TablePotential(variablesGD,role,tableGD);
				
				//Potential I
				double [] tableI ={0.85, 0.15};
				
				variablesI= new ArrayList<Variable>();
				variablesI.add(variableI);
				
				potentialvaluesI= new TablePotential(variablesI,role, tableI);
				
				//Potential DBI
				double [] tableDBI ={0.22, 0.78, 0.86, 0.14, 0.57, 0.43, 0.9, 0.1};
				variablesDBI = new ArrayList<Variable>();
				variablesDBI.add(variableD);
				variablesDBI.add(variableB);
				variablesDBI.add(variableI);
				
				potentialvaluesDBI= new TablePotential(variablesDBI,role,tableDBI);
				
				//potentialBA
				double [] tableba ={0.77, 0.23, 0.26, 0.74};
				variablesba = new ArrayList<Variable>();
				variablesba.add(variableB);
				variablesba.add(variableA);
							
				
						potentialvaluesba= new TablePotential(variablesba,role,tableba);
				
				//potentialAH
				double [] tableAH ={0.09, 0.91, 0.83, 0.17};
				variablesAH = new ArrayList<Variable>();
				variablesAH.add(variableA);
				variablesAH.add(variableH);
							
				
						potentialvaluesAH= new TablePotential(variablesAH,role,tableAH);
				
				//Potential H
				double [] tableH ={0.68, 0.32};
				variablesH= new ArrayList<Variable>();
				variablesH.add(variableH);
				
				potentialvaluesH= new TablePotential(variablesH,role, tableH);
				
				pruebaInferencia = new ProbNet();
				
							
				pruebaInferencia.addProbNode(variableA, nodeType);
				pruebaInferencia.addProbNode(variableB, nodeType);
				pruebaInferencia.addProbNode(variableC, nodeType);
				pruebaInferencia.addProbNode(variableD, nodeType);
				pruebaInferencia.addProbNode(variableE, nodeType);
				pruebaInferencia.addProbNode(variableF, nodeType);
				pruebaInferencia.addProbNode(variableG, nodeType);
				pruebaInferencia.addProbNode(variableH, nodeType);
				pruebaInferencia.addProbNode(variableI, nodeType);
				
				//Links throws NodeNotFoundException
				try {
					pruebaInferencia.addLink(variableA, variableB, true);
				} catch (NodeNotFoundException e) {
					e.printStackTrace();
				}
				try {
					pruebaInferencia.addLink(variableA, variableC, true);
				} catch (NodeNotFoundException e) {
					e.printStackTrace();
				}
				try {
					pruebaInferencia.addLink(variableB, variableD, true);
				} catch (NodeNotFoundException e) {
					e.printStackTrace();
				}
				
				pruebaInferencia.addLink(variableB, variableE, true);
				pruebaInferencia.addLink(variableC, variableE, true);
				pruebaInferencia.addLink(variableD, variableG, true);
				pruebaInferencia.addLink(variableE, variableF, true);
				pruebaInferencia.addLink(variableH, variableA, true);
				pruebaInferencia.addLink(variableI, variableD, true);
				
				
				pruebaInferencia.addPotential((Potential)potentialvaluesCA);
				pruebaInferencia.addPotential((Potential)potentialvaluesEBC);
				pruebaInferencia.addPotential((Potential)potentialvaluesFE);
				pruebaInferencia.addPotential((Potential)potentialvaluesGD);
				pruebaInferencia.addPotential((Potential)potentialvaluesI);
				pruebaInferencia.addPotential((Potential)potentialvaluesDBI);
				pruebaInferencia.addPotential((Potential)potentialvaluesba);
				pruebaInferencia.addPotential((Potential)potentialvaluesH);
				pruebaInferencia.addPotential((Potential)potentialvaluesAH);
				
	
	}

	@Test
	public void testProbNet() {
		// Test empty probabilistic network.
		// By default a ProbNet is a Bayesian Network
		int numBNConstraints = ConstraintManager.getUniqueInstance ().
				buildConstraintList (BayesianNetworkType.getUniqueInstance ()).
				size();
		assertEquals(numBNConstraints, emptyProbNet.getConstraints().size()); // No constraints
		for (NodeType nodeType : NodeType.values()) { // No nodes of every type
			assertEquals(0, emptyProbNet.getNumNodes(nodeType));
		}
	}

	@Test
	public void testAddConstraint() {
		// By default a ProbNet is a Bayesian Network
		int numBNConstraints = ConstraintManager.getUniqueInstance ().
				buildConstraintList (BayesianNetworkType.getUniqueInstance ()).
				size();
		try {
			emptyProbNet.addConstraint(new MaxNumParents(), true);
		} catch (ConstraintViolationException e) {
			fail("Fail in testAddConstraint()");
		}
		List<PNConstraint> constraints = emptyProbNet.getConstraints();
		assertEquals(numBNConstraints + 1, constraints.size());
	}

	@Test
	public void testRemoveConstraint() {
		PNConstraint constraint = new MaxNumParents();
		// By default a ProbNet is a Bayesian Network
		int numBNConstraints = ConstraintManager.getUniqueInstance ().
				buildConstraintList (BayesianNetworkType.getUniqueInstance ()).
				size();
		try {
			emptyProbNet.addConstraint(constraint, true);
		} catch (ConstraintViolationException e) {
			fail("Fail in testRemoveConstraint()");
		}
		emptyProbNet.removeConstraint(constraint);
		List<PNConstraint> constraints = emptyProbNet.getConstraints();
		assertEquals(numBNConstraints, constraints.size());		
	}

	@Test
	public void testGetNumPotentials() {
		assertEquals(3, simpleProbNet.getNumPotentials());
	}

	@Test
	public void testAddPotential() {
		ProbNet probNet = new ProbNet();
		// Add a potential and a variable
		probNet.addPotential(pA);
		ProbNode probNodeA = probNet.getProbNode(A);
		assertNotNull(probNodeA);
		assertTrue(probNodeA.getPotentials().contains(pA));
		// Add a potential with two variables. Both variables exists in probNet
		try {
			probNet.addProbNode(B, NodeType.CHANCE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		probNet.addPotential(pBA);
		assertEquals(2, probNet.getNumPotentials());
		ProbNode probNodeB = probNet.getProbNode(B);
		assertEquals(pBA, probNodeB.getPotentials().get(0));
	}

	@Test
	public void testGetProbNodeString() throws ProbNodeNotFoundException {
		ProbNode probNodeA = simpleProbNet.getProbNode("A");
		assertNotNull(probNodeA);
		ProbNode probNodeB = simpleProbNet.getProbNode("B");
		assertNotNull(probNodeB);
		ProbNode probNodeD = simpleProbNet.getProbNode("D");
		assertNotNull(probNodeD);
	}

	@Test
	public void testAddLink() throws ProbNodeNotFoundException {
		ProbNode probNodeA = simpleProbNet.getProbNode("A");
		ProbNode probNodeB = simpleProbNet.getProbNode("B");
		Node nodeA = probNodeA.getNode();
		Node nodeB = probNodeB.getNode();
		List<Node> AChildren = nodeA.getChildren();
		assertTrue(AChildren.contains(nodeB)); // test addLink
		List<Node> BParents = nodeB.getParents();
		assertTrue(BParents.contains(nodeA)); // test addLink
		assertEquals(1, BParents.size());// test that addLink adds only one link
	}

	@Test
	public void testGetProbNodes() {
	    List<ProbNode> probNodes = simpleProbNet.getProbNodes();
		assertEquals(4, probNodes.size());
	}

	@Test
	public void testGetVariables() {
	    List<Variable> variables = 
			simpleProbNet.getChanceAndDecisionVariables();
		assertEquals(3, variables.size());
		assertTrue(variables.contains(A));
		assertTrue(variables.contains(B));
		assertTrue(variables.contains(D));
	}
	
	@Test
	public void testGetVariablesArrayListOfNode() {
	    List<ProbNode> probNodes = simpleProbNet.getProbNodes();
		List<Node> nodes = ProbNet.getNodesOfProbNodes(probNodes);
		List<Variable> variables = ProbNet.getVariables(nodes);
		assertTrue(variables.contains(A));
		assertTrue(variables.contains(B));
		assertTrue(variables.contains(D));
	}

	@Test
	public void testGetNumNodes() {
		assertEquals(4, simpleProbNet.getNumNodes());
	}

	@Test
	public void testGetPotentials() {
	    List<Potential> potentials = simpleProbNet.getPotentials();
		assertTrue(potentials.contains(pA));
		assertTrue(potentials.contains(pBA));
		assertTrue(potentials.contains(pU));
	}

	@Test
	/** Just make sure that the class <code>ProbNet</code> returns the correct
	 *  number of potentials with the correct variables. */
	public void testGetProjectedPotentials() 
	throws NoFindingException, 
	NonProjectablePotentialException, WrongCriterionException {
	    List<? extends Potential> projectedPotentials = 
			simpleProbNet.tableProjectPotentials(simpleEvidence);
		assertEquals(3, projectedPotentials.size());
		boolean constantPotentialFound = false;
		boolean bPotentialFound = false;
		boolean utilityPotentialFound = false;
		for (Potential potential : projectedPotentials) {
		    List<Variable> potentialVariables = potential.getVariables();
			constantPotentialFound =  constantPotentialFound || 
			    potentialVariables.size() == 0;
			bPotentialFound = bPotentialFound || 
			    (potentialVariables.contains(B) && 
			     potentialVariables.size() == 1);
			utilityPotentialFound = utilityPotentialFound ||
				(potential.getUtilityVariable() != null &&
						potential.getUtilityVariable().equals(U) && 
						potentialVariables.size() == 1 
						&& potential.isUtility()
				&& potentialVariables.contains(D));
		}
		assertTrue(constantPotentialFound);
		assertTrue(bPotentialFound);
		assertTrue(utilityPotentialFound);
	}

	@Test
	public void testGetProbNodesArrayListOfVariable() {
	    List<Variable> abVariables = new ArrayList<Variable>();
		for (Variable variable : this.abVariables) {
			abVariables.add(variable);
		}
		List<ProbNode> chanceProbNodes = 
			simpleProbNet.getProbNodes(abVariables);
		assertEquals(2, chanceProbNodes.size());
		Variable variable0 = chanceProbNodes.get(0).getVariable();
		Variable variable1 = chanceProbNodes.get(1).getVariable();
		if (variable0 == A) {
			assertEquals(variable1, B);
		} else {
			assertEquals(variable0, B);
			assertEquals(variable1, A);
		}
	}

	@Test
	public void testGetNumNodesNodeType() {
		assertEquals(2, simpleProbNet.getNumNodes(NodeType.CHANCE));
		assertEquals(1, simpleProbNet.getNumNodes(NodeType.DECISION));
		assertEquals(1, simpleProbNet.getNumNodes(NodeType.UTILITY));
	}

	@Test
	public void testGetProbNodesNodeType() {
		// test chance nodes
	    List<ProbNode> chanceProbNodes = 
			simpleProbNet.getProbNodes(NodeType.CHANCE);
		assertEquals(2, chanceProbNodes.size());
		Variable variable0 = chanceProbNodes.get(0).getVariable();
		Variable variable1 = chanceProbNodes.get(1).getVariable();
		if (variable0 == A) {
			assertEquals(variable1, B);
		} else {
			assertEquals(variable0, B);
			assertEquals(variable1, A);
		}
		// test decision nodes
		List<ProbNode> decisionProbNodes = 
			simpleProbNet.getProbNodes(NodeType.DECISION);
		assertEquals(1, decisionProbNodes.size());
		assertEquals(D, decisionProbNodes.get(0).getVariable());
		// test utility nodes
		List<ProbNode> utilityProbNodes = 
			simpleProbNet.getProbNodes(NodeType.UTILITY);
		assertEquals(1, utilityProbNodes.size());
		ProbNode utilityNode = utilityProbNodes.get(0);
		assertTrue(utilityNode.getPotentials().contains(pU));
	}

	@Test
	public void testGetPotentialsVariable() {
	    List<Potential> potentials = simpleProbNet.getPotentials(A);
		assertEquals(3, potentials.size());
		assertTrue(potentials.contains(pA));
		assertTrue(potentials.contains(pBA));
		assertTrue(potentials.contains(pU));
	}

	@Test
	public void testGetPotentialsType() {
		// test chance potentials
	    List<Potential> chancePotentials = 
			simpleProbNet.getPotentialsByType(NodeType.CHANCE);
		assertEquals(2, chancePotentials.size());
		assertTrue(chancePotentials.contains(pA));
		assertTrue(chancePotentials.contains(pBA));
		
		// test utility potentials
		List<Potential> utilityPotentials = 
			simpleProbNet.getPotentialsByType(NodeType.UTILITY);
		assertEquals(1, utilityPotentials.size());
		assertTrue(utilityPotentials.contains(pU));
	}

	@Test
	public void testGetUtilityPotentials() {
		// test 0 potentials
	    List<Potential> BUtilityPotentials = 
			simpleProbNet.getUtilityPotentials(B);
		assertEquals(0, BUtilityPotentials.size());

		// test 1 potential
		List<Potential> AUtilityPotentials = 
			simpleProbNet.getUtilityPotentials(A);
		assertEquals(1, AUtilityPotentials.size());
		assertEquals(pU, AUtilityPotentials.get(0));
		AUtilityPotentials = 
			simpleProbNet.getUtilityPotentials(D);
		assertEquals(1, AUtilityPotentials.size());
		assertEquals(pU, AUtilityPotentials.get(0));
	}

	@Test
	public void testExtractPotentials() {
	    List<Potential> APotentials = simpleProbNet.extractPotentials(A);
		assertEquals(3, APotentials.size());
		assertTrue(APotentials.contains(pA));
		assertTrue(APotentials.contains(pBA));
		assertTrue(APotentials.contains(pU));
		List<Potential> DPotentials = simpleProbNet.extractPotentials(D);
		assertEquals(1, DPotentials.size());
		assertTrue(DPotentials.contains(pU));
		List<Potential> BPotentials = simpleProbNet.extractPotentials(B);
		assertEquals(1, BPotentials.size());
		assertTrue(BPotentials.contains(pBA));
	}

	@Test
	public void testRemovePotential() {
		simpleProbNet.removePotential(pBA);
		assertEquals(0, simpleProbNet.extractPotentials(B).size());
		assertEquals(2, simpleProbNet.getNumPotentials());
		simpleProbNet.removePotential(pA);
		assertEquals(1, simpleProbNet.extractPotentials(A).size());
		assertEquals(1, simpleProbNet.getNumPotentials());
		simpleProbNet.removePotential(pU);
		assertEquals(0, simpleProbNet.extractPotentials(D).size());
		assertEquals(0, simpleProbNet.getNumPotentials());		
	}

	@Test
	public void testRemovePotentialsProbNode() {
		ProbNode probNodeA = simpleProbNet.getProbNode(A);
		simpleProbNet.removePotentials(probNodeA);
		assertEquals(2, simpleProbNet.getNumPotentials());
		ProbNode probNodeB = simpleProbNet.getProbNode(B);
		simpleProbNet.removePotentials(probNodeB);
		assertEquals(1, simpleProbNet.getNumPotentials());
	}

	@Test
	public void testGetProbNode() throws ProbNodeNotFoundException {
		ProbNode probNodeD = simpleProbNet.getProbNode("D", NodeType.DECISION);
		assertNotNull(probNodeD);
		ProbNode probNodeB = null;
		try {
			probNodeB = simpleProbNet.getProbNode("B", NodeType.DECISION);
		} catch (ProbNodeNotFoundException e) {
		}
		assertNull(probNodeB);
		probNodeB = simpleProbNet.getProbNode("B", NodeType.CHANCE);
		assertNotNull(probNodeB);
	}

	@Test
	public void testGetVariablesNodeType() {
		// test utility variables
	    List<Variable> utilityVariables = 
			simpleProbNet.getVariables(NodeType.UTILITY);
		assertEquals(1, utilityVariables.size());
		ProbNode utilityNode = 
			simpleProbNet.getProbNode(utilityVariables.get(0));
		assertTrue(utilityNode.getPotentials().get(0).isUtility());
		
		// test chance variables
		List<Variable> chanceVariables = 
			simpleProbNet.getVariables(NodeType.CHANCE);
		assertEquals(2, chanceVariables.size());
		assertTrue(chanceVariables.contains(A));
		assertTrue(chanceVariables.contains(B));
		
		// test decision variables
		List<Variable> decisionVariables = 
			simpleProbNet.getVariables(NodeType.DECISION);
		assertEquals(1, decisionVariables.size());
		assertTrue(decisionVariables.contains(D));
	}

	@Test
	public void testRemoveProbNode() {
		ProbNode probNodeB = simpleProbNet.getProbNode(B); 
		simpleProbNet.removeProbNode(probNodeB);
		ProbNode probNodeA = simpleProbNet.getProbNode(A);
		assertEquals(1, probNodeA.getNode().getNumChildren());
		ProbNode probNodeD = simpleProbNet.getProbNode(D);
		assertEquals(0, probNodeD.getNode().getNumParents());
	}

	@Test
	public void testRemoveLink() {
		// No remove because link is directed
		simpleProbNet.removeLink(A, B, false); // It directed would be true
		ProbNode probNodeA = simpleProbNet.getProbNode(A);
		ProbNode probNodeB = simpleProbNet.getProbNode(B); 
		assertTrue(probNodeA.node.getChildren().contains(probNodeB.node));
		// Remove a link
		simpleProbNet.removeLink(A, B, true);
		assertFalse(probNodeA.node.getChildren().contains(probNodeB.node));		
	}

	@Test
	public void testCopy() {
		ProbNet copied = simpleProbNet.copy();
		List<Variable> copiedVariables = 
			copied.getChanceAndDecisionVariables();
		// test variables
		assertEquals(3, copiedVariables.size());
		assertTrue(copiedVariables.contains(A));
		assertTrue(copiedVariables.contains(B));
		assertTrue(copiedVariables.contains(D));
		List<Variable> utilityVariables = 
			copied.getVariables(NodeType.UTILITY);
		assertEquals(1, utilityVariables.size());
		// test potentials
		assertEquals(3, copied.getNumPotentials());
		ProbNode probNodeA = copied.getProbNode(A);
		assertTrue(probNodeA.getPotentials().contains(pA));
		ProbNode probNodeB = copied.getProbNode(B);
		assertTrue(probNodeB.getPotentials().contains(pBA));
		ProbNode probNodeD = copied.getProbNode(D);
		ProbNode probNodeU = 
			(ProbNode)probNodeD.node.getChildren().get(0).getObject();
		assertTrue(probNodeU.getPotentials().contains(pU));
		// test graph structure
		Node nodeA = probNodeA.node;
		Node nodeB = probNodeB.node;
		Node nodeD = probNodeD.node;
		Node nodeU = probNodeU.node;
		assertTrue(nodeA.getChildren().contains(nodeB));
		assertTrue(nodeA.getChildren().contains(nodeU));
		assertEquals(2, nodeA.getNeighbors().size());
		assertTrue(nodeB.getParents().contains(nodeA));
		assertTrue(nodeB.getChildren().contains(nodeD));
		assertEquals(2, nodeB.getNeighbors().size());
		assertTrue(nodeD.getParents().contains(nodeB));
		assertTrue(nodeD.getChildren().contains(nodeU));
		assertEquals(2, nodeD.getNeighbors().size());
		assertTrue(nodeU.getParents().contains(nodeA));
		assertTrue(nodeU.getParents().contains(nodeD));
		assertEquals(2, nodeU.getNeighbors().size());
	}

	@Test
	public void testGetAdditionalConstraints() {
		ProbNet bnProbNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		PNConstraint maxNumParents = new MaxNumParents();
		try {
			bnProbNet.addConstraint(maxNumParents);
		} catch (ConstraintViolationException e) {
			fail("Unreachable code.");
		}
		List<PNConstraint> additionalConstraints = bnProbNet.getAdditionalConstraints();
		assertEquals(1, additionalConstraints.size());
		assertTrue(additionalConstraints.contains(maxNumParents));
	}
	
}
