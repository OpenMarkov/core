package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

public class Intervention extends TreeADDPotential {

	// Constructors
	/**
	 * Creates an intervention without branches. 
	 * @param topVariable
	 * @param state
	 */
	public Intervention(Variable topVariable) {
		super(null, topVariable, PotentialRole.INTERVENTION);
	}
	
	/**
	 * Creates an intervention having as much states in each branch as equal interventions. 
	 * The number of states and interventions must be the same.
	 * @param topVariable. <code>Variable</code>
	 * @param states. One state for each intervention in the list. <code>List</code> of <code>State</code>
	 * @param interventions. It is possible that this list contains some equal interventions.
	 *  <code>List</code> of <code>Intervention</code>
	 */
	public Intervention(Variable topVariable, List<State> states, List<Intervention> interventions) {
		super(null, topVariable, PotentialRole.INTERVENTION);
		List<Intervention> distinctInterventions = new ArrayList<Intervention>();
		Map<Intervention, Set<State>> correspondingStates = new HashMap<Intervention, Set<State>>();
		int numInterventions = interventions.size();
		for (int i = 0; i < numInterventions; i++) {
			Intervention intervention = interventions.get(i);
			int numDistinctInterventions = distinctInterventions.size();
			boolean noMatch = true;
			Intervention distinctIntervention = null;
			State correspondingState = null;
			
			// See if there is any intervention equal to "intervention" in "distinctInterventions"
			for (int j = 0; j < numDistinctInterventions && noMatch; j++) {
				distinctIntervention = distinctInterventions.get(j);
				noMatch &= !(distinctIntervention == intervention || distinctIntervention.equals(intervention));
				correspondingState = (!noMatch) ? states.get(i) : correspondingState;
			}
			if (noMatch) { // If no, add it to distinctInterventions and create a set of states in corresponding states
				distinctInterventions.add(intervention);
				Set<State> statesIntervention = new HashSet<State>();
				statesIntervention.add(states.get(i));
				correspondingStates.put(intervention, statesIntervention);
			} else { // Intervention is equal to a previous intervention. Add the state to the corresponding states set
				if (distinctIntervention != null) {
					correspondingStates.get(distinctIntervention).add(correspondingState);
				}
			}
		}
		
		// Create branches
		for (Intervention intervention : distinctInterventions) {
			ArrayList<State> statesOfIntervention = new ArrayList<State>(correspondingStates.get(intervention));
			if (intervention != null) {
				addBranch(new TreeADDBranch(statesOfIntervention, topVariable, intervention, null));
			}
		}
	}
	
	/**
	 * Creates an intervention with only one branch containing several states.
	 * @param topVariable
	 * @param states
	 */
	public Intervention(Variable topVariable, State... states) {
		super(null, topVariable, PotentialRole.INTERVENTION);
		List<State> branchStates = Arrays.asList(states);
		addBranch(new TreeADDBranch(branchStates, topVariable, null));
	}
	
	/**
	 * Creates an intervention with only one branch containing several states. 
	 * @param topVariable
	 * @param states
	 */
	public Intervention(Variable topVariable, List<State> states) {
		super(null, topVariable, PotentialRole.INTERVENTION);
		List<State> branchStates = new ArrayList<State>(states.size());
		branchStates.addAll(states);
		addBranch(new TreeADDBranch(branchStates, topVariable, null));
	}
	
	/**
	 * Creates an intervention with only one branch. 
	 * @param topVariable
	 * @param states
	 * @param intervention
	 */
	public Intervention(Variable topVariable, List<State> states, Intervention intervention) {
		super(null, topVariable, PotentialRole.INTERVENTION);
		List<State> branchStates = new ArrayList<State>(states.size());
		branchStates.addAll(states);
		addBranch(new TreeADDBranch(branchStates, topVariable, intervention, null));
	}
	
	/**
	 * Creates an intervention with a continuous variable with a partitioned interval.
	 * The partitioned interval must have the same number of sub-intervals than interventions.
	 * @param topVariable
	 * @param interventions
	 */
	public Intervention(Variable topVariable, PartitionedInterval partitionedInterval, List<Intervention> interventions) {
		super(null, topVariable, PotentialRole.INTERVENTION);
		double[] limits = partitionedInterval.getLimits();
		for (int i = 0; i < limits.length - 1; i++) {
			addBranch(new TreeADDBranch(
					new Threshold(limits[i], false), new Threshold(limits[i + 1], true), 
					topVariable, interventions.get(i), null));
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
		State[] states = chanceVariable.getStates();
		
		// Select interventions and states whose probability is greater than 0.0.
		List<Intervention> selectedInterventions = new ArrayList<Intervention>();
		List<State> selectedStates = new ArrayList<State>();
		for (int i = 0; i < probabilities.length; i++) {
			if (probabilities[i] > 0.0) {
				selectedInterventions.add(interventions[i]);
				selectedStates.add(states[i]);
			}
		}
		
		Intervention intervention;
		if (selectedInterventions.size() == 0) { // All probabilities == 0.0
			intervention = null;
		} else {
			if (selectedInterventions.size() == 1) { // Only one intervention with probability != 0.0
				intervention = selectedInterventions.get(0);
			} else { // More than one intervention with probability != 0.0
				if (equalInterventions(selectedInterventions.toArray(new Intervention[selectedInterventions.size()]))) {
					intervention = selectedInterventions.get(0); // All interventions are equals
				} else {
					intervention = new Intervention(chanceVariable, selectedStates, selectedInterventions);
				}
			}
		}
		
		return intervention;
	}
	
	/**
	 * Creates an intervention 
	 * @param decisionVariable
	 * @param utilities
	 * @param interventions
	 * @return Optimal intervention
	 */
	public static Intervention optimalIntervention(Variable decisionVariable, 
			double[] utilities, Intervention[] interventions) {
		State[] states = decisionVariable.getStates();
		List<State> optimalStates = new ArrayList<State>();
		List<Intervention> optimalInterventions = new ArrayList<Intervention>();
		double max = Double.NEGATIVE_INFINITY;
		Intervention optimalIntervention = null;
		for (int i = 0; i < states.length; i++) {
			if (utilities[i] > max) {
				max = utilities[i];
				optimalStates.clear();
				optimalStates.add(states[i]);
				optimalInterventions.clear();
				optimalInterventions.add(interventions[i]);
				if (interventions[i] != null) {
					optimalIntervention = interventions[i];
				}
			} else if (utilities[i] == max) {  // there is a tie
				optimalStates.add(states[i]);
				if (interventions[i] != null) {
					if (!optimalInterventions.equals(interventions[i])) {
						optimalInterventions.add(interventions[i]);
					}
				}
			}
		}
		Intervention intervention = null;
		if (optimalInterventions.size() > 1) {
			intervention = new Intervention(decisionVariable, optimalStates, optimalInterventions);
		} else {
			intervention = new Intervention(decisionVariable, optimalStates, optimalIntervention);
		}
    	return intervention;
	}

	/** 
	 * Add <code>Intervention</code> to edges of this intervention
	 * @param intervention
	 */
	public Intervention concatenate(Intervention intervention) {
		//  
		Intervention oldIntervention;
		for (TreeADDBranch branch : branches) {
			oldIntervention = (Intervention)branch.getPotential();
			if (oldIntervention == null) {
				branch.setPotential(intervention);
			} else {
				((Intervention)branch.getPotential()).concatenate(intervention);
			}
		}
		return this;
	}
	
	 /**
     * @param intervention. <code>Intervention</code>
     * @return True when <code>this</code> and <code>intervention</code> are equals.
     */
    public boolean equals(Intervention intervention) {
    	int numBranches = branches.size();
        boolean areEqual =
                    intervention!= null && 
                    intervention.topVariable == topVariable &&
                    intervention.getBranches().size() == numBranches;
        if (areEqual) {
            // Compare each branch
            for (int i = 0; i < numBranches && areEqual; i++) {
                TreeADDBranch branch = branches.get(i);
                // Get the corresponding branch to "this.branches.get(i)" in the other "intervention"
                List<State> states = branch.getStates();
                // A branch always has at least one state
                TreeADDBranch interventionBranch = intervention.getBranch(states.get(0)); 
                areEqual &= interventionBranch != null;
                // Compare states
                if (areEqual) {
                	List<State> interventionBranchStates = interventionBranch.getStates(); 
                	areEqual &= interventionBranchStates.size() == states.size() &&
                			interventionBranchStates.containsAll(states);
                }
                // Compare potentials
                if (areEqual) {
                    Intervention interventionBranchPotential = (Intervention)interventionBranch.getPotential();
                    Intervention branchPotential = (Intervention)branch.getPotential();
                    areEqual &= !((interventionBranchPotential == null && branchPotential != null) ||
                            (interventionBranchPotential != null && branchPotential == null));
                    // Recursive part
                    areEqual &= branchPotential != null ? interventionBranchPotential.equals(branchPotential) : true;
                }
            }
        }
        return areEqual;
    }	
    
	/**
	 * @param interventions
	 * @return <code>true</code> when all the interventions are equal.
	 */
	private static boolean equalInterventions(Intervention[] interventions) {
		boolean equalInterventions = true;
		if (interventions != null && interventions.length > 1) {
			if (interventions[0] == null) {
				for (int i = 1; i < interventions.length && equalInterventions; i++) {
					equalInterventions &= interventions[i] == null;
				}
			} else {
				for (int i = 1;  i < interventions.length && equalInterventions; i++) {
					equalInterventions &= interventions[0].equals(interventions[i]);
				}
			}
		}
		return equalInterventions;
	}

    /**
     * @return List of interventions contained in branches if they are not null.
     */
    public List<Intervention> getNextInterventions() {
    	List<Intervention> nextInterventions = new ArrayList<Intervention>();
    	for (TreeADDBranch branch : branches) { // branches is never null according to TreeADDPotential code
    		Potential branchPotential = branch.getPotential();
    		if (branchPotential != null) {
    			nextInterventions.add((Intervention)branchPotential);
    		}
    	}
    	return nextInterventions;
    }
	
    /**
     * @return <code>List</code> of <code>State</code>
     */
    public List<State> getNonZeroProbabilityStates() {
    	List<State> states = new ArrayList<State>();
    	for (TreeADDBranch branch : branches) {
    		states.addAll(branch.getStates());
    	}
    	return states;
    }
    
	/**
	 * @param branch
	 * @return The intervention corresponding to 'branch'
	 */
	public static Intervention getInterventionBranch(TreeADDBranch branch) {
		return (Intervention) (branch.getPotential());
	}
	
	/** 
	 * @param state
	 * @return branch that contains state or null
	 */
	public TreeADDBranch getBranch(State state) {
		for (TreeADDBranch branch : branches) {
			if (branch.getBranchStates().contains(state)) {
				return branch;
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(indent);
		strBuffer.append(topVariable.getName());
		// Print variables
		if (branches != null && branches.size() > 0) {
			strBuffer.append("\n");
			for (TreeADDBranch branch : branches) {
				strBuffer.append(branch);
			}
		}
		return strBuffer.toString();
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
		
		
		//TODO Get the leaves and, for each one, draw an arc from to a an empty/sink artificial node
		//Map<Intervention,Integer> leaves = new Hashtable<Intervention,Integer>();
	
		
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
		
		if (net != null) {
			Node node;
			try {
				node = net.getNode(topVariable.getName());
			} catch (NodeNotFoundException e) {
				node = null;
			}
			if (node != null) {
				switch (node.getNodeType()) {
				case DECISION:
					string = "decision";
					break;
				case CHANCE:
					string = "ellipse";
					break;
				default:
					break;
				}
			}
			else {
				string = "decision";
			}
		} else {
			string = "decision";
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
