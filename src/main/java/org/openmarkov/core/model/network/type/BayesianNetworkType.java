
package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.AllChanceVariablesHaveChancePotentials;
import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoCycles;
import org.openmarkov.core.model.network.constraint.NoSelfLoops;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;

public class BayesianNetworkType extends NetworkType
{
    private static BayesianNetworkType instance = null;

    // Constructor
    private BayesianNetworkType ()
    {
        constraints.put (NoCycles.getUniqueInstance (), ConstraintBehavior.YES);
        constraints.put (AllChanceVariablesHaveChancePotentials.getUniqueInstance (),
                         ConstraintBehavior.YES);
        constraints.put (NoSelfLoops.getUniqueInstance (),
                         ConstraintBehavior.YES);
        constraints.put (OnlyChanceNodes.getUniqueInstance (),
                         ConstraintBehavior.YES);
        constraints.put (OnlyDirectedLinks.getUniqueInstance (),
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
}
