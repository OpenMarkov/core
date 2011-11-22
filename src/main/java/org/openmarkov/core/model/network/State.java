package org.openmarkov.core.model.network;

import java.util.LinkedHashMap;

public class State {

	// Attributes
	public LinkedHashMap<String, String> additionalProperties;
	
	private String name;
	
	// Constructor
	public State(String name) {
		this.name = name;
		additionalProperties = new LinkedHashMap<String, String>(); 
	}

	// Methods
	public String getName() {
		return name;
	}
	
	public void setName(String newName){
		this.name = newName;
	}

	public boolean equals (Object state){
		return (this.name.equals(((State)state).getName()));
	}
	
	public String toString() {
		return name;
	}
}
