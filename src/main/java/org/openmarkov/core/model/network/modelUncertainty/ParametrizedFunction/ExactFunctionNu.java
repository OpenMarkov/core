/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
/**
 * Parametrization of the Exact distribution which nu as a parameter
 * @author cyago
 * @version 1 20/10/2020 - only implemented the methods currently needed
 */

@ParametrizedFunctionType(distributionName = "Exact", parametrizationName = "Nu", parameters = { "nu"}, isValidForTTE = true)
public class ExactFunctionNu extends ExactFunction {

	/**
	 * Default constructor
	 */
	public ExactFunctionNu() {
		super(0.0);
	}

	/**
	 * Sets the parameters of the probability function
	 * @param params - parameters of the probability function
	 */
	@Override
	public void setParameters(double[] params) {
		super.setParameters(params);
	}


}
