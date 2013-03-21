/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

public class RangeFunction extends ProbDensFunctionWithKnownInverseCDF
{
    private double a;
    private double b;

    public RangeFunction ()
    {
        super (TypeProbDensityFunction.RANGE);
    }

    /**
     * @param type
     * @param a
     * @param b
     */
    public RangeFunction (double a, double b)
    {
    	this();
        this.a = a;
        this.b = b;
    }

    @Override
    public void placeParameters (Double[] params)
    {
        a = params[0];
        b = params[1];
    }

    @Override
    public boolean doParametersVerifyDomainConstraint (boolean isChanceVariable)
    {
        return ((0 <= a) && (a < b) && (b <= 1) && isChanceVariable)
               || ((a < b) && !isChanceVariable);
    }

    @Override
    public boolean isPossibleDistribution (boolean isChance)
    {
        return true;
    }

    @Override
    public int getNumberOfRequiredArguments ()
    {
        return 2;
    }

    @Override
    public double[] getParameters ()
    {
        double[] x = new double[2];
        x[0] = a;
        x[1] = b;
        return x;
    }

    @Override
    public double getMaximum ()
    {
        return b;
    }

    @Override
    public double getMean ()
    {
        return (a + b) / 2;
    }

    @Override
    public double getInverseCumulativeDistributionFunction (double y)
    {
        return a + (b - a) * y;
    }

    @Override
    public double getVariance ()
    {
        return Math.pow (b - a, 2.0) / 12;
    }
}
