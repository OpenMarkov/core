/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;

import java.util.Arrays;
import java.util.List;

public class LinearRegressionPotentialTest {

	private LinearCombinationPotential potential = null;
	private Variable ageAtStateEntryVar = null;
	private Variable timeInStateVar = null;
	private Variable ageVar = null;

	@Before public void setUp() throws Exception {

		// Revision Risk
		ageAtStateEntryVar = new Variable("Age at state entry", "3.4", "4.4");
		timeInStateVar = new Variable("Time in state", "0", "1");
		ageVar = new Variable("Age", "4.4");

		List<Variable> variables = Arrays.asList(ageAtStateEntryVar, ageVar, timeInStateVar);
		double[] coefficients = new double[] { 0, 1, -1 };
		String[] covariates = new String[] { "Constant", "Age", "Time in state" };

		potential = new LinearCombinationPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, covariates,
				coefficients);
	}

	@Test public void testTableProject() {
		EvidenceCase evidence = new EvidenceCase();
		List<TablePotential> projectedPotentials = null;
		try {
			projectedPotentials = potential.tableProject(evidence, null);
		} catch (NonProjectablePotentialException | WrongCriterionException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(1, projectedPotentials.size());

		TablePotential projectedPotential = projectedPotentials.get(0);

		double[] expectedValues = new double[] { 0, 1, 1, 0 };
		Assert.assertArrayEquals(expectedValues, projectedPotential.values, 0.00001);
	}

}
