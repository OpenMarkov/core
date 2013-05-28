/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.operation;

import java.util.ArrayList;
import java.util.HashMap;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;


/** Auxiliary class for <code>DiscretePotentialOperationsTest</code>. Creates
 * four  <code>TablePotential</code>s and puts them in an <code>ArrayList</code>
 * of <code>TablePotential</code>s:<p>
 * <code>t1</code>: 0 variables (constant potential). table[0] = 0.7<p>
 * <code>t2</code>: 2 variables. <code>a</code> (3 states) and <code>b</code>
 * (3 states)<p> table = {0.1, 0.2, 0.7, 0.2, 0.5, 0.3, 0.6, 0.3, 0.1}.
 * table.length = 9.<p>
 * <code>t3</code>: 0 variables (constant potential). table[0] = 0.5<p>
 * <code>t4</code>: 3 variables. <code>c</code> (2 states), <code>a</code> 
 * (3 states) and <code>d</code> (2 states)<p> 
 * table = {0.2, 0.8, 0.1, 0.9, 0.3, 0.7, 0.4, 0.6, 0.9, 0.1, 0.8, 0.2}.
 * table.length = 12.<p> */
public class SharedTestUtilities {
	
	// Constants

	private final static String[] letters = {"A", "B", "C", "D", "E", "F", "G",
		"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
		"V", "W", "X", "Y", "Z"};

	// Attributes
	// TablePotential used for potential operations
	/** 0 variables (constant potential). table[0] = 0.7. */
	public TablePotential t1;

	/** 2 variables. <code>a</code> (3 states) and <code>b</code> (3 states)<p>
     * table = {0.1, 0.2, 0.7, 0.2, 0.5, 0.3, 0.6, 0.3, 0.1}.<p>
     * table.length = 9. */
	public TablePotential t2;

	/** 0 variables (constant potential). table[0] = 0.5. */
	public TablePotential t3;

	/** 3 variables. <code>c</code> (2 states), <code>a</code> (3 states) and 
	 * <code>d</code> (2 states)<p> 
	 * table = {0.2, 0.8, 0.1, 0.9, 0.3, 0.7, 0.4, 0.6, 0.9, 0.1, 0.8, 0.2}<p>
	 * table.length = 12.*/
	public TablePotential t4;
	
	// Variables used to create the preceding TablePotentials
	/** <code>Variable</code> with 3 states */
	public Variable a;

	/** <code>Variable</code> with 3 states */
	public Variable b;

	/** <code>Variable</code> with 2 states */
	public Variable c;

	/** <code>Variable</code> with 2 states */
	public Variable d;
	
	// States of the preceding variables
	public State[] statesA;

	public State[] statesB;

	public State[] statesC;

	public State[] statesD;

	// Arrays of variables used in the TablePotential constructor
	/** Contains zero variables */
	public ArrayList<Variable> variablesT1;

	/** Contains 2 variables: <code>a</code> and <code>b</code>. */
	public ArrayList<Variable> variablesT2;

	/** Contains zero variables */
	public ArrayList<Variable> variablesT3;

	/** Contains 3 variables: <code>c</code>, <code>a</code>  and 
	 * <code>d</code>. */
	public ArrayList<Variable> variablesT4;
	
	/** Contains 4 variables: <code>a</code>, <code>b</code>, <code>c</code> and
	 * <code>d</code>. */
	public ArrayList<Variable> totalVariables;
	
	/** Contains 1 variables: <code>a</code>. */
	public ArrayList<Variable> arrayVariablesA;	
	
	/** Contains 3 variables: <code>a</code>, <code>b</code> and <code>c</code>.
	  */
	public ArrayList<Variable> arrayVariablesABC;
	
	/** Contains 3 variables: <code>b</code>, <code>d</code> and <code>d</code>.
	 */
	public ArrayList<Variable> arrayVariablesBCD;
	
	// Array of TablePotentials
	/** Potentials: <code>t1, t2, t3</code> and <code>t4</code> */
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
		variablesT1 = new ArrayList<Variable>(); // 0 variables. A constant.
		variablesT2 = new ArrayList<Variable>(); // 2 variables
		variablesT2.add(a);
		variablesT2.add(b);
		variablesT3 = new ArrayList<Variable>(); // 0 variables. A constant.
		variablesT4 = new ArrayList<Variable>(); // 3 variables
		variablesT4.add(c);
		variablesT4.add(a);
		variablesT4.add(d);
		totalVariables = new ArrayList<Variable>();
		totalVariables.add(a);
		totalVariables.add(b);
		totalVariables.add(c);
		totalVariables.add(d);
		arrayVariablesA = new ArrayList<Variable>();
		arrayVariablesA.add(a);
		arrayVariablesABC = new ArrayList<Variable>();
		arrayVariablesABC.add(a);
		arrayVariablesABC.add(b);
		arrayVariablesABC.add(c);
		arrayVariablesBCD = new ArrayList<Variable>();
		arrayVariablesBCD.add(b);
		arrayVariablesBCD.add(c);
		arrayVariablesBCD.add(d);
		
		// Create TablePotential's
		t1 = new TablePotential(
				variablesT1, PotentialRole.CONDITIONAL_PROBABILITY);
		t2 = new TablePotential(
				variablesT2, PotentialRole.CONDITIONAL_PROBABILITY);
		t3 = new TablePotential(
				variablesT3, PotentialRole.CONDITIONAL_PROBABILITY);
		t4 = new TablePotential(
				variablesT4, PotentialRole.CONDITIONAL_PROBABILITY);
		// Initialize not constant potentials tables
		t2.values[0] = 0.1;
		t2.values[1] = 0.2;
		t2.values[2] = 0.7;
		t2.values[3] = 0.2;
		t2.values[4] = 0.5;
		t2.values[5] = 0.3;
		t2.values[6] = 0.6;
		t2.values[7] = 0.3;
		t2.values[8] = 0.1;
		
		t4.values[0] = 0.2;
		t4.values[1] = 0.8;
		t4.values[2] = 0.1;
		t4.values[3] = 0.9;
		t4.values[4] = 0.3;
		t4.values[5] = 0.7;
		t4.values[6] = 0.4;
		t4.values[7] = 0.6;
		t4.values[8] = 0.9;			
		t4.values[9] = 0.1;
		t4.values[10] = 0.8;
		t4.values[11] = 0.2;
		
		// Create array of potentials
		potentials = new ArrayList<TablePotential>();
		
		// Initialize array with potentials
		potentials.add(t1);
		potentials.add(t2);
		potentials.add(t3);
		potentials.add(t4);
		
		// Initialize the constant potentials
		t1.values[0] = 0.7;
		t3.values[0] = 0.5;
	}

	/** Generates sets of variables and potentials. 
	  * @param numPotentials The number of potentials to generate.
	  * @param numVarsEachPotential The number of variables in each potential.
	  * @param numVarsInCommon The number of variables the potential i has in 
	  * common with the potential i+1.
	  * @param numStates Number of states of each variable. */
	public static ArrayList<TablePotential> generatePotentials(
			int numPotentials, int numVarsEachPotential, int numVarsInCommon, 
			int numStates) throws Exception {
		ArrayList<TablePotential> potentials = new ArrayList<TablePotential>();
	    
		// Creates the states of each variable
		State[] states = new State[numStates]; 
		for (int i = 0; i < numStates; i++) {
			states[i] = new State("s"+i);
		}	    	 
	    
		// Calculates the number of variables to create
		int numVariablesToCreate = numVarsEachPotential + (numPotentials - 1) * 
			(numVarsEachPotential - numVarsInCommon);
		// Create the number of variables calculated
		Variable[] variables = new Variable[numVariablesToCreate];
		for (int iVariable = 0; iVariable < numVariablesToCreate; iVariable++) {
			variables[iVariable] = new Variable("X" + iVariable, states); 
		}

		// Creates the potentials
		for (int iPotential = 0; iPotential < numPotentials; iPotential++) {
			ArrayList<Variable> variablesPotential = 
				    new ArrayList<Variable>(numVarsEachPotential);
			for (int iVariable = 0; iVariable < numVarsEachPotential; 
			        iVariable++) {
				variablesPotential.add(variables[iPotential * 
					(numVarsEachPotential - numVarsInCommon) + iVariable]);
			}
			TablePotential iTablePotential = new TablePotential(
					variablesPotential, PotentialRole.CONDITIONAL_PROBABILITY);
			potentials.add(iTablePotential);
		}
			    
		return potentials;
	}
	
	/** Create a <code>TablePotential</code> with binary variables A, B, ... X 
	 *  (<code>numVariables</code>) and additionalProperties = received 
	 *  <code>additionalProperties</code> object if it is not <code>null</code>
	 *  or additionalProperties = 
	 *  <code>HashMap<String, Object> additionalProperties</code> with one 
	 *  entry: key = "property", value = "property" if 
	 *  <code>additionalProperties</code> is <code>null</code>
	 *  @param numVariables. <code>int</code>
	 *  @param table. <code>double[]</code>
	 *  @param additionalProperties. 
	 *    <code>HashMap<String, Object> additionalProperties</code>
	 *  @return TablePotential */
	public static TablePotential createTablePotential(int numVariables, 
			double[] table, HashMap<String, Object> properties) {
		// Create variables
		ArrayList<Variable> variables = new ArrayList<Variable>(numVariables);
		Variable variable;
		State[] states = {new State("0"), new State("1")};
		
		for (int numVariable = 0; numVariable < numVariables; numVariable++) {
			String variableName = letters[numVariable];
			variable = new Variable(variableName, states);
			variables.add(variable);
		}
		
		// Create additionalProperties
		if (properties == null) {
			properties = new HashMap<String, Object>();
			properties.put("property", "property");
		}
		
		TablePotential tablePotential = new TablePotential(
				variables, PotentialRole.CONDITIONAL_PROBABILITY, table);
		return tablePotential;
	}

}
