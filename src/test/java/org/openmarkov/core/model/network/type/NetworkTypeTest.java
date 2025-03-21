/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.DistinctLinks;
import org.openmarkov.core.model.network.constraint.DistinctVariableNames;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.NoEmptyName;
import org.openmarkov.core.model.network.constraint.NoMultipleLinks;
import org.openmarkov.core.model.network.constraint.NoRevelationArc;
import org.openmarkov.core.model.network.constraint.NoSelfLoop;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.OnlyOneAgent;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;
import org.openmarkov.core.model.network.constraint.PNConstraint;

import java.util.List;

public class NetworkTypeTest {

	@Test public void testDefaultNetworkTypeIsBayesian() {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		List<PNConstraint> constraints = probNet.getConstraints();
		Assertions.assertTrue(constraints.contains(new NoEmptyName()));
		Assertions.assertTrue(constraints.contains(new DistinctVariableNames()));
		Assertions.assertTrue(constraints.contains(new OnlyChanceNodes()));
		Assertions.assertTrue(constraints.contains(new OnlyAtemporalVariables()));
		Assertions.assertFalse(constraints.contains(new OnlyTemporalVariables()));
		Assertions.assertTrue(constraints.contains(new OnlyOneAgent()));
		Assertions.assertTrue(constraints.contains(new DistinctLinks()));
		Assertions.assertTrue(constraints.contains(new NoMultipleLinks()));
		Assertions.assertTrue(constraints.contains(new OnlyDirectedLinks()));
		Assertions.assertFalse(constraints.contains(new OnlyUndirectedLinks()));
		Assertions.assertTrue(constraints.contains(new NoRevelationArc()));
		Assertions.assertTrue(constraints.contains(new NoSelfLoop()));
		Assertions.assertTrue(constraints.contains(new NoCycle()));
	}

	@Test public void testConvertingBayesianIntoMarkov() throws ConstraintViolationException {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		probNet.setNetworkType(MarkovNetworkType.getUniqueInstance());
		List<PNConstraint> constraints = probNet.getConstraints();
		Assertions.assertFalse(constraints.contains(new OnlyDirectedLinks()));
		Assertions.assertTrue(constraints.contains(new OnlyUndirectedLinks()));
	}

	@Test public void testRemovingConstraintsNoLongerApplicable() throws ConstraintViolationException {
		ProbNet probNet = new ProbNet();
		probNet.setNetworkType(InfluenceDiagramType.getUniqueInstance());
		List<PNConstraint> constraints = probNet.getConstraints();
		Assertions.assertFalse(constraints.contains(new OnlyChanceNodes()));
	}

	// TODO Adapt this test to check the exception (migrating from JUnit 4 to JUnit 5)
	//@Test 
	//(expected = ConstraintViolationException.class)
	public void testImpossibleNetworkTypeConversion()
			throws ConstraintViolationException, NonProjectablePotentialException,
			WrongCriterionException, DoEditException {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		AddNodeEdit addVariableEdit = new AddNodeEdit(probNet, new Variable("a"), NodeType.DECISION);
		probNet.setNetworkType(InfluenceDiagramType.getUniqueInstance());

		probNet.doEdit(addVariableEdit);
		probNet.setNetworkType(BayesianNetworkType.getUniqueInstance());
	}

	// TODO Adapt this test to check the exception (migrating from JUnit 4 to JUnit 5)
	//@Test
	//(expected = ConstraintViolationException.class)
	public void testAddingNotApplicableConstraints()
			throws ConstraintViolationException {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		probNet.addConstraint(new OnlyUndirectedLinks());

	}
}
