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

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class NoUtilityParentTest {

	private ProbNet probNetImproperUtilityChildren; 
	private ProbNet probNetProperUtilityChildren; 
	private ProbNet influenceDiagram; 
	
	@Before
	public void setUp() throws Exception {
		probNetImproperUtilityChildren = ConstraintsTests.getNotOnlyUtilityChildrenInfluenceDiagram();
		probNetProperUtilityChildren = ConstraintsTests.getOnlyUtilityChildrenInfluenceDiagram();
		influenceDiagram= ConstraintsTests.getInfuenceDiagram();
	}
	
	

	@Test
	public void testOnlyUtilityChilren() {
		boolean exceptionLaunched = false;
		PNConstraint constraint = new NoUtilityParent();
		try {
            probNetImproperUtilityChildren.addConstraint (constraint,
                                                          true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);

		probNetImproperUtilityChildren.removeConstraint(constraint);
		exceptionLaunched = false;	
		assertFalse(constraint.checkProbNet(probNetImproperUtilityChildren));
		
		 //test only utility children
        assertTrue (constraint.checkProbNet (probNetProperUtilityChildren));
	}

	@Test
	public void testUndoableEditWillHappen() throws Exception {
		// Add constraints as listeners.
		PNESupport pNESupport = influenceDiagram.getPNESupport ();
		influenceDiagram.addConstraint(new NoUtilityParent(), true);
		// Create edits
		Variable vu=influenceDiagram.getVariable("U");
		Variable vc1 = new Variable("C1", 0);
		Variable vc2 = new Variable("C2", 0);

		// test no exception in legal edit
		AddVariableEdit legalAdd = new AddVariableEdit(influenceDiagram, vc1, 
			NodeType.UTILITY);
		AddLinkEdit legalLink = new AddLinkEdit(influenceDiagram,vu,vc1,true);
		
		//add the node C1
		try{
			influenceDiagram.doEdit(legalAdd);
		} catch(Exception cve){
			fail(cve.getMessage());
		}
		
		//link U->C1
		try {
			pNESupport.announceEdit(legalLink);
			legalLink.doEdit();
		} catch (Exception cve) {
			fail(cve.getMessage());
		}

		// test exception in no legal edit
		legalAdd = new AddVariableEdit(influenceDiagram, vc2, 
				NodeType.DECISION);
		AddLinkEdit ilegalLink = new AddLinkEdit(influenceDiagram,vu,vc2,true);
		
		//add the node C2
		try{
			pNESupport.announceEdit(legalAdd);
			legalAdd.doEdit();
		} catch(Exception cve){
			fail(cve.getMessage());
		}
		
		
		boolean exceptionLaunched = false;
		try {
			pNESupport.announceEdit(ilegalLink);
		} catch (Exception cve) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched); // pNESupport must launch an exception.
	}


	
	
}
