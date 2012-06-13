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
import org.openmarkov.core.model.network.constraint.NoInstances;

public class OOBNType extends NetworkType
{
    private static OOBNType instance = null;

    // Constructor
    protected OOBNType ()
    {
        super ();
        overrideConstraintBehavior (NoInstances.class, ConstraintBehavior.NO);
    }

    // Methods
    public static OOBNType getUniqueInstance ()
    {
        if (instance == null)
        {
            instance = new OOBNType ();
        }
        return instance;
    }

    /** @return String "OOBN" */
    public String toString() {
    	return "OOBN";
    }
    
}

