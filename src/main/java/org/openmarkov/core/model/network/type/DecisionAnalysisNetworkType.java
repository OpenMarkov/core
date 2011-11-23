package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoRevelationArc;

public class DecisionAnalysisNetworkType extends NetworkType
{
    private static DecisionAnalysisNetworkType instance = null;

    // Constructor
    private DecisionAnalysisNetworkType ()
    {
        super ();
        constraints.put (new NoRevelationArc (), ConstraintBehavior.NO);
    }

    // Methods
    public static DecisionAnalysisNetworkType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new DecisionAnalysisNetworkType ();
        }
        return instance;
    }
    
    /** @return String "DecisionAnalysisNetwork". */
    public String toString() {
    	return "DecisionAnalysisNetwork";
    }
    
}
