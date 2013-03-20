/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

public class ErlangFunction extends ProbDensFunction
{
    private int    k;
    private double lambda;
    
    /**
     * @param type
     * @param lambda
     */
    public ErlangFunction (int k, double lambda)
    {
        this ();
        this.k = k;
        this.lambda = lambda;
    }

    public ErlangFunction ()
    {
        super (TypeProbDensityFunction.ERLANG);
        // TODO Auto-generated constructor stub
    }

    @Override
    public int getNumberOfRequiredArguments ()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double[] getParameters ()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void placeParameters (Double[] args)
    {
    	  k = (int) Math.round(args[0]);
          lambda = args[1];
    }

    @Override
    public boolean isPossibleDistribution (boolean isChance)
    {
        return false;
    }

    @Override
    public boolean doParametersVerifyDomainConstraint (boolean isChanceVariable)
    {
        return (k >= 0) && (lambda > 0);
    }

    @Override
    public double getMean ()
    {
        return k / lambda;
    }

    @Override
    public double getMaximum ()
    {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getSample ()
    {
        double sumSamples;
        sumSamples = 0.0;
        for (int i = 0; i < k; i++)
        {
            sumSamples = sumSamples + (new ExponentialFunction (lambda)).getSample ();
        }
        return sumSamples;
    }

    @Override
    public double getVariance ()
    {
        return k / Math.pow (lambda, 2.0);
    }
}
