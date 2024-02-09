/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.plugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PotentialManagerTest {

	PotentialManager manager = null;
	Variable variableA;
	Variable variableB;
	Variable variableC;
	Variable variableU;
	ProbNet probNet;
	Node nodeU;

	@BeforeAll public void setUp() throws Exception {
		manager = new PotentialManager();

		probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		variableA = new Variable("A", "no", "yes");
		variableB = new Variable("B", "no", "yes");
		variableC = new Variable("C", "no", "yes");
		variableU = new Variable("U");

		probNet.addNode(variableA, NodeType.CHANCE);
		probNet.addNode(variableB, NodeType.CHANCE);
		probNet.addNode(variableC, NodeType.CHANCE);
		nodeU = probNet.addNode(variableU, NodeType.UTILITY);

		probNet.addLink(variableB, variableA, true);
		probNet.addLink(variableC, variableA, true);
		probNet.addLink(variableB, variableU, true);
		probNet.addLink(variableC, variableU, true);
		probNet.addLink(variableA, variableU, true);

		List<Variable> potentialUVariables = new ArrayList<Variable>();
		potentialUVariables.addAll(Arrays.asList(variableU, variableA, variableB, variableC));
		Potential potentialU = new UniformPotential(potentialUVariables, PotentialRole.CONDITIONAL_PROBABILITY);
		nodeU.setPotential(potentialU);
	}

	@Test public void testGetAllPotentialsNames() {
		Set<String> potentialNames = manager.getAllPotentialsNames();
		Assertions.assertNotNull(potentialNames);
		Assertions.assertFalse(potentialNames.isEmpty());
	}

	@Test public void testGetByName() {
		List<Variable> variables = Arrays.asList(variableA, variableB, variableC);
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
		CycleLength defaultCycleLength = new CycleLength();
		String cycleLengthShiftName = manager.getPotentialName(CycleLengthShift.class);

		Set<String> potentialNames = manager.getAllPotentialsNames();

		for (String potentialType : potentialNames) {
			if (!potentialType.equals(PotentialManager.getPotentialName(SameAsPrevious.class))) {
				Potential potential = null;

				if (potentialType.equals("Tree/ADDDelta")) {
					continue;
				}
				if (potentialType.equals(cycleLengthShiftName)) {
					potential = manager.getByName(potentialType, variables, role, defaultCycleLength);
				} else {
					potential = manager.getByName(potentialType, variables, role);
				}

				Assertions.assertNotNull(potential);
				Assertions.assertEquals(potentialType, PotentialManager.getPotentialName(potential.getClass()));
			}
		}
	}

	//@Test
	public void testGetByNameUtility() {
		List<Variable> variables = Arrays.asList(variableA, variableB, variableC);

		List<String> potentialNames = manager.getFilteredPotentials(nodeU);

		for (String potentialType : potentialNames) {
			if (potentialType.equals("CycleLengthShift")) {
				continue;
			}

			Potential potential = manager.getByName(potentialType, variableU, variables);
			Assertions.assertNotNull(potential);
			Assertions.assertEquals(PotentialRole.CONDITIONAL_PROBABILITY, potential.getPotentialRole());
			Assertions.assertEquals(potentialType, PotentialManager.getPotentialName(potential.getClass()));
		}
	}
}
