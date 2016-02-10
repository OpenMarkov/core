package org.openmarkov.core.model.network.modelUncertainty;

/**
 * @author manolo
 * Represents the continuous interval [a,b]
 *
 */
public class DomainInterval {
	
	public DomainInterval(double a, double b) {
		super();
		this.a = a;
		this.b = b;
	}

	private double a;
	
	private double b;
	
	public double min(){
		return a;
	}
	
	public double max(){
		return b;
	}

}
