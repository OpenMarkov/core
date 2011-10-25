package org.openmarkov.core.model.network.sensitivityAnalysis;

import umontreal.iro.lecuyer.probdist.BetaDist;
import umontreal.iro.lecuyer.randvar.BetaGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

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
		if (createSSJPDF) createSSJPDF();
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
	public void createSSJPDF() {
		ssjPDF = new BetaDist(alpha,beta);
	}

	

	@Override
	public void initializeGenerator() {
		generator = new BetaGen(stream,(BetaDist) ssjPDF);
		
	}



	



	
	
		
	/*@Override
	public double getMean() {
		// TODO Auto-generated method stub
		return alpha / (alpha+beta);
	}

	@Override
	public double getStdDeviation() {
		// TODO Auto-generated method stub
		return Math.sqrt((alpha*beta)/(Math.pow(alpha+beta,2)*(alpha+beta+1)));
	}*/

}
