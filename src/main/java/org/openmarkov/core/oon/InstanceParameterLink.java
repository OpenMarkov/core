package org.openmarkov.core.oon;

public class InstanceParameterLink extends ParameterLink {
	
	private Instance sourceInstance;
	private Instance destInstance;
	private Instance destSubInstance;
	
	/**
	 * Constructor
	 * @param sourceInstance
	 * @param destInstance
	 */
	public InstanceParameterLink(Instance sourceInstance, Instance destInstance, Instance destSubInstance) {
		super();
		this.sourceInstance = sourceInstance;
		this.destInstance = destInstance;
		this.destSubInstance = destSubInstance;
	}
	
	/**
	 * @return the sourceInstance
	 */
	public Instance getSourceInstance() {
		return sourceInstance;
	}

	/**
	 * @return the destInstance
	 */
	public Instance getDestInstance() {
		return destInstance;
	}

	/**
	 * @return the destSubInstance
	 */
	public Instance getDestSubInstance() {
		return destSubInstance;
	}	
	
	
}
