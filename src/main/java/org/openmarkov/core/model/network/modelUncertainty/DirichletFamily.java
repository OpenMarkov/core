/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.List;
import java.util.Random;

import cern.jet.random.Gamma;

public class DirichletFamily extends FamilyDistribution
{
    private double[] alpha;
    
    public DirichletFamily (List<UncertainValue> uncertainValues)
    {
        super (filterByFunction(DirichletFunction.class, uncertainValues));
        int size = family.size ();
        this.alpha = new double[size];
        for (int i = 0; i < size; i++)
        {
            alpha[i] = ((DirichletFunction)(family.get (i).getProbDensFunction())).getAlpha();
        }
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
   
    
    public double[] getSample (Random randomGenerator)
    {
        int length = alpha.length;
        double sum = 0.0;
        double[] samples = new double[length];
        
        for (int i = 0; i < length; i++)
        {
        	samples[i] = Gamma.staticNextDouble(alpha[i], 1);
        	sum += samples[i];
        }
        // Normalize the samples
        for (int i = 0; i < length; i++)
        {
            samples[i] /= sum;
        }
        return samples;
    }    
    
//    public double[] getSample (Random randomGenerator)
//    {
//        int length = alpha.length;
//        double sumAuxSamples = 0.0;
//        double auxSample;
//        double[] sample = new double[length];
//        double[] auxSamples = new double[length];
//        
//        //double min = Tools.min(alpha);
//        // Generate samples using Gamma distributions
//        for (int i = 0; i < length; i++)
//        {
//            auxSample = (new GammaFunction (alpha[i],1.0)).getSample (randomGenerator);
//            auxSamples[i] = auxSample;
//            sumAuxSamples = sumAuxSamples + auxSample;
//        }
//        // Normalize the samples
//        for (int i = 0; i < length; i++)
//        {
//            sample[i] = auxSamples[i] / sumAuxSamples;
//        }
//        return sample;
//    }    

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
