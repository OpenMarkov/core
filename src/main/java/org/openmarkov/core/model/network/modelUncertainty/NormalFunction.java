package org.openmarkov.core.model.network.modelUncertainty;


public class NormalFunction extends ProbDensFunction {
	
	double mu;
	
	double sigma;
	
	private StandardNormalFunction standard;
	

	public NormalFunction() {
		super(TypeProbDensityFunction.NORMAL);
		standard = new StandardNormalFunction();
	}


	public NormalFunction(double mu2, double sigma2) {
		this();
		mu = mu2;
		sigma = sigma2;
	}


	@Override
	public int getNumberOfRequiredArguments() {
		return 2;
	}


	@Override
	public void placeParameters(Double[] args) {
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
		return mu;
	}


	@Override
	public double getSample() {
		return sigma*standard.getSample()+mu;
	}


	@Override
	public double getVariance() {
	
		return Math.pow(sigma, 2.0);
	}

	
}
