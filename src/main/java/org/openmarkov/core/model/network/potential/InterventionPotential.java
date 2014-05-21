package org.openmarkov.core.model.network.potential;

import java.util.List;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

public class InterventionPotential extends Potential {

	// Attributes
	private State state = null;
	
	// Constructor
	public InterventionPotential(List<Variable> variables, State state) {
		super(variables, PotentialRole.INTERVENTION);
		this.state = state;
	}

	// Methods
	@Override
	public List<TablePotential> tableProject(EvidenceCase evidenceCase,
			InferenceOptions inferenceOptions,
			List<TablePotential> projectedPotentials)
			throws NonProjectablePotentialException, WrongCriterionException {
		// This method has no sense
		return null;
	}

	@Override
	public Potential copy() {
		return new InterventionPotential(variables, state);
	}

	@Override
	public boolean isUncertain() {
		return false;
	}
	
	public State getDecisionValue() {
		return state;
	}
	
	public Variable getDecisionVariable() {
		return variables.get(0);
	}

}
