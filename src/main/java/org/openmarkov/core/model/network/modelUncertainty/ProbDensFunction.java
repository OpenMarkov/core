/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

public abstract class ProbDensFunction
{
    private ProbDensityFunctionType type;

    public ProbDensityFunctionType getType ()
    {
        return type;
    }

    public void setType (ProbDensityFunctionType type)
    {
        this.type = type;
    }

    public ProbDensFunction (ProbDensityFunctionType type)
    {
        this.type = type;
    }

    public static int getNumberOfRequiredArguments (ProbDensityFunctionType type)
    {
        return constructNewProbDensFunction (type).getNumberOfRequiredArguments ();
    }

    public abstract int getNumberOfRequiredArguments ();

    public abstract double[] getParameters ();

    public void setParameters (String[] args)
    {
        Double[] values = parseDoubles (args);
        setParameters (values);
    }

    public abstract void setParameters (Double[] args);

    public static ProbDensFunction constructNewProbDensFunction (ProbDensityFunctionType type)
    {
        ProbDensFunction probDensFunction = null;
        switch (type)
        {
            case EXACT :
                probDensFunction = new ExactFunction ();
                break;
            case BETA :
                probDensFunction = new BetaFunction ();
                break;
            case COMPLEMENT :
                probDensFunction = new ComplementFunction ();
                break;
            case DIRICHLET :
                probDensFunction = new DirichletFunction ();
                break;
            case GAMMA :
                probDensFunction = new GammaFunction ();
                break;
            case GAMMAMV :
                probDensFunction = new GammamvFunction ();
                break;
            case LOGNORMAL :
                probDensFunction = new LogNormalFunction ();
                break;
            case NORMAL :
                probDensFunction = new NormalFunction ();
                break;
            case RANGE :
                probDensFunction = new RangeFunction ();
                break;
            case TRIANGULAR :
                probDensFunction = new TriangularFunction ();
                break;
            case EXPONENTIAL :
                probDensFunction = new ExponentialFunction ();
                break;
            case ERLANG :
                probDensFunction = new ErlangFunction ();
                break;
            case STANDARDNORMAL :
                probDensFunction = new StandardNormalFunction ();
                break;
        }
        return probDensFunction;
    }

    public abstract boolean isPossibleDistribution (boolean isChance);

    public static boolean isPossibleDistribution (ProbDensityFunctionType type, boolean isChance)
    {
        return constructNewProbDensFunction (type).isPossibleDistribution (isChance);
    }

    public abstract boolean verifyParametersDomain (boolean isChanceVariable);

    private static Double[] parseDoubles (String[] params)
    {
        Double[] values;
        values = new Double[params.length];
        for (int i = 0; i < params.length; i++)
        {
            values[i] = Double.parseDouble (params[i]);
        }
        return values;
    }

    /**
     * Some subclasses can override this method.
     * @return
     */
    public abstract double getMean ();

    public final double getStandardDeviation ()
    {
        return Math.sqrt (getVariance ());
    }

    public abstract double getVariance ();

    public abstract double getMaximum ();

    public abstract double getSample (Random randomGenerator);
}
