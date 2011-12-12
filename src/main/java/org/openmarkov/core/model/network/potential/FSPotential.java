/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;

import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;


/** @author marias
  * @author fjdiez
  * @version 1.0
  * @since OpenMarkov 1.0 */
// TODO Borrar esta clase
public abstract class FSPotential extends Potential {
	
	// Constructor
	/** @param variables <code>ArrayList</code> of something that extends 
     * <code>Variable></code>
     * @param role. <code>PotentialRole</code> */
	public FSPotential(ArrayList<Variable> variables, PotentialRole role) {
		super((ArrayList<Variable>)variables, role);
	}
	
	// Methods
    /** @param evidenceCase <code>EvidenceCase</code>
     * @return An array of <code>FSPotential</code>s whose product is 
     * the projection of this potential on the variables not contained in 
     * the <code>evidenceCase</code> 
     * @throws NotEnoughMemoryException 
     * @throws WrongCriterionException 
     * @throws NoFindingException */
	public abstract ArrayList<TablePotential> tableProject(
	EvidenceCase evidenceCase, InferenceOptions inferenceOptions) 
	throws NotEnoughMemoryException, WrongCriterionException;

	/** This method is <code>static</code> because sometimes it can be used
	 *    without creating the <code>TablePotential</code>; for instance, to 
	 *    estimate the amount of memory that would be necessary to actually
	 *    create the PotentialTable.
	 * @param fsVariables <code>ArrayList</code> of <code>Variable</code>s.
	 * @return array of <code>int[]</code> with the dimension of each variable.
	 */
	public static int[] calculateDimensions(ArrayList<Variable> fsVariables) {
		int numVariables = 0;
		if (fsVariables != null) {
			numVariables = fsVariables.size();
		}
		int[] dimensions = new int[numVariables];
		for (int i = 0; i < numVariables; i++) {
			dimensions[i] = fsVariables.get(i).getNumStates();
		}
		return dimensions;
	}

}