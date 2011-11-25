package org.openmarkov.core.model.network.modelUncertainty;

public class ErlangFunction extends ProbDensFunction {
	
	/**
	 * @param type
	 * @param lambda
	 */
	public ErlangFunction(int k, double lambda) {
		this();
		this.k = k;
		this.lambda = lambda;
	}

	int k;
	
	double lambda;

	public ErlangFunction() {
		super(TypeProbDensityFunction.ERLANG);
		// TODO Auto-generated constructor stub
	}

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
		
		return false;
	}

	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
	
		return (k>=0)&&(lambda>0);
	}

	@Override
	public double getMean() {
		
		return k/lambda;
	}

	@Override
	public double getMaximum() {
		
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public double getSample() {
		double sumSamples;
		
		sumSamples = 0.0;
		for (int i=0;i<k;i++){
			sumSamples = (new ExponentialFunction(lambda)).getSample();
			
		}
		return sumSamples;
	}
	
	

}
