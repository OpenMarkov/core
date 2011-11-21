package org.openmarkov.core.exception;


@SuppressWarnings("serial")
public class NodeWrapperNumberFormatException extends Exception {

	public NodeWrapperNumberFormatException(int i) {
		super("NodeWrapper.convertStatesToPartitionedInterval" 
	        +" in row " + i +" there is a NumberFormatException!!");
	}
	
}
