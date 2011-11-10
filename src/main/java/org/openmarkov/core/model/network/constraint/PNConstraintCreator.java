package org.openmarkov.core.model.network.constraint;

import java.lang.reflect.Method;

import org.openmarkov.core.exception.ConstraintException;

/****
 * Gets the unique instance of a PNConstraint by class name
 * 
 * @author caroline
 * 
 */
public class PNConstraintCreator {

	private static final String PACKAGE_NAME = "openmarkov.networks.constraints.";

	/*****
	 * Method that returns the unique instance of PNConstraint with the given class name using reflection
	 * @param className name of the PNConstraint class
	 * @return <code>PNConstraint</code> instance of PNconstraint  
	 * @throws <code>ConstraintException</code>
	 */
	public static PNConstraint createPNConstraint(String className)
			throws ConstraintException {

		PNConstraint constraint = null;
		try {
			Class<PNConstraint> pnClass = (Class<PNConstraint>) Class
					.forName(PACKAGE_NAME + className);
			Method method = pnClass.getMethod("getUniqueInstance", new Class[0]);
			constraint = (PNConstraint) method.invoke(pnClass, new Object[0]);
		} catch (ClassNotFoundException e) {
			throw (new ConstraintException(className, "PNConstraint for Classname " + className
					+ " does not exist."));
		
		} catch (NoSuchMethodException e) {
			throw (new ConstraintException(className, "PNConstraint for Classname " + className
					+ "does not implement getUniqueInstance."));
		} catch (Exception e) {
			throw (new ConstraintException(className, "Error getting instance of PNConstraint for Classname " + className
					+ "."));
		} 

		return constraint;
	}

}
