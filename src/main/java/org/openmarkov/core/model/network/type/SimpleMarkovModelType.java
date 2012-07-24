/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/


package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;
import org.openmarkov.core.model.network.type.plugin.ProbNetType;

@ProbNetType(name="SMM")
public class SimpleMarkovModelType extends NetworkType
{
    // Attributes
    private static SimpleMarkovModelType instance = null;

    // Constructor
    private SimpleMarkovModelType ()
    {
        super ();
        overrideConstraintBehavior (OnlyAtemporalVariables.class, ConstraintBehavior.NO);
        overrideConstraintBehavior (OnlyTemporalVariables.class, ConstraintBehavior.NO);
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
    	return "SIMPLE_MARKOV_MODEL";
    }
    
}
