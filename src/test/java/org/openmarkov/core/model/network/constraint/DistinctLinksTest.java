package org.openmarkov.core.model.network.constraint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class DistinctLinksTest {

	private ProbNet influenceDiagram;

	@Before
	public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getInfuenceDiagram();
	}

	@Test
	public void testCheckProbNet() {
		boolean exceptionLaunched = false;
		Variable vu = null;
		Variable va = null;
		try {
			vu = influenceDiagram.getVariable("U");
			va = influenceDiagram.getVariable("A");
			influenceDiagram.removeConstraint(new DistinctLinks());
			influenceDiagram.addConstraint(new DistinctLinks(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		// add undirected link between A and U
		exceptionLaunched = false;
		try {
			influenceDiagram.removeConstraint(new DistinctLinks());
			influenceDiagram.addLink(va, vu, false);
			influenceDiagram.addConstraint(new DistinctLinks(), true);
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertFalse(exceptionLaunched);
		
		
		
		
		
		// add directed link between U and D
		exceptionLaunched = false;
		try {
			Variable vd = influenceDiagram.getVariable("D");
			influenceDiagram.removeConstraint(new DistinctLinks());
			influenceDiagram.addLink(vu, vd, true);
			influenceDiagram.addConstraint(new DistinctLinks(), true);
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertFalse(exceptionLaunched);

			// add directed link between A and U
		exceptionLaunched = false;
		try {
			influenceDiagram.removeConstraint(new DistinctLinks());

			influenceDiagram.addLink(va, vu, true);
			influenceDiagram.addConstraint(new DistinctLinks(), true);
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
			influenceDiagram.removeLink(va, vu, true);
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(exceptionLaunched);

		
		// add undirected link between A and U
		exceptionLaunched = false;
		try {
			influenceDiagram.removeConstraint(new DistinctLinks());
			influenceDiagram.addLink(va, vu, false);
			influenceDiagram.addConstraint(new DistinctLinks(), true);
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail("AddLink failed");
		}
		assertTrue(exceptionLaunched);

	}
	
	@Test
	public void testUndoableEditWillHappen() throws Exception {
		PNESupport pNESupport = new PNESupport(false);
		PNConstraint constraint = new DistinctLinks();

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
		InvertLinkEdit ilegalInvertLinkEdit = new InvertLinkEdit(
				influenceDiagram, vU, vD, true);
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
		InvertLinkEdit legalInvertLinkEdit = new InvertLinkEdit(
				influenceDiagram, vU, vD, false);
		try {
			pNESupport.announceEdit(legalInvertLinkEdit);
			legalInvertLinkEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		exceptionLaunched = false;
		// do ilegal LinkEdit. Add an undirected link between U and D
		AddLinkEdit ilegalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("U"), influenceDiagram.getVariable("D"),
				false);
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
