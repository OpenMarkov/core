package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.AllChanceVariablesHaveChancePotentials;
import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoCycles;
import org.openmarkov.core.model.network.constraint.NoSelfLoops;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.UtilityNodes;

public class POMDPType extends NetworkType
{
    private static POMDPType instance = null;

    // Constructor
    private POMDPType ()
    {
        super();
        
        constraints.put (NoCycles.getUniqueInstance (), ConstraintBehavior.YES);
        constraints.put (AllChanceVariablesHaveChancePotentials.getUniqueInstance (),
                         ConstraintBehavior.YES);
        constraints.put (NoSelfLoops.getUniqueInstance (),
                         ConstraintBehavior.YES);
        constraints.put (OnlyDirectedLinks.getUniqueInstance (),
                         ConstraintBehavior.YES);
        constraints.put (UtilityNodes.getUniqueInstance (),
                         ConstraintBehavior.YES);
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
}

