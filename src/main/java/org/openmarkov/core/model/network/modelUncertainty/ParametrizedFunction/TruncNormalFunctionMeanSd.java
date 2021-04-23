/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.NormalFunction;
import org.openmarkov.core.model.network.modelUncertainty.WeibullFunction;

import java.util.Random;

/**
 * Parametrization of the Normal distribution with mean and standard deviation. It is truncated: values under 0 are set to 0.
 * @author cyago
 * @version 1 20/10/2020 - only implemented the methods currently needed
 */
@ParametrizedFunctionType(distributionName = "Normal", parametrizationName = "Truncated (not recommended) Mean / Sd", parameters = { "mu", "sigma"}, isValidForTTE = true)
public class TruncNormalFunctionMeanSd extends NormalFunction {

	/**
	 * Default constructor
	 */
	public TruncNormalFunctionMeanSd() {
		super(0.0,0);
	}


	@Override public double getSample(Random randomGenerator) {
		double value = super.getSample(randomGenerator);
		return (value>0)? value: 0;
	}


//	/**
//	 * Sets the parameters of the probability function. There are two parameters mu and sigma.
//	 *
//	 * @param params - parameters[1]= mu and parameters[0] = sigma
//	 */
//	@Override
//	public void setParameters(double[] params) {
//		super.setParameters(params);
//	}



}
