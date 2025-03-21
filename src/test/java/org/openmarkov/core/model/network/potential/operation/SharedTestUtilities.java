/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;

/**
 * Auxiliary class for <code>DiscretePotentialOperationsTest</code>. Creates
 * four  <code>TablePotential</code>s and puts them in an <code>ArrayList</code>
 * of <code>TablePotential</code>s:<p>
 * <code>tpConstant07</code>: 0 variables (constant potential). table[0] = 0.7<p>
 * <code>tpAB</code>: 2 variables. <code>a</code> (3 states) and <code>b</code>
 * (3 states)<p> table = {0.1, 0.2, 0.7, 0.2, 0.5, 0.3, 0.6, 0.3, 0.1}.
 * table.length = 9.<p>
 * <code>tpConstant05</code>: 0 variables (constant potential). table[0] = 0.5<p>
 * <code>tpCAB</code>: 3 variables. <code>c</code> (2 states), <code>a</code>
 * (3 states) and <code>d</code> (2 states)<p>
 * table = {0.2, 0.8, 0.1, 0.9, 0.3, 0.7, 0.4, 0.6, 0.9, 0.1, 0.8, 0.2}.
 * table.length = 12.<p>
 */
public class SharedTestUtilities {

	// Constants

	private final static String[] letters = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
			"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	// Attributes
	// TablePotential used for potential operations
	/**
	 * 0 variables (constant potential). table[0] = 0.7.
	 */
	public TablePotential tpConstant07;

	/**
	 * 2 variables. <code>a</code> (3 states) and <code>b</code> (3 states)<p>
	 * table = {0.1, 0.2, 0.7, 0.2, 0.5, 0.3, 0.6, 0.3, 0.1}.<p>
	 * table.length = 9.
	 */
	public TablePotential tpAB;

	/**
	 * 0 variables (constant potential). table[0] = 0.5.
	 */
	public TablePotential tpConstant05;

	/**
	 * 3 variables. <code>c</code> (2 states), <code>a</code> (3 states) and
	 * <code>d</code> (2 states)<p>
	 * table = {0.2, 0.8, 0.1, 0.9, 0.3, 0.7, 0.4, 0.6, 0.9, 0.1, 0.8, 0.2}<p>
	 * table.length = 12.
	 */
	public TablePotential tpCAB;

	/**
	 * 1 variable. <code>a</code> (3 states)<p>
	 * table = {0.5, 0.4, 0.1}.<p>
	 * table.length = 3.
	 */
	public TablePotential tpA;

	// Variables used to create the preceding TablePotentials
	/**
	 * <code>Variable</code> with 3 states
	 */
	public Variable a;

	/**
	 * <code>Variable</code> with 3 states
	 */
	public Variable b;

	/**
	 * <code>Variable</code> with 2 states
	 */
	public Variable c;

	/**
	 * <code>Variable</code> with 2 states
	 */
	public Variable d;

	// States of the preceding variables
	public State[] statesA;

	public State[] statesB;

	public State[] statesC;

	public State[] statesD;

	// Arrays of variables used in the TablePotential constructor
	/**
	 * Contains zero variables
	 */
	public ArrayList<Variable> variablesEmpty;

	/**
	 * Contains 1 variables: <code>a</code>.
	 */
	public ArrayList<Variable> variablesA;

	/**
	 * Contains 2 variables: <code>a</code> and <code>b</code>.
	 */
	public ArrayList<Variable> variablesAB;

	/**
	 * Contains 3 variables: <code>c</code>, <code>a</code>  and
	 * <code>d</code>.
	 */
	public ArrayList<Variable> variablesCAD;

	/**
	 * Contains 3 variables: <code>a</code>, <code>b</code> and <code>c</code>.
	 */
	public ArrayList<Variable> arrayVariablesABC;

	/**
	 * Contains 3 variables: <code>b</code>, <code>d</code> and <code>d</code>.
	 */
	public ArrayList<Variable> arrayVariablesBCD;

	/**
	 * Contains 4 variables: <code>a</code>, <code>b</code>, <code>c</code> and
	 * <code>d</code>.
	 */
	public ArrayList<Variable> variablesABCD;

	// Array of TablePotentials
	/**
	 * Potentials: <code>tpConstant07, tpAB, tpConstant05</code> and <code>tpCAB</code>
	 */
	public ArrayList<TablePotential> potentials;

	public SharedTestUtilities() throws Exception {
		// Create states of variables
		statesA = new State[3]; // 3 states variable
		statesA[0] = new State("0");
		statesA[1] = new State("1");
		statesA[2] = new State("2");
		statesB = new State[3]; // 3 states variable
		statesB[0] = new State("0");
		statesB[1] = new State("1");
		statesB[2] = new State("2");
		statesC = new State[2]; // 2 states variable
		statesC[0] = new State("0");
		statesC[1] = new State("1");
		statesD = new State[2]; // 2 states variable
		statesD[0] = new State("0");
		statesD[1] = new State("1");

		// Create variables:
		a = new Variable("A", statesA);
		b = new Variable("B", statesB);
		c = new Variable("C", statesC);
		d = new Variable("D", statesD);

		// Create ArrayList's for the TablePotential's constructors
		variablesAB = new ArrayList<>(); // 2 variables
		variablesAB.add(a);
		variablesAB.add(b);
		variablesEmpty = new ArrayList<>(); // 0 variables. A constant.
		variablesCAD = new ArrayList<>(); // 3 variables
		variablesCAD.add(c);
		variablesCAD.add(a);
		variablesCAD.add(d);
		variablesABCD = new ArrayList<>();
		variablesABCD.add(a);
		variablesABCD.add(b);
		variablesABCD.add(c);
		variablesABCD.add(d);
		variablesA = new ArrayList<>();
		variablesA.add(a);
		arrayVariablesABC = new ArrayList<>();
		arrayVariablesABC.add(a);
		arrayVariablesABC.add(b);
		arrayVariablesABC.add(c);
		arrayVariablesBCD = new ArrayList<>();
		arrayVariablesBCD.add(b);
		arrayVariablesBCD.add(c);
		arrayVariablesBCD.add(d);

		// Create TablePotential's
		tpConstant07 = new TablePotential(variablesEmpty, PotentialRole.CONDITIONAL_PROBABILITY);
		tpAB = new TablePotential(variablesAB, PotentialRole.CONDITIONAL_PROBABILITY);
		tpConstant05 = new TablePotential(variablesEmpty, PotentialRole.CONDITIONAL_PROBABILITY);
		tpCAB = new TablePotential(variablesCAD, PotentialRole.CONDITIONAL_PROBABILITY);
		tpA = new TablePotential(variablesA, PotentialRole.CONDITIONAL_PROBABILITY);
		// Initialize not constant potentials tables
		tpAB.values[0] = 0.1;
		tpAB.values[1] = 0.2;
		tpAB.values[2] = 0.7;
		tpAB.values[3] = 0.2;
		tpAB.values[4] = 0.5;
		tpAB.values[5] = 0.3;
		tpAB.values[6] = 0.6;
		tpAB.values[7] = 0.3;
		tpAB.values[8] = 0.1;

		tpCAB.values[0] = 0.2;
		tpCAB.values[1] = 0.8;
		tpCAB.values[2] = 0.1;
		tpCAB.values[3] = 0.9;
		tpCAB.values[4] = 0.3;
		tpCAB.values[5] = 0.7;
		tpCAB.values[6] = 0.4;
		tpCAB.values[7] = 0.6;
		tpCAB.values[8] = 0.9;
		tpCAB.values[9] = 0.1;
		tpCAB.values[10] = 0.8;
		tpCAB.values[11] = 0.2;

		tpA.values[0] = 0.5;
		tpA.values[1] = 0.4;
		tpA.values[2] = 0.1;

		// Create array of potentials
		potentials = new ArrayList<>();

		// Initialize array with potentials
		potentials.add(tpConstant07);
		potentials.add(tpAB);
		potentials.add(tpConstant05);
		potentials.add(tpCAB);

		// Initialize the constant potentials
		tpConstant07.values[0] = 0.7;
		tpConstant05.values[0] = 0.5;
	}

	/**
	 * Generates sets of variables and potentials.
	 *
	 * @param numPotentials        The number of potentials to generate.
	 * @param numVarsEachPotential The number of variables in each potential.
	 * @param numVarsInCommon      The number of variables the potential i has in
	 *                             common with the potential i+1.
	 * @param numStates            Number of states of each variable.
	 */
	public static ArrayList<TablePotential> generatePotentials(int numPotentials, int numVarsEachPotential,
			int numVarsInCommon, int numStates) throws Exception {
		ArrayList<TablePotential> potentials = new ArrayList<>();

		// Creates the states of each variable
		State[] states = new State[numStates];
		for (int i = 0; i < numStates; i++) {
			states[i] = new State("s" + i);
		}

		// Calculates the number of variables to create
		int numVariablesToCreate = numVarsEachPotential + (numPotentials - 1) * (
				numVarsEachPotential - numVarsInCommon
		);
		// Create the number of variables calculated
		Variable[] variables = new Variable[numVariablesToCreate];
		for (int iVariable = 0; iVariable < numVariablesToCreate; iVariable++) {
			variables[iVariable] = new Variable("X" + iVariable, states);
		}

		// Creates the potentials
		for (int iPotential = 0; iPotential < numPotentials; iPotential++) {
			ArrayList<Variable> variablesPotential = new ArrayList<>(numVarsEachPotential);
			for (int iVariable = 0; iVariable < numVarsEachPotential; iVariable++) {
				variablesPotential.add(variables[iPotential * (numVarsEachPotential - numVarsInCommon) + iVariable]);
			}
			TablePotential iTablePotential = new TablePotential(variablesPotential,
					PotentialRole.CONDITIONAL_PROBABILITY);
			potentials.add(iTablePotential);
		}

		return potentials;
	}

	/**
	 * Create a <code>TablePotential</code> with binary variables A, B, ... X
	 * (<code>numVariables</code>)
	 *
	 * @param numVariables <code>int</code>
	 * @param table        <code>double[]</code>
	 * @return TablePotential
	 */
	public static TablePotential createTablePotential(int numVariables, double[] table) {
		// Create variables
		ArrayList<Variable> variables = new ArrayList<>(numVariables);
		Variable variable;
		State[] states = { new State("0"), new State("1") };

		for (int numVariable = 0; numVariable < numVariables; numVariable++) {
			String variableName = letters[numVariable];
			variable = new Variable(variableName, states);
			variables.add(variable);
		}

		TablePotential tablePotential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, table);
		return tablePotential;
	}

}
