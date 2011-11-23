
package org.openmarkov.core.model.network.type;

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
    
    public boolean isApplicableConstraint(PNConstraint constraint)
    {
        return (constraints.get (constraint) != ConstraintBehavior.NO);
    }
    
    public HashMap<PNConstraint, ConstraintBehavior> getConstraints()
    {
        return constraints;
    }
    
    /** @return An identifier that can be used in exception messages or text 
     * files. <code>String</code>*/
    public abstract String toString();
    
}
