/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.ExponentialFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
/**
 * Parametrization of the Exponential distribution which the rate (lambda) as parameter
 * @author cyago
 * @version 1 20/10/2020 - only implemented the methods currently needed
 */

@ParametrizedFunctionType(distributionName = "Exponential", parametrizationName = "Lambda", parameters = { "lambda"}, isValidForTTE = true)
public class ExponentialFunctionLambda extends ExponentialFunction {

	/**
	 * Default constructor
	 */
	public ExponentialFunctionLambda() {
		super(0.0);
	}

	/**
	 * Sets the parameters of the probability function. There is only one parameter, the rate lambda
	 *
	 * @param params - parameters of the probability function [lambda].
	 */
	@Override
	public void setParameters(double[] params) {
		super.setParameters(params);
	}


}
