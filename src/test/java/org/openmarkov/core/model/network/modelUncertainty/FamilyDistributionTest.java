/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author manolo
 * 
 */
public abstract class FamilyDistributionTest {

    FamilyDistribution family;

    double maxErrorMean = 0.001;
    double maxErrorStDeviation = 0.01;

    @Test
    public void testMeanAndVariance() {
        int numSamples = 100000;
        Random randomGenerator = new XORShiftRandom();
        family = constructAndInitializeNewFamilyDistributions();

        List<double[]> samples = new ArrayList<>();
        for (int i = 0; i < numSamples; i++) {
            samples.add(family.getSample(randomGenerator));
        }
        testMean(samples);
        testStandardDeviation(samples);

    }

    /**
     * @return
     */
    private FamilyDistribution constructAndInitializeNewFamilyDistributions() {

        List<UncertainValue> list = initializeListUncertainValues();

        FamilyDistribution fam = FamilyDistribution.constructNewFamilyDistributions(list,
                getTypeProbDensFunction());
        return fam;
    }

    /**
     * @return
     */
    protected abstract List<UncertainValue> initializeListUncertainValues();

    /**
     * @param samples
     */
    private void testMean(List<double[]> samples) {

        int numChildrenFam = samples.get(0).length;
        double[] auxSamples;
        double[] meanSample;
        meanSample = new double[numChildrenFam];
        for (int i = 0; i < numChildrenFam; i++) {
            auxSamples = new double[samples.size()];
            for (int j = 0; j < samples.size(); j++) {
                auxSamples[j] = samples.get(j)[i];
            }
            meanSample[i] = Tools.meanSample(auxSamples);
        }

        assertMeanTest(meanSample, family.getMean(), maxErrorMean);

    }

    /**
     * @param samples
     */
    private void testStandardDeviation(List<double[]> samples) {
        int numChildrenFam = samples.get(0).length;
        double[] auxSamples;
        double[] stDSample;
        stDSample = new double[numChildrenFam];
        for (int i = 0; i < numChildrenFam; i++) {
            auxSamples = new double[samples.size()];
            for (int j = 0; j < samples.size(); j++) {
                auxSamples[j] = samples.get(j)[i];
            }
            stDSample[i] = Math.sqrt(Tools.varianceSample(auxSamples));
        }
        assertMeanTest(stDSample, family.getStandardDeviation(), maxErrorStDeviation);

    }

    /**
     * @param meanSample
     * @param mean
     * @param maxErrorMean2
     */
    private void assertMeanTest(double[] meanSample, double[] meanFamily, double maxErrorMean2) {

        for (int i = 0; i < meanSample.length; i++) {
            assertEquals(meanSample[i], meanFamily[i], maxErrorMean2);
        }

    }

    /**
     * @return
     */
    protected abstract TypeProbDensityFunction getTypeProbDensFunction();

}
