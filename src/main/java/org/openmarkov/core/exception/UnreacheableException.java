package org.openmarkov.core.exception;

/**
 * Exceptions of this class should never
 */
public class UnreacheableException extends RuntimeException {
    
    public UnreacheableException(InvalidNetworkTypeException e) {
        super(e);
    }
    
}
