package org.openmarkov.core.model.network.modelUncertainty;

public class GammamvFunctionTest extends GammaAbstractTest {

	@Override
	public TypeProbDensityFunction getTypeProbDensFunction() {
		return TypeProbDensityFunction.GAMMAMV;
	}

	@Override
	public Double[] initializeParamsProbDensFunctionTest() {
		// TODO Auto-generated method stub
		return super.initializeParamsProbDensFunctionTest();
	}

}
