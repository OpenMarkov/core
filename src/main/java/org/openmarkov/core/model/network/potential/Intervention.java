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
	 * @param topVariable. <code>Variable</code>
	 * @param states. <code>List</code> of <code>State</code>
	 * @param interventions. <code>List</code> of <code>Intervention</code>
	 * @param variables. <code>List</code> of <code>Variable</code>
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
	 * Creates an intervention from a set of interventions and probabilities.
	 * @param chanceVariable. <code>Variable</code>
	 * @param probabilities. <code>double[]</code>
	 * @param interventions. <code>Intervention[]</code>
	 * @return A Intervention. <code>Intervention</code>
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
	 * Creates an intervention 
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
				((Intervention) potentialBranch).concatenate(intervention);
			}
		}
	}
	
	/**
	 * @param state
	 * @return The intervention corresponding to a state
	 */
	private Intervention getInterventionChild(State state) {
		Intervention child = null;
		boolean found = false;
		for (int i = 0; i < branches.size() && !found; i++) {
			TreeADDBranch auxBranch = branches.get(0);
			if (auxBranch.getBranchStates().contains(state)) {
				found = true;
				child = getInterventionBranch(auxBranch);
			}
		}
		return child;
	}
	
	/**
	 * @param variable
	 * @param state
	 * @return Projects an Intervention over an assignment 'variable' = 'state'
	 */
	private Intervention project(Variable variable, State state) {
		Intervention projection = null;

		if (this.topVariable == variable) {
			projection = getInterventionChild(state);
		} else {
			if (branches != null) {
				for (int i = 0; i < branches.size(); i++) {
					TreeADDBranch auxBranch = branches.get(i);
					Intervention auxProjection = getInterventionBranch(auxBranch).project(variable, state);
					auxBranch.setPotential(auxProjection);
				}
			}
		}
		return projection;
	}
	
	/**
	 * @param intervention
	 * @return True if this and 'intervention' are equal. Note that the variables can be in different order in the paths
	 */
	public boolean equals(Intervention intervention) {
		boolean areEquals = false;
		if (branches != null) {
			for (int i = 0; i < branches.size() && areEquals; i++) {
				TreeADDBranch auxBranch = branches.get(i);
				Intervention auxInterventionBranch = getInterventionBranch(auxBranch);
				for (State state : auxBranch.getStates()) {
					areEquals = auxInterventionBranch.equals(intervention.project(topVariable, state));
				}
			}
		} else {
			areEquals = intervention.branches == null;
		}
		return areEquals;
	}
		
		
	/**
	 * @param branch
	 * @return The intervention corresponding to 'branch'
	 */
	private static Intervention getInterventionBranch(TreeADDBranch branch) {
		return (Intervention) (branch.getPotential());
	}
	
		
}
