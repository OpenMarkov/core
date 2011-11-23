package org.openmarkov.core.model.network.potential;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;


import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;


import org.junit.Test;

/** Test of <code>Potential</code> class. As this class is abstract we use the
 * class <code>TablePotential</code>.  */
public class PotentialTest {

	/** Creates a potential */
	@Test public void testCreation() {
		TablePotential potential = null;
		// check if it is possible to create a potential without variables
		try {
			potential = new TablePotential(
					null, PotentialRole.CONDITIONAL_PROBABILITY);
		} catch (NotEnoughMemoryException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			fail("Potential creation failed");
		}
		assertNotNull(potential);
		// test number of variables of created potential
		ArrayList<Variable> variables =	potential.getVariables();
		assertEquals(0, variables.size());
		
		// check that potential creation with an array of zero variables
		// produces the same result as above.
		try {
			potential = new TablePotential(
					variables, PotentialRole.CONDITIONAL_PROBABILITY);
		} catch (NotEnoughMemoryException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			fail("Potential creation failed");
		}
		assertNotNull(potential);
		// test number of variables of created potential
		variables = potential.getVariables();
		assertEquals(0, variables.size());		
	}
}
