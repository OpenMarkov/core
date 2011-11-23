package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;

public class DynamicBayesianNetwork extends NetworkType
{
    private static DynamicBayesianNetwork instance = null;

    // Constructor
    private DynamicBayesianNetwork ()
    {
        super();
        constraints.put (new OnlyChanceNodes (), ConstraintBehavior.YES);
        constraints.put (new OnlyAtemporalVariables (), ConstraintBehavior.NO);
        constraints.put (new OnlyTemporalVariables (), ConstraintBehavior.YES);
    }

    // Methods
    public static DynamicBayesianNetwork getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new DynamicBayesianNetwork ();
        }
        return instance;
    }
    
    /** @return String "DynamicBayesianNetwork" */
    public String toString() {
    	return "DynamicBayesianNetwork";
    }

}

