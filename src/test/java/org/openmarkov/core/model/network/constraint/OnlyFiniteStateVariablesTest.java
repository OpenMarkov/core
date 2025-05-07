/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.*;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.action.VariableTypeEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyFiniteStateVariablesTest {

	// Attributes
	private ProbNet influenceDiagram;
	private ProbNet mixedVariableInfluenceDiagram;

	// Methods
	@BeforeEach public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getInfuenceDiagram();
		mixedVariableInfluenceDiagram = getMixedVariableInfluenceDiagram();
	}

	private ProbNet getMixedVariableInfluenceDiagram() {
		ProbNet net = ConstraintsTests.getInfuenceDiagram();
		Variable vC = new Variable("C");

		try {
			Variable vU = net.getVariable("U");
			net.addNode(vC, NodeType.CHANCE);
			net.addLink(vU, vC, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return net;
	}

	@Test public void testCheckProbNet() {
		OnlyFiniteStatesVariables testedConstraint = new OnlyFiniteStatesVariables();
		influenceDiagram.addConstraint(testedConstraint);
		mixedVariableInfluenceDiagram.addConstraint(testedConstraint);
		
		assertTrue(testedConstraint.checkProbNet(influenceDiagram));
		assertFalse(testedConstraint.checkProbNet(mixedVariableInfluenceDiagram));
	}

	@Test public void testUndoableEditWillHappen() throws Exception {

		PNESupport pNESupport = new PNESupport(false);
		PNConstraint constraint = new OnlyFiniteStatesVariables();
		influenceDiagram.addConstraint(constraint);

		pNESupport.addUndoableEditListener(constraint);

		Variable vc1 = new Variable("D1", 0);

		// test no exception in legal edit
		AddNodeEdit legalAdd = new AddNodeEdit(influenceDiagram, vc1, NodeType.DECISION);
		//add the node D1 (decision + finite state)
		try {
			pNESupport.announceEdit(legalAdd);
			legalAdd.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		Variable vc2 = new Variable("C1", 0);
		// test no exception in legal edit
		legalAdd = new AddNodeEdit(influenceDiagram, vc2, NodeType.CHANCE);
		//add the node C1 (chance + finite state)
		try {
			pNESupport.announceEdit(legalAdd);
			legalAdd.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		Variable vc3 = new Variable("U1");
		// test no exception in legal edit (utility + numeric)
		legalAdd = new AddNodeEdit(influenceDiagram, vc3, NodeType.UTILITY);
		//add the node U1
		try {
			pNESupport.announceEdit(legalAdd);
			legalAdd.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		Variable vc4 = new Variable("D2");
		// test exception in illegal edit (decision + numeric)
		AddNodeEdit illegalAdd = new AddNodeEdit(influenceDiagram, vc4, NodeType.DECISION);
		//add the node D2
		boolean exceptionLaunched = false;
		try {
			pNESupport.announceEdit(illegalAdd);

		} catch (Exception cve) {
			exceptionLaunched = true;
		}

		assertTrue(exceptionLaunched);

		Variable vc5 = new Variable("C2");
		// test  exception in illegal edit (chance + numeric)
		illegalAdd = new AddNodeEdit(influenceDiagram, vc5, NodeType.CHANCE);
		//add the node D2
		exceptionLaunched = false;
		try {
			pNESupport.announceEdit(illegalAdd);

		} catch (Exception cve) {
			exceptionLaunched = true;
		}

		assertTrue(exceptionLaunched);

		VariableTypeEdit legalEdit = new VariableTypeEdit(influenceDiagram.getNode("D1"), VariableType.DISCRETIZED);
		//edit variable type of D1
		// test  exception in legal edit (decision + discrete)
		try {
			pNESupport.announceEdit(legalEdit);
			legalEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		legalEdit = new VariableTypeEdit(influenceDiagram.getNode("C1"), VariableType.DISCRETIZED);
		//edit variable type of C1
		// test  exception in legal edit (chance + discrete)
		try {
			pNESupport.announceEdit(legalEdit);
			legalEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		VariableTypeEdit illegalEdit = new VariableTypeEdit(influenceDiagram.getNode("C1"), VariableType.NUMERIC);
		//edit variable type of C1
		// test  exception in illegal edit (chance + numeric)
		exceptionLaunched = false;
		try {
			pNESupport.announceEdit(illegalEdit);

		} catch (Exception cve) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);
	}

}
