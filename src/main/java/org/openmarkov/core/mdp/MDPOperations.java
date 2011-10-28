package org.openmarkov.core.mdp;

import java.util.ArrayList;

import org.openmarkov.core.model.network.Variable;

/**
 * @author Jorge
 *
 */
public final class MDPOperations {
	
	/**
	 * @param searchedVariables
	 * @param variables
	 * @return
	 * @throws Exception
	 */
	protected static int[] getOrdering(ArrayList<? extends Variable> searchedVariables, 
			ArrayList<? extends Variable> variables) throws Exception {

		if (variables.isEmpty()) {
			throw new Exception("segundo parametro de getOrdering no puede ser una lista vacia");
		}

		int []ordering = new int[variables.size()]; 

		for (int i = 0; i < ordering.length; i++) {
			ordering[i] = searchedVariables.indexOf(variables.get(i));
		}

		return ordering;
	}	
}
