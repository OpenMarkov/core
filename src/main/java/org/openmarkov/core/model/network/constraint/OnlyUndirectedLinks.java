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

@Constraint (name = "OnlyUndirectedLinks", defaultBehavior = ConstraintBehavior.NO)
public class OnlyUndirectedLinks extends PNConstraint {

	// Attributes.
    private String explanation;

    @Override
	public boolean checkProbNet(ProbNet probNet) {
		ArrayList<Node> nodes = probNet.getGraph().getNodes();
		for (Node node : nodes) {
			// Only check children because with this is enough
			// to look for directed links
			if (node.getChildren().size() != 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkEvent(UndoableEditEvent event) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = 
			UtilConstraints.getEditsType(event, AddLinkEdit.class);
		for (PNEdit edit : edits) {
			if (((AddLinkEdit)edit).isDirected()) {
				AddLinkEdit addLink = (AddLinkEdit)edit;
				explanation = new String(
					addLink.getVariable1() + " --> " + addLink.getVariable2());
				return false;
			}
		}
		return true;
	}

    @Override
    protected String getMessage ()
    {
        return explanation + ". no directed links allowed";
    }

}
