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
        overwriteConstraintBehavior (OnlyChanceNodes.class, ConstraintBehavior.YES);
        overwriteConstraintBehavior (OnlyDirectedLinks.class, ConstraintBehavior.NO);
        overwriteConstraintBehavior (OnlyUndirectedLinks.class, ConstraintBehavior.YES);        
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

