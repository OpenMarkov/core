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


public class MinPotentialTest {

	// Attributes
	private ProbNet probNet;
	
	private ProbNode probNodeAvPre;
	
	private MaxPotential minPotential;
	
	// Initialization
	@Before
	public void setUp() throws Exception {
        String name = "prueba-min";
        String fullNetName = IOTests.testsPath + name + ".elv";
        probNet = ElviraParser.getUniqueInstance().loadProbNet(fullNetName);
        probNodeAvPre = probNet.getProbNode(probNet.getVariable("av_pre"));
	}

	@Test
	public void testGetDeltaPotential() throws NotEnoughMemoryException {
        minPotential = (MaxPotential)probNodeAvPre.getPotentials().get(0);
		TablePotential deltaPotential = 
			(TablePotential)minPotential.getDeltaPotential();
		Variable pseudoVariable = minPotential.getPseudoVariable();
		Variable conditionedVariable = minPotential.getConditionedVariable();
		assertNotNull(pseudoVariable);
		assertNotNull(conditionedVariable);
		ArrayList<Variable> deltaVariables = deltaPotential.getVariables();
		assertTrue(deltaVariables.contains(pseudoVariable));
		assertTrue(deltaVariables.contains(conditionedVariable));
	}

	@Test
	public void testAccruedPotential() {

	}

	@Test public void testGetCPT() {
		
	}
	
}
