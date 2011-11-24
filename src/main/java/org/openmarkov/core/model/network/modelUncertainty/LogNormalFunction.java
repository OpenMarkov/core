package org.openmarkov.core.model.network.modelUncertainty;

public class LogNormalFunction extends ProbDensFunction {

	double mu;
	
	double sigma;
	
	public LogNormalFunction() {
		super(TypeProbDensityFunction.LOGNORMAL);
	}

	

	@Override
	public int getNumberOfRequiredArguments() {
		return 2;
	}

	@Override
	public void placeParameters(Double[] args,boolean createSSJPDF) {
		mu = args[0];
		sigma = args[1];

	}

	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return !isChance;
	}

	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		return (sigma>0);
	}
	
	@Override
	public double[] getParameters() {
		double[] a=new double[2];
		a[0]=mu;
		a[1]=sigma;
		return a;
	}



	@Override
	public double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}



	@Override
	public double getMean() {
		// TODO Auto-generated method stub
		return 0;
	}



}
