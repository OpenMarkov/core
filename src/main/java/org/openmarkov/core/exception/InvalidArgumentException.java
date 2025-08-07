package org.openmarkov.core.exception;

public class InvalidArgumentException extends BundledOpenMarkovException {
    
    public final Object value;
    public final String argumentName;
    public final String reason;
    
    public InvalidArgumentException(Object value, String argumentName, String reason) {
        this.value = value;
        this.argumentName = argumentName;
        this.reason = reason;
    }
    
}
