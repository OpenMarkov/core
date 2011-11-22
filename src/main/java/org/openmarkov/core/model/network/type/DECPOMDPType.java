package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyOneAgent;

public class DECPOMDPType extends POMDPType
{
    private static DECPOMDPType instance = null;

    // Constructor
    private DECPOMDPType ()
    {
        super();
        
        overwriteConstraintBehavior (OnlyOneAgent.class, ConstraintBehavior.NO);
    }

    // Methods
    public static DECPOMDPType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new DECPOMDPType ();
        }
        return instance;
    }
}

