package org.openmarkov.core.model.network.constraint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;


public class OnlyDirectedLinksTest {

	private ProbNet probNetMixed;
	
	private ProbNet probNetUndirected;
	
	private ProbNet probNetDirected;
	
	@Before
	public void setUp() throws Exception {
		probNetMixed = ConstraintsTests.getTestProbNetMixed();
		probNetUndirected = ConstraintsTests.getTestProbNetUndirected();
		probNetDirected = ConstraintsTests.getTestProbNetDirected();
	}

	/** Checks or not all the <code>probNet</code> in different situations in
	 * <code>OnlyDirectedLinks</code> constructor. */
	public void testCheckProbNet() {
		// test only directed links insertions without checking.
		boolean exceptionLaunched = false;
		try {
			probNetMixed.addConstraint(
					OnlyDirectedLinks.getUniqueInstance(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);

		probNetMixed.removeConstraint(
				OnlyDirectedLinks.getUniqueInstance().getClass());
		exceptionLaunched = false;	
		assertFalse(OnlyDirectedLinks.getUniqueInstance()
				.checkProbNet(probNetUndirected));
		
		// test only directed links insertions with checking.
		assertFalse(OnlyDirectedLinks.getUniqueInstance()
				.checkProbNet(probNetMixed));
	}

	/** Checks veto */
	@Test
	public void testUndoableEditWillHappen() 
	        throws Exception {
		
		// Add constraints as listeners.
		PNESupport pNESupport = new PNESupport(probNetDirected, false);
		probNetDirected.addConstraint(
				OnlyDirectedLinks.getUniqueInstance(), true);
		ArrayList<PNConstraint> constraints = probNetDirected.getConstraints();
		for (PNConstraint constraint : constraints) { // sets listeners
			pNESupport.addUndoableEditListener(constraint);
		}
		// Create edits
		Variable va = probNetDirected.getVariable("A");
		Variable vc = probNetDirected.getVariable("C");

		// test no exception in legal edit
		AddLinkEdit cEdit = new AddLinkEdit(probNetDirected, va, vc, true);
		try {
			pNESupport.announceEdit(cEdit);
			cEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}
		
		// test exception in no legal edit
		boolean exceptionLaunched = false;
		AddLinkEdit iEdit;
		try {
            iEdit = new AddLinkEdit(probNetDirected, va, vc, false);
            pNESupport.announceEdit(iEdit);
		} catch (Exception cve) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched); // pNESupport must launch an exception.
	}

}
