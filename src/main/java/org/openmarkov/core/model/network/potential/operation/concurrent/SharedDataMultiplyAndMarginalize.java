/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.operation.concurrent;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

public class SharedDataMultiplyAndMarginalize extends SharedDataMultiply {

	ArrayList<Variable> variablesToKeep;

	ArrayList<Variable> variablesToEliminate;
	
	// Attributes related to the product potential that will be marginalized
	ArrayList<Variable> unionVariables;
	
	int[] unionCoordinate;
	
	int[] unionDimensions;
	
	int eliminationSize;

	@SuppressWarnings("unchecked")
	public SharedDataMultiplyAndMarginalize(
			ArrayList<Potential> potentials, 
			ArrayList<Variable> fSVariablesToKeep,
			ArrayList<Variable> fSVariablesToEliminate) 
    		throws NotEnoughMemoryException {

		super((ArrayList<TablePotential>)(Object)potentials);
    	
    	this.variablesToKeep = 
    		(ArrayList<Variable>)((Object)fSVariablesToKeep);

    	this.variablesToEliminate = 
    		(ArrayList<Variable>)((Object)fSVariablesToEliminate);
	}

	/** Does some previous not parallel operations 
	 * @throws Exception */
	@SuppressWarnings("unchecked")
	public void initializeMultiplyAndMarginalize() 
			throws Exception {
		super.initialize();
		// variables in the resulting potential
		ArrayList<Variable> unionVariables = (ArrayList<Variable>)
			variablesToEliminate.clone();
		unionVariables.addAll(variablesToKeep);
		int numUnionVariables = unionVariables.size();
		
		// current coordinate in the resulting potential
		unionCoordinate = new int[numUnionVariables];
		unionDimensions = TablePotential.calculateDimensions(unionVariables);
		
		// Defines some arrays for the proper potentials...
		double[][] tables = new double[numPotentials][];
		int[] initialPositions = new int[numPotentials];
		int[] currentPositions = new int[numPotentials];
		int[][] accumulatedOffsets = new int[numPotentials][];
		// ... and initializes them
		TablePotential unionPotential = new TablePotential(unionVariables,null);
		for (int i = 0; i < numPotentials; i++) {
			TablePotential potential = (TablePotential)potentials.get(i);
			tables[i] = potential.values;
			initialPositions[i] = potential.getInitialPosition();
			currentPositions[i] = initialPositions[i];
			accumulatedOffsets[i] = unionPotential
				.getAccumulatedOffsets(potential.getOriginalVariables());
		}
		
		// The elimination size is the product of the dimensions of the
		// variables to eliminate
		eliminationSize = 1;
		for (Variable variable : variablesToEliminate) {
			eliminationSize *= variable.getNumStates();
		}
		
	}

}
