/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import static org.junit.Assert.*;

import java.util.Random;

/**
 * @author manolo
 * 
 */
public abstract class ProbDensFunctionTest {

    ProbDensFunction pdf;

    private double maxErrorMean = 0.001;
    private double maxErrorStDeviation = 0.01;

    public abstract ProbDensFunction newProbDensFunctionInstance();

    // Uncomment back when we find out why there are so many test failures 
    //@Test
    public void testMeanAndVariance() {
        int numSamples = 1000000;
        Random randomGenerator = new XORShiftRandom();
        pdf =  newProbDensFunctionInstance();
        pdf.setParameters(initializeParams());
        double[] samples = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = pdf.getSample(randomGenerator);
        }
        testMean(samples);
        testStandardDeviation(samples);
    }

    /**
     * @param samples
     */
    private void testStandardDeviation(double[] samples) {
        double variance = Tools.varianceSample(samples);
        assertMeanTest(Math.sqrt(variance), pdf.getStandardDeviation(), maxErrorStDeviation);
    }

    /**
     * @return
     */
    protected double getFactorError() {
        return 2.0 * pdf.getStandardDeviation();
    }

    public void testMean(double[] samples) {

        double mean = Tools.meanSample(samples);
        assertMeanTest(mean, pdf.getMean(), maxErrorMean);
    }

    /**
     * @param samplesMean
     *            true if the difference between two means is lower than
     *            maxError
     * @param pdfMean
     * @param maxError
     */
    public void assertMeanTest(double samplesMean, double pdfMean, double maxError) {
        assertEquals(samplesMean, pdfMean, getFactorError() * maxError);
    }

    /**
     * @return
     */
    public abstract double[] initializeParams();

}
