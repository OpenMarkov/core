/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.action.VariableTypeEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import static org.junit.Assert.*;

public class OnlyNumericVariablesTest {

	private ProbNet influenceDiagram;

	@Before public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getNumericInfluenceDiagram();
	}

	@Test public void testCheckProbNet() {
		boolean exceptionLaunched = false;
		try {
			influenceDiagram.removeConstraint(new OnlyNumericVariables());
			influenceDiagram.addConstraint(new OnlyNumericVariables(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		try {
			influenceDiagram.removeConstraint(new OnlyNumericVariables());
			Variable varE = new Variable("E", 2);
			influenceDiagram.addNode(varE, NodeType.CHANCE);
			influenceDiagram.addConstraint(new OnlyNumericVariables(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);

	}

	@Test public void testUndoableEditWillHappen() throws Exception {

		PNESupport pNESupport = new PNESupport(false);
		PNConstraint constraint = new OnlyNumericVariables();
		influenceDiagram.addConstraint(constraint, true);
		pNESupport.addUndoableEditListener(constraint);

		boolean exceptionLaunched = false;
		Variable vc1 = new Variable("E");
		// test no exception in legal edit
		AddNodeEdit legalAdd = new AddNodeEdit(influenceDiagram, vc1, NodeType.DECISION);
		// add the node E (decision + numeric)
		try {
			pNESupport.announceEdit(legalAdd);
			legalAdd.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		Variable vc2 = new Variable("F", 3);
		// test exception in ilegal edit
		AddNodeEdit ilegalAdd = new AddNodeEdit(influenceDiagram, vc2, NodeType.DECISION);
		// add the node F (decision + finite state)
		try {
			pNESupport.announceEdit(ilegalAdd);
			ilegalAdd.doEdit();
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(exceptionLaunched);

		exceptionLaunched = false;
		Node node = influenceDiagram.getNode(vc1);
		VariableTypeEdit ilegalEdit = new VariableTypeEdit(node, VariableType.DISCRETIZED);
		// add the node F (decision + finite state)
		try {
			pNESupport.announceEdit(ilegalEdit);
			ilegalEdit.doEdit();
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(exceptionLaunched);

	}

}
