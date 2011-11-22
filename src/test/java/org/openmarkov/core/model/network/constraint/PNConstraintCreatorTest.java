
package org.openmarkov.core.model.network.constraint;

import org.junit.Assert;
import org.junit.Test;
import org.openmarkov.core.exception.ConstraintException;

public class PNConstraintCreatorTest {

	@Test
	public void testCreatePNConstraint()
	{
		boolean expceptionLaunched=false;
		String pnClassName= "NoUtilityParent";
		try {
		    ConstraintPool constraintFactory = new ConstraintPool();
			PNConstraint constraint=constraintFactory.getConstraint(NoUtilityParent.class);
			Assert.assertNotNull(constraint);
			Assert.assertTrue(constraint.getClass().getName().endsWith(pnClassName));
		
		} catch (ConstraintException e) {
			expceptionLaunched=true;
		}
		Assert.assertFalse(expceptionLaunched);
	}
	
	
}
