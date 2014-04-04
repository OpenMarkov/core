package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
/**
 * 
 * @author myebra
 *
 */
@SuppressWarnings("serial")
public class TimeSliceEdit extends SimplePNEdit{
	/**
	 * The last time slice before the edition
	 */
	private int lastTimeSlice;
	/**
	 * The new time slice after the edition
	 */
	private int newTimeSlice;
	/**
	 * The edited node
	 */
	private Node node = null;
	/**
	 * the last base name of the temporal variable
	 */
	private String lastBaseName;
	/**
	 * The last variable name
	 */
	private String lastName; 
	
/**
 * 
 * @param node
 * @param timeSlice
 */
	public TimeSliceEdit(Node node, int timeSlice) {
		super(node.getProbNet());
		this.lastTimeSlice = node.getVariable().getTimeSlice();
		this.newTimeSlice = timeSlice;
		this.lastBaseName =  node.getVariable().getBaseName();
		this.lastName =  node.getVariable().getName();
		this.node = node;
		
	}

	@Override
	public void doEdit() throws DoEditException {
		//onlyTemporal && not only atemporal
		node.getVariable().setTimeSlice(newTimeSlice);
		if (newTimeSlice == Integer.MIN_VALUE && lastTimeSlice != Integer.MIN_VALUE && lastBaseName != null) {
			node.getVariable().setBaseName(null);
			int beginSlicePart = lastName.lastIndexOf('[') - 1;
			String newName = null;
			if (beginSlicePart > 0) {
				newName = lastName.substring(0, beginSlicePart);
			}
			node.getVariable().setName(newName);
		}
		//not only temporaL && not only atemporal but also set name and base name
		if (lastTimeSlice == Integer.MIN_VALUE) {
			node.getVariable().setBaseName(lastBaseName);	
			node.getVariable().setName(lastName+ " " + "["+ String.valueOf(newTimeSlice)+"]");
		}
	}
	@Override
	public void undo() {
		super.undo();
		//onlyTemporal
		node.getVariable().setTimeSlice(lastTimeSlice);
		//not only temporaL && not only atemporal but also set name and base name
		if (lastTimeSlice == Integer.MIN_VALUE) {
			node.getVariable().setBaseName(lastBaseName);
			node.getVariable().setName(lastName);
		}
	}

}
