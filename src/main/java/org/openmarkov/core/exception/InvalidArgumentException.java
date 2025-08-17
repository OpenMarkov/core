package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;

public class InvalidArgumentException extends OpenMarkovException {
    
    public final boolean valueIsSet;
    public final @Nullable Object value;
    public final @Nullable String argumentName;
    public final String reason;
    
    public InvalidArgumentException(@Nullable Object value, @Nullable String argumentName, String reason) {
        this.valueIsSet = true;
        this.value = value;
        this.argumentName = argumentName;
        this.reason = reason;
    }
    
    public InvalidArgumentException(@Nullable String argumentName, String reason) {
        this.valueIsSet = false;
        this.value = null;
        this.argumentName = argumentName;
        this.reason = reason;
    }
    
    public InvalidArgumentException(@Nullable Object value, String reason) {
        this.valueIsSet = true;
        this.value = value;
        this.argumentName = null;
        this.reason = reason;
    }
    
    public InvalidArgumentException(String reason) {
        this.valueIsSet = false;
        this.value = null;
        this.argumentName = null;
        this.reason = reason;
    }
    
    @Override protected @Nullable String getExceptionMessage() {
        String argumentValueString = "";
        if (this.valueIsSet) {
            argumentValueString = "(" + this.value + ")";
        }
        String argumentName = "";
        if (this.argumentName != null) {
            argumentName = this.argumentName;
        }
        argumentName += " ";
        return "Argument " + argumentName + argumentValueString + " is invalid because " + this.reason + ".";
    }
    
    @Override protected @Nullable String getExceptionTitle() {
        return "Arguments aren't valid";
    }
}
