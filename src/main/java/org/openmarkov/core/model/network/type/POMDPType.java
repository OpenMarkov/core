package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;

public class POMDPType extends NetworkType
{
    private static POMDPType instance = null;

    // Constructor
    protected POMDPType ()
    {
        super ();
        constraints.put (new OnlyAtemporalVariables (), ConstraintBehavior.NO);
        constraints.put (new OnlyTemporalVariables (), ConstraintBehavior.YES);
    }

    // Methods
    public static POMDPType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new POMDPType ();
        }
        return instance;
    }

    /** @return String "POMDP" */
    public String toString() {
    	return "POMDP";
    }
    
}

