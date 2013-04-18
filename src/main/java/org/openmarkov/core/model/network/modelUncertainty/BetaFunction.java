/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

public class BetaFunction extends ProbDensFunction
{
    double alpha;
    double beta;

    public BetaFunction ()
    {
        super (TypeProbDensityFunction.BETA);
    }

    @Override
    public void placeParameters (Double[] params)
    {
        alpha = params[0];
        beta = params[1];
    }

    @Override
    public boolean doParametersVerifyDomainConstraint (boolean isChanceVariable)
    {
        return ((alpha > 0) && (beta > 0));
    }

    @Override
    public boolean isPossibleDistribution (boolean isChance)
    {
        return isChance;
    }

    @Override
    public int getNumberOfRequiredArguments ()
    {
        return 2;
    }

    @Override
    public double[] getParameters ()
    {
        double[] a = new double[2];
        a[0] = alpha;
        a[1] = beta;
        return a;
    }

    @Override
    public double getMaximum ()
    {
        return 1;
    }

    @Override
    public double getMean ()
    {
        return alpha / (alpha + beta);
    }

    @Override
    public double getSample (Random randomGenerator)
    {
        double[] alphas;
        DirichletFamily family;
        double sample;
        // We use a Dirichlet family for obtaining the sample
        alphas = new double[2];
        alphas[0] = alpha;
        alphas[1] = beta;
        family = new DirichletFamily (alphas);
        sample = family.getSample (randomGenerator)[0];
        return sample;
    }

    @Override
    public double getVariance ()
    {
        double sumAlphaBeta;
        sumAlphaBeta = alpha + beta;
        return (alpha * beta) / (Math.pow (sumAlphaBeta, 2.0) * (sumAlphaBeta + 1));
    }
}
