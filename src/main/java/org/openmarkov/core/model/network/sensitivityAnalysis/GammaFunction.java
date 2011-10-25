package org.openmarkov.core.model.network.sensitivityAnalysis;

import org.openmarkov.core.model.network.sensitivityAnalysis.TypeProbDensityFunction;

import umontreal.iro.lecuyer.randvar.GammaGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class GammaFunction extends GammaAbstract {
	
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




	

	
	

}
