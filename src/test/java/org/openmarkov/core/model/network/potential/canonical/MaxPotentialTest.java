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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

public class MaxPotentialTest {

	// Attributes
	private PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
	private ICIModelType modelType = ICIModelType.CAUSAL_MAX;
	
	private TablePotential subPotentialLeakyC;
	private MaxPotential maxPotential;

	// Initialization
	@Before
	public void setUp() throws Exception {
		
		// Define the variables
		Variable variableA = new Variable("A", "A0", "A1", "A2");
		Variable variableB = new Variable("B", "B0", "B1");
		Variable variableC = new Variable("C", "C0", "C1", "C2");

		// Conditional probability table for C: causal MIN
		ArrayList<Variable> variablesABC = new ArrayList<Variable>();
		variablesABC.add(variableC);
		variablesABC.add(variableA);
		variablesABC.add(variableB);

		double[] valuesCA = {1.0, 0.0, 0.0, 0.0, 0.3, 0.7, 0.0, 0.1, 0.9};
		ArrayList<Variable> variablesCA = new ArrayList<Variable>();
		variablesCA.add(variableC);
		variablesCA.add(variableA);
		TablePotential subPotentialCA= new TablePotential(variablesCA, role, valuesCA);
		
		double [] valuesCB ={1.0, 0.0, 0.0, 0.0, 0.2, 0.8};
		
		double [] valuesLeakyC ={0.989, 0.01, 0.001};
		ArrayList<Variable> variablesC = new ArrayList<Variable>();
		variablesC.add(variableC);
		subPotentialLeakyC = new TablePotential(variablesC, role, valuesLeakyC);

		maxPotential = new MaxPotential(modelType, variablesABC, role);
		maxPotential.addSubPotential(subPotentialCA);
		maxPotential.setNoisyParameters (variableB, valuesCB);
		maxPotential.addSubPotential(subPotentialLeakyC);
	}

	@Test
	public void testGetLeakPotential() throws NotEnoughMemoryException {
		assertEquals(subPotentialLeakyC, maxPotential.getLeakPotential());
	}

	@Test
	public void testGetCPT() throws NotEnoughMemoryException {
		double admissibleError = 0.000000001;
		double[] cPTValues = maxPotential.getCPT().values;
		assertEquals(0.989, cPTValues[0], admissibleError);
		assertEquals(0.01, cPTValues[1], admissibleError);
		assertEquals(0.2997, cPTValues[4], admissibleError);
		assertEquals(0.0999, cPTValues[7], admissibleError);
		assertEquals(0.0, cPTValues[15], admissibleError);
		assertEquals(0.98002, cPTValues[17], admissibleError);
	}

}
