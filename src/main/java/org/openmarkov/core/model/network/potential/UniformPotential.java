package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.HashSet;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

/** Potential with discrete and/or continuous variables.
 * @author marias
 * @version 1.0 */
public class UniformPotential extends Potential {

	// Attributes
	/** Value of a potential configuration when all the variables are 
	 * discrete. */
	private double discreteValue = 0.0;
	
	// Constructor
	public UniformPotential(ArrayList<Variable> variables, PotentialRole role) {
		super(variables, role);
		if (allVariablesAreDiscrete(variables)) {
			discreteValue = calculateDiscreteValue(variables);
		}
		type = PotentialType.UNIFORM;
	}

	// Methods
	@Override
	/** @return If this is a utility potential, it represents the case in 
	 * which all the utilities are zero; therefore, it suffices to return
	 * an empty list. If this is a conditional probability P(Y|X1,...,Xn), it 
	 * returns a <code>TablePotential<code> that is uniform potential P(y). 
	 * If this is a join probability, P(X1,...,Xn), it returns a 
	 * <code>TablePotential<code> that is equal to this potential.
	 * In all cases, the argument <code>evidenceCase</code> is irrelevant.
	 * @param evidenceCase. <code>evidenceCase</code>
	 * @throws NonProjectablePotentialException when this is a conditional
	 * probability potential and the conditioned variable is numeric. */
	public ArrayList<TablePotential> tableProject(EvidenceCase evidenceCase, 
			InferenceOptions inferenceOptions)
			throws NotEnoughMemoryException, NonProjectablePotentialException {
		TablePotential projectedPotential;
		switch(role) {
		case CONDITIONAL_PROBABILITY: 
		case JOIN_PROBABILITY: 
		case POLICY:
			Variable conditionedVariable = variables.get(0);
			if (evidenceCase.contains(conditionedVariable)) {
				if (conditionedVariable.getVariableType() == 
						VariableType.NUMERIC) {
					// returns an empty list of potentials
					return new ArrayList<TablePotential>();
				} else {
					// returns a constant
					projectedPotential = 
						new TablePotential(new ArrayList<Variable>(),
								PotentialRole.CONDITIONAL_PROBABILITY);
					projectedPotential.values[0] = 
						1 / conditionedVariable.getNumStates();
				}
			} else {
				// the conditioned variable does not make part of the evidence
				if (conditionedVariable.getVariableType() == 
					VariableType.NUMERIC
				) {
					throw new NonProjectablePotentialException("Numeric variable " + 
							conditionedVariable.getName() + " makes it impossible "
							+ "to project this uniform potential into a table.");

				} else {
					// returns a uniform potential
					ArrayList<Variable> potentialVariables = 
						new ArrayList<Variable>(1);
					projectedPotential = new TablePotential(potentialVariables,
							PotentialRole.CONDITIONAL_PROBABILITY);
				}
			}
		//TODO write the code for other types of potentials but remember that
		// in the case of utility potentials it suffices to return the empty list. 
		}  // end of switch/case statement
		ArrayList<TablePotential> projectedPotentials = 
			new ArrayList<TablePotential>();
		return projectedPotentials;
	}

	/** @param evidenceCase. <code>EvidenceCase</code>
	 * @return <code>true</code> when the potential has no variables in the
	 * <code>evidenceCase</code> */
	private boolean noVariablesInEvidenceCase(EvidenceCase evidenceCase) {
		HashSet<Variable> evidenceVariables = 
			new HashSet<Variable>(evidenceCase.getVariables());
		for (Variable variable : variables) {
			if (evidenceVariables.contains(variable)) {
				return false;
			}
		}
		return true;
	}

	/** @return discreteValue. <code>double</code> */
	public double getDiscreteValue() {
		return discreteValue;
	}

	/** @return <code>true</code> if all the variables are FINITE_STATES.
	 * @param variables. <code>ArrayList</code> of <code>Variable</code> */
	private boolean allVariablesAreDiscrete(ArrayList<Variable> variables) {
		for (Variable variable : variables) {
			if (variable.getVariableType() != VariableType.FINITE_STATES) {
				return false;
			}
		}
		return true;
	}

	/** @param variables. <code>ArrayList</code> of <code>Variable</code>
	 * @return 1 / multiplication of the number of states of conditioning 
	 * variables. */
	private double calculateDiscreteValue(ArrayList<Variable> variables) {
		int statesSpace = 1;
		for (int i = 1; i < variables.size(); i++) {
			statesSpace *= variables.get(i).getNumStates();
		}
		return 1 / new Double(statesSpace);
	}

	@Override
	public Potential shift(ProbNet probNet, int timeSlice)
			throws ProbNodeNotFoundException, NotEnoughMemoryException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
