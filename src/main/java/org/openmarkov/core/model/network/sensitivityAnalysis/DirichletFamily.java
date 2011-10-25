package org.openmarkov.core.model.network.sensitivityAnalysis;

import java.util.ArrayList;

import org.openmarkov.core.model.network.UncertainValue;

import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import umontreal.iro.lecuyer.randvarmulti.DirichletGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class DirichletFamily extends FamilyDistribution {
	
	double[] alphas;

	protected MRG32k3a stream;
	
	protected DirichletGen generator;
	
	public DirichletFamily(ArrayList<UncertainValue> siblings) {
		super(siblings);
		double alpha[];
				
		int size = family.size();
		alpha = new double[size];
		for (int i=0;i<size;i++){
			alpha[i]= ((DirichletFunction)(family.get(i).getProbDensityFunction())).alpha;
		}
		ssjPDFM = new DirichletDist(alpha);
		alphas = alpha;
	
	}
	
	public double[] getMean(){
		return ssjPDFM.getMean();
	}

	
	public void initializeGenerator() {
		generator = new DirichletGen(stream,alphas);
		
	}

	public void createRandomGenerator() {
		stream = new MRG32k3a();
	}
	
	public double[] getSample() {
		double[] sample=new double[alphas.length];
		generator.nextPoint(sample);
		return sample;
	}
	

}
