package org.openmarkov.core.model.network;

public enum PolicyType {
	OPTIMAL("Optimal"),
	DETERMINISTIC("Deterministic"),
	PROBABILISTIC("Probabilistic");
	
	String name;
	int type;
	
	PolicyType(String name) {
		this.type = this.ordinal();
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	public int getType() {
		return type;
	}


}
