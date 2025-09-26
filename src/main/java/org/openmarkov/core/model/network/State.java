/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.localize.ClassLocalizable;

import java.util.LinkedHashMap;

public class State implements Cloneable, ClassLocalizable {
    
    // Attributes
    public LinkedHashMap<String, String> additionalProperties;
    
    private String name;
    
    // Constructor
    public State(String name) {
        this.name = name;
        additionalProperties = new LinkedHashMap<>();
    }
    
    public State(State state) {
        this.additionalProperties = new LinkedHashMap<>(state.additionalProperties);
        this.name = state.name;
    }
    
    // Methods
    public String getName() {
        return name;
    }
    
    public void setName(String newName) {
        this.name = newName;
    }
    
    public boolean equals(State state) {
        return (this.name.equals(state.name));
    }
    
    @Override public String toString() {
        return this.localize();
    }
    
    @Override public State clone() {
        return new State(this);
    }
}
