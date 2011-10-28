package org.openmarkov.core.mdp;

public class MDPFiniteHorizonParams extends MDPParams {
	private int horizonLength;
	
	public MDPFiniteHorizonParams(int horizonLength) {
		super(1,10e-4);
		this.horizonLength= horizonLength;
	}
	
	public MDPFiniteHorizonParams(double discountRate, int horizonLength) {
		super(discountRate,10e-4);
		this.horizonLength= horizonLength;
	}
	
	public int getHorizonLength() {
		return horizonLength;
	}
}
