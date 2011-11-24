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
	public void placeParameters(Double[] params) {
		auxPlaceParameters(params);
		
	}

	
	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return !isChance;
	}
	

	@Override
	public final double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}
	
	@Override
	public final double getMean() {
		return (kabstract*thetaabstract);
	}

	@Override
	public final double getSample() {
		//TODO
		return 0;
	}


}
