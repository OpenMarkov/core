
package org.openmarkov.core.model.network.type;

import java.util.ArrayList;
import java.util.HashMap;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.PNConstraint;

public abstract class NetworkType
{
    protected HashMap<PNConstraint, ConstraintBehavior> constraints;
    
    public NetworkType()
    {
        constraints = new HashMap<PNConstraint, ConstraintBehavior> ();
    }
    
    public boolean isAbidingConstraint(PNConstraint constraint)
    {
        return (constraints.get (constraint) != ConstraintBehavior.NO);
    }
    
    public boolean isAbidingConstraints (ArrayList<PNConstraint> constraints)
    {
        boolean abides = true;
        for (PNConstraint constraint : this.constraints.keySet ())
            abides = abides && isAbidingConstraint (constraint);
        return abides;
    }

    public HashMap<PNConstraint, ConstraintBehavior> getConstraints()
    {
        return constraints;
    }
    
    
}
