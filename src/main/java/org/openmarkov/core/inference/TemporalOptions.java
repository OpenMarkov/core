package org.openmarkov.core.inference;

public class TemporalOptions implements Cloneable {
	
	private int numberOfSlices;
	
	private TransitionTime transition;
	
	public TemporalOptions(){
		numberOfSlices = 20;
		transition = TransitionTime.BEGINNING;
	}

	public int getNumberOfSlices() {
		return numberOfSlices;
	}

	public void setNumberOfSlices(int numberOfSlices) {
		this.numberOfSlices = numberOfSlices;
	}

	public TransitionTime getTransition() {
		return transition;
	}

	public void setTransition(TransitionTime transition) {
		this.transition = transition;
	}

	public TemporalOptions clone() {
		TemporalOptions temporalOptions = new TemporalOptions();
		temporalOptions.setNumberOfSlices(this.numberOfSlices);
		temporalOptions.setTransition(this.getTransition());
		return temporalOptions;
	}
	
	

}
