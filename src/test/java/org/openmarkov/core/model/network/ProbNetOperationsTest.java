/**
 * 
 */
package org.openmarkov.core.model.network;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.OpenMarkovTests;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/** @author marias */
public class ProbNetOperationsTest {

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
		simpleProbNet.addVariable(D, NodeType.DECISION);
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
		
		
		//notEnoughMemoryException 
		potentialvaluesCAB= new TablePotential(variablesCAB,role,tableCAB);
		
		peque = new ProbNet();
		
		NodeType nodeType = NodeType.CHANCE;
		
		peque.addVariable(variableA, nodeType);
		peque.addVariable(variableB, nodeType);
		peque.addVariable(variableC, nodeType);
		
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
							
				
				//notEnoughMemoryException 
				potentialvaluesFE= new TablePotential(variablesFE,role,tableFE);
				
				//potentialGD
				double [] tableGD ={0.49, 0.51, 0.75, 0.25};				
				variablesGD = new ArrayList<Variable>();
				variablesGD.add(variableG);
				variablesGD.add(variableD);
							
				
				//notEnoughMemoryException 
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
							
				
				//notEnoughMemoryException 
				potentialvaluesba= new TablePotential(variablesba,role,tableba);
				
				//potentialAH
				double [] tableAH ={0.09, 0.91, 0.83, 0.17};
				variablesAH = new ArrayList<Variable>();
				variablesAH.add(variableA);
				variablesAH.add(variableH);
							
				
				//notEnoughMemoryException 
				potentialvaluesAH= new TablePotential(variablesAH,role,tableAH);
				
				//Potential H
				double [] tableH ={0.68, 0.32};
				variablesH= new ArrayList<Variable>();
				variablesH.add(variableH);
				
				potentialvaluesH= new TablePotential(variablesH,role, tableH);
				
				pruebaInferencia = new ProbNet();
				
							
				pruebaInferencia.addVariable(variableA, nodeType);
				pruebaInferencia.addVariable(variableB, nodeType);
				pruebaInferencia.addVariable(variableC, nodeType);
				pruebaInferencia.addVariable(variableD, nodeType);
				pruebaInferencia.addVariable(variableE, nodeType);
				pruebaInferencia.addVariable(variableF, nodeType);
				pruebaInferencia.addVariable(variableG, nodeType);
				pruebaInferencia.addVariable(variableH, nodeType);
				pruebaInferencia.addVariable(variableI, nodeType);
				
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
	public void testPrune1() throws Exception {
				
		assertNotNull(this.peque);
		ProbNode probNodeA = peque.getProbNode("A");
		ProbNode probNodeB = peque.getProbNode("B");
		Variable A = probNodeA.getVariable();
		Variable B = probNodeB.getVariable();
		Finding findingA = new Finding(A, 1); // A:absent(0)
		HashMap<Variable, Finding> findings = new HashMap<Variable, Finding>();
		findings.put(A, findingA);
		EvidenceCase evidenceCase = new EvidenceCase(findings);
		ArrayList<Variable> variablesOfInterest = new ArrayList<Variable>();
		variablesOfInterest.add(B);
		// test pruned net
		ProbNet pruned = ProbNetOperations.
				getPruned(peque, variablesOfInterest, evidenceCase);
		ProbNetOperations.projectEvidence(pruned, evidenceCase);
		assertEquals(1, pruned.getNumNodes());
		boolean throwNodeNotFound = false;
		try {
			pruned.getVariable("A");
		} catch (ProbNodeNotFoundException e) {
			throwNodeNotFound = true;
		}
		assertTrue(throwNodeNotFound);
		assertNotNull(pruned.getVariable("B"));
		throwNodeNotFound = false;
		try {
			pruned.getVariable("C");
		} catch (ProbNodeNotFoundException e) {
			throwNodeNotFound = true;
		}
		assertTrue(throwNodeNotFound);
		assertEquals(1, pruned.getNumPotentials());
		probNodeB = pruned.getProbNode("B");
		TablePotential bPotential = 
			(TablePotential)probNodeB.getPotentials().get(0);
		assertEquals(1, bPotential.getNumVariables());
		assertTrue(bPotential.contains(B));
		assertEquals(2, ((TablePotential)bPotential).values.length);
		int[] offsets = bPotential.getOffsets();
		assertEquals(1, offsets.length);
		int initialPosition = bPotential.getInitialPosition();
		assertEquals(0, initialPosition);
		double a = bPotential.values[initialPosition];
			assertEquals(a, 0.9,OpenMarkovTests.maxError);
		assertEquals(bPotential.values[initialPosition + offsets[0]], 0.1,OpenMarkovTests.maxError);
	}

	@Test
	/** Test: prune barren nodes and prune parts of the network isolated due to 
	 *  evidence. */
	public void testPrune2() throws Exception {
				
		assertNotNull(pruebaInferencia);
		ProbNode probNodeA = pruebaInferencia.getProbNode("A");
		ProbNode probNodeB = pruebaInferencia.getProbNode("B");
		ProbNode probNodeC = pruebaInferencia.getProbNode("C");
		ProbNode probNodeD = pruebaInferencia.getProbNode("D");
		ProbNode probNodeE = pruebaInferencia.getProbNode("E");
		ProbNode probNodeF = pruebaInferencia.getProbNode("F");
		ProbNode probNodeG = pruebaInferencia.getProbNode("G");
		ProbNode probNodeH = pruebaInferencia.getProbNode("H");
		ProbNode probNodeI = pruebaInferencia.getProbNode("I");
		Variable A = probNodeA.getVariable();
		Variable B = probNodeB.getVariable();
		Variable C = probNodeC.getVariable();
		Variable D = probNodeD.getVariable();
		Variable E = probNodeE.getVariable();
		Variable F = probNodeF.getVariable();
		Variable G = probNodeG.getVariable();
		Variable H = probNodeH.getVariable();
		Variable I = probNodeI.getVariable();
		// Set up evidence: A = 1 and D = 1
		Finding findingA = new Finding(A, 1);
		Finding findingD = new Finding(D, 1);		
		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(findingA);
		evidence.addFinding(findingD);
		// Set up variables of interest: E	
		ArrayList<Variable> variablesOfInterest = new ArrayList<Variable>();
		variablesOfInterest.add(E);
		
		ProbNet pruned = ProbNetOperations.getPruned(
				pruebaInferencia, variablesOfInterest, evidence);
		ProbNetOperations.projectEvidence(pruned, evidence);
		
		ArrayList<Variable> variablesPruned = pruned.getVariables();
		assertTrue(variablesPruned.contains(B));
		assertTrue(variablesPruned.contains(C));
		assertTrue(variablesPruned.contains(E));
		assertTrue(variablesPruned.contains(I));
		assertFalse(variablesPruned.contains(A));
		assertFalse(variablesPruned.contains(D));
		assertFalse(variablesPruned.contains(H));
		assertFalse(variablesPruned.contains(G));
		assertFalse(variablesPruned.contains(F));

		// Test B potentials
		probNodeB = pruned.getProbNode("B");
		ArrayList<Potential> potentialsB = probNodeB.getPotentials();
		assertEquals(2, potentialsB.size());
		// Test projected potential p(B|A), A = 1 = psi(B)
		// Get psi(B)
		TablePotential potential0B = (TablePotential)potentialsB.get(0);
		if (potential0B.getNumVariables() == 2) {
			potential0B = (TablePotential)potentialsB.get(1);
		}
		assertEquals(1, potential0B.getNumVariables());
		assertTrue(potential0B.contains(B));
		assertEquals(2, potential0B.values.length);
		int[] offsets0B = potential0B.getOffsets();
		assertEquals(1, offsets0B.length);
		assertEquals(1, offsets0B[0]);
		int initialPosition = potential0B.getInitialPosition();
		assertEquals(0, initialPosition);
		assertEquals(0.26, potential0B.values[potential0B.getInitialPosition()],OpenMarkovTests.maxError);
		assertEquals(0.74, potential0B.values[
		        potential0B.getInitialPosition() + offsets0B[0]],OpenMarkovTests.maxError);
		// Test projected potential p(D|B,I), D = 1 = psi(B,I)
		TablePotential potential1B = (TablePotential)potentialsB.get(1);
		if (potential1B.getNumVariables() == 1) {
			potential1B = (TablePotential)potentialsB.get(0);
		}
		assertEquals(2, potential1B.getNumVariables());
		assertTrue(potential1B.contains(B));
		assertTrue(potential1B.contains(I));
		
	}
	
	/*@Test
	/** Test: prune barren nodes and prune parts of the network isolated due to 
	 *  evidence. *
	public void testPrune3() throws Exception {
		// Set up
		ProbNet pruebaInferencia = ElviraParser.getUniqueInstance()
			.loadProbNet(IOTests.testsPath + "PruebaInferencia.elv");
		assertNotNull(pruebaInferencia);
		ProbNode probNodeA = pruebaInferencia.getProbNode("A");
		ProbNode probNodeB = pruebaInferencia.getProbNode("B");
		ProbNode probNodeC = pruebaInferencia.getProbNode("C");
		ProbNode probNodeD = pruebaInferencia.getProbNode("D");
		ProbNode probNodeE = pruebaInferencia.getProbNode("E");
		ProbNode probNodeF = pruebaInferencia.getProbNode("F");
		ProbNode probNodeG = pruebaInferencia.getProbNode("G");
//		ProbNode probNodeH = pruebaInferencia.getProbNode("H");
		ProbNode probNodeI = pruebaInferencia.getProbNode("I");
		Variable A = probNodeA.getVariable();
		Variable B = probNodeB.getVariable();
		Variable C = probNodeC.getVariable();
		Variable D = probNodeD.getVariable();
		Variable E = probNodeE.getVariable();
		Variable F = probNodeF.getVariable();
		Variable G = probNodeG.getVariable();
//		Variable H = probNodeH.getVariable();
		Variable I = probNodeI.getVariable();
		// Set up evidence: A = 1 and D = 1
		Finding findingA = new Finding(A, 1);
		Finding findingG = new Finding(G, 1);		
		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(findingA);
		evidence.addFinding(findingG);
		// Set up variables of interest: E	
		ArrayList<Variable> variablesOfInterest = new ArrayList<Variable>();
		variablesOfInterest.add(E);
		
		ProbNet pruned = pruebaInferencia.prune(variablesOfInterest, evidence);
		pruned.projectEvidence(pruned, evidence);
		
		ArrayList<Variable> variablesPruned = pruned.getVariables();
		assertTrue(variablesPruned.contains(B));
		assertTrue(variablesPruned.contains(C));
		assertTrue(variablesPruned.contains(E));
		assertFalse(variablesPruned.contains(A));
		assertFalse(variablesPruned.contains(D));
//		assertFalse(variablesPruned.contains(H));
		assertFalse(variablesPruned.contains(G));
		assertFalse(variablesPruned.contains(F));
		System.err.println(variablesPruned);
		assertEquals(3, variablesPruned.size());

		// Test B potentials
		probNodeB = pruned.getProbNode("B");
		ArrayList<Potential> potentialsB = probNodeB.getPotentials();
		assertEquals(2, potentialsB.size());
		// Test projected potential p(B|A), A = 1 = psi(B)
		TablePotential potential0B = (TablePotential)potentialsB.get(0);
		assertEquals(1, potential0B.getNumVariables());
		assertTrue(potential0B.contains(B));
		assertEquals(4, potential0B.values.length);
		int[] offsets0B = potential0B.getOffsets();
		assertEquals(1, offsets0B.length);
		assertEquals(1, offsets0B[0]);
		int initialPosition = potential0B.getInitialPosition();
		assertEquals(2, initialPosition);
		assertEquals(0.26, potential0B.values[potential0B.getInitialPosition()]);
		assertEquals(0.74, potential0B.values[
		        potential0B.getInitialPosition() + offsets0B[0]]);
		// Test projected potential p(D|B,I), D = 1 = psi(B,I)
		TablePotential potential1B = (TablePotential)potentialsB.get(1);
		assertEquals(2, potential1B.getNumVariables());
		assertTrue(potential1B.contains(B));
		assertTrue(potential1B.contains(I));
		
	}*/
	
	/**
	 * Test method for {@link org.openmarkov.core.model.network.ProbNetOperations#projectEvidence(org.openmarkov.core.model.network.ProbNet, org.openmarkov.core.model.network.EvidenceCase)}.
	 */
	@Test
	public final void testProjectEvidence() {
	}

	/**
	 * Test method for {@link org.openmarkov.core.model.network.ProbNetOperations#removeBarrenNodes(org.openmarkov.core.model.network.ProbNet, java.util.Collection, java.util.HashSet)}.
	 */
	@Test
	public final void testRemoveBarrenNodes() {
	}

	/**
	 * Test method for {@link org.openmarkov.core.model.network.ProbNetOperations#removeUnreachableNodes(org.openmarkov.core.model.network.ProbNet, java.util.Collection, java.util.HashSet)}.
	 */
	@Test
	public final void testRemoveUnreachableNodes() {
	}

}
