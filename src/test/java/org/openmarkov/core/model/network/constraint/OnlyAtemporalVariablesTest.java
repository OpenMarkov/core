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
public class OnlyAtemporalVariablesTest {

	private ProbNet influenceDiagram;

	@BeforeEach public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getInfuenceDiagram();
	}

	@Test public void testCheckProbNet() {

		boolean exceptionLaunched = false;
		try {

			influenceDiagram.removeConstraint(new OnlyAtemporalVariables());
			influenceDiagram.addConstraint(new OnlyAtemporalVariables(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		try {
			influenceDiagram.removeConstraint(new OnlyAtemporalVariables());
			Variable var = new Variable(" [10]", "YES", "NO");
			influenceDiagram.addNode(var, NodeType.CHANCE);
			influenceDiagram.addConstraint(new OnlyAtemporalVariables(), true);
		} catch (ConstraintViolationException e1) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);
	}

	@Test public void testUndoableEditWillHappen() throws Exception {
		PNESupport pNESupport = new PNESupport(false);
		PNConstraint constraint = new OnlyAtemporalVariables();

		influenceDiagram.addConstraint(constraint, true);
		pNESupport.addUndoableEditListener(constraint);

		boolean exceptionLaunched = false;
		Variable var = new Variable("F");
		AddNodeEdit legalEdit = new AddNodeEdit(influenceDiagram, var, NodeType.CHANCE);
		// add the variable
		try {
			pNESupport.announceEdit(legalEdit);
			legalEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		Variable var1 = new Variable(" [11]", "Y", "N");
		AddNodeEdit ilegalEdit = new AddNodeEdit(influenceDiagram, var1, NodeType.CHANCE);
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
