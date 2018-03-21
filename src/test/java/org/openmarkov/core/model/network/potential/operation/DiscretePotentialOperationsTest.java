/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmarkov.core.exception.DivideByZeroException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.Choice;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.util.UtilTestMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class DiscretePotentialOperationsTest {

	/*  Public scope for use in all tests. */
	public static final double maxError = 0.0001;
	private final int numConstantPotentials = 10;
	private final int numNormalPotentials = 10;
	private final int numVarsNormalPotentials = 4;
	private final int numVariableStates = 3;
	private SharedTestUtilities commonVariables;
	private List<TablePotential> constantPotentials;

	private List<TablePotential> normalPotentials;

	private List<TablePotential> allPotentials;

	private TablePotential aPotential;

	/**
	 * This method creates the variables used in the tests
	 */
	@Before public void setUp() throws Exception {
		constantPotentials = new ArrayList<>();
		normalPotentials = new ArrayList<>();
		allPotentials = new ArrayList<>();
		try {
			constantPotentials = SharedTestUtilities.generatePotentials(numConstantPotentials, 0, 0, 0);
			normalPotentials = SharedTestUtilities
					.generatePotentials(numNormalPotentials, numVarsNormalPotentials, 0, numVariableStates);
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
				auxTablePotential.values[j] = auxTablePotential.values.length * i + j + 1;
			}
		}

		aPotential = null;

		commonVariables = new SharedTestUtilities();
	}

	@Test public void testSum() {
		List<TablePotential> potentials = new ArrayList<>();
		potentials.add(commonVariables.tpAB);
		potentials.add(commonVariables.tpCAB);
		// Call method under test
		TablePotential sum = DiscretePotentialOperations.sum(potentials);
		// test variables
		List<Variable> sumVariables = sum.getVariables();
		assertEquals(4, sumVariables.size());
		List<Variable> testVariables = Arrays
				.asList(commonVariables.a, commonVariables.b, commonVariables.c, commonVariables.d);
		assertTrue(sumVariables.containsAll(testVariables));
		// test table content
		double[] table = sum.values;
		assertEquals(36, table.length);
		int[] configuration = { 0, 0, 0, 0 };
		assertEquals(0.3, getConfiguration(testVariables, configuration, sum), maxError);
		configuration[0] = 1; // a=1, b=0, c=0, d=0
		assertEquals(0.3, getConfiguration(testVariables, configuration, sum), maxError);
		configuration[0] = 0;
		configuration[1] = 1; // a=0, b=1, c=0, d=0
		assertEquals(0.4, getConfiguration(testVariables, configuration, sum), maxError);
		configuration[1] = 0;
		configuration[2] = 1; // a=0, b=0, c=1, d=0
		assertEquals(0.9, getConfiguration(testVariables, configuration, sum), maxError);
		configuration[2] = 0;
		configuration[3] = 1; // a=0, b=0, c=0, d=1
		assertEquals(0.5, getConfiguration(testVariables, configuration, sum), maxError);
		configuration[0] = 1;
		configuration[1] = 1;
		configuration[2] = 1; // a=1, b=1, c=1, d=1
		assertEquals(0.6, getConfiguration(testVariables, configuration, sum), maxError);
	}

	@Test public void testMultiply() {

		// Call method under test
		TablePotential multiplication = DiscretePotentialOperations.multiply(commonVariables.potentials);
		List<Variable> variables = multiplication.getVariables();
		assertEquals(4, variables.size());
		assertTrue(variables.contains(commonVariables.c));
		assertTrue(variables.contains(commonVariables.b));
		assertTrue(variables.contains(commonVariables.a));
		assertTrue(variables.contains(commonVariables.d));
		assertEquals(36, multiplication.values.length);
		int[] coordinate = { 0, 0, 0, 0 }; // test configuration a=0,b=0,c=0,d=0
		double value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
		assertEquals(0.007, value, maxError);
		coordinate[0] = 1; // test configuration a=1,b=0,c=0,d=0
		value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
		assertEquals(0.007, value, maxError);
		coordinate[0] = 0;
		coordinate[1] = 1; // test configuration a=0,b=1,c=0,d=0
		value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
		assertEquals(0.014, value, maxError);
		coordinate[1] = 0;
		coordinate[2] = 1; // test configuration a=0,b=0,c=1,d=0
		value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
		assertEquals(0.028, value, maxError);
		coordinate[2] = 0;
		coordinate[3] = 1; // test configuration a=0,b=0,c=0,d=1
		value = getConfiguration(commonVariables.variablesABCD, coordinate, multiplication);
		assertEquals(0.014, value, maxError);
	}

	@SuppressWarnings("unchecked") @Test public void testNewMultiply() {
		// Test constant multiplication
		List<TablePotential> potentials = allPotentials;

		// Only 1 potential so in this case the method does not do anything
		potentials.add(constantPotentials.get(1));
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: one constant potential");
		}
		assertEquals(1, aPotential.values.length);
		assertEquals(2.0, aPotential.values[0], maxError);

		// Two constant potentials
		potentials.add(constantPotentials.get(1));
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: two constant potentials");
		}
		assertEquals(4.0, aPotential.values[0], maxError);

		// Several constant potentials
		for (int i = 2; i < numConstantPotentials; i++) {
			potentials.add(constantPotentials.get(i));
		}
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: several constant potentials");
		}
		assertEquals(7257600.0, aPotential.values[0], maxError);

		// Test constant and binary potential multiplication
		// Only 1 potential so in this case the method does not do anything
		potentials.clear();
		potentials.add(normalPotentials.get(0));
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: one normal potential");
		}

		// Check variables:
		// Same number of variables
		TablePotential normalPotential = normalPotentials.get(0);
		assertEquals(aPotential.getVariables().size(), normalPotential.getVariables().size());
		// Same variables
		assertTrue(aPotential.getVariables().containsAll(normalPotential.getVariables()));

		// Check table
		// size:
		assertEquals(81, aPotential.values.length);
		// travels around all the table
		assertEquals(1.0, aPotential.values[0], maxError);

		// Two normal potentials
		potentials.add(normalPotentials.get(1));
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: two normal potentials");
		}
		// check table size
		assertEquals(81 * 81, aPotential.values.length);
		// check table content
		assertEquals(82.0, aPotential.values[0], maxError);
		ArrayList<Variable> variablesPotentials = (ArrayList<Variable>) ((Object) getUnionVariablesOrdered(potentials));
		int[] coordinate1 = { 1, 0, 0, 0, 0, 0, 0, 0 };
		double configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate1, aPotential);
		assertEquals(164.0, configuration, maxError);
		coordinate1[2] = 1;
		coordinate1[4] = 1;
		coordinate1[6] = 1;
		configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate1, aPotential);
		assertEquals(1012.0, configuration, maxError);

		// Two potentials constant and normal: c * n
		potentials.clear();
		potentials.add(constantPotentials.get(1));
		potentials.add(normalPotentials.get(0));
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: two potentials constant and normal: c * n");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(2.0, aPotential.values[0], maxError);
		int[] coordinate2 = { 1, 0, 0, 0 };
		variablesPotentials = (ArrayList<Variable>) ((Object) getUnionVariablesOrdered(potentials));
		configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate2, aPotential);
		assertEquals(4.0, configuration, maxError);

		// Two potentials constant and normal: n * c
		potentials.clear();
		potentials.add(normalPotentials.get(0));
		potentials.add(constantPotentials.get(1));
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: two potentials constant and normal: n * c");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(2.0, aPotential.values[0], maxError);
		variablesPotentials = (ArrayList<Variable>) ((Object) getUnionVariablesOrdered(potentials));
		configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate2, aPotential);
		assertEquals(4.0, configuration, maxError);

		// Three potentials constant and normal: c1 * c2 * n
		potentials.clear();
		potentials.add(constantPotentials.get(1));
		potentials.add(constantPotentials.get(2));
		potentials.add(normalPotentials.get(0));
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: three potentials constant and normal: " + "c1 * c2 * n");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(6.0, aPotential.values[0], maxError);
		variablesPotentials = (ArrayList<Variable>) ((Object) getUnionVariablesOrdered(potentials));
		int[] coordinate3 = { 1, 0, 0, 0, 0, 0, 0, 0 };
		configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate3, aPotential);
		assertEquals(12.0, configuration, maxError);

		// Three potentials constant and normal: c1 * n * c2
		potentials.clear();
		potentials.add(constantPotentials.get(1));
		potentials.add(normalPotentials.get(0));
		potentials.add(constantPotentials.get(2));
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: three potentials constant and normal: " + "c1 * n * c2");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(6.0, aPotential.values[0], maxError);
		variablesPotentials = (ArrayList<Variable>) ((Object) getUnionVariablesOrdered(potentials));
		configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate3, aPotential);
		assertEquals(12.0, configuration, maxError);

		// Three potentials constant and normal: n * c1 * c2
		potentials.clear();
		potentials.add(normalPotentials.get(0));
		potentials.add(constantPotentials.get(1));
		potentials.add(constantPotentials.get(2));
		try {
			aPotential = (TablePotential) PotentialOperations.multiply(potentials);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			fail("testNewMultiply: three potentials constant and normal: " + "n * c1 * c2");
		}
		assertEquals(81, aPotential.values.length);
		assertEquals(6.0, aPotential.values[0], maxError);
		variablesPotentials = (ArrayList<Variable>) ((Object) getUnionVariablesOrdered(potentials));
		configuration = UtilTestMethods.getConfiguration(variablesPotentials, coordinate3, aPotential);
		assertEquals(12.0, configuration, maxError);
	}

	@Test public void testMultiplyAndMarginalize() {
		// Call method under test
		TablePotential mulAndMarg = (TablePotential) DiscretePotentialOperations
				.multiplyAndMarginalize(commonVariables.potentials, commonVariables.a);
		List<Variable> variables = mulAndMarg.getVariables();
		assertEquals(3, variables.size());
		assertTrue(variables.contains(commonVariables.b));
		assertTrue(variables.contains(commonVariables.c));
		assertTrue(variables.contains(commonVariables.d));
		assertEquals(12, mulAndMarg.values.length);
		int[] coordinate = { 0, 0, 0 }; // coordinate = {0,0,0}
		double value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
		assertEquals(0.0875, value, maxError);
		coordinate[0] = 1; // coordinate = {1,0,0}
		value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
		assertEquals(0.063, value, maxError);
		coordinate[0] = 0;
		coordinate[1] = 1; // coordinate = {0,1,0}
		value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
		assertEquals(0.2625, value, maxError);
		coordinate[1] = 0;
		coordinate[2] = 1; // coordinate = {0,0,1}
		value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
		assertEquals(0.273, value, maxError);
		coordinate[0] = 2;
		coordinate[1] = 1; // coordinate = {2,1,1}
		value = getConfiguration(commonVariables.arrayVariablesBCD, coordinate, mulAndMarg);
		assertEquals(0.1435, value, maxError);
	}

	@Test
	/** Multiplies and marginalize projected potentials */ public void testMultiplyAndMarginalizeProjected()
			throws NoFindingException, WrongCriterionException, NonProjectablePotentialException {
		// Create data
		// Variables
		Variable A = new Variable("A", 2);
		Variable B = new Variable("B", 2);
		Variable C = new Variable("C", 2);
		// Arrays of variables
		ArrayList<Variable> bPotentialVariables = new ArrayList<>();
		bPotentialVariables.add(B);
		bPotentialVariables.add(A);
		ArrayList<Variable> bcPotentialVariables = new ArrayList<>();
		bcPotentialVariables.add(C);
		bcPotentialVariables.add(A);
		bcPotentialVariables.add(B);
		// table of potentials
		double[] bTable = { 0.7, 0.3, 0.9, 0.1 };
		double[] bcTable = { 0.15, 0.85, 0.84, 0.16, 0.29, 0.71, 0.98, 0.02 };
		// tablePotentials
		TablePotential bPotential = new TablePotential(bPotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY,
				bTable);
		TablePotential bcPotential = new TablePotential(bcPotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY,
				bcTable);
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
		ArrayList<TablePotential> potentials = new ArrayList<>();
		potentials.add(bPotential);
		potentials.add(bcPotential);
		// variables to keep and marginalize
		ArrayList<Variable> variablesToKeep = new ArrayList<>();
		variablesToKeep.add(C);
		ArrayList<Variable> variablesToMarginalize = new ArrayList<>();
		variablesToMarginalize.add(B);

		// Do test
		TablePotential result = (TablePotential) DiscretePotentialOperations
				.multiplyAndMarginalize(potentials, variablesToKeep, variablesToMarginalize);
		// Test table
		assertEquals(0.192, result.values[0], maxError);
		assertEquals(0.808, result.values[1], maxError);
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
	/** Multiplies and maximizes two potentials: <code>tpAB(a,b)</code> and
	 * <code>tpCAB(c,a,d)</code> that share a variable: <code>a</code>. */ public void testMultiplyAndMaximize() {
		ArrayList<Potential> potentialsVariable = new ArrayList<>();
		potentialsVariable.add(commonVariables.tpAB);
		potentialsVariable.add(commonVariables.tpCAB);
		Variable variableToMaximize = commonVariables.a; // The common variable
		Object[] potentials = DiscretePotentialOperations.multiplyAndMaximize(potentialsVariable, variableToMaximize);
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
		assertEquals(0.21, maximized.values[0], maxError);
		HashMap<Variable, Integer> coordinate = new HashMap<>();
		coordinate.put(commonVariables.b, 0);
		coordinate.put(commonVariables.c, 0);
		coordinate.put(commonVariables.d, 0);
		assertEquals(0.21, maximized.getProbability(coordinate), 0.0001);
		coordinate.put(commonVariables.b, 1);
		assertEquals(0.09, maximized.getProbability(coordinate), 0.0001);

		//		assertNotNull(maximized.interventions);
		//		assertEquals(maximized.interventions.length, maximized.values.length);
	}

	@Test public void testGetProperPotentials() {
		// Call method under test
		List<TablePotential> properPotentials = AuxiliaryOperations
				.getNonConstantPotentials(commonVariables.potentials);
		assertEquals(2, properPotentials.size());
		assertTrue(properPotentials.contains(commonVariables.tpAB));
		assertTrue(properPotentials.contains(commonVariables.tpCAB));
	}

	@Test public void testGetConstantFactor() {
		// Call method under test
		double constantFactor = DiscretePotentialOperations.getConstantFactor(commonVariables.potentials);
		assertEquals(0.35, constantFactor, maxError);
	}

	@Test public void testGetAccumulatedOffsets() {
	}

	@Test public void testNormalize() {
		TablePotential multiplication = DiscretePotentialOperations.multiply(commonVariables.potentials);
		//			TablePotential normalized;
		try {
			//				normalized = (TablePotential)
			DiscretePotentialOperations.normalize(multiplication);
			//				int[] configuration = {0, 0, 0, 0};
			//				int[] dimensions = normalized.getDimensions();
			//				List<Variable> variablesNormalized = normalized.getVariables();
		} catch (NormalizeNullVectorException e) {
			fail("Null vector exception");
			e.printStackTrace();
		}
	}

	@Test public void testDivide() throws DivideByZeroException {
		// Call method under test
		TablePotential division = (TablePotential) DiscretePotentialOperations
				.divide(commonVariables.tpConstant07, commonVariables.tpAB);
		assertEquals(2, division.getVariables().size());
		assertTrue(division.contains(commonVariables.a));
		assertTrue(division.contains(commonVariables.b));
		assertEquals(7, division.values[0], maxError);

		division = (TablePotential) DiscretePotentialOperations.divide(commonVariables.tpAB, commonVariables.tpCAB);
		assertEquals(4, division.getVariables().size());
		assertTrue(division.contains(commonVariables.a));
		assertTrue(division.contains(commonVariables.b));
		assertTrue(division.contains(commonVariables.d));
		assertTrue(division.contains(commonVariables.d));

		int[] coordinate = { 0, 0, 0, 0 };
		double value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
		assertEquals(0.5, value, maxError);
		coordinate[2] = 1; // {0,0,1,0}
		value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
		assertEquals(0.125, value, maxError);
		coordinate[2] = 0;
		coordinate[0] = 1; // {1,0,0,0}
		value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
		assertEquals(2, value, maxError);
		coordinate[0] = 0;
		coordinate[1] = 1; // {0,1,0,0}
		value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
		assertEquals(1, value, maxError);
		coordinate[1] = 0;
		coordinate[3] = 1; // {0,0,0,1}
		value = getConfiguration(commonVariables.variablesABCD, coordinate, division);
		assertEquals(0.25, value, maxError);
	}

	@SuppressWarnings("unchecked") @Test public void testMaximize() {
		// Call method under test
		Object[] potentials = DiscretePotentialOperations.maximize(commonVariables.tpAB, commonVariables.a);
		TablePotential maximizedPotential = (TablePotential) potentials[0];
		GTablePotential choicesPotential = (GTablePotential) potentials[1];
		// Test maximized potential
		// Check variables
		List<Variable> variablesPotential = maximizedPotential.getVariables();
		assertEquals(1, variablesPotential.size());
		assertTrue(variablesPotential.contains(commonVariables.b));
		// Check table
		double[] table = maximizedPotential.values;
		assertEquals(3, table.length); // size table reduced
		assertEquals(0.7, table[0], maxError);
		assertEquals(0.5, table[1], maxError);
		assertEquals(0.6, table[2], maxError);
		// Test choices potential
		// Check variables. It must contain same variables as
		// maximized potential
		variablesPotential = choicesPotential.getVariables();
		assertEquals(1, variablesPotential.size());
		assertTrue(variablesPotential.contains(commonVariables.b));
		// Check table
		Choice choice = (Choice) choicesPotential.elementTable.get(0); // table[0]
		assertEquals(2, choice.getValues()[0]);
		assertEquals(commonVariables.a, choice.getVariable());
		assertEquals(1, choice.getNumValues());

		choice = (Choice) choicesPotential.elementTable.get(1); // table[1]
		assertEquals(1, choice.getValues()[0]);
		assertEquals(commonVariables.a, choice.getVariable());
		assertEquals(1, choice.getNumValues());

		choice = (Choice) choicesPotential.elementTable.get(2); // table[2]
		assertEquals(0, choice.getValues()[0]);
		assertEquals(commonVariables.a, choice.getVariable());
		assertEquals(1, choice.getNumValues());

	}

	@Test public void testReorder1() {
		double[] table = { 0.2, 0.8, 0.4, 0.6 };
		final int numVariables = 2;

		// Setup: create a table potential
		TablePotential orderedAB = // Variables: A, B
				SharedTestUtilities.createTablePotential(numVariables, table);
		List<Variable> variablesAB = orderedAB.getVariables();

		// Create variables in other order: B, A
		List<Variable> variablesBA = new ArrayList<>();
		variablesBA.add(variablesAB.get(1));
		variablesBA.add(variablesAB.get(0));

		// Call method DiscretePotentialOperations.reorder
		TablePotential reorderedBA = DiscretePotentialOperations.reorder(orderedAB, variablesBA);

		// Test
		// 1. Test variables
		List<Variable> variablesReordered = reorderedBA.getVariables();
		assertEquals(numVariables, variablesReordered.size());
		assertEquals(variablesReordered.get(0), variablesAB.get(1));
		assertEquals(variablesReordered.get(1), variablesAB.get(0));
		// 2. Test table
		assertEquals(0.2, reorderedBA.values[0], maxError);
		assertEquals(0.4, reorderedBA.values[1], maxError);
		assertEquals(0.8, reorderedBA.values[2], maxError);
		assertEquals(0.6, reorderedBA.values[3], maxError);
	}

	@Test public void testReorder2() {
		// Original TablePotential: commonVariables.tpCAB. Variables: C,A,D
		List<Variable> variablesBeforeReorder = commonVariables.tpCAB.getVariables();
		List<Variable> variablesAfterReorder = new ArrayList<>(variablesBeforeReorder);
		Collections.reverse(variablesAfterReorder); // reorder the variables
		assertEquals(variablesAfterReorder.size(), variablesBeforeReorder.size());
		assertEquals( // Test Collections.reverse
				variablesAfterReorder.get(0), variablesBeforeReorder.get(2));
		TablePotential tablePotentialAfterReorder = DiscretePotentialOperations
				.reorder(commonVariables.tpCAB, variablesAfterReorder);
		// Test variables:
		// 1. Test numVariables
		List<Variable> variablesReorderedPotential = tablePotentialAfterReorder.getVariables();
		int numVariablesReordered = variablesReorderedPotential.size();
		assertEquals(variablesAfterReorder.size(), numVariablesReordered);
		// 2. Test variables
		for (int i = 0; i < numVariablesReordered; i++) {
			assertEquals(variablesAfterReorder.get(i), variablesReorderedPotential.get(i));
		}
		// Test table of TablePotential
		assertEquals(commonVariables.tpCAB.values.length, tablePotentialAfterReorder.values.length);
		double[] reorderedTable = { 0.2, 0.4, 0.1, 0.9, 0.3, 0.8, 0.8, 0.6, 0.9, 0.1, 0.7, 0.2 };
		for (int i = 0; i < reorderedTable.length; i++) {
			assertEquals(reorderedTable[i], tablePotentialAfterReorder.values[i], maxError);
		}
	}

	@Test public void testReorder3() {
		// another reorder
		List<Variable> variablesBeforeReorder = commonVariables.tpCAB.getVariables();
		List<Variable> variablesAfterReorder = new ArrayList<>(variablesBeforeReorder);
		variablesAfterReorder.remove(0);
		Collections.reverse(variablesAfterReorder); // reorder the variables
		variablesAfterReorder.add(0, variablesBeforeReorder.get(0));
		// Ordination: (0, 1, 2) -> (0, 2, 1)
		TablePotential tablePotentialAfterReorder = DiscretePotentialOperations
				.reorder(commonVariables.tpCAB, variablesAfterReorder);
		// Test variables:
		// 1. Test numVariables
		List<Variable> variablesReorderedPotential = tablePotentialAfterReorder.getVariables();
		int numVariablesReordered = variablesReorderedPotential.size();
		assertEquals(variablesAfterReorder.size(), numVariablesReordered);
		// 2. Test variables
		for (int i = 0; i < numVariablesReordered; i++) {
			assertEquals(variablesAfterReorder.get(i), variablesReorderedPotential.get(i));
		}
		// Test table of TablePotential
		assertEquals(commonVariables.tpCAB.values.length, tablePotentialAfterReorder.values.length);
		double[] reorderedTable = { 0.2, 0.8, 0.4, 0.6, 0.1, 0.9, 0.9, 0.1, 0.3, 0.7, 0.8, 0.2 };
		for (int i = 0; i < reorderedTable.length; i++) {
			assertEquals(reorderedTable[i], tablePotentialAfterReorder.values[i], maxError);
		}
	}

	@Test
	/** Test without utilities */ public void testSumOutVariable1() {
		Marginalization marginalization = new SumOutVariable(commonVariables.a, commonVariables.potentials);
		TablePotential utility = marginalization.getUtility();
		assertTrue(utility == null || (utility.values.length == 1 && utility.values[0] == 0.0)); // No utility
		TablePotential probability = marginalization.getProbability();
		assertNotNull(probability);
		TablePotential tablePotential = marginalization.getProbability();
		assertEquals(tablePotential.getPotentialRole(), PotentialRole.JOINT_PROBABILITY);
		List<Variable> variables = tablePotential.getVariables();
		assertEquals(3, variables.size());
		assertTrue(variables.contains(commonVariables.b));
		assertTrue(variables.contains(commonVariables.c));
		assertTrue(variables.contains(commonVariables.d));
		assertEquals(12, tablePotential.values.length);
	}

	// TODO Manolo> Hay un problema con la forma en que el métoodo SumOutVariable particiona el conjunto de potenciales entre de probabilidad y de utilidad
	// que me lleva a desactivar este test.
	//@Test

	/**
	 * Test perfect-knowledge
	 */
	public void testSumOutVariable2() {
		ProbNet perfectKnowledge = IDFactory.createNoKnowledge();
		Variable disease = null;
		Variable therapy = null;
		try {
			disease = perfectKnowledge.getVariable("Disease");
			therapy = perfectKnowledge.getVariable("Therapy");
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
			fail("Variable not found");
		}
		List<Potential> networkPotentials = perfectKnowledge.getPotentials(disease);
		List<TablePotential> networkTablePotentials = getTablePotentials(networkPotentials);
		Marginalization marginalization = new SumOutVariable(disease, networkTablePotentials);
		// Asserts
		TablePotential utility = marginalization.getUtility();
		assertNotNull(utility);
		assertNull(utility.strategyTrees);
		List<Variable> utilityVariables = utility.getVariables();
		assertEquals(1, utilityVariables.size());
		assertTrue(utilityVariables.contains(therapy));
	}

	@Test public void testSumOutVariable3() {
		// Create P(X), P/Y/X), U(X,D)
		// Variables
		Variable X = new Variable("X", "a", "b");
		Variable Y = new Variable("Y", "c", "d");
		Variable decisionVariable = new Variable("D", "opt 1", "opt 2");
		// Potential P(X)
		List<Variable> pXvariables = new ArrayList<>(1);
		pXvariables.add(X);
		TablePotential pX = new TablePotential(pXvariables, PotentialRole.CONDITIONAL_PROBABILITY,
				new double[] { 0.8, 0.2 });
		// Potential P(Y|X)
		List<Variable> pYXvariables = new ArrayList<>(2);
		pYXvariables.add(Y);
		pYXvariables.add(X);
		TablePotential pYX = new TablePotential(pYXvariables, PotentialRole.CONDITIONAL_PROBABILITY,
				new double[] { 0.9, 0.1, 0.4, 0.6 });
		// Potential U(X,D)
		List<Variable> decXDVariables = new ArrayList<>(2);
		decXDVariables.add(X);
		decXDVariables.add(decisionVariable);
		TablePotential pU = new TablePotential(decXDVariables, PotentialRole.CONDITIONAL_PROBABILITY,
				new double[] { 20, 30, 10, 40 });
		// List of potentials
		List<TablePotential> potentials = new ArrayList<>(3);
		potentials.add(pX);
		potentials.add(pYX);
		potentials.add(pU);

		// Invocation
		Marginalization marginalization = new SumOutVariable(X, potentials);

		// TODO Finish
	}

	@Test public void testSumOutVariable4() {
		Marginalization marginalization = new SumOutVariable(commonVariables.a, commonVariables.potentials);
		TablePotential utility = marginalization.getUtility();
		assertTrue(utility == null || (utility.values.length == 1 && utility.values[0] == 0.0)); // No utility
		TablePotential probability = marginalization.getProbability();
		assertNotNull(probability);
		TablePotential tablePotential = marginalization.getProbability();
		assertEquals(tablePotential.getPotentialRole(), PotentialRole.JOINT_PROBABILITY);
		List<Variable> variables = tablePotential.getVariables();
		assertEquals(3, variables.size());
		assertTrue(variables.contains(commonVariables.b));
		assertTrue(variables.contains(commonVariables.c));
		assertTrue(variables.contains(commonVariables.d));
		assertEquals(12, tablePotential.values.length);
	}

	@Test public void testMerge() {
		Variable decisionA = new Variable("DecA", 3);
		Variable X = new Variable("X", "Xa", "Xb");
		Variable Y = new Variable("Y", "Ya", "Yb", "Yc");
		StrategyTree interventionX1 = new StrategyTree(X);
		StrategyTree interventionX2 = new StrategyTree(X);
		StrategyTree interventionXY1 = new StrategyTree(Y);
		StrategyTree interventionXY2 = new StrategyTree(Y);
		StrategyTree interventionXY3 = new StrategyTree(Y);
		StrategyTree interventionXY4 = new StrategyTree(Y);
		StrategyTree interventionXY5 = new StrategyTree(Y);
		StrategyTree interventionXY6 = new StrategyTree(Y);
		UncertainValue uncertainValue1 = new UncertainValue(1.0);
		UncertainValue uncertainValue2 = new UncertainValue(2.0);
		UncertainValue uncertainValue3 = new UncertainValue(3.0);
		UncertainValue uncertainValue4 = new UncertainValue(4.0);
		UncertainValue uncertainValue5 = new UncertainValue(5.0);
		UncertainValue uncertainValue6 = new UncertainValue(6.0);
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
		TablePotential x1P = new TablePotential(Arrays.asList(X), role);
		TablePotential xyP = new TablePotential(Arrays.asList(X, Y), role);
		TablePotential x2P = new TablePotential(Arrays.asList(X), role);
		// TODO Finish this method
	}

	/**
	 * Convert a Potential list to a TablePotential list.
	 *
	 * @param potentials
	 * @return
	 */
	private List<TablePotential> getTablePotentials(List<Potential> potentials) {
		List<TablePotential> tablePotentials = new ArrayList<>(potentials.size());
		for (Potential potential : potentials) {
			try {
				if (potential instanceof ExactDistrPotential) {
					tablePotentials.add(((ExactDistrPotential) potential).getTablePotential());
				} else {
					tablePotentials.add((TablePotential) potential.getCPT());
				}
			} catch (NonProjectablePotentialException | WrongCriterionException e) {
				fail("Can not get TablePotential from ExactDistrPotential. Potential: " + potential);
			}
		}
		return tablePotentials;
	}

	@Ignore("Ignored because a NullPointerException") @Test public void testMaxOutVariable() {
		// Method invocation
		ProbNet perfectKnowledge = IDFactory.createPerfectKnowledge();
		Variable disease = null;
		Variable therapy = null;
		try {
			disease = perfectKnowledge.getVariable("Disease");
			therapy = perfectKnowledge.getVariable("Therapy");
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
			fail("Variable not found");
		}
		List<Potential> potentials = perfectKnowledge.getPotentials(therapy);
		List<TablePotential> tablePotentials = projectToTable(potentials, perfectKnowledge);
		TablePotential utility = (new MaxOutVariable(therapy, null, tablePotentials)).getUtility();

		// Asserts
		// Test utility potential
		assertNotNull(utility);

		// Utility variables
		List<Variable> utilityVariables = utility.getVariables();
		assertEquals(1, utilityVariables.size());

		// Test utility values
		assertEquals(disease, utilityVariables.get(0));
		assertEquals(2, utility.values.length);
		assertEquals(10, utility.values[0], maxError);
		assertEquals(7.75, utility.values[1], maxError);

		// Test utility interventions
		assertNotNull(utility.strategyTrees);
		assertEquals(2, utility.strategyTrees.length);
		StrategyTree strategyTreeNo = utility.strategyTrees[0];
		assertEquals(therapy, strategyTreeNo.getRootVariable());
		List<TreeADDBranch> branches = strategyTreeNo.getBranches();
		assertEquals(1, branches.size());
		TreeADDBranch branch = branches.get(0);
		List<State> states = branch.getBranchStates();
		assertEquals(1, states.size());
		State noState = states.get(0);
		assertEquals(0, therapy.getStateIndex(noState));
	}

	private List<TablePotential> projectToTable(List<Potential> potentials, ProbNet probNet) {
		List<TablePotential> tablePotentials = new ArrayList<TablePotential>(potentials.size());
		for (Potential potential : potentials) {

			try {
				List<TablePotential> projectedPotentials = potential
						.tableProject(new EvidenceCase(), probNet.getInferenceOptions());
				tablePotentials.addAll(projectedPotentials);
			} catch (NonProjectablePotentialException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrongCriterionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tablePotentials;
	}

	//TODO This test should be reviewed after we decide what to do with the role policy
/*	@Test
	public void joinTestMaxOutAndSumOut() throws NodeNotFoundException {
		ProbNet testDecision = org.openmarkov.core.model.network.factory.IDFactory.buildIDDecideTest();

		// Remove disease
		Variable disease = testDecision.getVariable("Disease");
		List<TablePotential> diseasePotentials = getTablePotentials(testDecision.getPotentials(disease));
		List<TablePotential> afterRemovingDiseasePotentials = 
				DiscretePotentialOperations.sumOutVariable(disease, diseasePotentials);
		removePotentials(testDecision, diseasePotentials);
		testDecision.removeNode(testDecision.getNode(disease));
		addNoPolicyPotentials(testDecision, afterRemovingDiseasePotentials);
		
		// Remove therapy
		Variable therapy = testDecision.getVariable("Therapy");
		List<TablePotential> therapyPotentials = getTablePotentials(testDecision.getPotentials(therapy));
		List<TablePotential> afterRemovingTherapyPotentials = 
				DiscretePotentialOperations.maxOutVariable(therapy, therapyPotentials);
		removePotentials(testDecision, therapyPotentials);
		testDecision.removeNode(testDecision.getNode(therapy));
		addNoPolicyPotentials(testDecision, afterRemovingTherapyPotentials);
	
		// Remove result of test
		Variable resultOfTest = testDecision.getVariable("Result of test");
		List<TablePotential> resultOfTestPotentials = getTablePotentials(testDecision.getPotentials(resultOfTest));
		List<TablePotential> afterRemovingResultOfTestPotentials = 
				DiscretePotentialOperations.sumOutVariable(resultOfTest, resultOfTestPotentials);
		removePotentials(testDecision, resultOfTestPotentials);
		testDecision.removeNode(testDecision.getNode(resultOfTest));
		addNoPolicyPotentials(testDecision, afterRemovingResultOfTestPotentials);

		// Remove do test
		Variable doTest = testDecision.getVariable("Do test?");
		List<TablePotential> doTestPotentials = getTablePotentials(testDecision.getPotentials(doTest));
		List<TablePotential> afterRemovingDoTestPotentials = 
				DiscretePotentialOperations.maxOutVariable(doTest, doTestPotentials);
		removePotentials(testDecision, afterRemovingDoTestPotentials);
		testDecision.removeNode(testDecision.getNode(doTest));
		addNoPolicyPotentials(testDecision, afterRemovingDoTestPotentials);
	}*/

	private void addNoPolicyPotentials(ProbNet probNet, List<TablePotential> potentials) {
		for (Potential potential : potentials) {
			if (potential.getPotentialRole() != PotentialRole.POLICY) {
				probNet.addPotential(potential);
			}
		}
	}

	private void removePotentials(ProbNet probNet, List<TablePotential> potentials) {
		for (Potential potential : potentials) {
			probNet.removePotential(potential);
		}
	}

	/**
	 * Translates the coordinate received in (variables, coordinateVariables)
	 * to the potential variables ordination and returns the configuration
	 * value.
	 *
	 * @param variables           <code>ArrayList</code> of <code>Variable</code>
	 * @param coordinateVariables <code>int[]</code>
	 * @param potential           <code>TablePotential</code>
	 * @return configuration value. <code>double</code>
	 */
	private double getConfiguration(List<Variable> variables, int[] coordinateVariables, TablePotential potential) {
		List<Variable> variablesPotential = potential.getVariables();
		int[] coordinate = new int[variablesPotential.size()];
		int i = 0;
		for (Variable variablePotential : variablesPotential) {
			coordinate[i++] = coordinateVariables[variables.indexOf(variablePotential)];
		}
		return potential.values[potential.getPosition(coordinate)];
	}

	/**
	 * @param potentials <code>ArrayList</code> of <code>Potential</code>.
	 * @return An <code>ArrayList</code> of <code>Variable</code> with all the
	 * variables of the potentials in order: first the variables of the first
	 * potential, next the remaining variables of the second potential, etc.
	 */
	private List<Variable> getUnionVariablesOrdered(List<? extends Potential> potentials) {
		List<Variable> variables = new ArrayList<>();
		for (Potential potential : potentials) {
			for (Variable variable : potential.getVariables()) {
				if (!variables.contains(variable)) {
					variables.add(variable);
				}
			}
		}
		return variables;
	}

}
