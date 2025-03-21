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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoMultipleLinksTest {

	private ProbNet influenceDiagram;

	@BeforeEach public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getInfuenceDiagram();
	}

	@Test public void testCheckProbNet() {

		boolean exceptionLaunched = false;
		try {
			influenceDiagram.removeConstraint(new NoMultipleLinks());
			influenceDiagram.addConstraint(new NoMultipleLinks(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		// add directed link between U and A
		exceptionLaunched = false;
		try {
			influenceDiagram.removeConstraint(new NoMultipleLinks());
			Variable vu = influenceDiagram.getVariable("U");
			Variable va = influenceDiagram.getVariable("A");
			influenceDiagram.addLink(vu, va, true);
			influenceDiagram.addConstraint(new NoMultipleLinks(), true);
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertFalse(exceptionLaunched);

		// add undirected link between A and D
		try {
			influenceDiagram.removeConstraint(new NoMultipleLinks());
			Variable vd = influenceDiagram.getVariable("D");
			Variable va = influenceDiagram.getVariable("A");
			influenceDiagram.addLink(vd, va, false);
			influenceDiagram.addConstraint(new NoMultipleLinks(), true);
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(exceptionLaunched);

	}
	@Disabled
	@Test public void testUndoableEditWillHappen() throws Exception {
		PNESupport pNESupport = new PNESupport(false);
		PNConstraint constraint = new NoMultipleLinks();

		influenceDiagram.addConstraint(constraint, true);
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
		// do ilegal LinkAdd. Add an undirected link between U and A
		AddLinkEdit ilegalAdd = new AddLinkEdit(influenceDiagram, vU, vA, false);
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
		// do ilegal LinkEdit. Add an undirected link between U and A
		AddLinkEdit ilegalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("U"),
				influenceDiagram.getVariable("A"), false);
		try {
			pNESupport.announceEdit(ilegalLinkEdit);
			ilegalLinkEdit.doEdit();
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(exceptionLaunched);

		// do legal invert link
		InvertLinkEdit legalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vA, true);
		try {
			pNESupport.announceEdit(legalInvertLinkEdit);
			legalInvertLinkEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		exceptionLaunched = false;
		// do ilegal InvertLink. Add an directed link between U and A
		InvertLinkEdit ilegalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vA, false);
		try {
			pNESupport.announceEdit(ilegalInvertLinkEdit);
			ilegalInvertLinkEdit.doEdit();
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(exceptionLaunched);
	}

}
