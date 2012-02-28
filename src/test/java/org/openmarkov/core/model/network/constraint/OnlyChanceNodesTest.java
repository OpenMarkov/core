/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.constraint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddProbNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;


public class OnlyChanceNodesTest {

	// Attributes
	private ProbNet influenceDiagram;
	
	private ProbNet probNetDirected;
	
	// Methods
	@Before
	public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getInfuenceDiagram();
		probNetDirected =  ConstraintsTests.getTestProbNetDirected();
	}

	/** Checks or not all the <code>probNet</code> in different situations in
	 * <code>OnlyDirectedLinks</code> constructor. */
	@Test 
	public void testCheckProbNet() throws ConstraintViolationException {
		// test only directed links insertions without checking.
		assertFalse(new OnlyChanceNodes().checkProbNet(influenceDiagram));
	}

	/** Checks veto */
	@Test
	public void testUndoableEditWillHappen() 
	        throws Exception {
		
		// Add constraints as listeners.
		PNESupport pNESupport = new PNESupport(false);
        probNetDirected.addConstraint (new OnlyChanceNodes (), true);
		ArrayList<PNConstraint> constraints = probNetDirected.getConstraints();
		for (PNConstraint constraint : constraints) { // sets listeners
			pNESupport.addUndoableEditListener(constraint);
		}
		// Create edits
		Variable vd = new Variable("D", 0);
		Variable ve = new Variable("E", 0);

		// test no exception in legal edit
		AddProbNodeEdit legalEdit = new AddProbNodeEdit(probNetDirected, ve, 
			NodeType.CHANCE);
		try {
			pNESupport.announceEdit(legalEdit);
			legalEdit.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}
		
		// test exception in no legal edit
		AddProbNodeEdit ilegalEdit = new AddProbNodeEdit(probNetDirected, vd, 
				NodeType.DECISION);
		boolean exceptionLaunched = false;
		try {
			pNESupport.announceEdit(ilegalEdit);
		} catch (Exception cve) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched); // pNESupport must launch an exception.
	}


}
