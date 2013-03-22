/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

public class GammaFunction extends GammaAbstract
{
    private double k;
    private double theta;

    /**
     * @param k
     * @param theta
     */
    public GammaFunction (double k, double theta)
    {
        this ();
        this.k = k;
        this.theta = theta;
        this.kAbstract = k;
        this.thetaAbstract = theta;
    }

    public GammaFunction ()
    {
        super (TypeProbDensityFunction.GAMMA);
    }

    @Override
    public void auxPlaceParameters (Double[] params)
    {
        k = params[0];
        theta = params[1];
        this.kAbstract = k;
        this.thetaAbstract = theta;
    }

    @Override
    public boolean doParametersVerifyDomainConstraint (boolean isChanceVariable)
    {
        return (k > 0) && (theta > 0);
    }

    @Override
    public double[] getParameters ()
    {
        double[] a = new double[2];
        a[0] = k;
        a[1] = theta;
        return a;
    }
}
