/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.modelUncertainty;



public class ExactFunction extends ProbDensFunction {
	
	double nu;
	
	public ExactFunction() {
		super(TypeProbDensityFunction.EXACT);
	}

	@Override
	public void placeParameters(Double[] params) {
		nu = params[0];
		
	}

	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		return ((!isChanceVariable)||((0<=nu)&&(nu<=1)));
	}

	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return true;
	}

	@Override
	public int getNumberOfRequiredArguments() {
		return 1;
	}
	
	/**
	 * Some subclasses can override this method.
	 * @return
	 */
	public double getMean(){
		return nu;
	}

	@Override
	public double[] getParameters() {
		double[] a=new double[1];
		a[0]=nu;
		return a;
	}

	@Override
	public double getMaximum() {
		return nu;
	}

	@Override
	public double getSample() {
		return nu;
	}

	@Override
	public double getVariance() {
		
		return 0;
	}
	
}
