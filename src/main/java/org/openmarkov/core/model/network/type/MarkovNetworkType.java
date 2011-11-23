package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;

public class MarkovNetworkType extends NetworkType
{
    private static MarkovNetworkType instance = null;

    // Constructor
    private MarkovNetworkType ()
    {
        super();
        constraints.put (new OnlyChanceNodes (), ConstraintBehavior.YES);
        constraints.put (new OnlyDirectedLinks (), ConstraintBehavior.NO);
        constraints.put (new OnlyUndirectedLinks (), ConstraintBehavior.YES);        
    }

    // Methods
    public static MarkovNetworkType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new MarkovNetworkType ();
        }
        return instance;
    }

    /** @return String "MarkovNetwork" */
    public String toString() {
    	return "MarkovNetwork";
    }
    
}

