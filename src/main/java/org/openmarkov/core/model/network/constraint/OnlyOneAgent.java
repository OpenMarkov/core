package org.openmarkov.core.model.network.constraint;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint (name = "OnlyOneAgent", defaultBehavior = ConstraintBehavior.YES)
public class OnlyOneAgent extends PNConstraint {

    @Override
    public boolean checkProbNet(ProbNet probNet) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean checkEvent(UndoableEditEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected String getMessage ()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
