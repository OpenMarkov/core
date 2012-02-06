package org.openmarkov.core.model.network.constraint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddVariableEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class OnlyAtemporalVariablesTest {

	private ProbNet influenceDiagram;

	@Before
	public void setUp() throws Exception {
		influenceDiagram = ConstraintsTests.getInfuenceDiagram();
	}

	@Test
	public void testCheckProbNet() {

		boolean exceptionLaunched = false;
		try {

			influenceDiagram.removeConstraint(new OnlyAtemporalVariables());
			influenceDiagram.addConstraint(new OnlyAtemporalVariables(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		try {
			influenceDiagram.removeConstraint(new OnlyAtemporalVariables());
			Variable var = new Variable(" [10]", "YES", "NO");
			influenceDiagram.addVariable(var, NodeType.CHANCE);
			influenceDiagram.addConstraint(new OnlyAtemporalVariables(), true);
		} catch (ConstraintViolationException e1) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);
	}
	
	
	@Test
	public void testUndoableEditWillHappen() 
	        throws Exception {
		PNESupport pNESupport = new PNESupport(influenceDiagram, false);
		PNConstraint constraint = new OnlyAtemporalVariables();

		influenceDiagram.addConstraint(constraint, true);
		pNESupport.addUndoableEditListener(constraint);
		
		boolean exceptionLaunched = false;
		Variable var= new Variable("F");
		AddVariableEdit legalEdit= new AddVariableEdit(influenceDiagram,var,NodeType.CHANCE); 
		// add the variable
				try {
					pNESupport.announceEdit(legalEdit);
					legalEdit.doEdit();
				} catch (Exception cve) {
					fail(cve.getMessage());
				}

		Variable var1= new Variable(" [11]","Y","N");
		AddVariableEdit ilegalEdit= new AddVariableEdit(influenceDiagram,var1,NodeType.CHANCE); 
		// add the variable
				try {
					pNESupport.announceEdit(ilegalEdit);
					ilegalEdit.doEdit();
				} catch (Exception cve) {
					exceptionLaunched=true;
				}
				assertTrue(exceptionLaunched);

		
	}

}
