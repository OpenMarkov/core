package org.openmarkov.core.action;

import java.io.Serializable;

/**
 * Defines the different actions on the state an PartitionedInterval objects  
 * @author mpalacios
 * @version	1.0*/
public enum StateAction implements Serializable {
	ADD(0),
	REMOVE(1),
	RENAME(2),
    UP(3),
	DOWN(4),
	MODIFYDELIMITERINTERVAL(5),
	MODIFYVALUEINTERVAL(6);
	

	private final int value;

	StateAction(int value) {
		this.value = value;
	}

    public int value() { 
    	return value; 
    }
}
