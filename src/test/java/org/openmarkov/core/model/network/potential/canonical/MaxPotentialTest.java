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

public class MaxPotentialTest {

	// Attributes
	private MaxPotential maxPotential;
    private final double admissibleError = 0.000000001;
	

	// Initialization
	@Before
	public void setUp() throws Exception {
		
		// Define the variables
		Variable variableA = new Variable("A", "A0", "A1", "A2");
		Variable variableB = new Variable("B", "B0", "B1");
		Variable variableC = new Variable("C", "C0", "C1", "C2");

		// Conditional probability table for C: causal MAX
		ArrayList<Variable> variablesABC = new ArrayList<Variable>();
		variablesABC.add(variableC);
		variablesABC.add(variableA);
		variablesABC.add(variableB);

		maxPotential = new MaxPotential(variablesABC);
		maxPotential.setNoisyParameters (variableA, new double[] {1.0, 0.0, 0.0, 0.0, 0.3, 0.7, 0.0, 0.1, 0.9});
		maxPotential.setNoisyParameters (variableB, new double[] {1.0, 0.0, 0.0, 0.0, 0.2, 0.8});
	}

	@Test
	public void testGetLeakPotential() throws NotEnoughMemoryException {
        maxPotential.setLeakyParameters (new double[] {0.989, 0.01, 0.001});
		assertEquals(0.989, maxPotential.getLeakyParameters()[0], admissibleError);
        assertEquals(0.01, maxPotential.getLeakyParameters()[1], admissibleError);
        assertEquals(0.001, maxPotential.getLeakyParameters()[2], admissibleError);
	}

    @Test
    public void testGetCPT() throws NotEnoughMemoryException {
        maxPotential.setLeakyParameters (new double[] {0.989, 0.01, 0.001});
        double[] cPTValues = maxPotential.getCPT().values;
        assertEquals(0.989, cPTValues[0], admissibleError);
        assertEquals(0.01, cPTValues[1], admissibleError);
        assertEquals(0.2997, cPTValues[4], admissibleError);
        assertEquals(0.0999, cPTValues[7], admissibleError);
        assertEquals(0.0, cPTValues[15], admissibleError);
        assertEquals(0.98002, cPTValues[17], admissibleError);
    }
	
	@Test
	public void testGetCPTDefaultLeaky() throws NotEnoughMemoryException {
		double[] cPTValues = maxPotential.getCPT().values;
		assertEquals(1.0, cPTValues[0], admissibleError);
		assertEquals(0.0, cPTValues[1], admissibleError);
		assertEquals(0.3, cPTValues[4], admissibleError);
		assertEquals(0.1, cPTValues[7], admissibleError);
		assertEquals(0.0, cPTValues[15], admissibleError);
		assertEquals(0.98, cPTValues[17], admissibleError);
	}

}
