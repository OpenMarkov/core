package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint (name = "OnlyDirectedLinks", defaultBehavior = ConstraintBehavior.YES)
public class OnlyDirectedLinks extends PNConstraint {

	private String explanation;

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<Node> nodes = probNet.getGraph().getNodes();
		for (Node node : nodes) {
			if (node.getSiblings().size() != 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkEvent(UndoableEditEvent event)
	throws NotEnoughMemoryException, NonProjectablePotentialException,
			WrongCriterionException {
		ArrayList<PNEdit> edits = UtilConstraints.getEditsType(event,
				AddLinkEdit.class);
		for (PNEdit edit : edits) {
			if (!((AddLinkEdit) edit).isDirected()) {
				AddLinkEdit addLink = (AddLinkEdit)edit;
				explanation = new String(
					addLink.getVariable1() + " --- " + addLink.getVariable2());
				return false;
			}
		}
		return true;
	}

    @Override
    protected String getMessage ()
    {
        return explanation + ". Only directed links allowed";
    }

}
