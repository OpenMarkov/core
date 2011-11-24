package org.openmarkov.core.model.network.modelUncertainty;

public class RangeFunction extends ProbDensFunctionWithKnownInverseCDF {

	double a;
	
	double b;
	

	public RangeFunction(){
		super(TypeProbDensityFunction.RANGE);
		
	}

	@Override
	public void placeParameters(Double[] params) {
		a = params[0];
		b = params[1];
	}

	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		return ((0<=a)&&(a<b)&&(b<=1)&&isChanceVariable)||((a<b)&&!isChanceVariable);
	}

	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return true;
	}

	@Override
	public int getNumberOfRequiredArguments() {
		return 2;
	}
	
	@Override
	public double[] getParameters() {
		double[] x=new double[2];
		x[0]=a;
		x[1]=b;
		return x;
	}
	
	@Override
	public double getMaximum() {
		return b;
	}

	
	@Override
	public double getMean() {
		return (a+b)/2;
	}
	
	@Override
	public double getInverseCumulativeDistributionFunction(double y) {
		return a+(b-a)*y;
	}


}
