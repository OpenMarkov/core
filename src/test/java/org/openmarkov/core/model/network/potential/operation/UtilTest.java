/**
 * 
 */
package org.openmarkov.core.model.network.potential.operation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author carla
 *
 */
public class UtilTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link openmarkov.networks.potentials.operations.Util#round(double, java.lang.String)}.
	 */
	@Test
	public void testRound() {
		double value = 1452.309;
		
		String precisionString = "100";
		assertEquals(1500.0, Util.round(value, precisionString));
		precisionString = "10";
		assertEquals(1450.0, Util.round(value, precisionString));
		precisionString = "1";
		assertEquals(1452.0, Util.round(value, precisionString));
		precisionString = "0.1";
		assertEquals(1452.3, Util.round(value, precisionString));
		precisionString = "0.01";
		assertEquals(1452.31, Util.round(value, precisionString));
		precisionString = "0.001";
		assertEquals(1452.301, Util.round(value, precisionString));
		
		precisionString = ".01";
		assertEquals(1452.31, Util.round(value, precisionString));
		
		value = -0.99;
		precisionString = "1";
		assertEquals(-1, Util.round(value, precisionString));
		precisionString = "0.1";
		assertEquals(-1, Util.round(value, precisionString));
		precisionString = "0.01";
		assertEquals(-0.99, Util.round(value, precisionString));
	
	}

	/**
	 * Test method for {@link openmarkov.networks.potentials.operations.Util#roundedString(double, java.lang.String)}.
	 */
	@Test
	public void testRoundedString() {
		double value = 1452.302;
		
		String precisionString = "100";
		assertEquals("1500", Util.roundedString(value, precisionString));
		precisionString = "10";
		assertEquals("1450", Util.roundedString(value, precisionString));
		precisionString = "1";
		assertEquals("1452", Util.roundedString(value, precisionString));
		precisionString = "0.1";
		assertEquals("1452,3", Util.roundedString(value, precisionString));
		precisionString = "0.01";
		assertEquals("1452,30", Util.roundedString(value, precisionString));
		precisionString = "0.001";
		assertEquals("1452,302", Util.roundedString(value, precisionString));
		
		precisionString = ".01";
		//assertEquals(1452.31, Util.roundedString(value, precisionString));
		
		value = -0.99;
		precisionString = "1";
		assertEquals("-1", Util.roundedString(value, precisionString));
		precisionString = "0.1";
		assertEquals("-1,0", Util.roundedString(value, precisionString));
		precisionString = "0.01";
		assertEquals("-0,99", Util.roundedString(value, precisionString));
		
		value = 3.0;
		precisionString = "0.001";
		assertEquals("3,000", Util.roundedString(value, precisionString));
	}

}
