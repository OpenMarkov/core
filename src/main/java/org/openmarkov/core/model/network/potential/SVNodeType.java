package org.openmarkov.core.model.network.potential;

import java.io.Serializable;

import org.openmarkov.core.model.network.NodeType;

/** Codifies existing node types using this numbers:
 * <ol start="0">
 * <li>CHANCE
 * <li>DECISION
 * <li>UTILITY
 * </ol>
 * @author manuel
 * @author fjdiez */
public enum SVNodeType implements Serializable {

	ADDITION(0), 
	PRODUCT(1);
		
	/** Existing types: ADDITION(0), PRODUCT(1), ... */
	private final int type;

	/** @param <code>type</code> An integer: ADDITION(0), PRODUCT(1), ... 
	 * @argCondition value >= 0 and value < NodeType.values().length */
	SVNodeType(int type) {
    	this.type = type; 
    }

    /** @return <code>type</code> */
    public int type() { 
    	return type; 
    }
	    
    /** @param nodeType
     * @return nodeType.value. <code>int</code> */
    public static int type(NodeType nodeType) {
    	return nodeType.type();
    }

}
