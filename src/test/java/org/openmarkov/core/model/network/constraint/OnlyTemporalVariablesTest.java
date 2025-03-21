/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.*;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyTemporalVariablesTest {

	private ProbNet network;

	@BeforeEach public void setUp() throws Exception {
		network = ConstraintsTests.getTemporalVarNet();
	}

	@Test public void testCheckProbNet() {

		boolean exceptionLaunched = false;
		try {
			network.removeConstraint(new OnlyTemporalVariables());
			network.addConstraint(new OnlyTemporalVariables(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		try {
			network.removeConstraint(new OnlyTemporalVariables());
			Variable var = new Variable("A");
			network.addNode(var, NodeType.CHANCE);
			network.addConstraint(new OnlyTemporalVariables(), true);
		} catch (ConstraintViolationException e1) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);
	}

	@Test public void testUndoableEditWillHappen() throws Exception {
		PNESupport pNESupport = new PNESupport(false);
		PNConstraint constraint = new OnlyTemporalVariables();
		network.addConstraint(constraint, true);
		pNESupport.addUndoableEditListener(constraint);

		boolean exceptionLaunched = false;
		Variable var = new Variable(" [11]", "Y", "N");
		AddNodeEdit legalEdit = new AddNodeEdit(network, var, NodeType.CHANCE);
		// add the variable
		try {
			pNESupport.announceEdit(legalEdit);
			legalEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		Variable var1 = new Variable("F");
		AddNodeEdit ilegalEdit = new AddNodeEdit(network, var1, NodeType.CHANCE);
		// add the variable
		try {
			pNESupport.announceEdit(ilegalEdit);
			ilegalEdit.doEdit();
		} catch (Exception cve) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);
	}

}
