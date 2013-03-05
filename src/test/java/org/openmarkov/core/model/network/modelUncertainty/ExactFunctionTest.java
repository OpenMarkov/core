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

import org.junit.Test;

/**
 * @author manolo
 *
 */
public class ExactFunctionTest extends ProbDensFunctionTest {


	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#getTypeProbDensFunction()
	 */
	@Override
	public TypeProbDensityFunction getTypeProbDensFunction() {
		
		return TypeProbDensityFunction.EXACT;
	}

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionTest#initializeParamsProbDensFunctionTest(org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction)
	 */
	@Override
	public void initializeParamsProbDensFunctionTest(ProbDensFunction prob) {
		Double[] params={5.3};
		prob.placeParameters(params);
		
	}

}
