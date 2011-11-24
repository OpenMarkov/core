package org.openmarkov.core.model.network.modelUncertainty;

public class ExponentialFunction extends ProbDensFunctionWithKnownInverseCDF {

	double lambda;
	
	/**
	 * @return the lambda
	 */
	public double getLambda() {
		return lambda;
	}

	/**
	 * @param lambda the lambda to set
	 */
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}


	public ExponentialFunction(double lambda) {
		this();
		this.lambda = lambda;
		
	}
	
	public ExponentialFunction() {
		super(TypeProbDensityFunction.EXPONENTIAL);
	}



	@Override
	public int getNumberOfRequiredArguments() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public double[] getParameters() {
		double[]a = new double[1];
		a[0]=lambda;
		return a;
	}

	@Override
	public void placeParameters(Double[] params) {
		lambda = params[0];
		
	}

	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return false;
	}

	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		return (lambda>0);
	}

	@Override
	public double getMean() {
		// TODO Auto-generated method stub
		return 1/lambda;
	}

	@Override
	public double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}


	@Override
	public double getInverseCumulativeDistributionFunction(double y) {
		return (-1.0/lambda)*Math.log(1.0-y);
	}

}
