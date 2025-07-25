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
    
    /**
     * List of attributes that should pass to the GUI in order to display extra information (f.e. a network name, a number,...).
     */
    private final String @Nullable [] attributes;
    
    public OpenMarkovException(@Nullable String token, String @Nullable ... attributes) {
        this.token = token;
        this.attributes = attributes;
    }
    
    public OpenMarkovException(Exception e) {
        super(e);
        this.token = null;
        this.attributes = null;
    }
    
    public @Nullable String getToken() {
        return this.token;
    }
    
    public String[] getAttributes() {
        return attributes;
    }
}