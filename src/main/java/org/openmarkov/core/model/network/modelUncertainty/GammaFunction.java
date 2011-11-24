package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.model.network.modelUncertainty.TypeProbDensityFunction;

public class GammaFunction extends GammaAbstract {
	
	/**
	 * @param type
	 * @param k
	 * @param theta
	 */
	public GammaFunction(double k, double theta) {
		this();
		this.k = k;
		this.theta = theta;
	}


	double k;
	
	double theta;
	
	public GammaFunction() {
		super(TypeProbDensityFunction.GAMMA);
		
	}


	@Override
	public void auxPlaceParameters(Double[] params) {
		
		k = params[0];
		theta = params[1];
		this.kabstract = k;
		this.thetaabstract = theta;
		
	}




	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {

		return (k>0)&&(theta>0);
	}


	@Override
	public double[] getParameters() {
		double[] a=new double[2];
		a[0]=k;
		a[1]=theta;
		return a;
	}


	@Override
	public double getMean() {
		// TODO Auto-generated method stub
		return 0;
	}


	

}
