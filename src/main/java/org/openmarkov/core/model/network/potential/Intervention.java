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
import org.openmarkov.core.model.network.potential.sdag.SDAGIntervention;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

public class Intervention extends TreeADDPotential {

	// Constructors
	/**
	 * Creates an intervention without branches. 
	 * @param topVariable
	 */
	public Intervention(Variable topVariable) {
		super(null, topVariable, PotentialRole.INTERVENTION);
	}
	
	/**
	 * Creates an intervention having as much states in each branch as equal interventions. 
	 * The number of states and interventions must be the same.
	 * @param topVariable <code>Variable</code>
	 * @param states One state for each intervention in the list. <code>List</code> of <code>State</code>
	 * @param interventions It is possible that this list contains some equal interventions.
	 *  <code>List</code> of <code>Intervention</code>
	 */
	public Intervention(Variable topVariable, List<State> states, List<Intervention> interventions) {
		super(null, topVariable, PotentialRole.INTERVENTION);
		List<Intervention> distinctInterventions = new ArrayList<>();
		Map<Intervention, Set<State>> correspondingStates = new HashMap<>();
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
				Set<State> statesIntervention = new HashSet<>();
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
			ArrayList<State> statesOfIntervention = new ArrayList<>(correspondingStates.get(intervention));
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
		List<State> branchStates = new ArrayList<>(states.size());
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
		List<State> branchStates = new ArrayList<>(states.size());
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
	 * @param chanceVariable <code>Variable</code>
	 * @param probabilities <code>double[]</code>
	 * @param interventions <code>Intervention[]</code>
	 * @return A Intervention. <code>Intervention</code>
	 */
	public static Intervention averageOfInterventions(Variable chanceVariable, 
			double[] probabilities, Intervention[] interventions) {
		return Intervention.averageOfInterventions(chanceVariable, probabilities, interventions,false);
	}

	/**
	 * Creates an intervention from a set of interventions and probabilities.
	 * @param chanceVariable <code>Variable</code>
	 * @param probabilities <code>double[]</code>
	 * @param interventions <code>Intervention[]</code>
	 * @param coalescedInterventions <code>boolean</code>
	 * @return A Intervention. <code>Intervention</code>
	 */
	public static Intervention averageOfInterventions(Variable chanceVariable, 
			double[] probabilities, Intervention[] interventions, boolean coalescedInterventions) {
		State[] states = chanceVariable.getStates();
		
		// Select interventions and states whose probability is greater than 0.0.
		List<Intervention> selectedInterventions = new ArrayList<>();
		List<State> selectedStates = new ArrayList<>();
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
					// TODO Arreglar esto. No puede haber una referencia a SDAGInterventions desde Intervention. 
					// TODO SDAGIntervention no se sabe que es. No está documentado.
					// TODO coalescedInterventions no está documentado.
					//					intervention = (!coalescedInterventions)?
					//							new Intervention(chanceVariable, selectedStates, selectedInterventions):
					//								new SDAGIntervention(chanceVariable, selectedStates, selectedInterventions);
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
	 * @param coalescedInterventions 
	 * @return Optimal intervention
	 */
	public static Intervention optimalIntervention(Variable decisionVariable, 
			double[] utilities, Intervention[] interventions, boolean coalescedInterventions) {
		State[] states = decisionVariable.getStates();
		List<State> optimalStates = new ArrayList<>();
		List<Intervention> optimalInterventions = new ArrayList<>();
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < states.length; i++) {
			Intervention interventionI = interventions[i];
			double utilityI = utilities[i];
			if (utilityI > max) {
				max = utilityI;
				optimalStates.clear();
				optimalStates.add(states[i]);
				optimalInterventions.clear();
				optimalInterventions.add(interventionI);
			} else if (utilityI == max) {  // there is a tie
				optimalStates.add(states[i]);
				if (interventionI != null) {
					boolean isInOptimalInterventions = false;
					for (int j = 0; j < optimalInterventions.size() && !isInOptimalInterventions ; j++){
						isInOptimalInterventions = optimalInterventions.get(j).equals(interventionI);
					}
					if (!isInOptimalInterventions) {
						optimalInterventions.add(interventionI);
					}
				}
			}
		}
		
		Intervention intervention = null;
		boolean severalOptimalInterventions = optimalInterventions.size() > 1;
		if (!coalescedInterventions) {
			intervention = severalOptimalInterventions ? new Intervention(decisionVariable, optimalStates,
					optimalInterventions) : new Intervention(decisionVariable, optimalStates,
					optimalInterventions.get(0));
		} else {
			intervention = severalOptimalInterventions ? new SDAGIntervention(decisionVariable,
					optimalStates, optimalInterventions) : new SDAGIntervention(decisionVariable,
					optimalStates, optimalInterventions.get(0));

		}
		
    	return intervention;
		
	}
	
	
	
	/**
	 * Creates an intervention 
	 * @param decisionVariable
	 * @param utilities
	 * @param interventions
	 * @param coalescedInterventions 
	 * @return Optimal intervention
	 */
	public static Intervention optimalInterventionTakingOneOptimal(Variable decisionVariable, 
			double[] utilities, Intervention[] interventions, boolean coalescedInterventions) {
		State[] states = decisionVariable.getStates();
		List<State> optimalStates = new ArrayList<>();
		State optimalState = null;
		Intervention optimalIntervention = null;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < states.length; i++) {
			Intervention interventionI = interventions[i];
			double utilityI = utilities[i];
			if (utilityI > max) {
				max = utilityI;
				optimalState = states[i];
				optimalIntervention = interventionI;
			}
		}
		optimalStates.add(optimalState);
		Intervention intervention = null;
		if (optimalIntervention!=null){
			intervention = (!coalescedInterventions)? new Intervention(decisionVariable, optimalStates,
					optimalIntervention): new SDAGIntervention(decisionVariable,
					optimalStates, optimalIntervention);
		}
		else{
			intervention = (!coalescedInterventions)? new Intervention(decisionVariable, optimalStates): 
				new SDAGIntervention(decisionVariable,optimalStates);
			
		}
    	return intervention;
		
	}
	

	/**
	 * Creates an intervention 
	 * @param decisionVariable
	 * @param utilities
	 * @param interventions
	 * @param coalescedInterventions 
	 * @return Optimal intervention
	 */
	public static Intervention optimalInterventionTakingAllOptimal(Variable decisionVariable, 
			double[] utilities, Intervention[] interventions, boolean coalescedInterventions) {
		State[] states = decisionVariable.getStates();
		List<State> optimalStates = new ArrayList<>();
		List<Intervention> optimalInterventions = new ArrayList<>();
		Intervention intervention;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < states.length; i++) {
			double utilityI = utilities[i];
			if (utilityI >= max) {
				if (utilityI > max) {
					max = utilityI;
					optimalStates = new ArrayList<>();
					optimalInterventions = new ArrayList<>();
				}
				optimalStates.add(states[i]);
				optimalInterventions.add(interventions[i]);
			}
		}
		
		if (!areNullOptimalInterventions(optimalInterventions)){
			intervention = (!coalescedInterventions)? new Intervention(decisionVariable, optimalStates,
					optimalInterventions): new SDAGIntervention(decisionVariable,
					optimalStates, optimalInterventions);
		}
		else{
			intervention = (!coalescedInterventions)? new Intervention(decisionVariable, optimalStates): 
				new SDAGIntervention(decisionVariable,optimalStates);
			
		}
    	return intervention;
		
	}
	
	private static boolean areNullOptimalInterventions(List<Intervention> interventions){
		return (interventions.isEmpty() || interventions.get(0)==null);
	}
	
	/**
	 * Creates an intervention 
	 * @param decisionVariable
	 * @param utilities
	 * @param interventions
	 * @param coalescedInterventions 
	 * @return Optimal intervention
	 */
	public static Intervention optimalInterventionTakingOptimalMinimalDepth(Variable decisionVariable, 
			double[] utilities, Intervention[] interventions, boolean coalescedInterventions) {
		State[] states = decisionVariable.getStates();
		List<State> optimalStates = new ArrayList<>();
		State optimalState = null;
		Intervention optimalIntervention = null;
		int depthOfOptimalInterv = Integer.MAX_VALUE;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < states.length; i++) {
			Intervention interventionI = interventions[i];
			double utilityI = utilities[i];
			if (utilityI > max) {
				max = utilityI;
				optimalState = states[i];
				optimalIntervention = interventionI;
			}
			if (utilityI == max){
				if (interventionI != null){
				int auxDepth = interventionI.getDepth();
				if (auxDepth<depthOfOptimalInterv){
					optimalState = states[i];
					optimalIntervention = interventionI;
					depthOfOptimalInterv = auxDepth;
				}
				}
				
			}
		}
		optimalStates.add(optimalState);
		Intervention intervention = null;
		if (optimalIntervention!=null){
			intervention = (!coalescedInterventions)? new Intervention(decisionVariable, optimalStates,
					optimalIntervention): new SDAGIntervention(decisionVariable,
					optimalStates, optimalIntervention);
		}
		else{
			intervention = (!coalescedInterventions)? new Intervention(decisionVariable, optimalStates): 
				new SDAGIntervention(decisionVariable,optimalStates);
			
		}
    	return intervention;
		
	}
	
	

	private int getDepth() {
		int depth = 0;

		if (branches != null) {
			if (branches.size() > 0) {
				int auxDepth = 0;
				for (int i = 0; i < branches.size(); i++) {
					TreeADDBranch auxBranch = branches.get(i);
					Intervention auxInterventionBranch = getInterventionBranch(auxBranch);
					if (auxInterventionBranch != null) {
						auxDepth = Math.max(auxDepth,auxInterventionBranch.getDepth());
					}
				}
				depth = 1 + auxDepth;
			}
		} 

		return depth;
	}

	/** 
	 * Add <code>Intervention</code> to edges of this intervention
	 * @param intervention
	 * @return 
	 * @throws Exception 
	 */
	public Intervention concatenate(Intervention intervention) {
		//  
		Intervention oldIntervention;
		for (TreeADDBranch branch : branches) {
			oldIntervention = (Intervention)branch.getPotential();
			if (oldIntervention == null) {
				branch.setPotential(intervention);
			} else {
				Intervention branchIntervention = (Intervention)branch.getPotential();
				branchIntervention = branchIntervention.concatenate(intervention);
			}
		}
		return this;
	}
	
	
	
	public boolean hasCycle(){
		boolean hasCycle;
		hasCycle = false;
		if (branches!=null){
		for (int i=0;(i<branches.size()&&!hasCycle);i++){
			TreeADDBranch branch = branches.get(i);	
			if (branch != null){
			Intervention interventionBranch = (Intervention)branch.getPotential();
			if (interventionBranch != null){
				hasCycle = interventionBranch.isReachable(this);
			}
			}
			
		}
		}
	return hasCycle;
		
	}
	
	 /**
	 * @param intervention
	 * @return True iff 'intervention' can be reached from 'this'
	 */
	private boolean isReachable(Intervention intervention) {
		 boolean isReachable = false;
		 if (branches != null)
		 for (int i=0;(i<branches.size()&&!isReachable);i++){
				TreeADDBranch branch = branches.get(i);		
				if (branch != null){
				Intervention auxBranchIntervention = (Intervention)branch.getPotential();
				if (auxBranchIntervention != null){
				isReachable = auxBranchIntervention == intervention;
				isReachable = isReachable || auxBranchIntervention.isReachable(intervention);
				}
				}
		 }
		 return isReachable;
	}

	/**
     * @param intervention <code>Intervention</code>
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
                    areEqual &= interventionBranchPotential != null ? interventionBranchPotential.equals(branchPotential) : true;
                }
            }
        }
        return areEqual;
    }	
    
	/**
	 * @param interventions
	 * @return <code>true</code> when all the interventions are equal.
	 */
	protected static boolean equalInterventions(Intervention[] interventions) {
		boolean equalInterventions = true;
		int numInterventions = interventions.length;
		if (interventions != null && numInterventions > 1) {
			Intervention firstIntervention = interventions[0];
			for (int i = 1; i < numInterventions && equalInterventions; i++) {
				equalInterventions &= (firstIntervention == null) ? interventions[i] == null
						: firstIntervention.equals(interventions[i]);
			}
		}
		return equalInterventions;
	}
	
	/*protected static boolean equalInterventions(Intervention[] interventions) {
		return false;
	}*/

    /**
     * @return List of interventions contained in branches if they are not null.
     */
    public List<Intervention> getNextInterventions() {
    	List<Intervention> nextInterventions = new ArrayList<>();
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
    	List<State> states = new ArrayList<>();
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
		StringBuilder strBuffer = new StringBuilder();
//		strBuffer.append(indent);
//		strBuffer.append(topVariable.getName());
		// Print variables
		if (branches != null && branches.size() > 0) {
			//strBuffer.append("\n");
			//strBuffer.append(" = ");
			for (TreeADDBranch branch : branches) {
				strBuffer.append(branch);
			}
		}
		return strBuffer.toString();
	}

	
	public String toStringForGraphviz(ProbNet net) {

		String content = null;

		content = "digraph G {\n";

		Map<Intervention, Integer> idNode = new Hashtable<>();

		Set<Intervention> nodes = this.getInterventions();
		Set<Intervention> leaves = this.getInterventionsLeaves();
		
		int i = 0;
		for (Intervention skNode : nodes) {
			idNode.put(skNode, i);
			String strNodes;

			if (skNode.topVariable != null) {
				strNodes = skNode.topVariable.getName();
				if (leaves.contains(skNode)) {
					List<TreeADDBranch> skNodeBranches = skNode.getBranches();
					if ((skNodeBranches != null)&&(skNodeBranches.size()>0)){
						strNodes = strNodes + "=" + skNodeBranches.get(0).getStates().toString();
					}
				}

				content = content + i + " [label=\"" + strNodes + "\",shape="
						+ toStringShapeForGraphviz(net, skNode.topVariable) + "];\n";
			}
			i = i + 1;
		}

		for (Intervention node : nodes) {
			int nodeIdNode = idNode.get(node);
			if (node.branches != null) {
				List<Intervention> nodeInterv = node.getInterventionsChildren();

				for (int j = 0; j < node.branches.size(); j++) {

					Intervention child = nodeInterv.get(j);
					if (child != null) {

						List<State> states = node.branches.get(j).getBranchStates();
						String strStates = getStringStates(states);
						content = content + nodeIdNode + "->" + idNode.get(child) + "[label=\"" + strStates
								+ "\"];\n";
					}
				}
			}
		}

		content = content + "}\n";
		return content;
	}
	
	

	/**
	 * @return The Interventions that are the leaves of the tree rooted at 'this'
	 */
	private Set<Intervention> getInterventionsLeaves() {

		Set<Intervention> auxSet;

		auxSet = new HashSet<>();

		if (branches != null) {
			if (branches.size() > 0) {
				for (int i = 0; i < branches.size(); i++) {
					TreeADDBranch auxBranch = branches.get(i);
					Intervention auxInterventionBranch = getInterventionBranch(auxBranch);
					if (auxInterventionBranch != null) {
						auxSet.addAll(auxInterventionBranch.getInterventionsLeaves());
					} else {
						auxSet.add(this);
					}
				}
			} else {
				auxSet.add(this);
			}
		} else {
			auxSet.add(this);
		}

		return auxSet;
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
		
		List<Intervention> list = new ArrayList<>();
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
	
	
	public boolean hasInterventionForDecision(Variable decision) {
		boolean hasInterv = false;

		if (topVariable == decision) {
			hasInterv = true;
		} else {
			if (branches != null) {
				for (int i = 0; i < branches.size() && !hasInterv; i++) {
					TreeADDBranch branch = branches.get(i);
					Intervention branchInterv = getInterventionBranch(branch);
					if (branchInterv != null) {
						hasInterv = branchInterv.hasInterventionForDecision(decision);
					}
				}
			}
		}
		return hasInterv;
	}
	
	@Override
	public Potential deepCopy(ProbNet copyNet) {
		Intervention intervention = (Intervention) super.deepCopy(copyNet);
		return intervention;
	}
	
}
