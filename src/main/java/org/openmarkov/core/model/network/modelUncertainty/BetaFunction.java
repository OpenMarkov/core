package org.openmarkov.core.model.network.modelUncertainty;


public class BetaFunction extends ProbDensFunction {

	double alpha;
	
	double beta;


	
	
	
	public BetaFunction(){
		super(TypeProbDensityFunction.BETA);
	
		
	}

	@Override
	public void placeParameters(Double[] params,boolean createSSJPDF) {
		alpha = params[0];
		beta = params[1];
	}

	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		return ((alpha>0)&&(beta>0));
	}

	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return isChance;
	}

	@Override
	public int getNumberOfRequiredArguments() {
		return 2;
	}

	@Override
	public double[] getParameters() {
		double[]a = new double[2];
		a[0]=alpha;
		a[1]=beta;
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
	public double getSample() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void initializeGenerator() {
		// TODO Auto-generated method stub
		
	}

	}
