/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.GompertzFunction;
import org.openmarkov.core.model.network.modelUncertainty.WeibullFunction;

/**
 * Parametrization of the Gompertz distribution where shape= a, and rate =b
 * This is named after R package flexurv by Christopher Jackson (https://rdrr.io/cran/flexsurv/man/Gompertz.html)
 * @author cyago
 * @version 1 26/10/2021 - only implemented the methods currently needed
 */
@ParametrizedFunctionType(distributionName = "Gompertz", parametrizationName = "Shape / Rate", parameters = { "shape a", "rate b"}, isValidForTTE = true)
public class GompertzFunctionShapeRate extends GompertzFunction {

	/**
	 * Creates a GompertzFunctionShapeRate with "shape a"=1 and "rate b"=1
	 * Default constructor.
	 */
	public GompertzFunctionShapeRate() {
		super(1,1);
	}

}
