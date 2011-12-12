/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

public abstract class ProbDensFunctionWithKnownInverseCDF extends ProbDensFunction {

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction#getSample()
	 */

	public ProbDensFunctionWithKnownInverseCDF(TypeProbDensityFunction type) {
	
		super(type);
	}

	public abstract double getInverseCumulativeDistributionFunction(double y);
	
	public final double getSample(){
		double sample;
		double randomNumber;
		
		randomNumber = stream.nextDouble();
		sample = getInverseCumulativeDistributionFunction(randomNumber); 
		
		return sample;
	}

	@Override
	public abstract double getMaximum();
	
}
