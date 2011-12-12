package org.openmarkov.core.model.network.potential.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;


/** Unit test of:
 *  <ul>
 *  <li><code>testGetProperPotentials()
 *  </code>
 *  <li><code>testGetUnionVariables()</code>
 *  </ul */
public class AuxiliaryOperationsTest {

	private ArrayList<TablePotential> emptyPotentialsList;

	private ArrayList<TablePotential> oneConstantPotentialsList;

	private ArrayList<TablePotential> oneProperPotentialsList;

	private ArrayList<TablePotential> severalMixedPotentialsList;
		
	private final int NUM_POTENTIALS_ARRAY = 10;
	
	private final int NUM_CONSTANT_POTENTIALS = NUM_POTENTIALS_ARRAY;

	private final int numNormalPotentials = NUM_POTENTIALS_ARRAY;

	private final int NUM_VARS_PER_POTENTIAL = 4;

	private final int NUM_STATES_VARIABLES = 3;

	private ArrayList<TablePotential> constantPotentials;

	private ArrayList<TablePotential> normalPotentials;

	@Before
	public void setUp() throws Exception {
		emptyPotentialsList = new ArrayList<TablePotential>();

		try {
    		constantPotentials = SharedTestUtilities
				.generatePotentials(NUM_CONSTANT_POTENTIALS, 0, 0, 0);
		} catch (Exception e) {
			System.err.println(e.getStackTrace());
		}
		oneConstantPotentialsList = new ArrayList<TablePotential>();
		oneConstantPotentialsList.add(constantPotentials.get(0));

		try {
			normalPotentials = SharedTestUtilities 
				.generatePotentials(numNormalPotentials, 
					NUM_VARS_PER_POTENTIAL, 0, NUM_STATES_VARIABLES);
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		oneProperPotentialsList = new ArrayList<TablePotential>();
		oneProperPotentialsList.add(normalPotentials.get(0));

		severalMixedPotentialsList = new ArrayList<TablePotential>();

		// Initializes constant potentials tables with 1, 2, ... n
		for (int i = 0; i < NUM_CONSTANT_POTENTIALS; i++) {
			 severalMixedPotentialsList.add(constantPotentials.get(i));
			 severalMixedPotentialsList.add(normalPotentials.get(i));
		}
		
		// Initializes potentials tables with:
		// number of potential * potential table length + table position + 1
		TablePotential auxTablePotential = null;
		for (int i = 0; i < numNormalPotentials; i++) {
			auxTablePotential = normalPotentials.get(i);
			for (int j = 0; j < auxTablePotential.values.length; j++) {
				auxTablePotential.values[j] = 
					auxTablePotential.values.length * i + j + 1;
			}
		}
	}

	@Test
	public void testGetProperPotentials() {
		// Test empty list
		ArrayList<TablePotential> properPotentials = 
			AuxiliaryOperations.getProperPotentials(emptyPotentialsList);
		assertNotNull(properPotentials);
		assertEquals(0, properPotentials.size());
		
		// Test list with one constant potential
		properPotentials = 
			AuxiliaryOperations.getProperPotentials(oneConstantPotentialsList);
		assertEquals(0, properPotentials.size());
		
		// Test list with several proper and constant potentials
		properPotentials =
			AuxiliaryOperations.getProperPotentials(severalMixedPotentialsList);
		assertEquals(NUM_POTENTIALS_ARRAY, properPotentials.size());
	}

	@Test
	public void testGetUnionVariables() {
		// Check union of constant potentials (no variables)
		ArrayList<Variable> union = 
			AuxiliaryOperations.getUnionVariables(constantPotentials);
		assertNotNull(union);
		assertEquals(0, union.size());
		
		// Check union of several potentials
	   // Generate 2 potentials with 3 variables each with 2 variables in common
		try { 
			normalPotentials = 
				SharedTestUtilities.generatePotentials(2, 3, 2, 2);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Can not generate test");
		}
		union = AuxiliaryOperations.getUnionVariables(normalPotentials);
		// Check number of variables in union
		assertEquals(4, union.size());
		// Check that all the variables of the potentials are in the union
		for(Potential potential : normalPotentials) {
			ArrayList<Variable> variables = potential.getVariables();
			for (Variable variable : variables) {
				assertTrue(union.contains(variable));
			}
		}
	}

}
