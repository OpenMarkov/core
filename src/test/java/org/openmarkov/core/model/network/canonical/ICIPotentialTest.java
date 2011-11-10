package org.openmarkov.core.model.network.canonical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.MinMaxPotential;


public class ICIPotentialTest {

	// Attributes
	private ProbNet probNet;
	
	private ProbNode probNodeC;
	
	private MinMaxPotential minMaxPotential;
	
	private EvidenceCase evidence;
	
	private Variable A;
	
	// Initialization
	@Before	public void setUp() throws Exception {
        String name = "puerta-or";
        String fullNetName = IOTests.testsPath + name + ".elv";
        probNet = ElviraParser.getUniqueInstance().loadProbNet(fullNetName);
        probNodeC = probNet.getProbNode(probNet.getVariable("C"));
        minMaxPotential = (MinMaxPotential)probNodeC.getPotentials().get(0);
        A = probNet.getVariable("A");
        probNet.getVariable("B");
        probNet.getVariable("C");
        
        ArrayList<Finding> findings = new ArrayList<Finding>();
        Finding findingA = new Finding(A, 1);
        findings.add(findingA);
        evidence = new EvidenceCase();
        evidence.addFinding(findingA);
	}
	
	// Test methods
	@Test public void testProject() 
			throws NotEnoughMemoryException, NoFindingException, WrongCriterionException {
		ArrayList<TablePotential> potentials = 
			minMaxPotential.tableProject(evidence, null);
		assertEquals(4, potentials.size());
		Variable variablePseudoC = minMaxPotential.getPseudoVariable();
		for (Potential potential : potentials) {
			ArrayList<Variable> variables = potential.getVariables();
			assertTrue(variables.contains(variablePseudoC));
		}
	}

}
