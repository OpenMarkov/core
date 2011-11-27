package org.openmarkov.core.model.network.modelUncertainty;

public class StandardNormalFunction extends ProbDensFunction {

	@Override
	public int getNumberOfRequiredArguments() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void placeParameters(Double[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getMean() {
		
		return 0;
	}

	@Override
	public double getMaximum() {
		
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public double getSample() {
		//Odeh and Evans (1974)'s method
		double p[];
		double q[];
		p = new double[5];
		q = new double[5];
		
		
		return 0;
	}

}
