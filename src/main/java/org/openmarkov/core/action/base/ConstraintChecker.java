package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Consumer;

public class ConstraintChecker {
    
    private final HashSet<ConstraintViolatedException> exceptions;
    private final ProbNet probNet;
    
    public ConstraintChecker(ProbNet probNet) {
        this.probNet = probNet;
        this.exceptions = new HashSet<>();
    }
    
    public ConstraintChecker addException(ConstraintViolatedException exception) {
        this.exceptions.add(exception);
        return this;
    }
    
    public <Constraint extends PNConstraint> ConstraintChecker checkConstraint(Class<Constraint> constraintClass, ConstraintCheck<? super Constraint> checker) {
        Iterator<Constraint> constraints = probNet.getConstraintsOfClass(constraintClass).iterator();
        while (constraints.hasNext()) {
            checker.verify(constraints.next());
        }
        return this;
    }
    
    public ConstraintChecker check(Consumer<? super ArrayList<ConstraintViolatedException>> checker) {
        ArrayList<ConstraintViolatedException> exceptions = new ArrayList<>();
        checker.accept(exceptions);
        this.exceptions.addAll(exceptions);
        return this;
    }
    
    public void buildAndThrow() throws ConstraintViolatedException {
        switch (this.exceptions.size()) {
            case 0 -> {
            }
            case 1 -> throw this.exceptions.stream().findFirst().get();
            default -> throw new ConstraintViolatedException.MultipleConstraintsViolateds(this.exceptions);
        }
    }
    
    @FunctionalInterface
    public interface ConstraintCheck<Constraint extends PNConstraint> {
        void verify(Constraint constraint);
    }
    
    
}
