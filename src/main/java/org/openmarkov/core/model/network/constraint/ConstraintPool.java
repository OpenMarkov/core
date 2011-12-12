/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.constraint;

import java.util.HashMap;

import org.openmarkov.core.exception.ConstraintException;

/****
 * Gets the unique instance of a PNConstraint by class name
 * 
 * @author caroline
 * 
 */
public class ConstraintPool {

    private static ConstraintPool instance = null;
    private HashMap<Class<? extends PNConstraint>, PNConstraint> constraintInstances;
    
    private ConstraintPool(){
        constraintInstances = new HashMap<Class<? extends PNConstraint>, PNConstraint>();
    }
    
    public static ConstraintPool getUniqueInstance()
    {
        if(instance == null)
        {
            instance = new ConstraintPool();
        }
        
        return instance;
    }
    
	/*****
	 * Method that returns the unique instance of PNConstraint with the given class name using reflection
	 * @param className name of the PNConstraint class
	 * @return <code>PNConstraint</code> instance of PNconstraint  
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws <code>ConstraintException</code>
	 */
	public PNConstraint getConstraint(Class<? extends PNConstraint> constraintClass) {
	    
	    PNConstraint instance = constraintInstances.get (constraintClass);
	    
	    if(instance == null)
	    {
		    try
            {
                instance = constraintClass.newInstance ();
            }
            catch (InstantiationException e)
            {
                e.printStackTrace ();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
		    constraintInstances.put (constraintClass, instance);
	    }
		
		return instance;
	}

}
