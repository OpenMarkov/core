package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;

public class ConditionalGaussianPotentialTest {

	private ConditionalGaussianPotential gaussianPotential; 
	private Variable predictedAudiometry;
	private Variable processorTypeChanged;
	private Variable micAge;
	private Variable electrodeChanged;
	private Variable audiometry;
	
	@Before
	public void setUp()
	{
		Variable meanVariable = new Variable("Mean");
		Variable varianceVariable = new Variable("Variance");
		predictedAudiometry = new Variable("Predicted audiometry", "off/off", "off", "on");
		processorTypeChanged = new Variable("Processor type changed", "no", "yes");
		micAge = new Variable("Mic age", "<=30", ">30 and <=90", ">90 and <= 365", ">365");
		electrodeChanged = new Variable("Electrode changed", "0", "1", "2", "3+");
		audiometry = new Variable("Audiometry", "off/off", "off", "on");
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
		gaussianPotential = new ConditionalGaussianPotential(potentialVariables, PotentialRole.CONDITIONAL_PROBABILITY);
		gaussianPotential.setMean(meanPotential);
		gaussianPotential.setVariance(variancePotential);
	}
	
	@Test
    public void testTableProject() throws NonProjectablePotentialException, WrongCriterionException {
		
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
	
	@Test
    public void testTableProjectWithEvidence() throws NonProjectablePotentialException, WrongCriterionException, InvalidStateException, IncompatibleEvidenceException {

		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(new Finding(predictedAudiometry, 2)); // on
		evidence.addFinding(new Finding(micAge, 2)); // >90 and <= 365
		
		TablePotential projectedPotential = gaussianPotential.tableProject(evidence, null).get(0);
		
		Assert.assertEquals(24, projectedPotential.tableSize);
		Assert.assertEquals(0.2160, projectedPotential.values[0], 10E-4);
		Assert.assertEquals(0.2555, projectedPotential.values[1], 10E-4);
		Assert.assertEquals(0.5284, projectedPotential.values[2], 10E-4);
		Assert.assertEquals(0.2266, projectedPotential.values[3], 10E-4);
		Assert.assertEquals(0.2236, projectedPotential.values[4], 10E-4);
		Assert.assertEquals(0.5497, projectedPotential.values[5], 10E-4);
		Assert.assertEquals(0.2216, projectedPotential.values[6], 10E-4);
		Assert.assertEquals(0.2385, projectedPotential.values[7], 10E-4);
		Assert.assertEquals(0.5398, projectedPotential.values[8], 10E-4);
		
	}
	
}
