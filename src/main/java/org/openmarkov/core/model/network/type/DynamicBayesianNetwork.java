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
        overwriteConstraintBehavior (OnlyChanceNodes.class, ConstraintBehavior.YES);
        overwriteConstraintBehavior (OnlyAtemporalVariables.class, ConstraintBehavior.NO);
        overwriteConstraintBehavior (OnlyTemporalVariables.class, ConstraintBehavior.YES);
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
}

