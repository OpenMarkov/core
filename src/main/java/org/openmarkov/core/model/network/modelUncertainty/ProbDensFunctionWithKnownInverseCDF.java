package org.openmarkov.core.model.network.modelUncertainty;

public class ProbDensFunctionWithKnownInverseCDF extends ProbDensFunction {

	public ProbDensFunctionWithKnownInverseCDF(TypeProbDensityFunction type) {
		super(type);
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
	public void placeParameters(Double[] args, boolean createSSJPDF) {
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaximum() {
		// TODO Auto-generated method stub
		return 0;
	}

}
