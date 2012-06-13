package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.prm.AddInstanceEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint (name = "NoInstances", defaultBehavior = ConstraintBehavior.YES)
public class NoInstances extends PNConstraint {

	@Override
	public boolean checkProbNet(ProbNet probNet) {
		return probNet.getInstances().isEmpty();
	}

	@Override
	public boolean checkEdit(ProbNet probNet, PNEdit edit)
			throws NotEnoughMemoryException, NonProjectablePotentialException,
			WrongCriterionException {
		ArrayList<PNEdit> edits = UtilConstraints.getEditsType (edit, AddInstanceEdit.class);
		return edits.isEmpty();
	}
	
	@Override
	protected String getMessage() {
		return "there should be no instances";
	}
	

}
