package org.openmarkov.core.model.network;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.model.network.TemporalUnit.DiscountUnit;
import org.openmarkov.core.model.network.TemporalUnit.Unit;

public class TemporalUnitTest {

	
	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testMonthToYear() {
		double adjustedDiscount = TemporalUnit.getTemporalAdjustedDiscount(Unit.MONTH, 3, DiscountUnit.YEAR, 0.1);
		assertTrue(true);
		
		
		
	}

}
