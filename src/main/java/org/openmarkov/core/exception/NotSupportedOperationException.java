package org.openmarkov.core.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An operation that is not supported for a certain target. Mostly used when a class has many inheritors and some cannot
 * implement a certain operation that most should.
 *
 * @author jrico
 */
public final class NotSupportedOperationException extends OpenMarkovException {
    
    private final StackTraceElement operation;
    private final @Nullable String reason;
    
    /**
     * Creates an NotSupportedOperationException with a specific reason.
     * <p>
     * The operation that isn't supported is automatically taken from the current stack trace. This means when you
     * create this exception by calling {@code new NotSupportedOperationException("...")}, the method where said
     * {@code new} appears is the method/operation that is not supported.
     */
    public NotSupportedOperationException(@Nullable String reason) {
        var trace = Thread.currentThread().getStackTrace();
        this.operation = trace[2];
        this.reason = reason;
    }
    
    /**
     * Creates an NotSupportedOperationException with a specific reason.
     * <p>
     * The operation that isn't supported is automatically taken from the current stack trace. This means when you
     * create this exception by calling {@code new NotSupportedOperationException()}, the method where said {@code new}
     * appears is the method/operation that is not supported.
     */
    public NotSupportedOperationException() {
        var trace = Thread.currentThread().getStackTrace();
        this.operation = trace[2];
        this.reason = null;
    }
    
    
    @Override protected @NotNull String getExceptionMessage() {
        String message = "Operation " + this.operation.getMethodName() + " is not supported";
        if (this.reason != null && !this.reason.isBlank()) {
            message += ": " + this.reason;
        }
        return message;
    }
    
    @Override protected @NotNull String getExceptionTitle() {
        return "Operation not supported";
    }
    
    public StackTraceElement getOperation() {
        return this.operation;
    }
    
    public @Nullable String getReason() {
        return this.reason;
    }
    
}
