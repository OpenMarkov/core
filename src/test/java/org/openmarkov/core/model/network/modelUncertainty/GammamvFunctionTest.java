package org.openmarkov.core.model.network.modelUncertainty;

public class GammamvFunctionTest extends GammaAbstractTest {

    @Override
    public ProbDensityFunctionType getTypeProbDensFunction() {
        return ProbDensityFunctionType.GAMMAMV;
    }

    @Override
    public Double[] initializeParamsProbDensFunctionTest() {
        Double[] params = { 4.2, 3.0 };
        return params;
    }

}
