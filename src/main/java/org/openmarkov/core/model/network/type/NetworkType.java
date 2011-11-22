
package org.openmarkov.core.model.network.type;

import java.util.HashMap;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.ConstraintManager;
import org.openmarkov.core.model.network.constraint.ConstraintPool;
import org.openmarkov.core.model.network.constraint.PNConstraint;

public abstract class NetworkType
{
    protected HashMap<Class<? extends PNConstraint>, ConstraintBehavior> constraints;
    
    public NetworkType()
    {
        constraints = new HashMap<Class<? extends PNConstraint>, ConstraintBehavior> ();
    }
    
    public boolean isApplicableConstraint(PNConstraint constraint)
    {
        ConstraintBehavior behavior = (constraints.get (constraint.getClass ()) != null) ? constraints.get (constraint.getClass ())
                                                                                        : ConstraintManager.getUniqueInstance ().getDefaultBehavior (constraint.getClass ());
        return (behavior != ConstraintBehavior.NO);
    }
    
    public void overwriteConstraintBehavior(Class<? extends PNConstraint> constraintClass, ConstraintBehavior behavior)
    {
        constraints.put (constraintClass, behavior);
    }    
    
    public HashMap<Class<? extends PNConstraint>, ConstraintBehavior> getOverwrittenConstraints ()
    {
        return constraints;
    }
    
    
    
}
