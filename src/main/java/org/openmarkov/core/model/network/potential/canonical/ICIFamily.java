package org.openmarkov.core.model.network.potential.canonical;

public enum ICIFamily {
	OR(0),
	AND(1);

	private final int value;

	ICIFamily(int value) {
		this.value = value;
	}

    public int value() { 
    	return value; 
    }
    
    public String toString() {
    	String string = null;
    	if (value == 0) {
    		string = new String("OR");
    	} else {
    		string = new String("AND");
    	}
    	return string;
    }

}
