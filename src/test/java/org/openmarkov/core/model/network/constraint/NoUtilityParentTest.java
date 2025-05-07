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

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoUtilityParentTest {

	private ProbNet probNetImproperUtilityChildren;
	private ProbNet probNetProperUtilityChildren;
	private ProbNet influenceDiagram;

	@BeforeEach public void setUp() throws Exception {
		probNetImproperUtilityChildren = ConstraintsTests.getNotOnlyUtilityChildrenInfluenceDiagram();
		probNetProperUtilityChildren = ConstraintsTests.getOnlyUtilityChildrenInfluenceDiagram();
		influenceDiagram = ConstraintsTests.getInfuenceDiagram();
	}

	@Test public void testOnlyUtilityChilren() {
		assertFalse(probNetImproperUtilityChildren.checkProbNet());

		//test only utility children
		PNConstraint constraint = new NoUtilityParent();
		assertTrue(constraint.checkProbNet(probNetProperUtilityChildren));
	}

	@Test public void testUndoableEditWillHappen() throws Exception {
		// Add constraints as listeners.
		PNESupport pNESupport = influenceDiagram.getPNESupport();
		influenceDiagram.addConstraint(new NoUtilityParent());
		// Create edits
		Variable vu = influenceDiagram.getVariable("U");
		Variable vc1 = new Variable("C1", 0);
		Variable vc2 = new Variable("C2", 0);

		// test no exception in legal edit
		AddNodeEdit legalAddC1 = new AddNodeEdit(influenceDiagram, vc1, NodeType.UTILITY);

		//add the node C1
		try {
			influenceDiagram.doEdit(legalAddC1);
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		//link U->C1
		AddLinkEdit legalLink = new AddLinkEdit(influenceDiagram, vu, vc1, true);
		try {
			pNESupport.announceEdit(legalLink);
			legalLink.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		// test exception in no legal edit
		AddNodeEdit legalAddC2 = new AddNodeEdit(influenceDiagram, vc2, NodeType.DECISION);

		//add the node C2
		try {
			pNESupport.announceEdit(legalAddC2);
			legalAddC2.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		boolean exceptionLaunched = false;
		AddLinkEdit ilegalLink = new AddLinkEdit(influenceDiagram, vu, vc2, true);
		try {
			pNESupport.announceEdit(ilegalLink);
		} catch (Exception cve) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched); // pNESupport must launch an exception.
	}

}
