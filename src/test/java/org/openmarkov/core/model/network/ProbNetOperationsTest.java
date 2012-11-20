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
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.util.UtilTestMethods;

/** @author marias */
public class ProbNetOperationsTest {

	@Before
	public void setUp() throws Exception {
	}


	@Test
	public void testPrune1() throws Exception {
		
		//probNet peque					
		ProbNet peque;
		
		//Variables
		String aName = new String("A");
		String b = new String("B");
		String c = new String("C");
		
		//finite States variables
		Variable variableA = new Variable(aName,2);
		Variable variableB = new Variable(b,2);
		Variable variableC = new Variable(c,2);
			
				
		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");
			
		variableA.setAdditionalProperty(relevance,value);
		variableB.setAdditionalProperty(relevance,value);
		variableC.setAdditionalProperty(relevance,value);
		
		//Setting variable states
		State absent = new State("ausente");
		State present = new State("presente");
		State [] states= {absent, present};
		
		variableA.setStates(states);
		variableB.setStates(states);
		variableC.setStates(states); 
		
		//Potentials
		//PotentialType type = PotentialType.TABLE;
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
		
		//Potential A
		double [] tableA ={0.2, 0.8};
		
		ArrayList<Variable> variablesA = new ArrayList<Variable>();
		variablesA.add(variableA);
		
		TablePotential potentialvaluesA= new TablePotential(variablesA,role, tableA);
		
		
		//Potential BA
		double [] tableBA ={0.7, 0.3, 0.9, 0.1};
		
		ArrayList<Variable> variablesBA = new ArrayList<Variable>();
		variablesBA.add(variableB);
		variablesBA.add(variableA);
		
		
		TablePotential potentialvaluesBA = new TablePotential(variablesBA,role,tableBA);
		
		//potencial CAB
		double [] tableCAB ={0.15, 0.29, 0.84, 0.98, 0.85, 0.71, 0.16, 0.02};
		
		ArrayList<Variable> variablesCAB = new ArrayList<Variable>();
		variablesCAB.add(variableC);
		variablesCAB.add(variableA);
		variablesCAB.add(variableB);
		
		
		//notEnoughMemoryException 
		TablePotential potentialvaluesCAB = new TablePotential(variablesCAB,role,tableCAB);
		
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
				
		/** ProbNet for test: Two chance nodes A --> B, one decision D, B --> D;
		 * one utility U, A --> U, D --> U. */
		ProbNet simpleProbNet;
		
		ProbNet pruebaInferencia;
		
		Variable A;
		
		Variable B; 
		
		Variable D;
		
		Variable variableA = new Variable("A", 2);
		
		Variable variableB = new Variable("B", 2); 
		
		Variable variableC = new Variable("C", 2);
		
		Variable variableD = new Variable("D", 2);
		
		Variable variableE = new Variable("E", 2);
		
		Variable variableF = new Variable("F", 2);
		
		Variable variableG = new Variable("G", 2);
		
		Variable variableH = new Variable("H", 2);
		
		Variable variableI = new Variable("I", 2);
		
		Variable U;
		
		
		ArrayList<Variable> aVariables;
		
		ArrayList<Variable> baVariables;
		
		ArrayList<Variable> adVariables;
		
		ArrayList<Variable> variablesCA;
		
		ArrayList<Variable> variablesEBC;
		
		ArrayList<Variable> variablesFE;
		
		ArrayList<Variable> variablesGD;
		
		ArrayList<Variable> variablesI;	
		
		ArrayList<Variable> variablesDBI;
		
		ArrayList<Variable> variablesba;
		
		ArrayList<Variable> variablesAH;
		
		ArrayList<Variable> variablesH;
		
		TablePotential potentialvaluesCA;
		
		TablePotential potentialvaluesEBC;
		
		TablePotential potentialvaluesFE;
		
		TablePotential potentialvaluesGD;
		
		TablePotential potentialvaluesI;
		
		TablePotential potentialvaluesDBI;
		
		TablePotential potentialvaluesBA;
		
		TablePotential potentialvaluesAH;
		
		TablePotential potentialvaluesH;
		
		TablePotential pA;
		
		TablePotential pBA;
		
		TablePotential pU;
		
		EvidenceCase simpleEvidence;
		
		PotentialRole role;
		
		Finding eA;

		// create simpleProbNet
		// create variables
		A = new Variable("A", 2);
		B = new Variable("B", 2); 
		D = new Variable("D", 2);
		U = new Variable("U");
		// create Arrays of variables used in potentials
		aVariables = new ArrayList<Variable>(1);
		aVariables.add(A);
		baVariables = new ArrayList<Variable>(2);
		baVariables.add(B);
		baVariables.add(A);
		adVariables = new ArrayList<Variable>(2);
		adVariables.add(A);
		adVariables.add(D);
		// create potentials
		pA = new TablePotential(
				aVariables, PotentialRole.CONDITIONAL_PROBABILITY);
		pA.values[0] = 0.9;  pA.values[1] = 0.1;
		pBA = new TablePotential(
				baVariables, PotentialRole.CONDITIONAL_PROBABILITY);
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
	
		
		
		//ProbNet pruebaInferencia
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
		potentialvaluesBA= new TablePotential(variablesba,role,tableba);

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

		NodeType nodeType = NodeType.CHANCE;

		pruebaInferencia.addVariable(variableA, nodeType);
		pruebaInferencia.addVariable(variableB, nodeType);
		pruebaInferencia.addVariable(variableC, nodeType);
		pruebaInferencia.addVariable(variableD, nodeType);
		pruebaInferencia.addVariable(variableE, nodeType);
		pruebaInferencia.addVariable(variableF, nodeType);
		pruebaInferencia.addVariable(variableG, nodeType);
		pruebaInferencia.addVariable(variableH, nodeType);
		pruebaInferencia.addVariable(variableI, nodeType);

		pruebaInferencia.addLink(variableA, variableB, true);
		pruebaInferencia.addLink(variableA, variableC, true);
		pruebaInferencia.addLink(variableB, variableD, true);
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
		pruebaInferencia.addPotential((Potential)potentialvaluesBA);
		pruebaInferencia.addPotential((Potential)potentialvaluesH);
		pruebaInferencia.addPotential((Potential)potentialvaluesAH);

		// Set up evidence: A = 1 and D = 1
		Finding findingA = new Finding(variableA, 1);
		Finding findingD = new Finding(variableD, 1);		
		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(findingA);
		evidence.addFinding(findingD);
		// Set up variables of interest: E	
		ArrayList<Variable> variablesOfInterest = new ArrayList<Variable>();
		variablesOfInterest.add(variableE);
		
		ProbNet pruned = ProbNetOperations.getPruned(
				pruebaInferencia, variablesOfInterest, evidence);
		ProbNetOperations.projectEvidence(pruned, evidence);
		
		ArrayList<Variable> variablesPruned = pruned.getVariables();
		assertFalse(variablesPruned.contains(variableA));
		assertTrue(variablesPruned.contains(variableB));
		assertTrue(variablesPruned.contains(variableC));
		assertFalse(variablesPruned.contains(variableD));
		assertTrue(variablesPruned.contains(variableE));
		assertFalse(variablesPruned.contains(variableF));
		assertFalse(variablesPruned.contains(variableG));
		assertFalse(variablesPruned.contains(variableH));
		assertTrue(variablesPruned.contains(variableI));

		// Test B potentials
		ProbNode probNodeB = pruned.getProbNode("B");
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
		assertTrue(potential1B.contains(variableB));
		assertTrue(potential1B.contains(variableI));
	}
	
	@Test
	public final void testPrune3() throws Exception {
		// Create asia. Do not add potentials because they will not be used.
		String strAsia = "Asia";
		String strSmoker = "Smoker";
		String strTuberculosis = "Tuberculosis";
		String strCancer = "Cancer";
		String strTuberculosisOrCancer = "TuberculosisOrCancer";
		String strDyspnea = "Dyspnea";
		String strBronchitis = "Bronchitis";
		String strXRay = "XRay";
		ProbNet probNetAsia = UtilTestMethods.createProbNet(strAsia, strSmoker, strTuberculosis, 
				strCancer, strTuberculosisOrCancer, strDyspnea, strBronchitis, strXRay);
		UtilTestMethods.addLink(probNetAsia, strAsia, strTuberculosis, true);
		UtilTestMethods.addLink(probNetAsia, strSmoker, strCancer, true);
		UtilTestMethods.addLink(probNetAsia, strSmoker, strBronchitis, true);
		UtilTestMethods.addLink(probNetAsia, strTuberculosis, strTuberculosisOrCancer, true);
		UtilTestMethods.addLink(probNetAsia, strCancer, strTuberculosisOrCancer, true);
		UtilTestMethods.addLink(probNetAsia, strBronchitis, strDyspnea, true);
		UtilTestMethods.addLink(probNetAsia, strTuberculosisOrCancer, strDyspnea, true);
		UtilTestMethods.addLink(probNetAsia, strTuberculosisOrCancer, strXRay, true);
		
		EvidenceCase evidence = addEvidence(probNetAsia, null, strTuberculosis, 0);
		addEvidence(probNetAsia, null, strTuberculosisOrCancer, 0);
		ArrayList<Variable> variablesOfInterest = new ArrayList<Variable>(1);
		variablesOfInterest.add(probNetAsia.getVariable(strDyspnea));
		
		// Call method
		ProbNet pruned = ProbNetOperations.getPruned(probNetAsia, variablesOfInterest, evidence);

		// Test
		for(Variable variable : pruned.getVariables()) {
			System.out.println(variable);
		}
		assertNotNull(pruned.getVariable(strTuberculosis));
	}
	
	private EvidenceCase addEvidence(ProbNet probNet, EvidenceCase evidence, String variableName, int stateNumber) {
		if (evidence == null) {
			evidence = new EvidenceCase();
		}
		try {
			Variable variable = probNet.getVariable(variableName);
			Finding finding = new Finding(variable, stateNumber);
			evidence.addFinding(finding);
		} catch (ProbNodeNotFoundException e) {
			e.printStackTrace();
			fail("Variable " + variableName + " not found in probNet.");
		} catch (InvalidStateException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IncompatibleEvidenceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		return evidence;
	}
	
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
