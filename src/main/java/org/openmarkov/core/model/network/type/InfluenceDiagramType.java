
package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.AllChanceVariablesHaveChancePotentials;
import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoCycles;
import org.openmarkov.core.model.network.constraint.NoSelfLoops;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.UtilityNodes;

public class InfluenceDiagramType extends NetworkType
{
    private static InfluenceDiagramType instance= null;

    // Constructor
    private InfluenceDiagramType ()
    {
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
    public static InfluenceDiagramType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new InfluenceDiagramType ();
        }
        return instance;
    }
}
