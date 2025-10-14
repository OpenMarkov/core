package org.openmarkov.core.exception;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.stringformat.LocalizationFormatter;

public non-sealed class ConstraintViolatedException extends DoEditException {
    public final PNConstraint constraint;
    
    public ConstraintViolatedException(PNConstraint constraint) {
        this.constraint = constraint;
    }
    
    @Override public @NotNull String localize(LocalizationFormatter formatter) {
        String constraintExceptionMessage = Localizable.localize(this, formatter, ConstraintViolatedException.class.getName());
        if (this.getClass() == ConstraintViolatedException.class) {
            return constraintExceptionMessage;
        }
        String concreteExceptionMessage = Localizable.localize(this, formatter, this.getClass().getName());
        return constraintExceptionMessage + System.lineSeparator() + concreteExceptionMessage;
    }
    
    @Override public String toString() {
        return this.localize();
    }
    
    public static class LinkAlreadyExists extends ConstraintViolatedException {
        public LinkAlreadyExists(DistinctLinks constraint, Node from, Node to) {
            super(constraint);
            this.from = from;
            this.to = to;
        }
        
        private final Node from;
        private final Node to;
    }
    
    public static class VariableNameIsAlreadyPresent extends ConstraintViolatedException {
        //There is already a variable named {name} in the network.
        public VariableNameIsAlreadyPresent(DistinctVariableNames constraint, String name) {
            super(constraint);
            this.name = name;
        }
        
        private final String name;
    }
    
    public static class NodeCannotHaveMoreParents extends ConstraintViolatedException {
        //Node {node} already has {parentsSize} parents. No more can be added.
        public NodeCannotHaveMoreParents(MaxNumParents constraint, Node node, int parentsSize) {
            super(constraint);
            this.node = node;
            this.parentsSize = parentsSize;
        }
        
        private final Node node;
        private final int parentsSize;
    }
    
    public static class ModelDoesntAllowAddingLink extends ConstraintViolatedException {
        //Model doesn't allow adding link from {from} to {to}.
        public ModelDoesntAllowAddingLink(ModelNetworkConstraint constraint, Variable from, Variable to) {
            super(constraint);
            this.from = from;
            this.to = to;
        }
        
        private final Variable from;
        private final Variable to;
    }
    
    public static class ModelDoesntAllowInvertingLink extends ConstraintViolatedException {
        //Model doesn't allow inverting link from {from} to {to}.
        public ModelDoesntAllowInvertingLink(ModelNetworkConstraint constraint, Variable from, Variable to) {
            super(constraint);
            this.from = from;
            this.to = to;
        }
        
        private final Variable from;
        private final Variable to;
    }
    
    public static class ModelDoesntAllowRemovingLink extends ConstraintViolatedException {
        //Model doesn't allow removing link from {from} to {to}.
        public ModelDoesntAllowRemovingLink(ModelNetworkConstraint constraint, Variable from, Variable to) {
            super(constraint);
            this.from = from;
            this.to = to;
        }
        
        private final Variable from;
        private final Variable to;
    }
}
