/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.constraint;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.action.VariableTypeEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

public class OnlyFiniteStateVariablesTest {


	// Attributes
	private ProbNet influenceDiagram;
	private ProbNet mixedVariableInfluenceDiagram;

	// Methods
	@Before
	public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getInfuenceDiagram();
		mixedVariableInfluenceDiagram=  getMixedVariableInfluenceDiagram();
	}


	private ProbNet getMixedVariableInfluenceDiagram()
	{
		ProbNet net = ConstraintsTests.getInfuenceDiagram();
		Variable vC = new Variable("C");

		try {
			Variable vU=net.getVariable("U");
			net.addVariable(vC, NodeType.CHANCE);
			net.addLink(vU, vC, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return net;
	}

	@Test
	public void testCheckProbNet() {
		boolean exceptionLaunched = false;
		try {
			influenceDiagram.addConstraint(new 
					OnlyFiniteStatesVariables(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertTrue(!exceptionLaunched);
		try {
			mixedVariableInfluenceDiagram.addConstraint(new 
					OnlyFiniteStatesVariables(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);
	}
	
	
	
	@Test
	public void testUndoableEditWillHappen() throws Exception {
		
		
		PNESupport pNESupport = new PNESupport(influenceDiagram, false);
		PNConstraint constraint= new OnlyFiniteStatesVariables();
		influenceDiagram.addConstraint(constraint, true);
		
			pNESupport.addUndoableEditListener(constraint);
		
			Variable vc1 = new Variable("D1",0);
			

			// test no exception in legal edit
			AddVariableEdit legalAdd = new AddVariableEdit(influenceDiagram, vc1, 
				NodeType.DECISION);
			//add the node D1 (decision + finite state)
			try{
				pNESupport.announceEdit(legalAdd);
				legalAdd.doEdit();
			} catch(Exception cve){
				fail(cve.getMessage());
			}
		
			Variable vc2 = new Variable("C1",0);
			// test no exception in legal edit
			 legalAdd = new AddVariableEdit(influenceDiagram, vc2, 
				NodeType.CHANCE);
			//add the node C1 (chance + finite state)
			try{
				pNESupport.announceEdit(legalAdd);
				legalAdd.doEdit();
			} catch(Exception cve){
				fail(cve.getMessage());
			}
		
			Variable vc3 = new Variable("U1");
			// test no exception in legal edit (utility + numeric)
			 legalAdd = new AddVariableEdit(influenceDiagram, vc3, 
				NodeType.UTILITY);
			//add the node U1
			try{
				pNESupport.announceEdit(legalAdd);
				legalAdd.doEdit();
			} catch(Exception cve){
				fail(cve.getMessage());
			}
		
		
			Variable vc4 = new Variable("D2");
			// test exception in illegal edit (decision + numeric)
			AddVariableEdit illegalAdd = new AddVariableEdit(influenceDiagram, vc4, 
				NodeType.DECISION);
			//add the node D2
			boolean exceptionLaunched=false;
			try{
				pNESupport.announceEdit(illegalAdd);
				
			} catch(Exception cve){
				exceptionLaunched=true;
			}
		
			assertTrue(exceptionLaunched);
		
			Variable vc5 = new Variable("C2");
			// test  exception in illegal edit (chance + numeric)
			 illegalAdd = new AddVariableEdit(influenceDiagram, vc5, 
				NodeType.CHANCE);
			//add the node D2
			 exceptionLaunched=false;
			try{
				pNESupport.announceEdit(illegalAdd);
				
			} catch(Exception cve){
				exceptionLaunched=true;
			}
		
			assertTrue(exceptionLaunched);
		
			
			
			VariableTypeEdit legalEdit= new VariableTypeEdit(influenceDiagram.getProbNode("D1") ,VariableType.DISCRETIZED ); 
			//edit variable type of D1
			// test  exception in legal edit (decision + discrete)
			try{
				pNESupport.announceEdit(legalEdit);
				legalEdit.doEdit();
			} catch(Exception cve){
				fail(cve.getMessage());
			}
			
			legalEdit= new VariableTypeEdit(influenceDiagram.getProbNode("C1") ,VariableType.DISCRETIZED ); 
			//edit variable type of C1
			// test  exception in legal edit (chance + discrete)
			try{
				pNESupport.announceEdit(legalEdit);
				legalEdit.doEdit();
			} catch(Exception cve){
				fail(cve.getMessage());
			}
			
			VariableTypeEdit illegalEdit= new VariableTypeEdit(influenceDiagram.getProbNode("C1") ,VariableType.NUMERIC ); 
			//edit variable type of C1
			// test  exception in illegal edit (chance + numeric)
			 exceptionLaunched=false;
			try{
				pNESupport.announceEdit(illegalEdit);
			
			} catch(Exception cve){
				exceptionLaunched=true;
			}
			assertTrue(exceptionLaunched);
	}

}
