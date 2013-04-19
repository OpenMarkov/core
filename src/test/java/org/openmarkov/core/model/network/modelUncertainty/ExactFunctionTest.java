/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

/**
 * @author manolo
 * 
 */
public class ExactFunctionTest extends ProbDensFunctionTest {

    @Override
    public TypeProbDensityFunction getTypeProbDensFunction() {

        return TypeProbDensityFunction.EXACT;
    }

    @Override
    public Double[] initializeParamsProbDensFunctionTest() {
        Double[] params = { 5.3 };
        return params;
    }

    /**
     * @return
     */
    protected double getFactorError() {
        return 1.0;
    }

}
