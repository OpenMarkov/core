/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.model.network.modelUncertainty;

import static org.junit.Assert.assertEquals;

/**
 * @author manolo
 *
 */
public class StandardNormalFunctionTest extends ProbDensFunctionTest {

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#getTypeProbDensFunction()
	 */
	@Override
	public TypeProbDensityFunction getTypeProbDensFunction() {
		return TypeProbDensityFunction.STANDARDNORMAL;
	}

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#initializeParamsProbDensFunctionTest(org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction)
	 */
	@Override
	public void initializeParamsProbDensFunctionTest(ProbDensFunction prob) {
		
		
	}
	
	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#assertMeanTest(double)
	 */
	@Override
	public void assertMeanTest(double samplesMean,double pdfMean, double maxError) {
				
		StandardNormalFunction stdPDF = (StandardNormalFunction)pdf;
		
		//It  checks if the relative error (considering the lenght of the interval [a,b] is lower than maxError
		assertEquals(pdfMean, samplesMean,maxError*(6*pdf.getStandardDeviation()));
	}


}
