package org.openmarkov.core.model.network.modelUncertainty;

import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class NormalFunction  extends ProbDensFunction {
	
	double mu;
	
	double sigma;
	

	public NormalFunction() {
		super(TypeProbDensityFunction.NORMAL);
	}


	@Override
	public int getNumberOfRequiredArguments() {
		return 2;
	}


	@Override
	public void placeParameters(Double[] args,boolean createSSJPDF) {
		mu = args[0];
		sigma = args[1];
		if (createSSJPDF){
			createSSJPDF();
		}
		
	}


	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return !isChance;
	}


	@Override
	public boolean doParametersVerifyDomainConstraint(boolean isChanceVariable) {
		return (sigma>0);
	}
	
	@Override
	public double[] getParameters() {
		double[] a=new double[2];
		a[0]=mu;
		a[1]=sigma;
		return a;
	}
	
	@Override
	public double getMaximum() {
		
		return Double.POSITIVE_INFINITY;
	}
	

	@Override
	public void createSSJPDF() {
		ssjPDF = new NormalDist(mu,sigma);
		
	}




	@Override
	public void initializeGenerator() {
		generator = new NormalGen(stream,(NormalDist) ssjPDF);
	}


	
}
