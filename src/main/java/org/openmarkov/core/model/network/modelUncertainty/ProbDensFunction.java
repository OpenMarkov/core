package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

public abstract class ProbDensFunction {
	
	TypeProbDensityFunction type;
	
	protected Random stream;
		
		
	public TypeProbDensityFunction getType() {
		return type;
	}

	public void setType(TypeProbDensityFunction type) {
		this.type = type;
	}

	public ProbDensFunction(TypeProbDensityFunction type) {
		this.type = type;
		stream = new Random();
		
	}
	
	public static int getNumberOfRequiredArguments(TypeProbDensityFunction type){
		return constructNewProbDensFunction(type).getNumberOfRequiredArguments();
	}
	
	public abstract int getNumberOfRequiredArguments();
	
	public abstract double[] getParameters();

	public void placeParameters(String[] args){
		Double[] values = parseDoubles(args);
		placeParameters(values);
	}
	
	public abstract void placeParameters(Double[] args);
	
	public static ProbDensFunction constructNewProbDensFunction(TypeProbDensityFunction type){
		
		ProbDensFunction auxProb = null;
		switch (type){
		case EXACT:
			auxProb = new ExactFunction();
			break;
		case BETA:
			auxProb = new BetaFunction();
			break;
		case COMPLEMENT:
			auxProb = new ComplementFunction();
			break;
		case DIRICHLET:
			auxProb = new DirichletFunction();
			break;
		case GAMMA:
			auxProb = new GammaFunction();
			break;
		case GAMMAMV:
			auxProb = new GammamvFunction();
			break;
		case LOGNORMAL:
			auxProb = new LogNormalFunction();
			break;
		case NORMAL:
			auxProb = new NormalFunction();
			break;
		case RANGE:
			auxProb = new RangeFunction();
			break;
		case TRIANGULAR:
			auxProb = new TriangularFunction();
			break;
		}
		return auxProb;
	
	}
	
	public abstract boolean isPossibleDistribution(boolean isChance);
	
	public static boolean isPossibleDistribution(TypeProbDensityFunction type,boolean isChance){
		 return constructNewProbDensFunction(type).isPossibleDistribution(isChance);
	}

	public abstract boolean doParametersVerifyDomainConstraint(boolean isChanceVariable);
	
	private static Double[] parseDoubles(String[] params){
		Double[] values;
		values = new Double[params.length];
		for (int i=0;i<params.length;i++){
			values[i]=Double.parseDouble(params[i]);
		}
		return values;
	}
	
	
	/**
	 * Some subclasses can override this method.
	 * @return
	 */
	public abstract double getMean();
	
	public final double getStandardDeviation(){
		return Math.sqrt(getVariance());
	}

	public abstract double getVariance();

	public abstract double getMaximum();

	public abstract double getSample();
	
}
