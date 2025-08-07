package org.openmarkov.core.exception;

public class EmptyDatabaseException extends BundledOpenMarkovException {
    public final String source;
    
    public EmptyDatabaseException(String source) {
        this.source = source;
    }
}
