/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.model.network.modelUncertainty;

import static org.junit.Assert.*;

/**
 * @author manolo
 *
 */
public class RangeFunctionTest  extends ProbDensFunctionTest {
	
	public TypeProbDensityFunction getTypeProbDensFunction(){
		return TypeProbDensityFunction.RANGE;
	}

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#initializeParamsProbDensFunctionTest(org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction)
	 */
	@Override
	public void initializeParamsProbDensFunctionTest(ProbDensFunction prob) {
		Double[] params={3.0,106.0};
		prob.placeParameters(params);
		
	}

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#assertMeanTest(double)
	 */
	@Override
	public void assertMeanTest(double samplesMean,double pdfMean, double maxError) {
				
		RangeFunction rangePDF = (RangeFunction)pdf;
		
		//It  checks if the relative error (considering the lenght of the interval [a,b] is lower than maxError
		assertEquals(pdfMean, samplesMean,maxError*(rangePDF.b-rangePDF.a));
	}


}
