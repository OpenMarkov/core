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
}
