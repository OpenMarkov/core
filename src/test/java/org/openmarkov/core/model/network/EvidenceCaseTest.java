/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;



public class EvidenceCaseTest {
	
	private Variable variableA;
	private Variable variableB;
	private Variable variableC;
	
	private State absent;
	private State present;
	
	private PotentialRole role;
	
	private ArrayList<Variable> variablesA; 
	private ArrayList<Variable> variablesAB;
	private ArrayList<Variable> variablesCBA;
	
	private TablePotential potentialvaluesA;
	private TablePotential potentialvaluesAB;
	private TablePotential potentialvaluesCBA;
	
	private ProbNet probNet; 
	
	//private NetworkTypeConstraint networkTypeConstraint = null;
	
	@Before
	public void setUp() throws Exception {
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
		absent = new State("absent");
		present = new State("present");
		State [] states= {absent, present};
		
		variableA.setStates(states);
		variableB.setStates(states);
		variableC.setStates(states); 
		
		//Setting Precision
		double precision = 0.01;
		variableA.setPrecision(precision);
		variableB.setPrecision(precision);
		variableC.setPrecision(precision);
		
		//Potentials
		//PotentialType type = PotentialType.TABLE;
		role = PotentialRole.CONDITIONAL_PROBABILITY;
		
		//Potential A
		double [] tableA ={0.3, 0.7};
		
		variablesA = new ArrayList<Variable>();
		variablesA.add(variableA);
		
		potentialvaluesA= new TablePotential(variablesA,role, tableA);
		
		
		//Potential AB
		double [] tableAB ={0.0, 1.0, 1.0, 0.0};
		
		variablesAB = new ArrayList<Variable>();;
		variablesAB.add(variableB);
		variablesAB.add(variableA);
		
		
		potentialvaluesAB= new TablePotential(variablesAB,role,tableAB);
		
		double [] tableCBA ={0.2, 0.8, 0.6, 0.4, 0.5, 0.5, 0.8, 0.2};
		
		variablesCBA = new ArrayList<Variable>();
		variablesCBA.add(variableC);
		variablesCBA.add(variableA);
		variablesCBA.add(variableB);
		
		
		//notEnoughMemoryException 
		potentialvaluesCBA= new TablePotential(variablesCBA,role,tableCBA);
		
		
				
		//If NetworkTypeConstraint is null we create a Bayesian network
		//NetworkTypeConstraint networkTypeConstraint = null; 
		//ProbNet probNet = new ProbNet(networkTypeConstraint); 
		probNet = new ProbNet();
		
		NodeType nodeType = NodeType.CHANCE;
		
		probNet.addVariable(variableA, nodeType);
		probNet.addVariable(variableB, nodeType);
		probNet.addVariable(variableC, nodeType);
		
		//Links throws NodeNotFoundException
		try {
			probNet.addLink(variableA, variableB, true);
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			probNet.addLink(variableA, variableC, true);
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			probNet.addLink(variableB, variableC, true);
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		probNet.addPotential((Potential)potentialvaluesA);
		probNet.addPotential((Potential)potentialvaluesAB);
		probNet.addPotential((Potential)potentialvaluesCBA);
		
	}
	

	@Test 
	public void extendEvidence() throws NotEnoughMemoryException, 
	IOException, ProbNodeNotFoundException, 
	ConstraintViolationException, IncompatibleEvidenceException,
	InvalidStateException, WrongCriterionException, NullPointerException {
		

		assertNotNull(probNet);
		EvidenceCase evidence = new EvidenceCase();
		Variable A = probNet.getVariable("A");
		assertNotNull(A);
		Finding aFinding = new Finding(A, 0);
		evidence.addFinding(aFinding);
		evidence.extendEvidence(probNet, 1.0);
		assertEquals(2, evidence.getFindings().size());
		Variable B = probNet.getVariable("B");
		Finding bFinding = evidence.getFinding(B);
		assertNotNull(bFinding);
		assertEquals(1, bFinding.getStateIndex());
				
	}

}
