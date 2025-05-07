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
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DistinctLinksTest {

	private ProbNet influenceDiagram;

	@BeforeEach public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getInfuenceDiagram();
	}

	@Test public void testCheckProbNet() throws NodeNotFoundException {
		DistinctLinks testedConstraint = new DistinctLinks();
		influenceDiagram.addConstraint(testedConstraint);
		Variable vu = influenceDiagram.getVariable("U");
        Variable va = influenceDiagram.getVariable("A");
		assertTrue(testedConstraint.checkProbNet(influenceDiagram));
		
		influenceDiagram.addLink(va, vu, false);
		assertTrue(testedConstraint.checkProbNet(influenceDiagram));
		
		Variable vd = influenceDiagram.getVariable("D");
		influenceDiagram.addLink(vu, vd, true);
		influenceDiagram.addLink(vu, vd, true);
		assertFalse(testedConstraint.checkProbNet(influenceDiagram));
		
		influenceDiagram.removeLink(vu, vd, true);
		influenceDiagram.removeLink(vu, vd, true);
		influenceDiagram.addLink(vu, vd, true);
		assertTrue(testedConstraint.checkProbNet(influenceDiagram));
		
		influenceDiagram.addLink(va, vu, true);
		assertFalse(testedConstraint.checkProbNet(influenceDiagram));
		
		influenceDiagram.removeLink(va, vu, true);
		influenceDiagram.removeLink(va, vu, true);
		influenceDiagram.removeLink(va, vu, false);
		influenceDiagram.addLink(va, vu, false);
		assertTrue(testedConstraint.checkProbNet(influenceDiagram));
	}
	@Disabled
	@Test public void testUndoableEditWillHappen() throws Exception {
		PNESupport pNESupport = new PNESupport(false);
		PNConstraint constraint = new DistinctLinks();

		influenceDiagram.addConstraint(constraint);
		pNESupport.addUndoableEditListener(constraint);

		// do legal AddLink: add an directed link between U and A
		Variable vU = influenceDiagram.getVariable("U");
		Variable vA = influenceDiagram.getVariable("A");
		// creates an undirected link from node A to D
		AddLinkEdit legalEdit = new AddLinkEdit(influenceDiagram, vU, vA, true);
		try {
			pNESupport.announceEdit(legalEdit);
			legalEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		boolean exceptionLaunched = false;
		// do ilegal LinkAdd. Add an directed link between A and U
		AddLinkEdit ilegalAdd = new AddLinkEdit(influenceDiagram, vA, vU, true);
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
		Variable vD = influenceDiagram.getVariable("D");
		// do ilegal InvertLink. Add an directed link between D and U
		InvertLinkEdit ilegalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vD, true);
		try {
			pNESupport.announceEdit(ilegalInvertLinkEdit);
			ilegalInvertLinkEdit.doEdit();
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(exceptionLaunched);

		// do legal invert link: create undirected link between U and D
		InvertLinkEdit legalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vD, false);
		try {
			pNESupport.announceEdit(legalInvertLinkEdit);
			legalInvertLinkEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		exceptionLaunched = false;
		// do ilegal LinkEdit. Add an undirected link between U and D
		AddLinkEdit ilegalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("U"),
				influenceDiagram.getVariable("D"), false);
		try {
			pNESupport.announceEdit(ilegalLinkEdit);
			ilegalLinkEdit.doEdit();
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(exceptionLaunched);

	}

}
