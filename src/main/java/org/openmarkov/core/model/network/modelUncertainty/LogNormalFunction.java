/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

public class LogNormalFunction extends ProbDensFunction
{
    private double         mu;
    private double         sigma;
    /**
     * Auxiliary normal distribution used for sampling
     */
    private NormalFunction normal;

    public LogNormalFunction ()
    {
        super (TypeProbDensityFunction.LOGNORMAL);
    }

    @Override
    public int getNumberOfRequiredArguments ()
    {
        return 2;
    }

    @Override
    public void setParameters (Double[] args)
    {
        mu = args[0];
        sigma = args[1];
        normal = new NormalFunction (mu, sigma);
    }

    @Override
    public boolean isPossibleDistribution (boolean isChance)
    {
        return !isChance;
    }

    @Override
    public boolean doParametersVerifyDomainConstraint (boolean isChanceVariable)
    {
        return (sigma > 0);
    }

    @Override
    public double[] getParameters ()
    {
        double[] a = new double[2];
        a[0] = mu;
        a[1] = sigma;
        return a;
    }

    @Override
    public double getMaximum ()
    {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getMean ()
    {
        return Math.exp (mu + Math.pow (sigma, 2.0) / 2.0);
    }

    @Override
    public double getSample (Random randomGenerator)
    {
        return Math.exp (normal.getSample (randomGenerator));
    }

    @Override
    public double getVariance ()
    {
        double squareSigma;
        squareSigma = Math.pow (sigma, 2.0);
        return (Math.exp (squareSigma) - 1) * Math.exp (2 * mu + squareSigma);
    }
}
