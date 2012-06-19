/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.ArrayList;

public class DirichletFamily extends FamilyDistribution {
	
	double[] alphas;

	
	
	public DirichletFamily(ArrayList<UncertainValue> siblings) {
		super(siblings);
		double alpha[];
				
		int size = family.size();
		alpha = new double[size];
		for (int i=0;i<size;i++){
			alpha[i]= ((DirichletFunction)(family.get(i).getProbDensityFunction())).alpha;
		}
		
		alphas = alpha;
	
	}
	
	
	public DirichletFamily(double[] alphas) {
					
		int size = alphas.length;
		this.alphas = new double[size];
		for (int i=0;i<size;i++){
			this.alphas[i]= alphas[i];
		}
			
	}
	
	public double[] getMean(){
		return Tools.normalize(alphas);
	}

	
	
	public double[] getSample() {
		int length = alphas.length;
		double sumAuxSamples;
		double auxSample;
		double[] sample=new double[length];
		double[] auxSamples = new double[length];
		sumAuxSamples = 0.0;
		//Generate samples using Gamma distributions
		for (int i=0;i<length;i++){
			auxSample=(new GammaFunction(alphas[i],1.0)).getSample();
			auxSamples[i] = auxSample;
			sumAuxSamples = sumAuxSamples+auxSample;
		}
		//Normalize the samples
		for (int i=0;i<length;i++){
			sample[i] = auxSamples[i]/sumAuxSamples;
		}
		return sample;
	}
	
	

}
