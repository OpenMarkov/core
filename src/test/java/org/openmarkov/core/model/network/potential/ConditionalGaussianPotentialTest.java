package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;

public class ConditionalGaussianPotentialTest {

	@Test
    public void testTableProject() throws NonProjectablePotentialException, WrongCriterionException {
		Variable meanVariable = new Variable("Mean");
		Variable varianceVariable = new Variable("Variance");
		Variable predictedAudiometry = new Variable("Predicted audiometry", "off/off", "off", "on");
		Variable processorTypeChanged = new Variable("Processor type changed", "no", "yes");
		Variable micAge = new Variable("Mic age", "<=30", ">30 and <=90", ">90 and <= 365", ">365");
		Variable electrodeChanged = new Variable("Electrode changed", "0", "1", "2", "3+");
		Variable audiometry = new Variable("Audiometry", "off/off", "off", "on");
		List<Variable> parentVariables = Arrays.asList(predictedAudiometry, processorTypeChanged, micAge, electrodeChanged);
		List<Variable> meanPotentialVariables = new ArrayList<>(parentVariables);
		meanPotentialVariables.add(0, meanVariable);
		List<Variable> variancePotentialVariables = new ArrayList<>(parentVariables);
		variancePotentialVariables.add(0, varianceVariable);
		List<Variable> potentialVariables = new ArrayList<>(parentVariables);
		potentialVariables.add(0, audiometry);
		LinearCombinationPotential meanPotential = new LinearCombinationPotential(meanPotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY);
		meanPotential.setCoefficients(new double[] {0, 1, 0.1, -0.2, 0.05});
		LinearCombinationPotential variancePotential = new LinearCombinationPotential(variancePotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY);;
		variancePotential.setCoefficients(new double[] {1, 0, 0.2, 0.2, 0.1});
		ConditionalGaussianPotential gaussianPotential = new ConditionalGaussianPotential(potentialVariables, PotentialRole.CONDITIONAL_PROBABILITY);
		gaussianPotential.setMean(meanPotential);
		gaussianPotential.setVariance(variancePotential);
		
		TablePotential projectedPotential = gaussianPotential.tableProject(new EvidenceCase(), null).get(0);
		
		Assert.assertEquals(288, projectedPotential.tableSize);
		Assert.assertEquals(0.6914, projectedPotential.values[0], 10E-4);
		Assert.assertEquals(0.2417, projectedPotential.values[1], 10E-4);
		Assert.assertEquals(0.0668, projectedPotential.values[2], 10E-4);
		Assert.assertEquals(0.3085, projectedPotential.values[3], 10E-4);
		Assert.assertEquals(0.3829, projectedPotential.values[4], 10E-4);
		Assert.assertEquals(0.3085, projectedPotential.values[5], 10E-4);
		Assert.assertEquals(0.0668, projectedPotential.values[6], 10E-4);
		Assert.assertEquals(0.2417, projectedPotential.values[7], 10E-4);
		Assert.assertEquals(0.6914, projectedPotential.values[8], 10E-4);
		Assert.assertEquals(0.6305, projectedPotential.values[9], 10E-4);
		Assert.assertEquals(0.2477, projectedPotential.values[10], 10E-4);
		Assert.assertEquals(0.1216, projectedPotential.values[11], 10E-4);
	}
}
