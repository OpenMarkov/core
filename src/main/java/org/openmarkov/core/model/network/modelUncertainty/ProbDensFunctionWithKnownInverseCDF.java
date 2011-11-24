package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

public abstract class ProbDensFunctionWithKnownInverseCDF extends ProbDensFunction {

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction#getSample()
	 */

	public ProbDensFunctionWithKnownInverseCDF(TypeProbDensityFunction type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	public abstract double getInverseCumulativeDistributionFunction(double y);
	
	public final double getSample(){
		double sample;
		double randomNumber = stream.nextDouble();
		sample = getInverseCumulativeDistributionFunction(randomNumber); 
		return sample;
	}

	@Override
	public double getMaximum() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
