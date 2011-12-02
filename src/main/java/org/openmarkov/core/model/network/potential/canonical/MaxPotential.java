package org.openmarkov.core.model.network.potential.canonical;

import java.util.ArrayList;
import java.util.Collection;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.TablePotential;

public class MaxPotential extends MinMaxPotential {

	/** @param model. <code>ICIModel</code>.
	 * @param variables. <code>ArrayList</code> of <code>Variable</code>. */
	public MaxPotential(
			ICIModelType model, ArrayList<Variable> variables, PotentialRole role) {
		super(model, variables, role);
		type = PotentialType.MAX;
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
				deltaVariables, PotentialRole.JOIN_PROBABILITY);
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
	public TablePotential accruedPotential(TablePotential subPotential) 
			throws NotEnoughMemoryException {
		// TODO Revisar este metodo para el caso de un potential proyectado
		ArrayList<Variable> subPotentialVariables = subPotential.getVariables();
		ArrayList<Variable> accruedPotentialVariables =
			new ArrayList<Variable>();
		accruedPotentialVariables.add(pseudoVariable);
		for (int i = 1; i < subPotentialVariables.size(); i++) {
			accruedPotentialVariables.add(subPotentialVariables.get(i));
		}
		TablePotential accruedPotential = new TablePotential(
				accruedPotentialVariables, PotentialRole.JOIN_PROBABILITY);
		
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
	
}
