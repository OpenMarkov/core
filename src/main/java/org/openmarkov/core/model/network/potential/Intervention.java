package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmarkov.core.model.network.ProbNet;
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
		for (int i = 0; i < states.size(); i++) {
			List<State> branchStates = new ArrayList<State>(1);
			branchStates.add(states.get(i));
			TreeADDBranch branch = null;
			if (interventions == null || i >= interventions.size() || interventions.get(i) == null) {
				branch = new TreeADDBranch(branchStates, topVariable, null);
			} else {
				branch = new TreeADDBranch(branchStates, topVariable, interventions.get(i), null);
			}
			this.addBranch(branch);
		}
	}
	
	/**
	 * Creates an intervention with only one branch. 
	 * @param topVariable
	 * @param states
	 * @param intervention
	 * @param variables
	 */
	public Intervention(Variable topVariable, List<State> states,
			Intervention intervention, List<Variable> variables, boolean one) {
		super(variables, topVariable, PotentialRole.INTERVENTION);
		List<State> branchStates = new ArrayList<State>(states.size());
		branchStates.addAll(states);
		TreeADDBranch branch = null;
		branch = new TreeADDBranch(branchStates, topVariable, intervention, variables);
		this.addBranch(branch);
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
        	System.out.println("entrada (average): " + interventions[i]);
			if (probabilities[i] > 0.0) {
				selectedInterventions.add(interventions[i]);
				selectedStates.add(states[i]);
				if (interventions[i] != null) {
					allVariables.addAll(interventions[i].getVariables());
				}
			}
		}
		if (equalInterventions(selectedInterventions.toArray(new Intervention[selectedInterventions.size()]))) {
			return selectedInterventions.get(0);
		}
		if (selectedInterventions.size() > 1) {
			intervention = new Intervention(chanceVariable, selectedStates, selectedInterventions, 
					new ArrayList<Variable>(allVariables));
		} else {
			if (selectedInterventions.size() == 1) {
				intervention = selectedInterventions.get(0);
			}
		}
    	System.out.println("salida (average): " + intervention);
		return intervention;
	}
	
	private static boolean equalInterventions(Intervention[] interventions) {
		boolean equalInterventions = true;
		if (interventions[0] == null) {
			for (int i = 1; i < interventions.length; i++) {
				if (interventions[i] != null) {
					equalInterventions = false;
					break;
				}
			}
		} else {
			for (int i = 1;  i < interventions.length; i++) {
				if (!interventions[0].equals(interventions[i])) {
					equalInterventions = false;
					break;
				}
			}
		}
		return false;
		//return equalInterventions;
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
		List<State> optimalStates = new ArrayList<State>();
		List<Intervention> optimalInterventions = new ArrayList<Intervention>();
		Set<Variable> variables = new HashSet<Variable>(); //???
		double max = Double.NEGATIVE_INFINITY;
		Intervention optimalIntervention = null;
		for (int i = 0; i < states.length; i++) {
        	System.out.println("entrada (optimal): " + interventions[i]);
			if (utilities[i] > max) {
				max = utilities[i];
				optimalStates.clear();
				optimalStates.add(states[i]);
				optimalInterventions.clear();
				optimalInterventions.add(interventions[i]);
				variables.clear();
				if (interventions[i] != null) {
					optimalIntervention = interventions[i];
					variables.addAll(interventions[i].getVariables());
				}
			} else if (utilities[i] == max) {  // there is a tie
				// TODO deal properly with ties
				optimalStates.add(states[i]);
				if (interventions[i] != null) {
					if (!optimalInterventions.equals(interventions[i])) {
						optimalInterventions.add(interventions[i]);
					}
					variables.addAll(interventions[i].getVariables());
				}
			}
		}
		Intervention intervention = null;
		if (optimalInterventions.size() > 1) {
			intervention = new Intervention(decisionVariable, optimalStates, optimalInterventions, 
					new ArrayList<Variable>(variables));
		} else {
			intervention = new Intervention(decisionVariable, optimalStates, optimalIntervention, 
					new ArrayList<Variable>(variables), true);
		}
    	return intervention;
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
				projection = new Intervention(this.getVariables());
				for (int i = 0; i < branches.size(); i++) {
					TreeADDBranch auxBranch = branches.get(i);
					TreeADDBranch auxBranchCopy = new TreeADDBranch(auxBranch.getBranchStates(),auxBranch.getRootVariable(), auxBranch.getParentVariables());
					Intervention auxProjection = getInterventionBranch(auxBranch).project(variable, state);
					auxBranchCopy.setPotential(auxProjection);
					projection.addBranch(auxBranchCopy);
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
		boolean areEqual = 
					intervention!= null && intervention.topVariable == topVariable && 
					intervention.getBranches().size() == branches.size() && 
					intervention.getVariables().size() == variables.size() &&
					intervention.getVariables().containsAll(variables);
		if (areEqual) {
			for (int i = 0; i < branches.size() && areEqual; i++) {
				TreeADDBranch branch = branches.get(i);
				List<State> states = branch.getStates();
				// Compare each branch
				TreeADDBranch interventionBranch = intervention.getBranch(states.get(0));
				// Compare states
				areEqual &= interventionBranch != null && interventionBranch.getStates().size() == states.size() &&
						interventionBranch.getStates().containsAll(states);
				// Compare potentials
				if (areEqual) {
					Potential interventionBranchPotential = interventionBranch.getPotential();
					Potential branchPotential = branch.getPotential();
					areEqual &= !((interventionBranchPotential == null && branchPotential != null) ||
							(interventionBranch != null && branchPotential == null));
					if (branchPotential != null) {
						areEqual &= interventionBranch.getPotential().getClass() == branch.getPotential().getClass();
						areEqual &= interventionBranch.getPotential().equals(branch.getPotential()); // Recursive part
					}
				}
			}
		}
		return areEqual;
	}
	
	
	/**
	 * @param intervention
	 * @return True if this and 'intervention' are equal. Note that the variables can be in different order in the paths
	 */
	public boolean equals2(Intervention intervention) {
		boolean areEquals = true;
		if (branches != null) {
			for (int i = 0; i < branches.size() && areEquals; i++) {
				TreeADDBranch auxBranch = branches.get(i);
				Intervention auxInterventionBranch = getInterventionBranch(auxBranch);
				for (State state : auxBranch.getStates()) {
					if (topVariable==intervention.topVariable){
						areEquals = intervention.hasBranchWithState(state);
					}
					if (areEquals){
						Intervention auxIntervState = intervention.project(topVariable, state);
						areEquals = ((auxInterventionBranch==null)&&(auxIntervState==null)) 
								|| ((auxInterventionBranch!=null)&&auxInterventionBranch.equals(intervention.project(topVariable, state)));
					}					
				}
			}
		} else {
			areEquals = intervention.branches == null;
		}
		return areEquals;
	}
		
		
	/**
	 * @param state
	 * @return true if one of its branches children contains 'state'
	 */
	private boolean hasBranchWithState(State state) {
		boolean hasBranch = false;
		if (branches != null) {
			for (int i=0;i< branches.size() && !hasBranch; i++){
				hasBranch = branches.get(i).getBranchStates().contains(state);
			}
		}
		
		return hasBranch;
	}

	/**
	 * @param branch
	 * @return The intervention corresponding to 'branch'
	 */
	private static Intervention getInterventionBranch(TreeADDBranch branch) {
		return (Intervention) (branch.getPotential());
	}
	
	/** 
	 * @param state
	 * @return branch that contains state or null
	 */
	private TreeADDBranch getBranch(State state) {
		for (TreeADDBranch branch : branches) {
			if (branch.getBranchStates().contains(state)) {
				return branch;
			}
		}
		return null;
	}
	
	public String toStringForGraphviz(ProbNet net) {
		
		String content = null;
		
		content = "digraph G {\n";
		
		content = content + "rankdir=LR\n";
		
		Map<Intervention,Integer> idNode = new Hashtable<Intervention,Integer>();
		Set<Intervention> nodes = this.getInterventions();
		int i = 0;
		for (Intervention skNode:nodes){
			idNode.put(skNode, i);
			String strNodes;
			
			if (skNode.topVariable!=null){
				strNodes = skNode.topVariable.getName();
			
			content = content + i+" [label=\""+strNodes+"\",shape="+toStringShapeForGraphviz(net,skNode.topVariable)+"];\n";
			}
			i = i + 1;
		}
	
		
		for (Intervention node:nodes)
		{
			int nodeIdNode = idNode.get(node);
			if (node.branches!=null){
				List<Intervention> nodeInterv = node.getInterventionsChildren();
			
			for (int j=0;j<node.branches.size();j++){
			//for (Intervention child:node.getInterventionsChildren())
				Intervention child = nodeInterv.get(j);
				if (child!=null){

					List<State> states = branches.get(j).getBranchStates();
				    String strStates = getStringStates(states);
					content = content + nodeIdNode+"->"+idNode.get(child)+"[label=\""+strStates+"\"];\n";
				}
			}
			}
		}
		
		content = content + "}\n";
		return content;
}

	private String getStringStates(List<State> states) {
		String str = "";
		
		if (states!=null){
			int size = states.size();
			if (size>0){
				str = states.get(0).toString();
				for (int i=1;i<size;i++){
					str = str + states.get(i).toString();
					if (i<size-1){
						str = str + ", ";
					}
				}
			}			
		}
		
		return str;
	}

	private List<Intervention> getInterventionsChildren() {
		
		List<Intervention> list = new ArrayList<Intervention>();
		if (branches!=null){
			for (TreeADDBranch branch:branches){
				list.add(getInterventionBranch(branch));
			}
		}
		return list;
	}

	private String toStringShapeForGraphviz(ProbNet net,Variable topVariable) {
		String string = null;
		switch (net.getNode(topVariable).getNodeType()){
		case DECISION:
			string = "decision";
			break;
		case CHANCE:
			string = "ellipse";
			break;
		}
		return string;
	}

	private Set<Intervention> getInterventions() {
		return this.auxGetInterventions();
	}

	private Set<Intervention> auxGetInterventions() {
		Set<Intervention> auxSet;
		
		auxSet = new HashSet<>();
		auxSet.add(this);
		
		if (branches!=null){
			
			for (int i = 0; i < branches.size(); i++) {
				TreeADDBranch auxBranch = branches.get(i);
				Intervention auxInterventionBranch = getInterventionBranch(auxBranch);
				if (auxInterventionBranch!=null){
					auxSet.addAll(auxInterventionBranch.auxGetInterventions());
				}
			}
		}
		
		return auxSet;
	}
	
		
}
