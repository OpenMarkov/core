package org.openmarkov.core.model.network.constraint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Point2D;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.AddProbNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.ConstraintException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class NoClosedPathTest {

	private ProbNet directedNet;
	private ProbNet undirectedNet;

	@Before
	public void setUp() throws Exception {
		directedNet = ConstraintsTests.getTestProbNetDirected();
		undirectedNet = ConstraintsTests.getTestProbNetUndirected();
	}

	@Test
	public void testCheckProbNet() {

		boolean exceptionLaunched = false;
		try {
			directedNet.removeConstraint(new NoClosedPath());
			directedNet.addConstraint(new NoClosedPath(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		try {
			directedNet.removeConstraint(new NoClosedPath());
			Variable varA = directedNet.getVariable("A");
			Variable varC = directedNet.getVariable("C");
			directedNet.addLink(varA, varC, true);
			directedNet.addConstraint(new NoClosedPath(), true);
		} catch (ConstraintViolationException e1) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(exceptionLaunched);

		exceptionLaunched = false;
		try {
			undirectedNet.removeConstraint(new NoClosedPath());
			undirectedNet.addConstraint(new NoClosedPath(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		try {
			undirectedNet.removeConstraint(new NoClosedPath());
			Variable varA = undirectedNet.getVariable("A");
			Variable varC = undirectedNet.getVariable("C");
			undirectedNet.addLink(varA, varC, false);
			undirectedNet.addConstraint(new NoClosedPath(), true);
		} catch (ConstraintViolationException e1) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertTrue(exceptionLaunched);

	}

	@Test
	public void testUndoableEditWillHappen() throws Exception {

		PNESupport pNESupport = new PNESupport(false);
		PNConstraint constraint = new NoClosedPath();

		undirectedNet.addConstraint(constraint, true);
		pNESupport.addUndoableEditListener(constraint);

		// do legal add
		Variable vC = undirectedNet.getVariable("C");
		try {

			new AddProbNodeEdit(undirectedNet, new Variable("D"), NodeType.UTILITY,
					new Point2D.Double()).doEdit();
			Variable vD = undirectedNet.getVariable("D");

			// creates a link from C - D
			AddLinkEdit legalEdit = new AddLinkEdit(undirectedNet, vC, vD,
					false);
			pNESupport.announceEdit(legalEdit);
			legalEdit.doEdit();
		} catch (ConstraintViolationException e) {
			fail(e.getMessage());
		}
		
		boolean exceptionLaunched = false;
		Variable vA = undirectedNet.getVariable("C");
		// creates a link from A-C
		AddLinkEdit ilegalEdit = new AddLinkEdit(undirectedNet, vA, vC, false);
		try {
			pNESupport.announceEdit(ilegalEdit);
			ilegalEdit.doEdit();

		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);

		exceptionLaunched = false;
		// creates a link from A->C
		ilegalEdit = new AddLinkEdit(undirectedNet, vA, vC, true);
		try {
			pNESupport.announceEdit(ilegalEdit);
			ilegalEdit.doEdit();
		} catch (ConstraintViolationException e) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);
	}

}
