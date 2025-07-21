package org.openmarkov.core.exception;

/**
 * Exceptions of this class should never happen
 *
 * @author jrico
 */
public class UnreacheableException extends RuntimeException {
    
    public UnreacheableException(Exception e) {
        super(e);
    }
    
    public UnreacheableException(String message) {
        super(message);
    }
    
}
