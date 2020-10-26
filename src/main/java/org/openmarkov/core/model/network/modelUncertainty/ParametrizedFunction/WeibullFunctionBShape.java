/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.WeibullFunction;
/**
 * Parametrization of the Weibull  distribution which scale = b=lambda^(-k) and shape= K
 * @author cyago
 * @version 1 20/10/2020 - only implemented the methods currently needed
 */

@ParametrizedFunctionType(distributionName = "Weibull", parametrizationName = "Scale(b)/ Shape", parameters = { "k", "b"}, isValidForTTE = true)
public class WeibullFunctionBShape extends WeibullFunction {

	/**
	 * Default constructor
	 */
	public WeibullFunctionBShape() {
		super(0.0,0);
	}


	/**
	 * Sets the parameters of the probability function. There are two parameters b and lambda.
	 * b = lambda^-k ; lambda = b^k
	 * @param params - parameters of the probability function [b, lambda].
	 */
	@Override
	public void setParameters(double[] params) {
		params[1] = -Math.pow(params[1], params[0]);
		super.setParameters(params);
	}


}
