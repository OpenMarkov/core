package org.openmarkov.core.model.network.sensitivityAnalysis;
import umontreal.iro.lecuyer.probdist.GammaDist;
import umontreal.iro.lecuyer.randvar.GammaGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

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
		if (createSSJPDF){
			createSSJPDF();
		}
	}

		
	
	@Override
	public boolean isPossibleDistribution(boolean isChance) {
		return !isChance;
	}
	

	public void createSSJPDF() {
		ssjPDF = new GammaDist(kabstract,thetaabstract);
		
	}
	
	@Override
	public double getMaximum() {
		return Double.POSITIVE_INFINITY;
	}



	@Override
	public void initializeGenerator() {
		generator = new GammaGen(stream,(GammaDist)ssjPDF);
	}
	


	
	

}
