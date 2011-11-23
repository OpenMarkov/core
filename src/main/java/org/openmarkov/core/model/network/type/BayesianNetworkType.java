
package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;

public class BayesianNetworkType extends NetworkType
{
    private static BayesianNetworkType instance = null;

    // Constructor
    private BayesianNetworkType ()
    {
        super();
        constraints.put (new OnlyChanceNodes(),
                         ConstraintBehavior.YES);
    }

    // Methods
    public static BayesianNetworkType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new BayesianNetworkType ();
        }
        return instance;
    }
    
    /** @return String "BayesianNetwork". */
    public String toString() {
    	return "BayesianNetwork";
    }
    
}
