package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

public class Intervention extends TreeADDPotential {

	// Constructors
	/**
	 * @param variables
	 */
	public Intervention(List<Variable> variables) {
		super(variables, PotentialRole.INTERVENTION);
	}

	// Static methods
	/**
	 * Creates an intervention whose topVariable is a finite-states chance variable. Each branch has one state.
	 * @param topVariable
	 * @param states
	 * @param interventions
	 * @param variables
	 */
	public Intervention(Variable topVariable, List<State> states, 
			List<Intervention> interventions, List<Variable> variables) {
		super(variables, topVariable, PotentialRole.INTERVENTION);
		for (int i = 0; i < interventions.size(); i++) {
			List<State> branchStates = new ArrayList<State>(1);
			branchStates.add(states.get(i));
			TreeADDBranch branch = new TreeADDBranch(branchStates, topVariable, interventions.get(i), null);
			this.addBranch(branch);
		}
	}

	/**
	 * @param chanceVariable
	 * @param probabilities
	 * @param interventions
	 * @return
	 */
	public static Intervention averageOfInterventions(Variable chanceVariable, 
			double[] probabilities, Intervention[] interventions) {
		Intervention intervention = null;
		State[] states = chanceVariable.getStates();
		// Select interventions and states whose probability is greater than 0.0.
		List<Intervention> selectedInterventions = new ArrayList<Intervention>();
		List<State> selectedStates = new ArrayList<State>();
		Set<Variable> allVariables = new HashSet<Variable>();
		for (int i = 0; i < probabilities.length; i++) {
			if (probabilities[i] > 0.0) {
				selectedInterventions.add(interventions[i]);
				selectedStates.add(states[i]);
				allVariables.addAll(interventions[i].getVariables());
			}
		}
		if (selectedInterventions.size() > 1) {
			intervention = new Intervention(chanceVariable, selectedStates, selectedInterventions, 
					new ArrayList<Variable>(allVariables));
		} else {
			if (selectedInterventions.size() == 1) {
				intervention = selectedInterventions.get(0);
			}
		}
		return intervention;
	}
	
	/**
	 * @param decisionVariable
	 * @param utilities
	 * @param interventions
	 * @return
	 */
	public static Intervention optimalIntervention(Variable decisionVariable, 
			double[] utilities, Intervention[] interventions) {
		State[] states = decisionVariable.getStates();
		// Select interventions and states whose probability is greater than 0.0.
		List<Intervention> selectedInterventions = new ArrayList<Intervention>();
		List<State> optimalStates = new ArrayList<State>();
		Set<Variable> allVariables = null;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < utilities.length; i++) {
			if (utilities[i] >= max) {
				if (utilities[i] > max) {
					max = utilities[i];
					optimalStates.clear();
					selectedInterventions.clear();
				}
				optimalStates.add(states[i]);
				selectedInterventions.add(interventions[i]);
				/*
				TODO I suppose here you have forgotten to add the line:
				allVariables.addAll(interventions[i].getVariables());
				and to initiliaze allVariables in a line above:
				allVariables = new HashSet<Variable>();*/
			}
		}
		return new Intervention(decisionVariable, optimalStates, selectedInterventions, 
					new ArrayList<Variable>(allVariables));
	}

	/**
	 * @param intervention
	 */
	public void concatenate(Intervention intervention) {
		for (TreeADDBranch branch : branches) {
			Potential potentialBranch = branch.getPotential();
			if (potentialBranch == null || potentialBranch.getClass() == UniformPotential.class) {
				branch.setPotential(intervention);
			} else if (potentialBranch.getClass() == Intervention.class) {
				((Intervention)potentialBranch).concatenate(intervention);
			}
		}
	}
		
}
