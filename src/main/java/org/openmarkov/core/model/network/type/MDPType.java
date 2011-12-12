/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

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
        overwriteConstraintBehavior (NoCycle.class, ConstraintBehavior.YES);
        overwriteConstraintBehavior (AllChanceVariablesHaveChancePotentials.class,
                         ConstraintBehavior.YES);
        overwriteConstraintBehavior (NoSelfLoop.class, ConstraintBehavior.YES);
        overwriteConstraintBehavior (OnlyDirectedLinks.class, ConstraintBehavior.YES);
        overwriteConstraintBehavior (UtilityNodes.class, ConstraintBehavior.YES);
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

