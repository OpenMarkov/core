/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.TablePotential;


/** This class has usefully static methods for testing but it is not a test 
 * class. */
public class UtilTestMethods {

    /** Looks for a variable with the received name.
     * @param variables <code>ArrayList</code> of <code>Variable</code>s. 
     * @param name <code>String</code>.
     * @return First variable in <code>variables</code> with the given 
     * <code>name</code> or <code>null</code> if not exist. */
    public static Variable getVariableName(
    		Collection<? extends Variable> variables, String name) {
    	for (Variable variable : variables) {
    		if (variable.getName().equals(name)) {
    			return variable;
    		}
    	}
		return null;
	}

	/** @param variablesChoice <code>ArrayList</code> of <code>Variable</code>.
     * @param name <code>String</code>.
     * @return <code>true</code> if exists a <code>Variables</code> in 
     * 		   <code>variablesChoice</code> with the given name. */
	public static boolean existsVariableName(
			Collection<? extends Variable> variablesChoice, String name) {
		for (Variable variable : variablesChoice) {
			if (variable.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
    
    /** Translates the coordinate received in the first two parameters to the 
     * variables ordination of the third parameter and returns the configuration
     * value. 
     * @return A configuration of the potential received in the third parameter 
     * (an <code>Object</code>).
     * @param variables <code>ArrayList</code> of <code>Variable</code>.
     * @param coordinateVariables <code>int[]</code>.
     * @param potential <code>GTablePotential</code>. */
    @SuppressWarnings("unchecked")
	public static Object getConfiguration(ArrayList<Variable> variables, 
    		int[] coordinateVariables, GTablePotential potential) {
    	int position = 
    		getConfigurationPosition(variables, coordinateVariables, potential);
    	return potential.elementTable.get(position);
    }
    
    /** Translates the coordinate received in the first two parameters to the 
     * variables ordination of the third parameter and returns the configuration
     * value. 
     * @return A configuration of the potential received in the third parameter 
     * (a <code>double</code>).
     * @param variables <code>ArrayList</code> of <code>Variable</code>.
     * @param coordinateVariables <code>int[]</code>.
     * @param potential <code>TablePotential</code>. */
    public static double getConfiguration(ArrayList<Variable> variables,
    		int[] coordinateVariables, TablePotential potential) {
    	int position = 
    		getConfigurationPosition(variables, coordinateVariables, potential);
    	return potential.values[position];
    }

    /** This method invokes a private method of a class. 
     * Use this skeleton to call this method:<p>
     * <code>
	 * MyClass instance = new MyClass();<p>
	 * String expResult = "Expected Result";<p>
	 * Object[] params = {"A String Value", "Another Value"};<p>
	 * String result = (String) this.invokePrivateMethod(instance,
	 *  "myPrivateName", params);<p>
	 * assertEquals(expResult, result);<p>
	 * </code>
	 * @param test Object of the class that will be checked. <code>Object</code>
	 * @param methodName. <code>String</code>
	 * @param params[]. <code>Object[]</code>
	 * @return The <code>Object</code> returned by the private method.
	 * @throws Exception. A generic exception because the invoked method can 
	 *  fail in unpredictable ways. */
    public static Object invokePrivateMethod (Object test, String methodName, 
    		Object params[]) throws Exception {
        Object ret = null;
    		 
        final Method[] methods =
    	     test.getClass().getDeclaredMethods();
    	for (int i = 0; i < methods.length; i++) {
    	    if (methods[i].getName().equals(methodName)) {
    	        methods[i].setAccessible(true);
    	        ret = methods[i].invoke(test, params);
    	        break;
    	    }
    	}
    	return ret;
    }
    
    /** Translates the coordinate received in the first two parameters to the 
     * variables ordination of the third parameter and returns the position of
     * the configuration.
     * @return The position of the potential received in the third parameter 
     * (an <code>double</code>).
     * @param variables <code>ArrayList</code> of <code>Variable</code>.
     * @param coordinateVariables <code>int[]</code>.
     * @param potential <code>TablePotential</code>. */
    private static int getConfigurationPosition(ArrayList<Variable> variables,
    		int[] coordinateVariables, TablePotential potential) {
    	ArrayList<Variable> variablesPotential = potential.getVariables();
    	int[] coordinate = new int[variablesPotential.size()];
    	int i = 0;
    	for (Variable variable : variables) {
    		coordinate[variablesPotential.indexOf(variable)] = 
    			coordinateVariables[i++];
    	} 
    	return potential.getPosition(coordinate);
    }
    
}
