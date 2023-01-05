package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import javax.swing.*;
import java.util.List;

/**
 * This constrain only allows self loops in event nodes. Self loop is a directed edge which the same origin and destination
 * @author cmyago - cmyago adapted it from NoSelfLoop - 31/12/2019 - this constraint is only used in DESNets.
 * 04/10/2023 FI
 * @version 1.0 - self-loops only in Event nodes
 * @version 1.1 - self-loops in Chance and Event nodes - 05/04/2020
 */
@Constraint(name = "OnySelfLoopsWithEventAndChanceNodes", defaultBehavior = ConstraintBehavior.NO)
public class OnlySelfLoopsWithEventAndChanceNodes extends PNConstraint {

	/**
	 * This method checks if edit satisfies the constrain of no self-loops excepting in Event nodes and Chance nodes
	 * @param probNet <code>ProbNet</code> the probNet to be checked
	 * @param edit    <code>PNEdit</code>
	 * @return true if satisfies the constrain, false otherwise
	 * @throws NonProjectablePotentialException
	 * @throws WrongCriterionException
	 */
	@Override public boolean checkEdit(ProbNet probNet, PNEdit edit)
			throws NonProjectablePotentialException, WrongCriterionException {
		//Should be checked when changing a link
		List<PNEdit> edits = UtilConstraints.getSimpleEditsByType(edit, AddLinkEdit.class);
		for (PNEdit simpleEdit : edits) {
			Variable originVariable = ((AddLinkEdit) simpleEdit).getVariable1();
			Variable destinationVariable = ((AddLinkEdit) simpleEdit).getVariable2();
			if ((originVariable.equals(destinationVariable)) && ( (probNet.getNode(originVariable).getNodeType() != NodeType.EVENT) && (probNet.getNode(originVariable).getNodeType() != NodeType.CHANCE ) )){
				JOptionPane.showMessageDialog(null, getMessage(), "Constrain violation",JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	/**
	 * This method checks if probNet satisfies the constrain of no self-loops excepting in Event and Chance nodes
	 * @param probNet <code>ProbNet</code> probNet to be checked
	 * @return true if if satisfies the constrain, false otherwise
	 */
	@Override public boolean checkProbNet(ProbNet probNet) {
		for (Node node : probNet.getNodes()) {
			if ( probNet.isChild(node, node) &&  !((node.getNodeType() == NodeType.EVENT ) ||(node.getNodeType() == NodeType.CHANCE ) )) {
				JOptionPane.showMessageDialog(null, this, getMessage(),JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	/**
	 * This method shows an error message when the constrain is violated
	 * @return
	 */
	@Override protected String getMessage() {
		return "Self loops only allowed in event nodes ";
	}
}
