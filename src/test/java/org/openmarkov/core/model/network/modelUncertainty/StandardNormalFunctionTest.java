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
public class StandardNormalFunctionTest extends ProbDensFunctionTest {

    @Override
    public ProbDensityFunctionType getTypeProbDensFunction() {
        return ProbDensityFunctionType.STANDARDNORMAL;
    }

    @Override
    public Double[] initializeParamsProbDensFunctionTest() {
        return new Double[0];
    }

}
