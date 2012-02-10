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
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class NoLoopsTest {

	private ProbNet directedNet;
	private ProbNet undirectedNet;

	@Before
	public void setUp() throws Exception {
		directedNet = ConstraintsTests.getTestProbNetDirected();
		undirectedNet = ConstraintsTests.getTestProbNetUndirected();
	}

	@Test
	public void testCheckProbNet() throws ProbNodeNotFoundException {

		boolean exceptionLaunched = false;
		try {
			directedNet.removeConstraint(new NoLoops(directedNet));
			directedNet.addConstraint(new NoLoops(directedNet), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		try {
			directedNet.removeConstraint(new NoLoops(directedNet));
			Variable varA = directedNet.getVariable("A");
			Variable varC = directedNet.getVariable("C");
			directedNet.addLink(varA, varC, true);
			directedNet.addConstraint(new NoLoops(directedNet), true);
		} catch (ConstraintViolationException e1) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertTrue(exceptionLaunched);
		 
		
		//Test undirected net
		exceptionLaunched = false;
		try {
			undirectedNet.removeConstraint(new NoLoops(undirectedNet));
			undirectedNet.addConstraint(new NoLoops(undirectedNet), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);



		Variable varA = undirectedNet.getVariable("A");
		Variable varB = undirectedNet.getVariable("B");
		Variable varC = undirectedNet.getVariable("C");
		try {
			undirectedNet.removeConstraint(new NoLoops(undirectedNet));
			undirectedNet.addLink(varA, varC, false);
			undirectedNet.addConstraint(new NoLoops(undirectedNet), true);
		} catch (ConstraintViolationException e1) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		try {
			undirectedNet.removeConstraint(new NoLoops(undirectedNet));
			undirectedNet.removeLink(varA, varC, false);
			undirectedNet.removeLink(varB, varC, false);
			undirectedNet.addLink(varB, varC, true);
			undirectedNet.addLink(varA, varC, true);
			undirectedNet.addConstraint(new NoLoops(undirectedNet), true);
		} catch (ConstraintViolationException e1) {
			exceptionLaunched = true;
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertTrue(exceptionLaunched);


		try{
			undirectedNet.removeConstraint(new NoLoops(undirectedNet));
			undirectedNet.removeLink(varA, varB, false);
			undirectedNet.addLink(varA, varB, true);
			undirectedNet.addConstraint(new NoLoops(undirectedNet), true);
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
		PNConstraint constraint = new NoClosedPath(undirectedNet);

		undirectedNet.addConstraint(constraint, true);
		pNESupport.addUndoableEditListener(constraint);

		// do legal add
		Variable vC = undirectedNet.getVariable("C");
		try {

			new AddProbNodeEdit(undirectedNet, "D", NodeType.UTILITY,
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

