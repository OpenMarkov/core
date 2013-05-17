/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

@ProbDensFunctionType(name="Gamma-mv", isValidForProbabilities = false, parameters = {"mu", "sigma"})
public class GammamvFunction extends GammaAbstract
{
    private double mu;
    private double sigma;

    public GammamvFunction ()
    {
        this(0.0, 0.0);
    }
    
    public GammamvFunction (double mu, double sigma)
    {
        this.mu = mu;
        this.sigma = sigma;
    }    

    @Override
    public void setParameters (double[] parameters)
    {
        mu = parameters[0];
        sigma = parameters[1];
        this.kAbstract = Math.pow (mu / sigma, 2);
        this.thetaAbstract = Math.pow (sigma, 2) / mu;
    }

    @Override
    public boolean verifyParametersDomain (boolean isChanceVariable)
    {
        // TODO Auto-generated method stub
        return (mu > 0) && (sigma > 0);
    }

    @Override
    public double[] getParameters ()
    {
        double[] a = new double[2];
        a[0] = mu;
        a[1] = sigma;
        return a;
    }
}
