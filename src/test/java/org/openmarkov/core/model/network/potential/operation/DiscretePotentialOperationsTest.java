/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.OpenMarkovTests;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.Choice;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.util.UtilTestMethods;


public class DiscretePotentialOperationsTest {

	private SharedTestUtilities commonVariables;
	
	private final int numConstantPotentials = 10;

	private final int numNormalPotentials = 10;

	private final int numVarsNormalPotentials = 4;

	private final int numVariableStates = 3;

	private List<TablePotential> constantPotentials;

	private List<TablePotential> normalPotentials;

	private List<TablePotential> allPotentials;
	
	private TablePotential aPotential;
	

	/** This method creates the variables used in the tests */
	@Before
	public void setUp() throws Exception {
		constantPotentials = new ArrayList<TablePotential>();
		normalPotentials = new ArrayList<TablePotential>();
		allPotentials =	new ArrayList<TablePotential>();
		try {
    		constantPotentials = SharedTestUtilities
    		    .generatePotentials(numConstantPotentials, 0, 0, 0);
			normalPotentials = SharedTestUtilities
			    .generatePotentials(numNormalPotentials, 
				numVarsNormalPotentials, 0, numVariableStates);
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		
		// Initializes the constant potentials tables with 1, 2, ... n
		for (int i = 0; i < numConstantPotentials; i++) {
			 constantPotentials.get(i).values[0] = i + 1;
		}
		
		// Initializes the potentials tables with:
		TablePotential auxTablePotential = null;
		for (int i = 0; i < numNormalPotentials; i++) {
			auxTablePotential = normalPotentials.get(i);
			for (int j = 0; j < auxTablePotential.values.length; j++) {
				auxTablePotential.values[j] = 
					auxTablePotential.values.length * i + j + 1;
			}
		}
		
		aPotential = null;

		commonVariables = new SharedTestUtilities();
    }

	@Test
	public void testAdd() {
		List<TablePotential> potentials = new ArrayList<TablePotential>();
		potentials.add(commonVariables.t2);
		potentials.add(commonVariables.t4);
		// Call method under test
		TablePotential addition = DiscretePotentialOperations.sum(potentials);
		// test variables
		List<Variable> additionVariables = addition.getVariables();
		assertEquals(4, additionVariables.size());
		List<Variable> testVariables = new ArrayList<Variable>();
		testVariables.add(commonVariables.a);
		testVariables.add(commonVariables.b);
		testVariables.add(commonVariables.c);
		testVariables.add(commonVariables.d);
		assertTrue(additionVariables.containsAll(testVariables));
		// test table content
		double[] table = addition.values;
		assertEquals(36, table.length);
		int[] configuration = { 0, 0, 0, 0 };
		assertEquals(0.3,
				getConfiguration(testVariables, configuration, addition),
				OpenMarkovTests.maxError);
		configuration[0] = 1; // a=1, b=0, c=0, d=0
		assertEquals(0.3,
				getConfiguration(testVariables, configuration, addition),
				OpenMarkovTests.maxError);
		configuration[0] = 0;
		configuration[1] = 1; // a=0, b=1, c=0, d=0
		assertEquals(0.4,
				getConfiguration(testVariables, configuration, addition),
				OpenMarkovTests.maxError);
		configuration[1] = 0;
		configuration[2] = 1; // a=0, b=0, c=1, d=0
		assertEquals(0.9,
				getConfiguration(testVariables, configuration, addition),
				OpenMarkovTests.maxError);
		configuration[2] = 0;
		configuration[3] = 1; // a=0, b=0, c=0, d=1
		assertEquals(0.5,
				getConfiguration(testVariables, configuration, addition),
				OpenMarkovTests.maxError);
		configuration[0] = 1;
		configuration[1] = 1;
		configuration[2] = 1; // a=1, b=1, c=1, d=1
		assertEquals(0.6,
				getConfiguration(testVariables, configuration, addition),
				OpenMarkovTests.maxError);
	}

	@Test
	public void testMultiply() {

		// Call method under test
		TablePotential multiplication = DiscretePotentialOperations
				.multiply(commonVariables.potentials);
		List<Variable> variables = multiplication.getVariables();
		assertEquals(4, variables.size());
		assertTrue(variables.contains(commonVariables.c));
		assertTrue(variables.contains(commonVariables.b));
		assertTrue(variables.contains(commonVariables.a));
		assertTrue(variables.contains(commonVariables.d));
		assertEquals(36, multiplication.values.length);
		int[] coordinate = { 0, 0, 0, 0 }; // test configuration a=0,b=0,c=0,d=0
		double value = getConfiguration(commonVariables.totalVariables,
				coordinate, multiplication);
		assertEquals(0.007, value, OpenMarkovTests.maxError);
		coordinate[0] = 1; // test configuration a=1,b=0,c=0,d=0
		value = getConfiguration(commonVariables.totalVariables, coordinate,
				multiplication);
		assertEquals(0.007, value, OpenMarkovTests.maxError);
		coordinate[0] = 0;
		coordinate[1] = 1; // test configuration a=0,b=1,c=0,d=0
		value = getConfiguration(commonVariables.totalVariables, coordinate,
				multiplication);
		assertEquals(0.014, value, OpenMarkovTests.maxError);
		coordinate[1] = 0;
		coordinate[2] = 1; // test configuration a=0,b=0,c=1,d=0
		value = getConfiguration(commonVariables.totalVariables, coordinate,
				multiplication);
		assertEquals(0.028, value, OpenMarkovTests.maxError);
		coordinate[2] = 0;
		coordinate[3] = 1; // test configuration a=0,b=0,c=0,d=1
		value = getConfiguration(commonVariables.totalVariables, coordinate,
				multiplication);
		assertEquals(0.014, value, OpenMarkovTests.maxError);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNewMultiply() {
		// Test constant multiplication
	    List<TablePotential> potentials = allPotentials;
		
		// Only 1 potential so in this case the method does not do anything
		potentials.add(constantPotentials.get(1));
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: one constant potential");
		}
		assertEquals(1, aPotential.values.length);
		assertEquals(2.0, aPotential.values[0],OpenMarkovTests.maxError);
		
		// Two constant potentials
		potentials.add(constantPotentials.get(1));
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: two constant potentials");
		}
		assertEquals(4.0, aPotential.values[0],OpenMarkovTests.maxError);
		
		// Several constant potentials
		for (int i = 2; i < numConstantPotentials; i++) {
			potentials.add(constantPotentials.get(i));
		}
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: several constant potentials");
		}
		assertEquals(7257600.0, aPotential.values[0],OpenMarkovTests.maxError);
		
		// Test constant and binary potential multiplication
		// Only 1 potential so in this case the method does not do anything
		potentials.clear();
		potentials.add(normalPotentials.get(0));
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: one normal potential");
		}
		
		// Check variables:
		// Same number of variables
		TablePotential normalPotential = normalPotentials.get(0);
		assertEquals(aPotential.getVariables().size(), 
			normalPotential.getVariables().size());
		// Same variables
		assertTrue(aPotential.getVariables().containsAll(
			normalPotential.getVariables()));

		// Check table
		// size:
		assertEquals(81, aPotential.values.length);
		// travels around all the table
		assertEquals(1.0, aPotential.values[0],OpenMarkovTests.maxError);
		
		// Two normal potentials
		potentials.add(normalPotentials.get(1));
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: two normal potentials");
		}
		// check table size
		assertEquals(81 * 81, aPotential.values.length);
		// check table content
		assertEquals(82.0, aPotential.values[0],OpenMarkovTests.maxError);
		ArrayList<Variable> variablesPotentials = (ArrayList<Variable>)
			((Object)getUnionVariablesOrdered(potentials));
		int[] coordinate1 = {1,0,0,0,0,0,0,0};
		double configuration = UtilTestMethods.getConfiguration(
			variablesPotentials, coordinate1, aPotential);
		assertEquals(164.0, configuration,OpenMarkovTests.maxError);
		coordinate1[2] = 1; coordinate1[4] = 1; coordinate1[6] = 1;
		configuration = UtilTestMethods.getConfiguration(
			variablesPotentials, coordinate1, aPotential);
		assertEquals(1012.0, configuration,OpenMarkovTests.maxError);
		
		// Two potentials constant and normal: c * n
		potentials.clear();
		potentials.add(constantPotentials.get(1));
		potentials.add(normalPotentials.get(0));
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: two potentials constant and normal: c * n");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(2.0, aPotential.values[0],OpenMarkovTests.maxError);
		int[] coordinate2 = {1,0,0,0};
		variablesPotentials = (ArrayList<Variable>)
            ((Object)getUnionVariablesOrdered(potentials));
		configuration = UtilTestMethods.getConfiguration(
			variablesPotentials, coordinate2, aPotential);
		assertEquals(4.0, configuration,OpenMarkovTests.maxError);

		// Two potentials constant and normal: n * c
		potentials.clear();
		potentials.add(normalPotentials.get(0));
		potentials.add(constantPotentials.get(1));
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: two potentials constant and normal: n * c");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(2.0, aPotential.values[0],OpenMarkovTests.maxError);
		variablesPotentials = (ArrayList<Variable>)
            ((Object)getUnionVariablesOrdered(potentials));
        configuration = UtilTestMethods.getConfiguration(
    		variablesPotentials, coordinate2, aPotential);
		assertEquals(4.0, configuration,OpenMarkovTests.maxError);

		// Three potentials constant and normal: c1 * c2 * n
		potentials.clear();
		potentials.add(constantPotentials.get(1));
		potentials.add(constantPotentials.get(2));
		potentials.add(normalPotentials.get(0));
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: three potentials constant and normal: " + 
				"c1 * c2 * n");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(6.0, aPotential.values[0],OpenMarkovTests.maxError);
		variablesPotentials = (ArrayList<Variable>)
            ((Object)getUnionVariablesOrdered(potentials));
		int[] coordinate3 = {1,0,0,0,0,0,0,0};
        configuration = UtilTestMethods.getConfiguration(
		    variablesPotentials, coordinate3, aPotential);
		assertEquals(12.0, configuration,OpenMarkovTests.maxError);

		// Three potentials constant and normal: c1 * n * c2
		potentials.clear();
		potentials.add(constantPotentials.get(1));
		potentials.add(normalPotentials.get(0));
		potentials.add(constantPotentials.get(2));
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: three potentials constant and normal: " + 
				"c1 * n * c2");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(6.0, aPotential.values[0],OpenMarkovTests.maxError);
		variablesPotentials = (ArrayList<Variable>)
            ((Object)getUnionVariablesOrdered(potentials));
        configuration = UtilTestMethods.getConfiguration(
	        variablesPotentials, coordinate3, aPotential);
		assertEquals(12.0, configuration,OpenMarkovTests.maxError);

		// Three potentials constant and normal: n * c1 * c2
		potentials.clear();
		potentials.add(normalPotentials.get(0));
		potentials.add(constantPotentials.get(1));
		potentials.add(constantPotentials.get(2));
		try {
			aPotential = 
				(TablePotential)PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: three potentials constant and normal: " + 
				"n * c1 * c2");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(6.0, aPotential.values[0],OpenMarkovTests.maxError);
		variablesPotentials = (ArrayList<Variable>)
            ((Object)getUnionVariablesOrdered(potentials));
        configuration = UtilTestMethods.getConfiguration(
            variablesPotentials, coordinate3, aPotential);
	    assertEquals(12.0, configuration,OpenMarkovTests.maxError);
    }

	@Test
	public void testMultiplyAndMarginalize() {
		// Call method under test
		TablePotential mulAndMarg = (TablePotential) DiscretePotentialOperations
				.multiplyAndMarginalize(commonVariables.potentials,
						commonVariables.a);
		List<Variable> variables = mulAndMarg.getVariables();
		assertEquals(3, variables.size());
		assertTrue(variables.contains(commonVariables.b));
		assertTrue(variables.contains(commonVariables.c));
		assertTrue(variables.contains(commonVariables.d));
		assertEquals(12, mulAndMarg.values.length);
		int[] coordinate = { 0, 0, 0 }; // coordinate = {0,0,0}
		double value = getConfiguration(commonVariables.arrayVariablesBCD,
				coordinate, mulAndMarg);
		assertEquals(0.0875, value, OpenMarkovTests.maxError);
		coordinate[0] = 1; // coordinate = {1,0,0}
		value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate,
				mulAndMarg);
		assertEquals(0.063, value, OpenMarkovTests.maxError);
		coordinate[0] = 0;
		coordinate[1] = 1; // coordinate = {0,1,0}
		value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate,
				mulAndMarg);
		assertEquals(0.2625, value, OpenMarkovTests.maxError);
		coordinate[1] = 0;
		coordinate[2] = 1; // coordinate = {0,0,1}
		value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate,
				mulAndMarg);
		assertEquals(0.273, value, OpenMarkovTests.maxError);
		coordinate[0] = 2;
		coordinate[1] = 1; // coordinate = {2,1,1}
		value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate,
				mulAndMarg);
		assertEquals(0.1435, value, OpenMarkovTests.maxError);
	}
	
	@Test
	/** Multiplies and marginalize projected potentials */
	public void testMultiplyAndMarginalizeProjected() 
			throws NoFindingException, WrongCriterionException {
		// Create data
		// Variables
		Variable A = new Variable("A", 2);
		Variable B = new Variable("B", 2);
		Variable C = new Variable("C", 2);
		// Arrays of variables
		ArrayList<Variable> bPotentialVariables = new ArrayList<Variable>();
		bPotentialVariables.add(B);
		bPotentialVariables.add(A);
		ArrayList<Variable> bcPotentialVariables = new ArrayList<Variable>();
		bcPotentialVariables.add(C);
		bcPotentialVariables.add(A);
		bcPotentialVariables.add(B);
		// table of potentials
		double[] bTable = {0.7, 0.3, 0.9, 0.1};
		double[] bcTable = {0.15, 0.85, 0.84, 0.16, 0.29, 0.71, 0.98, 0.02};
		// tablePotentials
		TablePotential bPotential = new TablePotential(bPotentialVariables, 
				PotentialRole.CONDITIONAL_PROBABILITY, bTable);
		TablePotential bcPotential = new TablePotential(bcPotentialVariables,
				PotentialRole.CONDITIONAL_PROBABILITY, bcTable);
		// evidence case
		Finding aFinding = new Finding(A, 0);		
		EvidenceCase evidenceCase = new EvidenceCase();
		try {
			evidenceCase.addFinding(aFinding);
		} catch (InvalidStateException e1) {
			// Unreachable code
			e1.printStackTrace();
		} catch (IncompatibleEvidenceException e1) {
			// Unreachable code
			e1.printStackTrace();
		}
		// project potentials
		bPotential = bPotential.tableProject(evidenceCase, null).get(0);
		bcPotential = bcPotential.tableProject(evidenceCase, null).get(0);
		// collection of potentials to multiply and marginalize
		ArrayList<TablePotential> potentials = new ArrayList<TablePotential>();
		potentials.add(bPotential);
		potentials.add(bcPotential);
		// variables to keep and marginalize
		ArrayList<Variable> variablesToKeep = new ArrayList<Variable>();
		variablesToKeep.add(C);
		ArrayList<Variable> variablesToMarginalize = new ArrayList<Variable>();
		variablesToMarginalize.add(B);

		// Do test
		TablePotential result = (TablePotential) DiscretePotentialOperations
				.multiplyAndMarginalize(potentials, variablesToKeep,
						variablesToMarginalize);
		// Test table
		assertEquals(0.192, result.values[0], OpenMarkovTests.maxError);
		assertEquals(0.808, result.values[1], OpenMarkovTests.maxError);
		assertEquals(2, result.values.length);

		// Test variables
		List<Variable> variables = result.getVariables();
		assertEquals(1, variables.size());
		assertTrue(variables.contains(C));

		int[] offsets = result.getOffsets();
		assertEquals(1, offsets.length);
		assertEquals(1, offsets[0]);
	}
	
	@Test
	/** Multiplies and maximizes two potentials: <code>t2(a,b)</code> and 
	 * <code>t4(c,a,d)</code> that share a variable: <code>a</code>. */
	public void testMultiplyAndMaximize() {
		ArrayList<Potential> potentialsVariable = new ArrayList<Potential>();
		potentialsVariable.add(commonVariables.t2);
		potentialsVariable.add(commonVariables.t4);
		Variable variableToMaximize = commonVariables.a; // The common variable
		Object[] potentials = DiscretePotentialOperations.multiplyAndMaximize(
				potentialsVariable, variableToMaximize);
		// Test begins
		assertEquals(2, potentials.length); // Produces two potentials
		TablePotential maximized = (TablePotential) potentials[0];
		// Test variables of first potential
		List<Variable> maximizedVariables = maximized.getVariables();
		assertEquals(3, maximizedVariables.size());
		assertTrue(maximizedVariables.contains(commonVariables.b));
		assertTrue(maximizedVariables.contains(commonVariables.c));
		assertTrue(maximizedVariables.contains(commonVariables.d));
		assertFalse(maximizedVariables.contains(commonVariables.a));
		// Test maximization
		assertEquals(12, maximized.values.length);
		assertEquals(0.21, maximized.values[0], OpenMarkovTests.maxError);
	}

	@Test
	public void testGetProperPotentials() {
		// Call method under test
		List<TablePotential> properPotentials = AuxiliaryOperations.getProperPotentials(commonVariables.potentials);
		assertEquals(2, properPotentials.size());
		assertTrue(properPotentials.contains(commonVariables.t2));
		assertTrue(properPotentials.contains(commonVariables.t4));
	}

	@Test
	public void testGetConstantFactor() {
		// Call method under test
		double constantFactor = DiscretePotentialOperations
		    .getConstantFactor(commonVariables.potentials);
		assertEquals(0.35, constantFactor,OpenMarkovTests.maxError);
	}

	@Test
	public void testGetAccumulatedOffsets() {
	}

	@Test
	public void testNormalize() {
			TablePotential multiplication = DiscretePotentialOperations
				.multiply(commonVariables.potentials);
			TablePotential normalized;
			try {
				normalized = (TablePotential) 
					DiscretePotentialOperations.normalize(multiplication);
				int[] configuration = {0, 0, 0, 0};
				int[] dimensions = normalized.getDimensions();
				List<Variable> variablesNormalized = normalized.getVariables();
			} catch (NormalizeNullVectorException e) {
				fail("Null vector exception");
				e.printStackTrace();
			}
	}

	@Test
	public void testDivide() {
		// Call method under test
		TablePotential division = (TablePotential) DiscretePotentialOperations
			.divide(commonVariables.t1, commonVariables.t2);
		assertEquals(2, division.getVariables().size());
		assertTrue(division.contains(commonVariables.a));
		assertTrue(division.contains(commonVariables.b));
		assertEquals(7, division.values[0], OpenMarkovTests.maxError);
		
		division = (TablePotential)DiscretePotentialOperations
			.divide(commonVariables.t2, commonVariables.t4);
		assertEquals(4, division.getVariables().size());
		assertTrue(division.contains(commonVariables.a));
		assertTrue(division.contains(commonVariables.b));
		assertTrue(division.contains(commonVariables.d));
		assertTrue(division.contains(commonVariables.d));

		int[] coordinate = {0,0,0,0};
		double value = getConfiguration(
			commonVariables.totalVariables, coordinate, division);
		assertEquals(0.5, value, OpenMarkovTests.maxError);
		coordinate[2] = 1; // {0,0,1,0}
		value = getConfiguration(
			commonVariables.totalVariables, coordinate, division);
		assertEquals(0.125, value, OpenMarkovTests.maxError);
		coordinate[2] = 0;
		coordinate[0] = 1; // {1,0,0,0}
		value = getConfiguration(
				commonVariables.totalVariables, coordinate, division);
		assertEquals(2, value, OpenMarkovTests.maxError);
		coordinate[0] = 0;
		coordinate[1] = 1; // {0,1,0,0}
		value = getConfiguration(
				commonVariables.totalVariables, coordinate, division);
		assertEquals(1, value, OpenMarkovTests.maxError);
		coordinate[1] = 0;
		coordinate[3] = 1; // {0,0,0,1}
		value = getConfiguration(
				commonVariables.totalVariables, coordinate, division);
		assertEquals(0.25, value, OpenMarkovTests.maxError);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMaximize() {
		// Call method under test
		Object[] potentials = DiscretePotentialOperations.maximize(
				commonVariables.t2, commonVariables.a);
		TablePotential maximizedPotential = (TablePotential) potentials[0];
		GTablePotential<Choice> choicesPotential = (GTablePotential<Choice>) potentials[1];
		// Test maximized potential
		// Check variables
		List<Variable> variablesPotential = maximizedPotential.getVariables();
		assertEquals(1, variablesPotential.size());
		assertTrue(variablesPotential.contains(commonVariables.b));
		// Check table
		double[] table = maximizedPotential.values;
		assertEquals(3, table.length); // size table reduced
		assertEquals(0.7, table[0], OpenMarkovTests.maxError);
		assertEquals(0.5, table[1], OpenMarkovTests.maxError);
		assertEquals(0.6, table[2], OpenMarkovTests.maxError);
		// Test choices potential
		// Check variables. It must contain same variables as
		// maximized potential
		variablesPotential = choicesPotential.getVariables();
		assertEquals(1, variablesPotential.size());
		assertTrue(variablesPotential.contains(commonVariables.b));
		// Check table
		Choice choice = choicesPotential.elementTable.get(0); // table[0]
		assertEquals(2, choice.getValues()[0]);
		assertEquals(commonVariables.a, choice.getVariable());
		assertEquals(1, choice.getNumValues());

		choice = choicesPotential.elementTable.get(1); // table[1]
		assertEquals(1, choice.getValues()[0]);
		assertEquals(commonVariables.a, choice.getVariable());
		assertEquals(1, choice.getNumValues());

		choice = choicesPotential.elementTable.get(2); // table[2]
		assertEquals(0, choice.getValues()[0]);
		assertEquals(commonVariables.a, choice.getVariable());
		assertEquals(1, choice.getNumValues());
			
	}
	
	@Test
	public void testReorder1() {
		double[] table = {0.2, 0.8, 0.4, 0.6};
		final int numVariables = 2;
		
		// Setup: create a table potential
		TablePotential orderedAB = // Variables: A, B
			SharedTestUtilities.createTablePotential(numVariables, table, null);
		List<Variable> variablesAB = orderedAB.getVariables();
		
		// Create variables in other order: B, A
		List<Variable> variablesBA = new ArrayList<Variable>();
		variablesBA.add(variablesAB.get(1));
		variablesBA.add(variablesAB.get(0));
		
		// Call method DiscretePotentialOperations.reorder
		TablePotential reorderedBA = 
			DiscretePotentialOperations.reorder(orderedAB, variablesBA);
		
		// Test
		// 1. Test variables
		List<Variable> variablesReordered = reorderedBA.getVariables();
		assertEquals(numVariables, variablesReordered.size());
		assertEquals(variablesReordered.get(0), variablesAB.get(1));
		assertEquals(variablesReordered.get(1), variablesAB.get(0));
		// 2. Test table
		assertEquals(0.2, reorderedBA.values[0],OpenMarkovTests.maxError);
		assertEquals(0.4, reorderedBA.values[1],OpenMarkovTests.maxError);
		assertEquals(0.8, reorderedBA.values[2],OpenMarkovTests.maxError);
		assertEquals(0.6, reorderedBA.values[3],OpenMarkovTests.maxError);
	}
	
    @Test
    public void testReorder2() {
		// Original TablePotential: commonVariables.t4. Variables: C,A,D
	    List<Variable> variablesBeforeReorder = commonVariables.t4.getVariables();
	    List<Variable> variablesAfterReorder = new ArrayList<Variable> (variablesBeforeReorder);
		Collections.reverse(variablesAfterReorder); // reorder the variables
		assertEquals(
				variablesAfterReorder.size(),variablesBeforeReorder.size());
		assertEquals( // Test Collections.reverse
				variablesAfterReorder.get(0), variablesBeforeReorder.get(2));
			TablePotential tablePotentialAfterReorder =
				DiscretePotentialOperations.reorder(
						commonVariables.t4, variablesAfterReorder);
			// Test variables:
			// 1. Test numVariables
			List<Variable> variablesReorderedPotential = 
				tablePotentialAfterReorder.getVariables();
			int numVariablesReordered = variablesReorderedPotential.size();
			assertEquals(variablesAfterReorder.size(), numVariablesReordered);
			// 2. Test variables
			for (int i = 0; i < numVariablesReordered; i++) {
				assertEquals(variablesAfterReorder.get(i), 
						variablesReorderedPotential.get(i));
			}
			// Test table of TablePotential
			assertEquals(commonVariables.t4.values.length, 
					tablePotentialAfterReorder.values.length);
			double[] reorderedTable =
				{0.2, 0.4, 0.1, 0.9, 0.3, 0.8, 0.8, 0.6, 0.9, 0.1, 0.7, 0.2};
			for (int i = 0; i < reorderedTable.length; i++) {
				assertEquals(
						reorderedTable[i], tablePotentialAfterReorder.values[i],OpenMarkovTests.maxError);
			}
	}
	
	@Test
	public void testReorder3() {
		// another reorder
	    List<Variable> variablesBeforeReorder = 
			commonVariables.t4.getVariables();
	    List<Variable> variablesAfterReorder = new ArrayList<Variable>(variablesBeforeReorder);
		variablesAfterReorder.remove( 0 );
		Collections.reverse(variablesAfterReorder); // reorder the variables
		variablesAfterReorder.add( 0, variablesBeforeReorder.get(0 ));
		// Ordination: (0, 1, 2) -> (0, 2, 1)
		TablePotential tablePotentialAfterReorder = DiscretePotentialOperations
				.reorder(commonVariables.t4, variablesAfterReorder);
		// Test variables:
		// 1. Test numVariables
		List<Variable> variablesReorderedPotential = tablePotentialAfterReorder
				.getVariables();
		int numVariablesReordered = variablesReorderedPotential.size();
		assertEquals(variablesAfterReorder.size(), numVariablesReordered);
		// 2. Test variables
		for (int i = 0; i < numVariablesReordered; i++) {
			assertEquals(variablesAfterReorder.get(i),
					variablesReorderedPotential.get(i));
		}
		// Test table of TablePotential
		assertEquals(commonVariables.t4.values.length,
				tablePotentialAfterReorder.values.length);
		double[] reorderedTable = { 0.2, 0.8, 0.4, 0.6, 0.1, 0.9, 0.9, 0.1,
				0.3, 0.7, 0.8, 0.2 };
		for (int i = 0; i < reorderedTable.length; i++) {
			assertEquals(reorderedTable[i],
					tablePotentialAfterReorder.values[i],
					OpenMarkovTests.maxError);
		}
	}


    /** Translates the coordinate received in (variables, coordinateVariables)
     *  to the potential variables ordination and returns the configuration
     *  value. 
     * @return configuration value. <code>double</code>
     * @param variables <code>ArrayList</code> of <code>Variable</code>
     * @param coordinateVariables <code>int[]</code>
     * @param potential <code>TablePotential</code> */
    private double getConfiguration(List<Variable> variables, 
    		int[] coordinateVariables, TablePotential potential) {
        List<Variable> variablesPotential = potential.getVariables();
    	int[] coordinate = new int[variablesPotential.size()];
    	int i = 0;
    	for (Variable variablePotential : variablesPotential) {
    		coordinate[i++] = 
    			coordinateVariables[variables.indexOf(variablePotential)];
    	}
    	return potential.values[potential.getPosition(coordinate)];
    }
    
    /** @param potentials <code>ArrayList</code> of <code>Potential</code>.
     * @return An <code>ArrayList</code> of <code>Variable</code> with all the
     * variables of the potentials in order: first the variables of the first
     * potential, next the remaining variables of the second potential, etc.  */
    private List<Variable> getUnionVariablesOrdered(
    		List<? extends Potential> potentials) {
        List<Variable> variables = new ArrayList<Variable>();
    	for(Potential potential : potentials) {
    		for (Variable variable : potential.getVariables()) {
    			if (!variables.contains(variable)) {
    				variables.add(variable);
    			}
    		}
    	}
    	return variables;
    }
    
}
