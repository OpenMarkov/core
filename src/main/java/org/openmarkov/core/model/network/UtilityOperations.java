package org.openmarkov.core.model.network;

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
		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
		for (Node utilityNode : utilityNodes) {

			// Save the actual criterion scale
			double scale = utilityNode.getVariable().getDecisionCriterion()
					.getUnicriteriaScale();

			// Gets the main conversion unit (main criterion)
			String mainUnit = probNet.getInferenceOptions()
					.getMultiCriteriaOptions().getMainUnit();

			// Sets the main criterion as the node criterion
			for (Criterion probNetCriterion : probNet.getDecisionCriteria()) {
				if (probNetCriterion.getCriterionUnit().equals(mainUnit)) {
					utilityNode.getVariable().setDecisionCriterion(
							probNetCriterion);
					break;
				}
			}

			// Transform the potential with the scale
			Potential potential = utilityNode.getPotentials().get(0);
			potential.scalePotential(scale);
			/* 
			double[] potentialValues = ((TablePotential) potential).getValues();
			for (int j = 0; j < potentialValues.length; j++) {
				potentialValues[j] = potentialValues[j] * scale;
			}*/
		}
	}
	
	/**
	 * Transform the probNet scaling the utility potentials with the cost-effectiveness scale
	 * @param probNet transformed probNet
	 */
	public static void applyCEUtilityScaling(ProbNet probNet){
		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
		for (Node utilityNode : utilityNodes) {

			// Save the actual criterion scale
			double scale = utilityNode.getVariable().getDecisionCriterion()
					.getCeScale();

			// Transform the potential with the scale
			Potential potential = utilityNode.getPotentials().get(0);
			potential.scalePotential(scale);
			/*
			double[] potentialValues = ((TablePotential) potential).getValues();
			for (int j = 0; j < potentialValues.length; j++) {
				potentialValues[j] = potentialValues[j] * scale;
			}*/
		}
	}

	/**
	 * This method remove all terminal utility nodes (without childrens) which
	 * have selected "Null" as the cost effectiveness criterion. The loop
	 * ensures that all terminal utility nodes have been deleted.
	 * 
	 * @param probNet
	 * @return probNet without that utility nodes
	 */
	public static ProbNet removeTerminalNullCostEffectivenessNodes(
			ProbNet probNet) {

		ProbNet copyProbNet = probNet.copy();
		boolean stillHaveNodes;

		do {
			stillHaveNodes = false;
			List<Node> terminalUtiliyNodes = BasicOperations
					.getTerminalUtilityNodes(copyProbNet);

			for (Node terminalUtiliyNode : terminalUtiliyNodes) {
				if (terminalUtiliyNode.getVariable().getDecisionCriterion()
						.getCECriterion() == CECriterion.Null) {
					copyProbNet.removeNode(terminalUtiliyNode);
					stillHaveNodes = true;
				}
			}

		} while (stillHaveNodes);

		return copyProbNet;
	}
}
