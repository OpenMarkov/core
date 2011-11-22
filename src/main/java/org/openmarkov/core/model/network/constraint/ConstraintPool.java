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

    private static HashMap<Class<? extends PNConstraint>, PNConstraint> instances = new HashMap<Class<? extends PNConstraint>, PNConstraint>();
	/*****
	 * Method that returns the unique instance of PNConstraint with the given class name using reflection
	 * @param className name of the PNConstraint class
	 * @return <code>PNConstraint</code> instance of PNconstraint  
	 * @throws <code>ConstraintException</code>
	 */
	public PNConstraint getInstance(Class<? extends PNConstraint> constraintClass)
			throws ConstraintException {
	    
	    PNConstraint instance = instances.get (constraintClass);
	    
	    if(instance == null)
	    {
    		try
            {
    		    instance = constraintClass.newInstance ();
    		    instances.put (constraintClass, instance);
            }
            catch (InstantiationException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
	    }
		
		return instance;
	}

}
