/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

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
		double sample;
		int k;
		double r;
		double lambdaErlang;
		double u;
		int kForSampling;
		
		lambdaErlang = 1.0/thetaabstract;
		
		//Integer part of kabstract
		k = (int)(Math.ceil(kabstract));
		
		if (!isAnErlangFunction()){
			r = kabstract - k;
			u = (new RangeFunction(0,1)).getSample();
			kForSampling = (u<r)?k:(k+1);
		}
		else{
			kForSampling = k;
		}
		
		sample = (new ErlangFunction(kForSampling,lambdaErlang)).getSample();
			
		return sample;
	}
	
	public boolean isAnErlangFunction(){
		//TODO We have to use an epsilon instead of 0 in the next comparison because comparison with 0 in
		//float arithmetic is always dangerous
		return kabstract-Math.ceil(kabstract)==0;
	}
	
	@Override
	public final double getVariance() {
		// TODO Auto-generated method stub
		return 0;
	}

	


}
