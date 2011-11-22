package org.openmarkov.core.exception;

@SuppressWarnings("serial")
public class ConstraintException extends Exception {

	
	public ConstraintException (String className,String message)
	{
		super("Error getting Instance for " + className
					+ " . " + message);
		
	}
	
	
}
