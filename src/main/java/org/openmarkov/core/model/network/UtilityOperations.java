package org.openmarkov.core.model.network;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.BasicOperations;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

public class UtilityOperations {

	/**
	 * Transform a multicriteria net into an unicriterion net. All the utility
	 * potentials are adjusted according to the scales defined in their
	 * criteria.
	 * 
	 * @param probNet
	 */
	public static void transformToUnicriterion(ProbNet probNet) {
		for (Node utilityNode : probNet.getNodes(NodeType.UTILITY)) {
			if (utilityNode.getVariable().getDecisionCriterion() != null) {
				// Get the actual criterion scale
				double scale = utilityNode.getVariable().getDecisionCriterion().getUnicriteriaScale();
				if (scale != 0) {
					// Transform the potential with the scale
					Potential potential = utilityNode.getPotentials().get(0).deepCopy(probNet);
					potential.scalePotential(scale);
					utilityNode.setPotential(potential);
				} else {
					// Remove the potential and the node
					probNet.removePotentials(utilityNode.getPotentials());
					probNet.removeNode(utilityNode);

				}

			}
		}
	}
	
	/**
	 * Transform the probNet scaling the utility potentials with the cost-effectiveness scale
	 * @param probNet transformed probNet
	 */
	public static void applyCEUtilityScaling(ProbNet probNet){
		for (Node utilityNode : probNet.getNodes(NodeType.UTILITY)) {
			if (utilityNode.getVariable().getDecisionCriterion() != null) {
				// Save the actual criterion scale
				double scale = utilityNode.getVariable().getDecisionCriterion().getCeScale();
				if (scale != 0) {
					// Transform the potential with the scale
					Potential potential = utilityNode.getPotentials().get(0).deepCopy(probNet);
					potential.scalePotential(scale);
					utilityNode.setPotential(potential);
				} else {
					// Remove the potential and the node
					probNet.removePotentials(utilityNode.getPotentials());
					probNet.removeNode(utilityNode);
				}
			}
		}
	}

	/**
	 * Left Riemann sum defined in Elbasha 2016
	 * @param values Array of values where each value represents the value of the cycle defined by the index in the array
	 * @param lenghtOfCycle Number of cycles in a year (For example, with monthly cycles, n = 12)
	 * @return result of applying the Left Riemann sum
	 */
	public static double applyLeftRiemannSum (double [] values, int lenghtOfCycle) {
		double[] newValues = new double[values.length];
		double summatory = 0;
		int numberOfCycles = values.length/lenghtOfCycle;
		for (int k = 1; k < numberOfCycles; k++) {
			newValues[k] = values[k-1];
			summatory += newValues[k];
		}

		summatory = (1.0/lenghtOfCycle)*summatory;
		return summatory;
	}

	/**
	 * Right Riemann sum defined in Elbasha 2016
	 * @param values Array of values where each value represents the value of the cycle defined by the index in the array
	 * @param lenghtOfCycle Number of cycles in a year (For example, with monthly cycles, n = 12)
	 * @return result of applying the Right Riemann sum
	 */
	public static double applyRightRiemannSum (double [] values, int lenghtOfCycle) {
		double[] newValues = new double[values.length];
		double summatory = 0;
		int numberOfCycles = values.length/lenghtOfCycle;
		for (int k = 1; k < numberOfCycles; k++) {
			newValues[k] = values[k];
			summatory += values[k];
		}

		summatory = (1.0/lenghtOfCycle)*summatory;
		return summatory;
	}

	/**
	 * Half-cycle correction: Trapezoidal rule defined in Elbasha 2016 (is the same than the life-table method)
	 * @param values Array of values where each value represents the value of the cycle defined by the index in the array
	 * @param lenghtOfCycle Number of cycles in a year (For example, with monthly cycles, n = 12)
	 * @return result of applying the trapezoidal rule
	 */
	public static double applyTrapezoidalRule (double [] values, int lenghtOfCycle) {
		double[] newValues = new double[values.length];
		double summatory = 0;
		int numberOfCycles = values.length/lenghtOfCycle;
		for (int k = 1; k < numberOfCycles; k++) {
			newValues[k] = (1.0/(2*lenghtOfCycle))*(values[k-1]+values[k]);
			summatory += newValues[k];
		}
		return summatory;
	}


}
