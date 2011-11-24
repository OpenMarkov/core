package org.openmarkov.core.model.network.modelUncertainty;

public abstract class GammaAbstract extends ProbDensFunction {

	public GammaAbstract(TypeProbDensityFunction type) {
		super(type);
		
	}

	double kabstract;
	
	double thetaabstract;
	
	@Override
	public int getNumberOfRequiredArguments() {
		return 2;
	}

	
	protected abstract void auxPlaceParameters(Double[] args);	
	
	@Override
	public void placeParameters(Double[] params,boolean createSSJPDF) {
		auxPlaceParameters(params);
		
	}

	
	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return !isChance;
	}
	

	@Override
	public double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}


}
