/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.*;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoCyclesTest {

	private ProbNet probNetDirected;

	@BeforeEach public void setUp() throws Exception {
		probNetDirected = ConstraintsTests.getTestProbNetDirected();
	}

	/**
	 * Checks or not all the <code>probNet</code> in different situations in
	 * <code>OnlyDirectedLinks</code> constructor.
	 *
	 * @throws NodeNotFoundException
	 */
	@Test public void testCheckProbNet() throws NodeNotFoundException, DoEditException {
		NoCycle testedConstraint = new NoCycle();
		probNetDirected.addConstraint(testedConstraint);
		Variable va = probNetDirected.getNode("A", NodeType.CHANCE).getVariable();
		Variable vc = probNetDirected.getNode("C", NodeType.CHANCE).getVariable();
		
		assertTrue(testedConstraint.checkProbNet(probNetDirected));
		new AddLinkEdit(probNetDirected, vc, va, true).doEdit();
		assertFalse(testedConstraint.checkProbNet(probNetDirected));
	}

}
