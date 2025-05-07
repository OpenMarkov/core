/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.*;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.geom.Point2D;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoMixedParentsTest {

	private ProbNet influenceDiagram;

	@BeforeEach public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getOnlyUtilityChildrenInfluenceDiagram();
	}

	@Test public void testCheckProbNet() throws NodeNotFoundException {
		NoMixedParents testedConstraint = new NoMixedParents();
		influenceDiagram.addConstraint(testedConstraint);
		assertTrue(testedConstraint.checkProbNet(influenceDiagram));
		
		Variable vc = influenceDiagram.getVariable("C");
		Variable ve = new Variable("E", 2);
		influenceDiagram.addNode(ve, NodeType.DECISION);
		influenceDiagram.addLink(ve, vc, true);
		assertFalse(testedConstraint.checkProbNet(influenceDiagram));
	}
	@Disabled
	@Test public void testUndoableEditWillHappen() throws Exception {
		// Add constraints as listeners.
		PNESupport pNESupport = new PNESupport(false);
		PNConstraint constraint = new NoMixedParents();
		influenceDiagram.addConstraint(new NoMixedParents());
		pNESupport.addUndoableEditListener(constraint);

		//do legal AddLink: add link from utility node E to utility node C
		new AddNodeEdit(influenceDiagram, new Variable("E"), NodeType.UTILITY).doEdit();
		Variable vE = influenceDiagram.getNode("E", NodeType.UTILITY).getVariable();
		Variable vC = influenceDiagram.getVariable("C");
		// creates a link from utility node E to utility node C
		AddLinkEdit legalEdit = new AddLinkEdit(influenceDiagram, vE, vC, true);
		try {
			pNESupport.announceEdit(legalEdit);
			legalEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		//do legal LinkEdit:  add directed link from chance node F to utility node U
		new AddNodeEdit(influenceDiagram, new Variable("F"), NodeType.CHANCE, new Point2D.Double()).doEdit();

		AddLinkEdit legalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("F"),
				influenceDiagram.getVariable("U"), true);
		try {
			pNESupport.announceEdit(legalLinkEdit);
			legalLinkEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		//do legal invertLink: invert link from E to U

		InvertLinkEdit legalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vC, vE, true);
		try {
			pNESupport.announceEdit(legalInvertLinkEdit);
			legalInvertLinkEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		//do ilegal invertLink: invert link from U to C

		Variable vU = influenceDiagram.getVariable("U");
		InvertLinkEdit ilegalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vC, true);
		boolean exceptionLaunched = false;
		try {
			pNESupport.announceEdit(ilegalInvertLinkEdit);
			ilegalInvertLinkEdit.doEdit();
		} catch (Exception cve) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);

		//do ilegal AddLink: add link from utility node G to utility node U

		new AddNodeEdit(influenceDiagram, new Variable("G"), NodeType.UTILITY, new Point2D.Double()).doEdit();
		Variable vG = influenceDiagram.getNode("G", NodeType.UTILITY).getVariable();

		// creates a link from utility node G to utility node U
		AddLinkEdit ilegalEdit = new AddLinkEdit(influenceDiagram, vG, vU, true);
		exceptionLaunched = false;
		try {
			pNESupport.announceEdit(ilegalEdit);
			ilegalEdit.doEdit();
		} catch (Exception cve) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);

		// do ilegal LinkEdit: add link from utility node G to utility node U
		AddLinkEdit ilegalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("G"),
				influenceDiagram.getVariable("U"), true);
		exceptionLaunched = false;
		try {
			pNESupport.announceEdit(ilegalLinkEdit);
			ilegalLinkEdit.doEdit();
		} catch (Exception cve) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);

	}

}
