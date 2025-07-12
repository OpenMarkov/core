/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

public class OpenMarkovException extends Exception {
    /**
     * This token must correspond which one of the exception constants defined in <code>{@link OpenMarkovExceptionConstants}</code>
     */
    private final String token;
    
    private final String message;
    
    /**
     * List of attributes that should pass to the GUI in order to display extra information (f.e. a network name, a number,...).
     */
    private final String[] attributes;
    
    
    public OpenMarkovException() {
        this.token = null;
        this.message = null;
        this.attributes = null;
    }
    
    public OpenMarkovException(String token) {
        this.token = token;
        this.message = null;
        this.attributes = null;
    }
    
    public OpenMarkovException(Exception exception) {
        this.token = ((OpenMarkovException) exception).getToken();
        this.message = exception.getMessage();
        this.attributes = null;
    }
    
    
    public OpenMarkovException(String token, String... attributes) {
        this.token = token;
        this.message = null;
        this.attributes = attributes;
    }
    
    public OpenMarkovException(String message, Throwable cause) {
        super(message, cause);
        this.token = message;
        this.message = null;
        this.attributes = null;
    }
    
    public OpenMarkovException(String message, Throwable cause, String... attributes) {
        super(message, cause);
        this.token = message;
        this.message = null;
        this.attributes = attributes;
    }
    
    
    public String getToken() {
        return this.token;
    }
    
    public String[] getAttributes() {
        return attributes;
    }
}