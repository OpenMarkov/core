package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;

public class DynamicLimidType extends NetworkType
{
    private static DynamicLimidType instance = null;

    // Constructor
    private DynamicLimidType ()
    {
        super();
        
        overwriteConstraintBehavior (OnlyAtemporalVariables.class, ConstraintBehavior.NO);
        overwriteConstraintBehavior (OnlyTemporalVariables.class, ConstraintBehavior.YES);
    }

    // Methods
    public static DynamicLimidType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new DynamicLimidType ();
        }
        return instance;
    }
    
    /** @return String "DynamicLIMID" */
    public String toString() {
    	return "DynamicLIMID";
    }
    
}


