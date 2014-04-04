/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.constraint;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;


public class NoCyclesTest {

	private ProbNet probNetDirected;
	
	@Before
	public void setUp() throws Exception {
		probNetDirected = ConstraintsTests.getTestProbNetDirected();
	}

	/** Checks or not all the <code>probNet</code> in different situations in
	 * <code>OnlyDirectedLinks</code> constructor. 
	 * @throws NodeNotFoundException */
	@Test
	public void testCheckProbNet() throws NodeNotFoundException {
		try {
            probNetDirected.addConstraint (new NoCycle (), true);
		} catch (ConstraintViolationException e1) {
		}
        probNetDirected.removeConstraint (new NoCycle ());
		Variable va = 
			probNetDirected.getNode("A", NodeType.CHANCE).getVariable();
		Variable vc = 
			probNetDirected.getNode("C", NodeType.CHANCE).getVariable();
		
		boolean constraintExcepctionLaunched = false;
		try {
			// creates a cycle
			new AddLinkEdit(probNetDirected, vc, va, true).doEdit(); 
            probNetDirected.addConstraint (new NoCycle (), true);
		} catch (ConstraintViolationException e) {
			constraintExcepctionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(constraintExcepctionLaunched);
	}
	
}
