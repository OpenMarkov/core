package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.WeibullFunction;

/**
 * Parametrization of the Weibull  distribution which scale =lambda and shape= K
 *
 * @author cmyago
 * @version 1 20/10/2020 - only implemented the methods currently needed
 */
@ParametrizedFunctionType(distributionName = "Weibull", parametrizationName = "Scale / Shape", parameters = {"lambda", "k"}, isValidForTTE = true)
public class WeibullFunctionScaleShape extends WeibullFunction {

    /**
     * Default constructor
     */
    public WeibullFunctionScaleShape() {
        super(0.0, 0);
    }


//	/**
//	 * Sets the parameters of the probability function. There are two parameters lambda and k.
//	 *
//	 * @param params - parameters of the probability function [lambda,k].
//	 */
//	@Override
//	public void setParameters(double[] params) {
//		super.setParameters(params);
//	}


}
