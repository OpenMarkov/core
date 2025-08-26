package org.openmarkov.core.exception;

import org.openmarkov.java.exceptionUtils.ThrowableUtils;

/**
 * Exceptions of this class should never happen, but serve to still get a stacktrace in case they do happen due to a
 * programming error.
 * <p>
 * Calling {@link UnreacheableException#getCause()} will return the underlying cause of this exception, and it is always
 * an instance of a class different from {@link UnreacheableException}, even if you instantiated
 * {@link UnreacheableException} with another {@link UnreacheableException}.
 * <p>
 * An instance of {@link UnreacheableException} has no stacktrace, as said it is transferred to the underlying cause.
 *
 * @author jrico
 * @see ThrowableUtils#transferStackTrace(Throwable, Throwable)
 */
public class UnreacheableException extends RuntimeException {
    
    public UnreacheableException(Throwable cause) {
        super(extractUnderlyingCause(cause));
        cause = this.getCause();
        ThrowableUtils.transferStackTrace(this, cause);
    }
    
    public UnreacheableException(String message, Throwable cause) {
        super(message, extractUnderlyingCause(cause));
        cause = this.getCause();
        ThrowableUtils.transferStackTrace(this, cause);
    }
    
    private static Throwable extractUnderlyingCause(Throwable cause) {
        while (cause instanceof UnreacheableException) {
            cause = cause.getCause();
        }
        return cause;
    }
    
    
}
