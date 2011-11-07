package org.openmarkov.core.model.network.canonical;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;


public class MaxPotentialTest {

	// Attributes
	private ProbNet probNet;
	
	private ProbNode probNodeC;
	
	private MaxPotential maxPotential;
	
	// Initialization
	@Before	public void setUp() throws Exception {
        String name = "puerta-or";
        String fullNetName = IOTests.testsPath + name + ".elv";
        probNet = ElviraParser.getUniqueInstance().loadProbNet(fullNetName);
        probNodeC = probNet.getProbNode(probNet.getVariable("C"));
        probNet.getVariable("C");
	}
	
	@Test public void testGetDeltaPotential() throws NotEnoughMemoryException {
        maxPotential = (MaxPotential) probNodeC.getPotentials().get(0);
		TablePotential deltaPotential = 
			(TablePotential)maxPotential.getDeltaPotential();
		Variable pseudoVariable = maxPotential.getPseudoVariable();
		Variable conditionedVariable = maxPotential.getConditionedVariable();
		assertNotNull(pseudoVariable);
		assertNotNull(conditionedVariable);
		ArrayList<Variable> deltaVariables = deltaPotential.getVariables();
		assertTrue(deltaVariables.contains(pseudoVariable));
		assertTrue(deltaVariables.contains(conditionedVariable));
	}
	
	@Test public void testAccruedPotential() {
		
	}
	
	@Test public void testGetCPT() {
		
	}
	
}
