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
				if (interventions[i] != null) {
					allVariables.addAll(interventions[i].getVariables());
				}
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
		Set<Variable> variables = new HashSet<Variable>();
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < utilities.length; i++) {
			if (utilities[i] > max) {
				max = utilities[i];
				optimalStates.clear();
				optimalStates.add(states[i]);
				selectedInterventions.clear();
				if (interventions != null && interventions[i] != null) {
					selectedInterventions.add(interventions[i]); // Currently only one intervention can be here, also in draws.
					variables.clear();
					variables.addAll(interventions[i].getVariables());
				}
			} else if (utilities[i] == max) {
				//optimalStates.add(states[i]);
				/* TODO Change several classes to deal with draws that points to different previous interventions. 
				 * Currently, we leave as is, that is, we choose the first intervention, almost equivalent to choose
				 * randomly, because otherwise it suppose to change several classes and this can take a lot of time. 
				 * Anyway, draws will correspond to cases with zero probability that will be pruned because
				 * there are no real known cases, so far, with this type of draws. */
				if (interventions != null && interventions[i] != null) {
					variables.addAll(interventions[i].getVariables());
				}
			}
		}
		return new Intervention(decisionVariable, optimalStates, selectedInterventions, 
				new ArrayList<Variable>(variables));
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
		boolean has = false;
		if (branches != null) {
			for (int i=0;i< branches.size() && !has; i++){
				has = branches.get(i).getBranchStates().contains(state);
			}
		}
		
		return has;
	}

	/**
	 * @param branch
	 * @return The intervention corresponding to 'branch'
	 */
	private static Intervention getInterventionBranch(TreeADDBranch branch) {
		return (Intervention) (branch.getPotential());
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
