/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

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
 * @author cyago - cyago adapted it from NoSelfLoop - 31/12/2019 - this constraint is only used in DESNets
 */
@Constraint(name = "OnySelfLoopsWithEventNodes", defaultBehavior = ConstraintBehavior.NO) public class OnlySelfLoopsWithEventNodes
		extends PNConstraint {

	/**
	 * This method checks if edit satisfies the constrain of no self-loops excepting in event nodes
	 * @param probNet <code>ProbNet</code> the probNet to be checked
	 * @param edit    <code>PNEdit</code>
	 * @return true if satisfies the constrain, false otherwise
	 * @throws NonProjectablePotentialException
	 * @throws WrongCriterionException
	 */
	@Override public boolean checkEdit(ProbNet probNet, PNEdit edit)
			throws NonProjectablePotentialException, WrongCriterionException {
		List<PNEdit> edits = UtilConstraints.getSimpleEditsByType(edit, AddLinkEdit.class);
		for (PNEdit simpleEdit : edits) {
			Variable originVariable = ((AddLinkEdit) simpleEdit).getVariable1();
			Variable destinationVariable = ((AddLinkEdit) simpleEdit).getVariable2();
			if ((originVariable.equals(destinationVariable)) && (probNet.getNode(originVariable).getNodeType() != NodeType.EVENT)) {
				JOptionPane.showMessageDialog(null, null, getMessage(),JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	/**
	 *  This method checks if probNet satisfies the constrain of no self-loops excepting in event nodes
	 * @param probNet <code>ProbNet</code> probNet to be checked
	 * @return true if if satisfies the constrain, false otherwise
	 */
	@Override public boolean checkProbNet(ProbNet probNet) {
		for (Node node : probNet.getNodes()) {
			//TODO Sibling?? --> I think this can avoid dubplicate links which is good
			if ((probNet.isChild(node, node) &&  node.getNodeType() == NodeType.EVENT ) || probNet.isSibling(node, node)) {
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
