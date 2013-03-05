/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.modelUncertainty;

public class StandardNormalFunction extends ProbDensFunctionWithKnownInverseCDF {
	
	public StandardNormalFunction() {
		super(TypeProbDensityFunction.STANDARDNORMAL);

	}

	//Odeh and Evans' coefficients
	private static double p[]=new double[]{-0.322232431088,-1.0,-0.342242088547,-0.0204231210245,-0.453642210148E-4};
	private static double q[]=new double[]{0.0993484626060,0.588581570495,0.531103462366,0.103537752850,0.38560700634E-2};
	
	//Polynomials for the approximation
	private Polynomial numerator= new Polynomial(p,4);
	private Polynomial denominator= new Polynomial(q,4);
	
	public class Polynomial{
		
		//Coefficients
		double[] coeff;
		//Degree
		int deg;
		
		public Polynomial(double[] p, int i) {
			coeff = p;
			deg = i;
		}
		
		
		 public double evaluate(double x) {
		    double p = 0;
		    for (int i = deg; i >= 0; i--)
		          p = coeff[i] + (x * p);
		    return p;
			    
		}
	}


	@Override
	public int getNumberOfRequiredArguments() {
		return 0;
	}

	@Override
	public double[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void placeParameters(Double[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return false;
	}

	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		
		return true;
	}

	@Override
	public double getMean() {
		return 0;
	}

	@Override
	public double getMaximum() {
		
		return Double.POSITIVE_INFINITY;
	}

	
	/**
	 * We use the method proposed by Odeh and Evan (1974).
	 * It is an approximation for the inverse of the cummulative 
	 * distribution function of the standard normal.
	 * @param y
	 * @return
	 */
	@Override
	public double getInverseCumulativeDistributionFunction(double beta) {
		double inverse;
		if (beta<0.5){
			inverse = auxOdehAndEvansApproximation(beta);
		}
		else{
			inverse = -auxOdehAndEvansApproximation(1-beta);
		}
		return inverse;
	}
	
	private double auxOdehAndEvansApproximation(double beta){
		double y;
		double evalFunction;

		y = Math.sqrt(-2.0*Math.log(1.0-beta));
		evalFunction = -y-numerator.evaluate(y)/denominator.evaluate(y);
		
		return evalFunction;
	}

	@Override
	public double getVariance() {
		return 1.0;
	}

}
