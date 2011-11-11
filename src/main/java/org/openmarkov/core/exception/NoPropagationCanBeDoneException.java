package org.openmarkov.core.exception;

import java.util.ArrayList;

import org.openmarkov.core.model.network.constraint.PNConstraint;

/** Thrown when propagation cannot be done.*/
@SuppressWarnings("serial")
public class NoPropagationCanBeDoneException extends Exception{

    private ArrayList<PNConstraint> constraints;
    
    public NoPropagationCanBeDoneException(ArrayList<PNConstraint> constraints)
    {
        this.constraints = constraints;
    }
    
    /**
     * Returns the constraints.
     * @return the constraints.
     */
    public ArrayList<PNConstraint> getConstraints ()
    {
        return constraints;
    }    
    
    
}
