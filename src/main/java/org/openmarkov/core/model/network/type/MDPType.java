package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.AllChanceVariablesHaveChancePotentials;
import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.NoSelfLoop;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.UtilityNodes;

public class MDPType extends NetworkType
{
    private static MDPType instance = null;

    // Constructor
    private MDPType ()
    {
        super ();
        constraints.put (new NoCycle (), ConstraintBehavior.YES);
        constraints.put (new AllChanceVariablesHaveChancePotentials (),
                         ConstraintBehavior.YES);
        constraints.put (new NoSelfLoop (), ConstraintBehavior.YES);
        constraints.put (new OnlyDirectedLinks (), ConstraintBehavior.YES);
        constraints.put (new UtilityNodes (), ConstraintBehavior.YES);
    }

    // Methods
    public static MDPType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new MDPType ();
        }
        return instance;
    }
    
    /** @return String "MDP" */
    public String toString() {
    	return "MDP";
    }
    

}

