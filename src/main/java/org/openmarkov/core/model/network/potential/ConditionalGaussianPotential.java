/*
* Copyright 2014 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import umontreal.iro.lecuyer.probdist.NormalDist;

/**
 * This class represents a conditional Gaussian potential for discrete variables. 
 * It is defined by two potentials, namely the mean and the variance potentials 
 * In the case of discrete variables it uses each state index 
 */
@PotentialType(name = "Conditional Gaussian")
public class ConditionalGaussianPotential extends Potential{

	private Potential mean;
	private Potential variance;
	
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     * @param node. <code>Node</code>
     * @param variables. <code>ArrayList</code> of <code>Variable</code>.
     * @param role. <code>PotentialRole</code>.
     */
    public static boolean validate (Node node, List<Variable> variables, PotentialRole role)
    {
    	// not a utility potential, only discrete or discretized conditioned variables
    	return role != PotentialRole.UTILITY && !variables.isEmpty() 
        		&& variables.get(0).getVariableType() != VariableType.NUMERIC;
    }
    
	public ConditionalGaussianPotential(Variable utilityVariable,
			List<Variable> variables) {
		super(utilityVariable, variables);
		mean = getDefaultMeanPotential();
		variance = getDefaultVariancePotential();
	}

	public ConditionalGaussianPotential(Potential potential) {
		super(potential);
		
		if(potential instanceof ConditionalGaussianPotential)
		{
			mean =  ((ConditionalGaussianPotential)potential).mean.copy();
			variance =  ((ConditionalGaussianPotential)potential).variance.copy();
		}
	}

	public ConditionalGaussianPotential(List<Variable> variables,
			PotentialRole role) {
		super(variables, role);
		
		mean = getDefaultMeanPotential(); 
		variance = getDefaultVariancePotential();
	}
	
	public Potential getMean() {
		return mean;
	}

	public void setMean(Potential mean) {
		this.mean = mean;
	}

	public Potential getVariance() {
		return variance;
	}

	public void setVariance(Potential variance) {
		this.variance = variance;
	}

	@Override
	public List<TablePotential> tableProject(EvidenceCase evidenceCase,
			InferenceOptions inferenceOptions,
			List<TablePotential> projectedPotentials)
			throws NonProjectablePotentialException, WrongCriterionException {
		// returned value

        List<Variable> unobservedVariables = new ArrayList<Variable>(variables);
        if (evidenceCase != null) {
            unobservedVariables.removeAll(evidenceCase.getVariables());
        }
        // Create projected potential
        TablePotential projectedPotential = new TablePotential(unobservedVariables, role);
        // If there is no unobserved variables, the resulting potential is constant
        if (unobservedVariables.isEmpty()) {// Projection = constant potential
        	// For the time being no utility potentials are supported
            projectedPotential.values[0] = 1.0; 
        } else { 
        	// Project mean and variance potentials
        	TablePotential projectedMeanPotential = mean.tableProject(evidenceCase, inferenceOptions, projectedPotentials).get(0);
        	TablePotential projectedVariancePotential = variance.tableProject(evidenceCase, inferenceOptions, projectedPotentials).get(0);
        	
        	int numConfigurations = projectedMeanPotential.tableSize;
            // Go trough this potential using accumulatedOffests
            int numStates = getConditionedVariable().getNumStates();
            // TODO define thresholds
            double[] thresholds = getThresholds();
            // Copy configurations using the accumulated offsets algorithm
            for (int configuration = 0; configuration < numConfigurations; configuration++) {
            	int configurationIndex = configuration*numStates;
            	double mean = projectedMeanPotential.values[configuration];
            	double variance = projectedVariancePotential.values[configuration];
            	double lastCdf = 0;
            	for (int i = 0; i < numStates - 1; i++) {
            		double cdf = NormalDist.cdf(mean, variance, thresholds[i]);
            		projectedPotential.values[configurationIndex+i] = cdf - lastCdf;
            		lastCdf = cdf;
            	}
            	// The remaining probability is assigned to the last state
            	projectedPotential.values[configurationIndex+(numStates-1)] = 1-lastCdf;
            }
        }
        return Arrays.asList(projectedPotential);
	}

	private double[] getThresholds() {
		Variable conditionedVariable = getConditionedVariable();
		int numStates = conditionedVariable.getNumStates();
		double[] thresholds = new double[numStates];
		if(conditionedVariable.getVariableType() == VariableType.DISCRETIZED)
		{
			double[] limits = conditionedVariable.getPartitionedInterval().getLimits();
			// Ignore first limit, as it is considered minus infinity
			for(int i=0; i<numStates;++i)
				thresholds[i] = limits[i+1]; 
		}else
		{
			// Default thresholds
			for(int i=0; i<numStates;++i)
				thresholds[i] = i+0.5; 
		}
		return thresholds;
	}

	@Override
	public Potential copy() {
		return new ConditionalGaussianPotential(this);
	}

	@Override
	public boolean isUncertain() {
		return false;
	}
	
	private Potential getDefaultMeanPotential()
	{
		Variable meanVariable = new Variable("Mean");
		List<Variable> meanPotentialVariables = new ArrayList<>(variables);
		// Remove conditioned variable
		meanPotentialVariables.remove(0);
		// We create a utility potential because it is the only kind of
		// potential assumed to have a numeric conditioned variable
		return new TablePotential(meanVariable, meanPotentialVariables);
	}

	private Potential getDefaultVariancePotential()
	{
		Variable varianceVariable = new Variable("Variance");
		List<Variable> variancePotentialVariables = new ArrayList<>(variables);
		// Remove conditioned variable
		variancePotentialVariables.remove(0);
		// We create a utility potential because it is the only kind of
		// potential assumed to have a numeric conditioned variable
		TablePotential variancePotential = new TablePotential(varianceVariable, variancePotentialVariables);
		// Set variance to 1 for all configurations
		for(int i=0;i<variancePotential.values.length;++i)
			variancePotential.values[i] = 1;
		return variancePotential;
	}

	@Override
	public Potential addVariable(Variable variable) {
		variables.add(variable);
		mean = mean.addVariable(variable);
		variance = variance.addVariable(variable);
		return this;
	}

	@Override
	public Potential removeVariable(Variable variable) {
		variables.remove(variable);
		mean = mean.removeVariable(variable);
		variance = variance.removeVariable(variable);
		return this;
	}
	
	

}
