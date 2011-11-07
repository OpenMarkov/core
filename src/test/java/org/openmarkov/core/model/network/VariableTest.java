package org.openmarkov.core.model.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.InvalidStateException;


public class VariableTest {
	
	private Variable variable;
	
	private final int numStates = 3;
	
	private final String x = "X";
	
	private final String[] statesNames = {"0", "1", "2"};
	
	@Before
	public void setUp() throws Exception {
		variable = new Variable(x, numStates);
	}

	// Test constructors
	@Test public void testDiscreteConstructor2() {
    	Variable variable = new Variable(x, numStates);
    	State[] states = variable.getStates();
    	for (int i = 0; i < numStates; i++) {
    		assertTrue(states[i].getName().contentEquals(new String("" + i)));
    	}
	}
	
	@Test public void testFSVariableStringInt() {
    	Variable variable = new Variable(x, numStates);
		assertTrue(variable.getName().contains(x));
		assertEquals(numStates, variable.getNumStates());
		State[] states = variable.getStates();
		assertEquals(numStates, states.length);
		for (int i = 0; i < numStates; i++) {
			assertTrue(states[i].getName().contentEquals(statesNames[i]));
		}
	}

	@Test public void testRenameState() {
		// Rename not existing state (it does nothing)
    	Variable variable = new Variable(x, numStates);
    	boolean exceptionLaunched = false;
		try {
			variable.renameState("NoExists", "Yahoo");
		} catch (Exception e) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);
		State[] states = variable.getStates();
		assertEquals(numStates, states.length);
		for (int i = 0; i < numStates; i++) {
			assertTrue(states[i].getName().contentEquals(statesNames[i]));
		}
		// Rename one state
		String newName = "Yahoo";
		String oldName = states[numStates - 1].getName();
		try {
			variable.renameState(oldName, newName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		states = variable.getStates();
		assertEquals(numStates, states.length);
		for (int i = 0; i < numStates - 1; i++) {
			assertTrue(states[i].getName().contentEquals(statesNames[i]));
		}
		assertTrue(states[numStates - 1].getName().contentEquals(newName));
	}

	@Test public void testGetStateIndex() throws InvalidStateException {
		for (int i = 0; i < numStates; i++) {
			assertEquals(i, variable.getStateIndex(statesNames[i]));
		}
	}

}
