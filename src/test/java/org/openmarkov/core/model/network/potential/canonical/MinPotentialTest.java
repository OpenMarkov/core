/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.canonical;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.ICIModelType;
import org.openmarkov.core.model.network.potential.canonical.MinPotential;

public class MinPotentialTest {

	// Attributes
	private PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
	private ICIModelType modelType = ICIModelType.GENERAL_MIN;;
	
	private TablePotential subPotentialLeakyC;
	private MinPotential minPotential;

	// Initialization
	@Before
	public void setUp() throws Exception {
		
		// Define the variables
		Variable variableA = new Variable("A",3);
		State[] statesOfA = {new State("A0"), new State("A1"), new State("A2")};
		variableA.setStates(statesOfA);
		
		Variable variableB = new Variable("B",2);
		State[] statesOfB = {new State("B0"), new State("B1")};
		variableB.setStates(statesOfB);

		Variable variableC = new Variable("C",3);
		State[] statesOfC = {new State("C0"), new State("C1"), new State("C2")};
		variableC.setStates(statesOfC);

		// Conditional probability table for C: causal MIN
		ArrayList<Variable> variablesABC = new ArrayList<Variable>();
		variablesABC.add(variableC);
		variablesABC.add(variableA);
		variablesABC.add(variableB);

		double[] valuesCA = {0.0, 0.0, 1.0, 0.0, 0.2, 0.8, 0.7, 0.3, 0.0};
		ArrayList<Variable> variablesCA = new ArrayList<Variable>();
		variablesCA.add(variableC);
		variablesCA.add(variableA);
		TablePotential subPotentialCA= new TablePotential(variablesCA, role, valuesCA);
		
		double [] valuesCB ={0.0, 0.0, 1.0, 0.6, 0.3, 0.1};
		ArrayList<Variable> variablesCB = new ArrayList<Variable>();
		variablesCB.add(variableC);
		variablesCB.add(variableB);
		TablePotential subPotentialCB = new TablePotential(variablesCB, role, valuesCB);
		
		double [] valuesLeakyC ={0.01, 0.1, 0.89};
		ArrayList<Variable> variablesC = new ArrayList<Variable>();
		variablesC.add(variableC);
		subPotentialLeakyC = new TablePotential(variablesC, role, valuesLeakyC);

		minPotential = new MinPotential(modelType, variablesABC, role);
		minPotential.addSubPotential(subPotentialCA);
		minPotential.addSubPotential(subPotentialCB);
		minPotential.addSubPotential(subPotentialLeakyC);
	}

	@Test
	public void testGetLeakPotential() throws NotEnoughMemoryException {
		assertEquals(subPotentialLeakyC, minPotential.getLeakPotential());
	}

	@Test
	public void testGetCPT() throws NotEnoughMemoryException {
		double admissibleError = 0.000000001;
		double[] cPTValues = minPotential.getCPT().values;
		assertEquals(0.01, cPTValues[0], admissibleError);
		assertEquals(0.1, cPTValues[1], admissibleError);
		assertEquals(0.278, cPTValues[4], admissibleError);
		assertEquals(0.297, cPTValues[7], admissibleError);
		assertEquals(0.8812, cPTValues[15], admissibleError);
		assertEquals(0.0, cPTValues[17], admissibleError);
	}

}
