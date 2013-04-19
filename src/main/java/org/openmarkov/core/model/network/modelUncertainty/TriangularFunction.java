/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

public class TriangularFunction extends ProbDensFunctionWithKnownInverseCDF
{
    /**
     * Minimum
     */
    double a;
    /**
     * Maximum
     */
    double b;
    /**
     * Mode
     */
    double c;

    public TriangularFunction ()
    {
        super (ProbDensityFunctionType.TRIANGULAR);
    }

    @Override
    public void setParameters (Double[] params)
    {
        a = params[0];
        b = params[1];
        c = params[2];
    }

    @Override
    public boolean verifyParametersDomain (boolean isChanceVariable)
    {
        return (((0 <= a) && (a <= c) && (c <= b) && (b <= 1) && (a < b)) && isChanceVariable)
               || ((a <= c) && (c <= b) && (a < b) && !isChanceVariable);
    }

    @Override
    public boolean isPossibleDistribution (boolean isChance)
    {
        return true;
    }

    @Override
    public int getNumberOfRequiredArguments ()
    {
        return 3;
    }

    @Override
    public double[] getParameters ()
    {
        double[] x = new double[3];
        x[0] = a;
        x[1] = b;
        x[2] = c;
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
        return (a + b + c) / 3;
    }

    @Override
    public double getInverseCumulativeDistributionFunction (double y)
    {
        double sample;
        double diffBA;
        double ratioCABA;
        diffBA = b - a;
        double diffBC = b - c;
        double diffCA = c - a;
        ratioCABA = diffCA / diffBA;
        // if (x<ratioCABA){
        if (y < ratioCABA)
        {
            // if (x<c){
            sample = a + Math.sqrt (y * diffBA * diffCA);
        }
        else
        {
            sample = b - Math.sqrt ((1 - y) * diffBA * diffBC);
        }
        return sample;
    }

    @Override
    public double getVariance ()
    {
        return (Tools.square (a) + Tools.square (b) + Tools.square (c) - a * b - a * c - b * c) / 18;
    }
}
