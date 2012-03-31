/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.modelUncertainty;


public class GammamvFunction extends GammaAbstract {
	
	double mu;
	
	double sigma;

	public GammamvFunction() {
		super(TypeProbDensityFunction.GAMMAMV);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void auxPlaceParameters(Double[] args) {
		// TODO Auto-generated method stub
		mu = args[0];
		sigma = args[1];
		this.kabstract = Math.pow(mu/sigma,2);
		this.thetaabstract = Math.pow(sigma,2)/mu;
		
	}


	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		// TODO Auto-generated method stub
		return (mu>0)&&(sigma>0);
	}


	@Override
	public double[] getParameters() {
		double[] a=new double[2];
		a[0]=mu;
		a[1]=sigma;
		return a;
	}


	
}
