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

public class OnlyTemporalVariablesTest {

	private ProbNet network;

	@Before
	public void setUp() throws Exception {
		network = ConstraintsTests.getTemporalVarNet();
	}

	@Test
	public void testCheckProbNet() {

		boolean exceptionLaunched = false;
		try {
			network.removeConstraint(new OnlyTemporalVariables());
			network.addConstraint(new OnlyTemporalVariables(), true);
		} catch (Exception e1) {
			exceptionLaunched = true;
		}
		assertFalse(exceptionLaunched);

		try {
			network.removeConstraint(new OnlyTemporalVariables());
			Variable var = new Variable("A");
			network.addVariable(var, NodeType.CHANCE);
			network.addConstraint(new OnlyTemporalVariables(), true);
		} catch (ConstraintViolationException e1) {
			exceptionLaunched = true;
		}
		assertTrue(exceptionLaunched);
	}
	
	
	@Test
	public void testUndoableEditWillHappen() 
	        throws Exception {
		PNESupport pNESupport = new PNESupport(network, false);
		PNConstraint constraint = new OnlyTemporalVariables();
		network.addConstraint(constraint, true);
		pNESupport.addUndoableEditListener(constraint);
		
		boolean exceptionLaunched = false;
		Variable var= new Variable(" [11]","Y","N");
		AddVariableEdit legalEdit= new AddVariableEdit(network,var,NodeType.CHANCE); 
		// add the variable
				try {
					pNESupport.announceEdit(legalEdit);
					legalEdit.doEdit();
				} catch (Exception cve) {
					fail(cve.getMessage());
				}

		Variable var1= new Variable("F");
		AddVariableEdit ilegalEdit= new AddVariableEdit(network,var1,NodeType.CHANCE); 
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
