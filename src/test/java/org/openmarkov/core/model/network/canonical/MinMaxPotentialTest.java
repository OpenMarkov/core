package org.openmarkov.core.model.network.canonical;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;


public class MinMaxPotentialTest {

	// Attributes
	private ProbNet probNet;
	
	private ProbNode probNodeC;
	
	// Initialization
	@Before	public void setUp() throws Exception {
        String name = "puerta-or";
        String fullNetName = IOTests.testsPath + name + ".elv";
        probNet = ElviraParser.getUniqueInstance().loadProbNet(fullNetName);
        probNodeC = probNet.getProbNode(probNet.getVariable("C"));
        probNet.getVariable("C");
	}
	
	@Test public void testGetCPT() throws NotEnoughMemoryException {
		MaxPotential maxPotential = 
			(MaxPotential) probNodeC.getPotentials().get(0);
		TablePotential cpt = maxPotential.getCPT();
		assertEquals(0.999, cpt.values[0]);
		assertEquals(0.1993, cpt.values[2]);
		assertEquals(0.2997, cpt.values[4]);
		assertEquals(0.05994, cpt.values[6]);
	}
	
}
