package org.openmarkov.core.model.network.modelUncertainty;


public class ComplementFunction extends ProbDensFunction {

	double nu;
	
	public double getNu() {
		return nu;
	}



	public ComplementFunction() {
		super(TypeProbDensityFunction.COMPLEMENT);
	}

	
	
	@Override
	public void placeParameters(Double[] params,boolean createSSJPDF) {
		nu = params[0];
		
		
	}



	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		return (nu>0);
	}



	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return isChance;
	}



	@Override
	public int getNumberOfRequiredArguments() {
		return 1;
	}



	@Override
	public double[] getParameters() {
		double[]a = new double[1];
		a[0]=nu;
		return a;
	}



	@Override
	public double getMaximum() {
		return 1;
	}



	@Override
	public double getMean() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public void initializeGenerator() {
		// TODO Auto-generated method stub
		
	}
	
}
