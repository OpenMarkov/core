/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PurposeEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This constrain  checks that there is only one initial and has no parents
 * @author cyago - cyago adapted it from NoSelfLoop - 26/01/2020 - this constraint is only used in DESNets
 */
@Constraint(name = "InitialNodeConstrain", defaultBehavior = ConstraintBehavior.NO)
public class OnlyOneOrphanInitialEvent extends PNConstraint {

	/**
	 * This method checks if edit satisfies the constrain of only one initial node with no parents
	 * @param probNet <code>ProbNet</code> the probNet to be checked
	 * @param edit    <code>PNEdit</code>
	 * @return true if satisfies the constrain, false otherwise
	 * @throws NonProjectablePotentialException
	 * @throws WrongCriterionException
	 */
	@Override public boolean checkEdit(ProbNet probNet, PNEdit edit)
			throws NonProjectablePotentialException, WrongCriterionException {

		//We can have AddLinkEdit or PurposeEdit
		//Check there is only one  Initial Event

		if (edit instanceof PurposeEdit){
			PurposeEdit purposeEdit =(PurposeEdit) edit;
			if (purposeEdit.getNewPurpose().equals(PurposeType.INITIAL_EVENT.getName())){
				List<Node> eventNodes= probNet.getNodes(NodeType.EVENT);
				if ( eventNodes.stream().anyMatch(node->node.getPurpose().equals(PurposeType.INITIAL_EVENT.getName()))){
					JOptionPane.showMessageDialog(null, getMessage(),"checkEdit purposeEdit" +getErrorTitle() ,JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		} else if (edit instanceof AddLinkEdit){

			AddLinkEdit addLinkEdit = (AddLinkEdit) edit;
			Node destinationNode = probNet.getNode(addLinkEdit.getVariable2());
			//InitialEvent has no parents
			if ((destinationNode.getPurpose().equals(PurposeType.INITIAL_EVENT.getName()))) {
				//When double clicking in a node an AddLinkEdit  with every no Uitily node is generated, therefore if will be several error messages when double clicking the initial node
				//Until this is fixed no messages are shown
//				JOptionPane.showMessageDialog(null, "checkEdit addlinkedit" + addLinkEdit.getVariable1().getName() + getMessage(),getErrorTitle() ,JOptionPane.ERROR_MESSAGE);
				return false;
			}

		}

		return true;
	}

	/**
	 *  This method checks if probNet satisfies the constraint
	 * @param probNet <code>ProbNet</code> probNet to be checked
	 * @return true if if satisfies the constrain, false otherwise
	 */
	@Override public boolean checkProbNet(ProbNet probNet) {

		List<Node> eventNodes =  probNet.getNodes(NodeType.EVENT);
		List<Node> initialEventList = eventNodes.stream().filter(node->node.getPurpose().equals(PurposeType.INITIAL_EVENT.getName())).collect(Collectors.toList());
		if ((initialEventList.size()>1)
			|| ((initialEventList.size()==1) && initialEventList.get(0).getNumParents()>0)) {
//			JOptionPane.showMessageDialog(null, this, getMessage(), JOptionPane.ERROR_MESSAGE);
			JOptionPane.showMessageDialog(null, this, "checkProbNet" +getMessage(), JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;

	}

	/**
	 * Returns an error message when the constraint is violated
	 * @return a String with the cause of the constraint violation
	 */
	@Override protected String getMessage() {
		return "There must be only one Initial Event with no parent";
	}

	/**
	 * Returns the title of thw constraint violation error JOptionPane
	 * @return a String with the title of the constraint violation error JOptionPane
	 */
	private String getErrorTitle(){
		return "Constraint violation";
	}

}
