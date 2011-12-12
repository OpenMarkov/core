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


public class FamilyDistribution {
	
	ArrayList<UncertainValue> family;

	
	public ArrayList<UncertainValue> getFamily() {
		return family;
	}

	public void setFamily(ArrayList<UncertainValue> family) {
		this.family = family;
	}

	public FamilyDistribution(){
		family = null;
	}
		
	public FamilyDistribution(ArrayList<UncertainValue> arrayUncertain) {

		family = arrayUncertain;
	}

	
	public static FamilyDistribution constructNewFamilyDistributions(
			ArrayList<UncertainValue> siblings, TypeProbDensityFunction type2) {

		FamilyDistribution famDist = null;
		switch (type2){
		case DIRICHLET:
			famDist = new DirichletFamily(siblings);
			break;
		case COMPLEMENT:
			famDist = new ComplementFamily(siblings);
			break;
		}
		return famDist;
	}

	public double[] getMean() {
		double []mean;
		int size = family.size();
		mean = new double[size];
		for (int i=0;i<size;i++){
			mean[i]=family.get(i).getProbDensityFunction().getMean();
		}
		return mean;
		
	}
	
	public double[] getMaximum(){
		double []max;
		int size = family.size();
		max = new double[size];
		for (int i=0;i<size;i++){
			max[i]=family.get(i).getProbDensityFunction().getMaximum();
		}
		return max;
		
	}
	
	public void remove(UncertainValue child){
		family.remove(child);
	}

	public double[] getSample() {
		double []mean;
		int size = family.size();
		mean = new double[size];
		for (int i=0;i<size;i++){
			mean[i]=family.get(i).getProbDensityFunction().getSample();
		}
		return mean;
	}
	


}
