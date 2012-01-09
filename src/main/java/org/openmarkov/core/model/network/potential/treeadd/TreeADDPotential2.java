/**
 * 
 */
package org.openmarkov.core.model.network.potential.treeadd;

import java.awt.Label;
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
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;


/**
 * @author Mar Yebra
 *
 */
public class TreeADDPotential2 extends Potential  implements Cloneable {

	//topVariable represents the variable on the top of the tree, in other words the root variable
	private Variable topVariable;
	
	private PotentialType potentialType = PotentialType.TREE_ADD;
	
	/*This ArrayList stores the branches created in the TreeADDPotential constructor*/
	private ArrayList<TreeADDBranch> branches;
	
	/*label is incompatible with reference and reference is incompatible with potential*/
	/*This HashMap stores those potentials that have been labeled within the branches in a TreeADDPotential*/
	private HashMap<String, Potential> potentialsLabeled; 
	
	/**TreeADD constructor for the GUI**/
	public TreeADDPotential2(ArrayList<Variable> variables, Variable topVariable, PotentialRole role){
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
					branches.add(new TreeADDBranch(branchStates, potential, this));
				}
				// if the role of the treeADD is utility, it assigns a uniform potential
				if (role == PotentialRole.UTILITY) {
					potentialVariables = new ArrayList<Variable>();
					UniformPotential potential = new UniformPotential(potentialVariables, role);
					ArrayList<State> branchStates = new ArrayList<State>();
					branchStates.add(states[i]);
					branches.add(new TreeADDBranch(branchStates, potential, this));
				}
			}
		}
		
		// if topVariable is numeric, it creates a branch whose thresholds are the 
		// same as those defined for the variable
		if (variableType == VariableType.NUMERIC) {
			PartitionedInterval interval = topVariable.getPartitionedInterval();
			Threshold minimum = new Threshold((float)interval.getMin(), !interval.isLeftClosed());
			Threshold maximum = new Threshold((float)interval.getMax(), interval.isRightClosed());
			//Threshold minimum = topVariable.getThresholdMin();
			//Threshold maximum = topVariable.getThresholdMax();
			potentialVariables = new ArrayList<Variable>();
			UniformPotential potential = new UniformPotential(potentialVariables, role);
			branches.add(new TreeADDBranch(minimum, maximum, potential, this));
		}
	}
	
	public void addTreeADDBranch(TreeADDBranch branch){
		branches.add(branch);
	}
	
	public void setLabeledPotentials(){
		for (int i = 0; i < branches.size(); i++) {
			TreeADDBranch branch = branches.get(i);
			String label;
			if ((label = branch.getLabel()) != null) {
				potentialsLabeled.put(label, branch.getPotential());
			}
		}
	}
	
	public ArrayList<TreeADDBranch> getBranches() {
		return branches;
	}
	
	/**
	 * this method return a branch potential also when it is referenced 
	 * @param branch
	 * @return Potential or null if the reference it has not been labelled in this tree
	 */
	public Potential getAssignedPotential(TreeADDBranch branch){
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
	}
	
	/*public void setReferences(){
		for (int i = 0; i < branches.size(); i++){
			TreeADDBranch branch = branches.get(i);
			String reference;
			if ((reference = branch.getReference()) != null) {
				if(potentialsLabeled.get(reference) != null) {
					//para asignar este potencial tengo que comprobar que existe alguna label en el arbol igual que reference y que esta
					//label no este por encima en la jerarquia, es decir que no sea una label de una branch de ningun antecesor por encima del padre o tios
					branch.setPotential(potentialsLabeled.get(reference));
				}else{
					//If there is not any label in the ADD with that reference
					//throw new Exception();
				}
				
			}
		}
	}*/
	
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

}
