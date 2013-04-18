/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

public class DirichletFunction extends ProbDensFunction
{
    private double alpha;

    public DirichletFunction ()
    {
        super (TypeProbDensityFunction.DIRICHLET);
    }

    @Override
    public void placeParameters (Double[] params)
    {
        alpha = params[0];
    }

    @Override
    public boolean doParametersVerifyDomainConstraint (boolean isChanceVariable)
    {
        return (alpha > 0);
    }

    @Override
    public boolean isPossibleDistribution (boolean isChance)
    {
        return isChance;
    }

    @Override
    public int getNumberOfRequiredArguments ()
    {
        return 1;
    }

    @Override
    public double[] getParameters ()
    {
        double[] a = new double[1];
        a[0] = alpha;
        return a;
    }

    @Override
    public double getMaximum ()
    {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public double getMean ()
    {
        return alpha;
    }

    @Override
    public double getSample (Random randomGenerator)
    {
        return 0;
    }

    @Override
    public double getVariance ()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Returns the alpha.
     * @return the alpha.
     */
    public double getAlpha ()
    {
        return alpha;
    }

    /**
     * Sets the alpha.
     * @param alpha the alpha to set.
     */
    public void setAlpha (double alpha)
    {
        this.alpha = alpha;
    }
}
