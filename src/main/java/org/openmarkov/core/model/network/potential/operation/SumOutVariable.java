package org.openmarkov.core.model.network.potential.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

public class SumOutVariable extends Marginalization {
	
	   /**
     * @param chanceVariable <code>Variable</code>
     * @param potentials <code>List</code> of <code>TablePotential</code>
     * @return A <code>List</code> with two <code>TablePotential</code>,
     * marginal probability and new utility in this order.
     */
    public SumOutVariable(Variable chanceVariable, 
    		Collection<TablePotential> potentials) {
    	this(chanceVariable,potentials,false);
    }
    
    /**
     * @param chanceVariable <code>Variable</code>
     * @param potentials <code>List</code> of <code>TablePotential</code>
     * @return A <code>List</code> with two <code>TablePotential</code>,
     * marginal probability and new utility in this order.
     */
    // TODO Documentar sdagInterventions o simplemente quitar parámetro si ya no se usan los SDAGs.
    public SumOutVariable(Variable chanceVariable, Collection<TablePotential> potentials, boolean sdagInterventions) {
		// Get probability and utility potentials
		List<TablePotential> probPotentials = new ArrayList<>();
		List<TablePotential> utilityPotentials = new ArrayList<>();
		classifyProbAndUtilityPotentials(potentials, probPotentials, utilityPotentials);
		boolean thereIsUtility = utilityPotentials.size() != 0;
		List<TablePotential> outputPotentials = new ArrayList<>();
		TablePotential marginalProb;

		marginalProb = DiscretePotentialOperations.multiplyAndMarginalize(probPotentials, chanceVariable);
		// Do not return the probability potential if it depends on no variables
		// and its value is 1

		if (thereIsUtility) {
			for (TablePotential inputUtilityPotential : utilityPotentials) {
				List<Variable> inputUtilityVariables = inputUtilityPotential.getVariables();
				boolean thereAreInterventions = inputUtilityPotential.interventions != null;

				// build the marginal and conditional probabilities
				TablePotential joinProb = DiscretePotentialOperations.multiply(probPotentials);
				if (joinProb == null) {
					joinProb = new TablePotential(new ArrayList<Variable>(), PotentialRole.CONDITIONAL_PROBABILITY);
				}
				TablePotential conditionalProb = DiscretePotentialOperations.divide(joinProb, marginalProb);

				// initialize the output utility potential
				List<Variable> outputUtilityVariables = marginalProb.getVariables();
				for (Variable variable : inputUtilityPotential.getVariables()) {
					if (variable != chanceVariable && !outputUtilityVariables.contains(variable)) {
						outputUtilityVariables.add(variable);
					}
				}
				TablePotential outputUtilityPotential = new TablePotential(outputUtilityVariables,
						PotentialRole.UNSPECIFIED);
				// TODO Check whether the next line can be removed
				outputUtilityPotential.setCriterion(inputUtilityPotential.getCriterion());
				if (thereAreInterventions) {
					int outputValuesLength = outputUtilityPotential.values.length;
					outputUtilityPotential.interventions = new Intervention[outputValuesLength];
				}

				List<Variable> allVariables = new ArrayList<>(outputUtilityVariables.size() + 1);
				allVariables.add(chanceVariable);
				allVariables.addAll(outputUtilityVariables);
				int numVariables = allVariables.size();
				int[] allVariablesDimensions = TablePotential.calculateDimensions(allVariables);

				// constants for the iterations
				int chanceVariableSize = chanceVariable.getNumStates();
				int[] accOffsetsConditionalProbPotential = TablePotential.getAccumulatedOffsets(allVariables,
						conditionalProb.getVariables());
				int[] accOffsetsInputUtilityPotential = TablePotential.getAccumulatedOffsets(allVariables,
						inputUtilityVariables);

				// auxiliary variables that may change in every iteration
				int[] allVariablesCoordinate = new int[numVariables];
				int outputUtilityPotentialPosition = 0;
				int conditionalProbPotentialPosition = 0;
				int inputUtilityPotentialPosition = 0;
				int increasedVariable = 0;

				double[] probabilities = new double[chanceVariableSize];
				Intervention[] interventions = new Intervention[chanceVariableSize];

				// outer iterations correspond to the variables to in the
				// outputUtilityPotential
				for (int outerIteration = 0; outerIteration < TablePotential
						.computeTableSize(outputUtilityVariables); outerIteration++) {
					double sum = 0;
					// inner iterations correspond to the chance variable to
					// eliminate
					for (int innerIteration = 0; innerIteration < chanceVariableSize; innerIteration++) {
						double auxProb = conditionalProb.values[conditionalProbPotentialPosition];
						// This "if" is to ensure 0*(-Infinity) = 0
						if (auxProb > 0) {
							sum += auxProb * inputUtilityPotential.values[inputUtilityPotentialPosition];
						}
						if (thereAreInterventions) {
							probabilities[innerIteration] = auxProb;
							interventions[innerIteration] = inputUtilityPotential.interventions[inputUtilityPotentialPosition];
						}

						// find the next configuration and the index of the
						// increased variable
						increasedVariable = DiscretePotentialOperations.findNextConfigurationAndIndexIncreasedVariable(
								allVariablesDimensions, allVariablesCoordinate, increasedVariable);

						// Update coordinates
						conditionalProbPotentialPosition += accOffsetsConditionalProbPotential[increasedVariable];
						inputUtilityPotentialPosition += accOffsetsInputUtilityPotential[increasedVariable];
					}

					outputUtilityPotential.values[outputUtilityPotentialPosition] = sum;
					if (thereAreInterventions) {
						outputUtilityPotential.interventions[outputUtilityPotentialPosition] = Intervention
								.averageOfInterventions(chanceVariable, probabilities, interventions,
										sdagInterventions);
					}

					outputUtilityPotentialPosition++;

				} // end of outer loop

				// Return the utility potential if some of its values is
				// different from 0.0
				// or if there are interventions
				if (thereAreInterventions
						|| DiscretePotentialOperations.thereAreRelevantUtilities(outputUtilityPotential)) {
					boolean criteriaFound = false;
					for (int i = 0; i < outputPotentials.size(); i++) {
						if (outputPotentials.get(i).getCriterion() == outputUtilityPotential.getCriterion()) {
							outputPotentials.set(i,
									DiscretePotentialOperations.sum(outputPotentials.get(i), outputUtilityPotential));
							criteriaFound = true;
							break;
						}
					}
					if (!criteriaFound) {
						outputPotentials.add(outputUtilityPotential);
					}
				}
			}
		} // end of if (!thereIsUtility)

		if (marginalProb.getNumVariables() > 0
				|| !DiscretePotentialOperations.almostEqual(marginalProb.values[0], 1.0)) {
			marginalProb.setPotentialRole(PotentialRole.JOINT_PROBABILITY);
			setProbability(marginalProb);
		}
		setUtility(DiscretePotentialOperations.sum(outputPotentials));

		// return outputPotentials;
	}

}
