package org.openmarkov.core.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An operation that is not supported for a certain target. Mostly used when a class has many inheritors and some cannot
 * implement a certain operation that most should.
 *
 * @author jrico
 */
public final class UnsupportedOperationException extends OpenMarkovException2 {
    
    private final StackTraceElement operation;
    private final @Nullable String reason;
    
    /**
     * Creates an UnsupportedOperationException with a specific reason.
     * <p>
     * The operation that isn't supported is automatically taken from the current stack trace. This means when you
     * create this exception by calling {@code new UnsupportedOperationException("...")}, the method where said
     * {@code new} appears is the method/operation that is not supported.
     */
    public UnsupportedOperationException(@Nullable String reason) {
        var trace = Thread.currentThread().getStackTrace();
        this.operation = trace[2];
        this.reason = reason;
    }
    
    /**
     * Creates an UnsupportedOperationException with a specific reason.
     * <p>
     * The operation that isn't supported is automatically taken from the current stack trace. This means when you
     * create this exception by calling {@code new UnsupportedOperationException()}, the method where said {@code new}
     * appears is the method/operation that is not supported.
     */
    public UnsupportedOperationException() {
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
