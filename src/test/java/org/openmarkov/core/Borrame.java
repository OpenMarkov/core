package org.openmarkov.core;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.DistinctVariableNames;
import org.openmarkov.core.model.network.constraint.ProperUtilityPotentials;

public class Borrame {
    
    public static void main(String[] args) throws ConstraintViolatedException {
        System.out.println(new ConstraintViolatedException.LinkAlreadyExists(null, null, null));
        
        ProbNet probNet = new ProbNet();
        new ConstraintChecker(probNet)
                .addException(new ConstraintViolatedException.VariableNameIsAlreadyPresent(new DistinctVariableNames(), "A"))
                .addException(new ConstraintViolatedException.VariableNameIsAlreadyPresent(new DistinctVariableNames(), "B"))
                .addException(new ConstraintViolatedException.NetworkHasNoUtilityNodes(new ProperUtilityPotentials(), probNet))
                .addException(new ConstraintViolatedException.NetworkHasNoUtilityNodes(new ProperUtilityPotentials(), probNet))
                .buildAndThrow();
        
    }
}
