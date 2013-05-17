package org.openmarkov.core.model.network.modelUncertainty;

public class GammamvFunctionTest extends ProbDensFunctionTest {

    @Override
    public ProbDensFunction newProbDensFunctionInstance() {
        return new GammamvFunction();
    }
    
    @Override
    public double[] initializeParams() {
        double[] params = { 4.2, 3.0 };
        return params;
    }

}
