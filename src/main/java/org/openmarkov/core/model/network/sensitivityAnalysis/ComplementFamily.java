package org.openmarkov.core.model.network.sensitivityAnalysis;

import java.util.ArrayList;

import org.openmarkov.core.model.network.UncertainValue;

public class ComplementFamily extends FamilyDistribution {
	
	/**
	 * Probability mass for this family
	 */
	double probMass;
	

	public double getProbMass() {
		return probMass;
	}

	public void setProbMass(double probMass) {
		this.probMass = probMass;
	}

	public ComplementFamily(ArrayList<UncertainValue> arrayUncertain) {
		super(arrayUncertain);
	}
	
	public ComplementFamily() {
		//super(TypeProbDensityFunction.COMPLEMENT);
	}

	public double[] getMean(){
		
		int sizeFamily = family.size();
		
		double[]nu;
		nu = new double[sizeFamily];
				
		for (int i=0;i<sizeFamily;i++){
			nu[i] = ((ComplementFunction)(family.get(i).getProbDensityFunction())).getNu(); 
		}
		return Tools.normalize(nu,probMass);
	}
	
	public double[] getSample(){
		
		int sizeFamily = family.size();
		
		double[]nu;
		nu = new double[sizeFamily];
				
		for (int i=0;i<sizeFamily;i++){
			nu[i] = ((ComplementFunction)(family.get(i).getProbDensityFunction())).getNu(); 
		}
		return Tools.normalize(nu,probMass);
	}

}
