/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscretizedCauchyPotentialTest {

	private DiscretizedCauchyPotential discretizedPotential;
	private Variable predictedAudiometry;
	private Variable processorTypeChanged;
	private Variable micAge;
	private Variable electrodeChanged;
	private Variable audiometry;

	@BeforeAll public void setUp() {
		Variable medianVariable = new Variable("Median");
		Variable scaleVariable = new Variable("Scale");
		predictedAudiometry = new Variable("Predicted audiometry", "off/off", "off", "on");
		processorTypeChanged = new Variable("Processor type changed", "no", "yes");
		micAge = new Variable("Mic age", "<=30", ">30 and <=90", ">90 and <= 365", ">365");
		electrodeChanged = new Variable("Electrode changed", "0", "1", "2", "3+");
		audiometry = new Variable("Audiometry", "off/off", "off", "on");
		List<Variable> parentVariables = Arrays
				.asList(predictedAudiometry, processorTypeChanged, micAge, electrodeChanged);
		List<Variable> potentialVariables = new ArrayList<>(parentVariables);
		potentialVariables.add(0, audiometry);
		LinearCombinationPotential medianPotential = new LinearCombinationPotential(parentVariables,
				PotentialRole.CONDITIONAL_PROBABILITY);
		medianPotential.setCoefficients(new double[] { 0, 1, 0.1, -0.2, 0.05 });
		LinearCombinationPotential scalePotential = new LinearCombinationPotential(parentVariables,
				PotentialRole.CONDITIONAL_PROBABILITY);
		scalePotential.setCoefficients(new double[] { 1, 0, 0.2, 0.2, 0.1 });
		discretizedPotential = new DiscretizedCauchyPotential(potentialVariables,
				PotentialRole.CONDITIONAL_PROBABILITY);
		discretizedPotential.setMedian(medianPotential);
		discretizedPotential.setScale(scalePotential);
	}

	//@Test
	public void testTableProject() throws NonProjectablePotentialException, WrongCriterionException {

		TablePotential projectedPotential = discretizedPotential.tableProject(new EvidenceCase(), null).get(0);

		Assertions.assertEquals(288, projectedPotential.tableSize);
		Assertions.assertEquals(0.6914, projectedPotential.values[0], 10E-4);
		Assertions.assertEquals(0.2417, projectedPotential.values[1], 10E-4);
		Assertions.assertEquals(0.0668, projectedPotential.values[2], 10E-4);
		Assertions.assertEquals(0.3085, projectedPotential.values[3], 10E-4);
		Assertions.assertEquals(0.3829, projectedPotential.values[4], 10E-4);
		Assertions.assertEquals(0.3085, projectedPotential.values[5], 10E-4);
		Assertions.assertEquals(0.0668, projectedPotential.values[6], 10E-4);
		Assertions.assertEquals(0.2417, projectedPotential.values[7], 10E-4);
		Assertions.assertEquals(0.6914, projectedPotential.values[8], 10E-4);
		Assertions.assertEquals(0.6305, projectedPotential.values[9], 10E-4);
		Assertions.assertEquals(0.2477, projectedPotential.values[10], 10E-4);
		Assertions.assertEquals(0.1216, projectedPotential.values[11], 10E-4);
	}

	//@Test
	public void testTableProjectWithEvidence()
			throws NonProjectablePotentialException, WrongCriterionException, InvalidStateException,
			IncompatibleEvidenceException {

		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(new Finding(predictedAudiometry, 2)); // on
		evidence.addFinding(new Finding(micAge, 2)); // >90 and <= 365

		TablePotential projectedPotential = discretizedPotential.tableProject(evidence, null).get(0);

		Assertions.assertEquals(24, projectedPotential.tableSize);
		Assertions.assertEquals(0.2160, projectedPotential.values[0], 10E-4);
		Assertions.assertEquals(0.2555, projectedPotential.values[1], 10E-4);
		Assertions.assertEquals(0.5284, projectedPotential.values[2], 10E-4);
		Assertions.assertEquals(0.2266, projectedPotential.values[3], 10E-4);
		Assertions.assertEquals(0.2236, projectedPotential.values[4], 10E-4);
		Assertions.assertEquals(0.5497, projectedPotential.values[5], 10E-4);
		Assertions.assertEquals(0.2216, projectedPotential.values[6], 10E-4);
		Assertions.assertEquals(0.2385, projectedPotential.values[7], 10E-4);
		Assertions.assertEquals(0.5398, projectedPotential.values[8], 10E-4);

	}

	@Disabled("Ignored because an ArrayIndexOutOfBoundsException")
	@Test public void testTableProjectWithFullEvidence()
			throws NonProjectablePotentialException, WrongCriterionException, InvalidStateException,
			IncompatibleEvidenceException {

		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(new Finding(predictedAudiometry, 2)); // on
		evidence.addFinding(new Finding(processorTypeChanged, 0)); // no
		evidence.addFinding(new Finding(electrodeChanged, 1)); // 1
		evidence.addFinding(new Finding(micAge, 2)); // >90 and <= 365

		TablePotential projectedPotential = discretizedPotential.tableProject(evidence, null).get(0);

		Assertions.assertEquals(3, projectedPotential.tableSize);
		Assertions.assertEquals(0.2917990024368812, projectedPotential.values[0], 10E-4);
		Assertions.assertEquals(0.17647548013256514, projectedPotential.values[1], 10E-4);
		Assertions.assertEquals(0.5317255174305536, projectedPotential.values[2], 10E-4);
	}

}