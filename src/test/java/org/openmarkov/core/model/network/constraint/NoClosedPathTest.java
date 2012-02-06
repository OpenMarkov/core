package org.openmarkov.core.model.network.constraint;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.model.network.ProbNet;

public class NoClosedPathTest {
	
	private ProbNet net;

	@Before
	public void setUp() throws Exception {
		net = ConstraintsTests.getTestProbNetDirected();
	}
	
	
	@Test
	public void testCheckProbNet() {

		boolean exceptionLaunched = false;
		try {
			net.removeConstraint(new NoClosedPath());
			net.addConstraint(new NoClosedPath(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);
	}
}
