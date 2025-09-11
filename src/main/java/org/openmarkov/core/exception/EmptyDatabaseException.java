package org.openmarkov.core.exception;

public class EmptyDatabaseException extends Exception implements IBundledOpenMarkovException {
    public final String source;
    
    public EmptyDatabaseException(String source) {
        this.source = source;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
