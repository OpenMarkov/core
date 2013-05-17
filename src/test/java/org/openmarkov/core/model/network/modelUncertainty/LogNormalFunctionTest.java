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
public class LogNormalFunctionTest extends ProbDensFunctionTest {

    @Override
    public ProbDensFunction newProbDensFunctionInstance() {
        return new LogNormalFunction();
    }

    @Override
    public double[] initializeParams() {
        double[] params = { 1.5, 1.0 };
        return params;
    }

    /**
     * @return
     */
    protected double getFactorError() {
        return Math.max(1.0, pdf.getStandardDeviation());
    }

}
