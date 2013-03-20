/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.model.network.modelUncertainty;

/**
 * @author manolo
 *
 */
public class ErlangFunctionTest extends ProbDensFunctionTest {

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#getTypeProbDensFunction()
	 */
	@Override
	public TypeProbDensityFunction getTypeProbDensFunction() {
		
		return TypeProbDensityFunction.ERLANG;
	}

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#initializeParamsProbDensFunctionTest()
	 */
	@Override
	public Double[] initializeParamsProbDensFunctionTest() {
		Double[] params={2.0,1.5};
		return params;
	}

}
