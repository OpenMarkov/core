
package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;

public class SimpleMarkovModelType extends NetworkType
{
    // Attributes
    private static SimpleMarkovModelType instance = null;

    // Constructor
    private SimpleMarkovModelType ()
    {
        super ();
        overwriteConstraintBehavior (OnlyAtemporalVariables.class, ConstraintBehavior.NO);
        overwriteConstraintBehavior (OnlyTemporalVariables.class, ConstraintBehavior.YES);
    }

    // Methods
    public static SimpleMarkovModelType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new SimpleMarkovModelType ();
        }
        return instance;
    }

    /** @return String "SimpleMarkovModel" */
    public String toString() {
    	return "SimpleMarkovModel";
    }
    
}
