/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;

public class OpenMarkovException extends Exception {
    /**
     * This token must correspond which one of the exception constants defined in <code>{@link OpenMarkovExceptionConstants}</code>
     */
    private final @Nullable String token;
    
    private final @Nullable String message;
    
    /**
     * List of attributes that should pass to the GUI in order to display extra information (f.e. a network name, a number,...).
     */
    private final String @Nullable [] attributes;
    
    
    public OpenMarkovException() {
        this.token = null;
        this.message = null;
        this.attributes = null;
    }
    
    public OpenMarkovException(@Nullable String token) {
        this.token = token;
        this.message = null;
        this.attributes = null;
    }
    
    public OpenMarkovException(Exception exception) {
        this.token = ((OpenMarkovException) exception).token;
        this.message = exception.getMessage();
        this.attributes = null;
    }
    
    
    public OpenMarkovException(@Nullable String token, String @Nullable ... attributes) {
        this.token = token;
        this.message = null;
        this.attributes = attributes;
    }
    
    public OpenMarkovException(@Nullable String message, Throwable cause) {
        super(message, cause);
        this.token = message;
        this.message = null;
        this.attributes = null;
    }
    
    public OpenMarkovException(@Nullable String message, Throwable cause, String @Nullable ... attributes) {
        super(message, cause);
        this.token = message;
        this.message = null;
        this.attributes = attributes;
    }
    
    
    public @Nullable String getToken() {
        return this.token;
    }
    
    public String[] getAttributes() {
        return attributes;
    }
}