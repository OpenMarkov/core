/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableTest;

/** Test of <code>Potential</code> class. As this class is abstract we use the
 * class <code>TablePotential</code>.  */
public class PotentialTest {

	/** Creates a potential */
	@Test
	public void testCreation() {
		TablePotential potential = new TablePotential(null, PotentialRole.CONDITIONAL_PROBABILITY);
		assertNotNull(potential);
		// test number of variables of created potential
		List<Variable> variables = potential.getVariables();
		assertEquals(0, variables.size());

		// check that potential creation with an array of zero variables
		// produces the same result as above.
		potential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
		assertNotNull(potential);
		// test number of variables of created potential
		variables = potential.getVariables();
		assertEquals(0, variables.size());
	}

	// TODO Sobrecargar equals y poner en el comentario que 
	// equals ya NO consiste en comparar la dirección de memoria de dos objetos
	// TODO Cada tipo de potential tiene que tener un método equals y llamar al del padre
	/** Compares potential1 and potential2.
	 * @param potential1. <code>Potential</code>
	 * @param potential2. <code>Potential</code>
	 * @return <code>true</code> if both potentials are equal. */
	public static boolean equalPotentials(Potential potential1, Potential potential2) {
		boolean equals = true;
		if (potential1.getPotentialRole() == potential2.getPotentialRole() &&
				potential1.getClass() == potential2.getClass() &&
				potential1.getComment().contentEquals(potential2.getComment()) &&
				potential1.getNumVariables() == potential2.getNumVariables() &&
				potential1.isUtility() == potential2.isUtility()) {
		    List<Variable> variables1 = potential1.getVariables();
		    List<Variable> variables2 = potential2.getVariables();
			int numVariables = variables1.size();
			int i = 0;
			while (i < numVariables && equals) {
				if (!VariableTest.equalVariables(variables1.get(i), variables2.get(i))) {
					equals = false;
				}
				i++;
			}
			// TODO Mover a TablePotential
			/*if (potential1 instanceof TablePotential && potential2 instanceof TablePotential) {
				double[] values1 = ((TablePotential)potential1).values;
				double[] values2 = ((TablePotential)potential2).values;
				equals = values1.length == values2.length;
				i = 0; 
				while (equals && i < values1.length) {
					equals = values1[i] == values2[i];
					i++;
				}
			}*/
			TablePotentialTest.checkEqualPotentials((TablePotential)potential1, (TablePotential)potential2,0.0001);
			equals = true;
		}
		return equals;
	}
}
