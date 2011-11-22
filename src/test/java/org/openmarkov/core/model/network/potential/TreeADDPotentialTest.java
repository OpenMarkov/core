package org.openmarkov.core.model.network.potential;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

public class TreeADDPotentialTest {

	private TreeADDPotential potential;
	
	private ProbNet probNet;
	
	private Variable A;
	
	private Variable B;
	
	@Before
    public void setUp() throws Exception {
		probNet = PGMXReader.getUniqueInstance().loadProbNet(
				IOTests.testsPath + "TreeAdd-Example.pgmx");
		ArrayList<Potential> potentials = probNet.getPotentials();
		for (Potential potential : potentials) {
			if (potential.getPotentialType() == PotentialType.TREE_ADD) {
				this.potential = (TreeADDPotential) potential;
				B = potential.getVariable(0);
				A = potential.getVariable(1);
			}
		}
	}
	
	@Test
	public void testTableProject() 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
			WrongCriterionException, InvalidStateException, 
			IncompatibleEvidenceException {
		InferenceOptions options = new InferenceOptions(probNet, B);
		TablePotential tablePotential = 
			potential.tableProject(null, null).get(0);
		ArrayList<Variable> variables = tablePotential.getVariables();
		assertEquals(2, variables.size());
		assertEquals(1.0, tablePotential.values[0]);
		assertEquals(0.0, tablePotential.values[1]);
		assertEquals(0.9, tablePotential.values[2]);
		assertEquals(0.1, tablePotential.values[3]);
		
		Finding bFinding = new Finding(B, 0);
		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(bFinding);
		tablePotential = 
			potential.tableProject(evidence, null).get(0);
		variables = tablePotential.getVariables();
		assertEquals(1, variables.size());
		assertEquals(1.0, tablePotential.values[0]);
		assertEquals(0.9, tablePotential.values[1]);

		Finding aFinding = new Finding(A, 1);
		evidence = new EvidenceCase();
		evidence.addFinding(aFinding);
		tablePotential = 
			potential.tableProject(evidence, null).get(0);
		variables = tablePotential.getVariables();
		assertEquals(1, variables.size());
		assertEquals(0.9, tablePotential.values[0]);
		assertEquals(0.1, tablePotential.values[1]);
	}

	@Test
	public void testShift() {
		// TODO
	}

	@Test
	public void testGetInducedFindings() {
		// TODO
	}

}
