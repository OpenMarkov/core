package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Node;

public class ThereIsNoPotentialsInNodeException extends Exception implements IBundledOpenMarkovException {
    
    public ThereIsNoPotentialsInNodeException(Node node) {
        this.node = node;
    }
    
    public final Node node;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
