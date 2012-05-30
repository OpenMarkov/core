package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;

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
	private ProbNode probNode = null;
	/**
	 * 
	 */
	private String lastBaseName;
/**
 * 
 * @param probNode
 * @param timeSlice
 */
	public TimeSliceEdit(ProbNode probNode, int timeSlice) {
		super(probNode.getProbNet());
		this.lastTimeSlice = probNode.getVariable().getTimeSlice();
		this.newTimeSlice = timeSlice;
		this.lastBaseName =  probNode.getVariable().getBaseName();
		this.probNode = probNode;
	}

	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		probNode.getVariable().setTimeSlice(newTimeSlice);
		//probNode.getVariable().setBaseName(lastBaseName);		
	}
	@Override
	public void undo() {
		super.undo();
		probNode.getVariable().setTimeSlice(lastTimeSlice);
	}

}
