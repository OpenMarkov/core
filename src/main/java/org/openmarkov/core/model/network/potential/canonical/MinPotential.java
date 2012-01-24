/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.canonical;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.plugin.RelationType;

@RelationType(name="Min")
public class MinPotential extends MinMaxPotential {

	/** @param model. <code>ICIModel</code>.
	 * @param variables. <code>ArrayList</code> of <code>Variable</code>. */
	public MinPotential(
			ICIModelType modelType, ArrayList<Variable> variables) {
		super(modelType, variables);
		type = PotentialType.MIN;
	}
	
	/**
	 * 
	 * Constructor for MinPotential that assumes the ICIModelType is GENERAL_MIN
	 * @param variables
	 * @param role
	 */
    public MinPotential(ArrayList<Variable> variables) {
                this(ICIModelType.GENERAL_MIN, variables);
    }
    
    public TablePotential getDefaultLeakyPotential ()
            throws NotEnoughMemoryException
        {
            ArrayList<Variable> leakyVariables = new ArrayList<Variable> ();
            leakyVariables.add (variables.get (0));
            TablePotential tablePotential = new TablePotential (leakyVariables,
                                                                PotentialRole.CONDITIONAL_PROBABILITY);
            double[] leakyParameters = new double[variables.get (0).getNumStates ()];
            leakyParameters[leakyParameters.length - 1] = 1.0;
            for (int i = 0; i < leakyParameters.length - 1; ++i)
            {
                leakyParameters[i] = 0.0;
            }
            tablePotential.values = leakyParameters;
            return tablePotential;
        }       

	@Override
	/** @returns A <code>TablePotential</code> with two variables: 
	 *  <code>conditionedVariable</code> and <code>pseudoVariable</code>. */
	public TablePotential getDeltaPotential()
			throws NotEnoughMemoryException {
		Variable conditionedVariable = variables.get(0);
		ArrayList<Variable> deltaVariables = new ArrayList<Variable>();
		deltaVariables.add(pseudoVariable);
		deltaVariables.add(conditionedVariable);
		TablePotential deltaPotential = new TablePotential(
				deltaVariables, PotentialRole.CONDITIONAL_PROBABILITY);
		int numStatesConditioned = conditionedVariable.getNumStates();
		int numStatesPseudo = pseudoVariable.getNumStates(); // same number
		int actualConfiguration = 0;
		for (int i = 0; i < numStatesConditioned; i++) {
			for (int j = 0; j < numStatesPseudo; j++) {
				if (i == j) {
					deltaPotential.values[actualConfiguration] = 1;
				} else if (j == (i + 1)) {
					deltaPotential.values[actualConfiguration] = -1;
				} else {
					deltaPotential.values[actualConfiguration] = 0;
				}
				actualConfiguration++;
			}
		}
		return deltaPotential;
	}

	@Override
	/** @param subPotential. <code>TablePotential</code> 
	 *  In general it will be the conditional probability associated with 
	 *  a link of the ICI model (i.e., a conditional probability of the child 
	 *  node given the parent node) or the leak probability.
	 * @return The accrued potential. <code>TablePotential</code>. I.e., if 
	 *  subPotential is P(y) then the accrued potential is P(Y>=y), and if
	 *  the subPotential is P(y|x) then the accrued potential is P(Y>=y|x).
	 * @reference Efficient computation for the Noisy MAX
	 * @argCondition subPotential is a probability table of one variable
	 *  or a probability table of one variable given another variable. */
	protected TablePotential getAccruedPotential(TablePotential subPotential)
			throws NotEnoughMemoryException {
		// TODO Revisar este metodo para el caso de un potential proyectado
		ArrayList<Variable> subPotentialVariables = subPotential.getVariables();
		// Create a new TablePotential with the same variables,
		// except the first one, which is replaced by the pseudovariable
        ArrayList<Variable> accruedPotentialVariables =
                new ArrayList<Variable>(subPotentialVariables);
        accruedPotentialVariables.set(0, pseudoVariable);
        TablePotential accruedPotential = new TablePotential (accruedPotentialVariables,
                                                              PotentialRole.CONDITIONAL_PROBABILITY);
		
		// number of states in the pseudovariable
		int numStates = variables.get(0).getNumStates();
		
		double accumulator = 0;
		for (int i = subPotential.values.length - 1; i >= 0; i--) {
			accumulator += subPotential.values[i];
			accruedPotential.values[i] = accumulator;
			if (i % numStates == 0) {
				accumulator = 0;
			}
		}

		return accruedPotential;
	}
	
	
    @Override
    public double[] getDefaultLeakyParameters (int numStates)
    {
        double[] leakyParameters = new double[numStates];
        
        leakyParameters[numStates-1] = 1.0;
        for(int i=0; i<numStates-1; ++i)
        {
            leakyParameters[i] = 0.0;
        }
        return leakyParameters;
    }	
	
}
