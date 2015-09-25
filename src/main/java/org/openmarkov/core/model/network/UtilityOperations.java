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
				// Transform the potential with the scale
				Potential potential = utilityNode.getPotentials().get(0).deepCopy(probNet);
				potential.scalePotential(scale);
				utilityNode.setPotential(potential);
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
				// Transform the potential with the scale
				Potential potential = utilityNode.getPotentials().get(0).deepCopy(probNet);
				potential.scalePotential(scale);
				utilityNode.setPotential(potential);
			}
		}
	}


//	public static void unicriterionUtilityUnscaling(ProbNet probNet){
//		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
//		for (Node utilityNode : utilityNodes) {
//			if (utilityNode.getVariable().getDecisionCriterion() != null) {
//				// Save the actual criterion scale
//				double scale = 1 / utilityNode.getVariable().getDecisionCriterion().getUnicriteriaScale();
//				// Transform the potential with the scale
//				Potential potential = utilityNode.getPotentials().get(0);
//				potential.scalePotential(scale);
//			}
//		}
//	}
//
//	public static void ceUtilityUnscaling(ProbNet probNet){
//		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
//		for (Node utilityNode : utilityNodes) {
//			if (utilityNode.getVariable().getDecisionCriterion() != null) {
//				// Save the actual criterion scale
//				double scale = 1 / utilityNode.getVariable().getDecisionCriterion().getCeScale();
//				// Transform the potential with the scale
//				Potential potential = utilityNode.getPotentials().get(0);
//				potential.scalePotential(scale);
//			}
//		}
//	}

	/**
	 * This method remove all terminal utility nodes (without childrens) which
	 * have selected "Null" as the cost effectiveness criterion. The loop
	 * ensures that all terminal utility nodes have been deleted.
	 * 
	 * @param probNet
	 * @return probNet without that utility nodes
	 */
	/*
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
	*/
}
