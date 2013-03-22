/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.List;

public class DirichletFamily extends FamilyDistribution
{
    private double[] alpha;

    public DirichletFamily (List<UncertainValue> siblings)
    {
        super (siblings);
        double alpha[];
        int size = family.size ();
        alpha = new double[size];
        for (int i = 0; i < size; i++)
        {
        	alpha[i] = ((DirichletFunction)(siblings.get(i).getProbDensityFunction())).getAlpha();
            ((DirichletFunction) (family.get (i).getProbDensityFunction ())).setAlpha (alpha[i]);
        }
        this.alpha = alpha;
    }

    public DirichletFamily (double[] alphas)
    {
        int size = alphas.length;
        this.alpha = new double[size];
        for (int i = 0; i < size; i++)
        {
            this.alpha[i] = alphas[i];
        }
    }

    public double[] getMean ()
    {
        return Tools.normalize (alpha);
    }

    public double[] getSample ()
    {
        int length = alpha.length;
        double sumAuxSamples;
        double auxSample;
        double[] sample = new double[length];
        double[] auxSamples = new double[length];
        sumAuxSamples = 0.0;
        
        //double min = Tools.min(alpha);
        // Generate samples using Gamma distributions
        for (int i = 0; i < length; i++)
        {
            auxSample = (new GammaFunction (alpha[i],1.0)).getSample ();
            auxSamples[i] = auxSample;
            sumAuxSamples = sumAuxSamples + auxSample;
        }
        // Normalize the samples
        for (int i = 0; i < length; i++)
        {
            sample[i] = auxSamples[i] / sumAuxSamples;
        }
        return sample;
    }

	/* (non-Javadoc)
	 * @see org.openmarkov.core.model.network.modelUncertainty.FamilyDistribution#getVariance()
	 */
	@Override
	public double[] getVariance() {
		double[] variance;
		double sumAlpha;
		
		sumAlpha = Tools.sum(alpha);
		variance = new double[alpha.length];
		for (int i=0;i<alpha.length;i++){
			double alphaI = alpha[i];
			variance[i] = alphaI*(sumAlpha-alphaI)/(Math.pow(sumAlpha, 2.0)*(sumAlpha+1.0));
		}
		return variance;
	}
}
