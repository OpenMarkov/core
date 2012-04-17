/**
 * 
 */
package org.openmarkov.core.model.network.potential.treeadd;

import java.util.ArrayList;
import java.util.HashMap;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.plugin.RelationType;


/**
 * A TreeADDPotential is a type of Potential that implies several advantages instead of using tables when the potential has a substructure
 * that repeats itself several times.
 * 
 * Each TreeADDPotential is defined by a top variable and its branches
 * 
 * @author myebra
 *
 */
@RelationType(name="TreeADD", family="Tree")
public class TreeADDPotential extends Potential  implements Cloneable {

	/**
	 * topVariable represents the variable on the top of the tree, in other words the root variable
	 */
	private Variable topVariable;
	
	private PotentialType potentialType = PotentialType.TREE_ADD;
	
	/**
	 * This ArrayList stores the branches created in the TreeADDPotential constructor
	 */
	private ArrayList<TreeADDBranch> branches = new ArrayList<TreeADDBranch>();
	
	/**
	 * label is incompatible with reference and reference is incompatible with potential
	 * This HashMap stores those potentials that have been labeled within the branches in a TreeADDPotential
	 */
	//private HashMap<String, Potential> potentialsLabeled; 
	
	public TreeADDPotential (ArrayList<Variable> variables, PotentialRole role) {
		super(variables, role);
		if (role == PotentialRole.CONDITIONAL_PROBABILITY){
			new TreeADDPotential(variables, variables.get(1), role);
		} else if (role == PotentialRole.UTILITY) {
			new TreeADDPotential(variables, variables.get(0), role);
		}
	}
	
	/**TreeADD constructors for the GUI**/
	/**
	 * For role conditional
	 * @param variables
	 * @param topVariable
	 * @param role
	 */
	public TreeADDPotential(ArrayList<Variable> variables, Variable topVariable, PotentialRole role){
		super(variables, role);
		this.topVariable = topVariable;
		
		VariableType variableType = topVariable.getVariableType();
		ArrayList<Variable> potentialVariables;
		//if topVariable is finite states or discretized, it creates a branch for each state
		if (variableType == VariableType.FINITE_STATES|| variableType == VariableType.DISCRETIZED) {
			
			State[] states = topVariable.getStates();
			for (int i = 0; i < states.length; i++){
				// if potential role of the treeADD is a conditional probability it is assigned an uniform potential
				// to the conditioned variable which is always the first variable of the arrayList of variables
				if (role == PotentialRole.CONDITIONAL_PROBABILITY) {
					Variable conditionedVariable = variables.get(0);
					potentialVariables = new ArrayList<Variable>();
					potentialVariables.add(conditionedVariable);
					UniformPotential potential = new UniformPotential(potentialVariables, role);
					ArrayList<State> branchStates = new ArrayList<State>();
					branchStates.add(states[i]);
					branches.add(new TreeADDBranch(branchStates, potential, topVariable, variables));
					
				}
				
			}
		}
		
		// if topVariable is numeric, it creates a branch whose thresholds are the 
		// same as those defined for the variable
		if (variableType == VariableType.NUMERIC) {
			PartitionedInterval interval = topVariable.getPartitionedInterval();
			Threshold minimum = new Threshold((float)interval.getMin(), !interval.isLeftClosed());
			Threshold maximum = new Threshold((float)interval.getMax(), interval.isRightClosed());
			potentialVariables = new ArrayList<Variable>();
			potentialVariables.add(variables.get(0));
			UniformPotential potential = new UniformPotential(potentialVariables, role);
			branches.add(new TreeADDBranch(minimum, maximum, potential, topVariable, variables));
			
		}
	}
	/**
	 * For role Utility
	 * @param variables
	 * @param topVariable
	 * @param role
	 * @param utilityVariable
	 */
	public TreeADDPotential(ArrayList<Variable> variables, Variable topVariable, PotentialRole role, Variable utilityVariable){
		super(variables, role);
		setUtilityVariable(utilityVariable);
		this.topVariable = topVariable;
		
		VariableType variableType = topVariable.getVariableType();
		ArrayList<Variable> potentialVariables;
		//if topVariable is finite states or discretized, it creates a branch for each state
		if (variableType == VariableType.FINITE_STATES|| variableType == VariableType.DISCRETIZED) {
			
			State[] states = topVariable.getStates();
			for (int i = 0; i < states.length; i++){
				// if the role of the treeADD is utility, it assigns a uniform potential
				if (role == PotentialRole.UTILITY) {
					potentialVariables = new ArrayList<Variable>();
					UniformPotential potential = new UniformPotential(potentialVariables, role);
					potential.setUtilityVariable(utilityVariable);
					ArrayList<State> branchStates = new ArrayList<State>();
					branchStates.add(states[i]);
					branches.add(new TreeADDBranch(branchStates, potential, topVariable, variables));
					
				}
			}
		}
		
		// if topVariable is numeric, it creates a branch whose thresholds are the 
		// same as those defined for the variable
		if (variableType == VariableType.NUMERIC) {
			PartitionedInterval interval = topVariable.getPartitionedInterval();
			Threshold minimum = new Threshold((float)interval.getMin(), !interval.isLeftClosed());
			Threshold maximum = new Threshold((float)interval.getMax(), interval.isRightClosed());
			potentialVariables = new ArrayList<Variable>();
			potentialVariables.add(variables.get(0));
			UniformPotential potential = new UniformPotential(potentialVariables, role);
			potential.setUtilityVariable(utilityVariable);
			branches.add(new TreeADDBranch(minimum, maximum, potential, topVariable, variables));
			
		}
	}
	/**
	 * Constructor for the parser
	 */
	public TreeADDPotential (ArrayList<Variable> variables, Variable topVariable, PotentialRole role, ArrayList<TreeADDBranch> branches) {
		super(variables, role);
		this.topVariable = topVariable;
		this.role = role;
		this.branches = branches;
	}
	
	/**
	 * Copy constructor
	 * @param treeADD
	 */
	public TreeADDPotential (TreeADDPotential treeADD) {
		super(treeADD.getVariables(), treeADD.getPotentialRole());
		this.topVariable = treeADD.getTopVariable();
		this.potentialType = treeADD.getPotentialType(); 
		this.branches = treeADD.getBranches();
		if (treeADD.getPotentialRole()== PotentialRole.UTILITY) {
			if (treeADD.getUtilityVariable() != null) {
				this.setUtilityVariable(treeADD.getUtilityVariable());
			}
		}
	}
	/**
	 * 
	 * @param branch
	 */
	public void addTreeADDBranch(TreeADDBranch branch){
		branches.add(branch);
	}
	
	/*public void setLabeledPotentials(){
		for (int i = 0; i < branches.size(); i++) {
			TreeADDBranch branch = branches.get(i);
			String label;
			if ((label = branch.getLabel()) != null) {
				potentialsLabeled.put(label, branch.getPotential());
			}
		}
	}*/
	public PotentialType getPotentialType () {
		return this.potentialType;
	}
	public ArrayList<TreeADDBranch> getBranches() {
		return branches;
	}
	
	/**
	 * this method return a branch potential also when it is referenced 
	 * @param branch
	 * @return Potential or null if the reference it has not been labelled in this tree
	 */
	/*public Potential getAssignedPotential(TreeADDBranch branch){
		setLabeledPotentials();
		String reference;
		if ((reference = branch.getReference()) != null) {
			if(potentialsLabeled.get(reference) != null) {
				return potentialsLabeled.get(reference);
			}
		}else{
			//If reference is null that means that this branch has a potential associated
			return branch.getPotential();
		}
		return null;
	}*/
	
	public void setBranchAtIndex (int index, TreeADDBranch treeBranch) {
		this.branches.set(index, treeBranch);
	}
	
	public void setBranches(ArrayList<TreeADDBranch> branches) {
		this.branches = branches;
	}
	
	public Variable getTopVariable(){
		return topVariable;
	}
	
	public Variable getConditionedVariable() {
		if (role == PotentialRole.CONDITIONAL_PROBABILITY) {
			return variables.get(0);
		} else {
			return null;
			}
	}
	
	public void setTopVariable(Variable variable){
		this.topVariable = variable;
	}
	
	 /**
     * Adds variable to a treeADD potential 
     * @throws NotEnoughMemoryException 
     * 
     */
    public  Potential addVariable(Variable variable) throws NotEnoughMemoryException {
    	//return new UniformPotential(getVariables(), getPotentialRole());
    	ArrayList<Variable> variables = getVariables();
    	variables.add(variable);
    	 for (TreeADDBranch branch : getBranches()) {
    		 branch.setParentVariables(variables);
    		 branch.getPotential().addVariable(variable);
    	 }
    	 
    	 return this;
    }
    /**
     * Removes variable from a treeADD potential 
     * @throws NotEnoughMemoryException 
     * 
     */
    public  Potential removeVariable(Variable variable) throws NotEnoughMemoryException {
    	getVariables().add(variable);
    	return new UniformPotential(getVariables(), getPotentialRole());
    }

	@Override
	public ArrayList<TablePotential> tableProject(EvidenceCase evidenceCase,
			InferenceOptions inferenceOptions)
			throws NonProjectablePotentialException, NotEnoughMemoryException,
			WrongCriterionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Potential shift(ProbNet probNet, int timeDifference)
			throws ProbNodeNotFoundException, NotEnoughMemoryException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Object clone() throws CloneNotSupportedException {
		return this.clone();
		// TODO seguir clonando hacia abajo; hay que clonar tambien las ramas y los potenciales
	}

	@Override
	public Potential copy() throws NotEnoughMemoryException {
		// TODO Auto-generated method stub
		return null;
	}
	/**
     * Returns if an instance of a certain Potential type makes sense given the variables and the potential role 
     * @param variables
     * @param role
     */
	public static boolean validate (ProbNode probNode, ArrayList<Variable> variables, PotentialRole role)
    {
		boolean validate = false;
        // node must have at least one parent node
		if (role == PotentialRole.UTILITY) {
			//in variables there is not utility variable
			if(variables.size() >= 1) {
				validate = true;
			}
		}
		if (role == PotentialRole.CONDITIONAL_PROBABILITY){
			if (variables.size() >= 2) {
				validate = true;
			}
		}
		return validate;
    }
	
}
