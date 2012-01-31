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

@RelationType(name="GeneralizedMax", family="ICI")
public class MaxPotential extends MinMaxPotential {

	/** @param model. <code>ICIModel</code>.
	 * @param variables. <code>ArrayList</code> of <code>Variable</code>. */
	public MaxPotential(
			ICIModelType model, ArrayList<Variable> variables) {
		super(model, variables);
		type = PotentialType.MAX;
	}
	
	/**
	 * 
	 * Constructor for MaxPotential that assumes the ICIModelType is GENERAL_MAX
	 * @param variables
	 */
	public MaxPotential (ArrayList<Variable> variables)
    {
        this (ICIModelType.GENERAL_MAX, variables);
    }
	
    public TablePotential getDefaultLeakyPotential ()
        throws NotEnoughMemoryException
    {
        ArrayList<Variable> leakyVariables = new ArrayList<Variable> ();
        leakyVariables.add (variables.get (0));
        TablePotential tablePotential = new TablePotential (leakyVariables,
                                                            PotentialRole.CONDITIONAL_PROBABILITY);
        double[] leakyParameters = new double[variables.get (0).getNumStates ()];
        leakyParameters[0] = 1.0;
        for (int i = 1; i < leakyParameters.length; ++i)
        {
            leakyParameters[0] = 0.0;
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
				deltaVariables, PotentialRole.JOINT_PROBABILITY);
		int numStatesConditioned = conditionedVariable.getNumStates();
		int numStatesPseudo = pseudoVariable.getNumStates(); // same number
		int actualConfiguration = 0;
		for (int i = 0; i < numStatesConditioned; i++) {
			for (int j = 0; j < numStatesPseudo; j++) {
				if (i == j) {
					deltaPotential.values[actualConfiguration] = 1;
				} else if (j == (i - 1)) {
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
	public TablePotential getAccruedPotential(TablePotential subPotential) 
			throws NotEnoughMemoryException {
		// TODO Revisar este metodo para el caso de un potential proyectado
		ArrayList<Variable> subPotentialVariables = subPotential.getVariables();
		ArrayList<Variable> accruedPotentialVariables =
			new ArrayList<Variable>(subPotentialVariables);
		accruedPotentialVariables.set(0, pseudoVariable);

		TablePotential accruedPotential = new TablePotential(
				accruedPotentialVariables, PotentialRole.JOINT_PROBABILITY);
		
		// number of states in the pseudovariable
		int numStates = variables.get(0).getNumStates();
		
		double accumulator = 0;
		for (int i = 0; i < subPotential.values.length; i++) {
			accumulator += subPotential.values[i];
			accruedPotential.values[i] = accumulator;
			if ((i + 1) % numStates == 0) {
				accumulator = 0;
			}
		}
		return accruedPotential;
	}

    @Override
    public double[] getDefaultLeakyParameters (int numStates)
    {
        double[] leakyParameters = new double[numStates];
        
        leakyParameters[0] = 1.0;
        for(int i=1; i<numStates; ++i)
        {
            leakyParameters[i] = 0.0;
        }
        return leakyParameters;
    }
	
}
