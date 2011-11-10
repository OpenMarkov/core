package org.openmarkov.core.mdp;

/**
 * @author Jorge
 *
 */
public class MDPParams {
	/**
	 * 
	 */
	private double discountRate;
	
	/**
	 * 
	 */
	private double epsilon;
	
	/**
	 * 
	 */
	public MDPParams () {
		discountRate= 0.99;
		epsilon= 10e-2;
	}

	/**
	 * 
	 */
	public MDPParams (double discountRate, double epsilon) {
		this.discountRate= discountRate;
		this.epsilon= epsilon;
	}
	
	/**
	 * @param discountRate the discountRate to set
	 */
	public void setDiscountRate(double discountRate) {
		this.discountRate = discountRate;
	}
	
	/**
	 * @return the discountRate
	 */
	public double getDiscountRate() {
		return discountRate;
	}
	
	/**
	 * @param epsilon the epsilon to set
	 */
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	
	/**
	 * @return the epsilon
	 */
	public double getEpsilon() {
		return epsilon;
	}
}
