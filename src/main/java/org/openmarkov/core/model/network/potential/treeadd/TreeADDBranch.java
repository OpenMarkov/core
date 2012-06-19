package org.openmarkov.core.model.network.potential.treeadd;

import java.util.ArrayList;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
/**
 * TreeADDBranch represents branch of a treeADD. If the top variable of the treeADD is numeric a branch 
 * is defined by two thresholds: a minimun and a maximun limit
 * 
 * If top variable is finite states, then each branch is defined by its states
 * 
 * In both cases branches have a potential assigned,. If the branch is a leaf, it potential could be any kind of potential except a TreeADDPotential
 * 
 * @author myebra
 *
 */
public class TreeADDBranch {

	/**
	 * Each treeADDBranch has a potential associated
	 */
	private Potential potential;
	/**
	 * If the topVariable of the tree is a finite states or a discretized variable each branch has an associated state.
	 */
	private ArrayList<State> states;
	private ArrayList<Variable> parentVariables;
	/**
	 * If the topVariable of the tree is a continuous variable it is defined in a continuous interval which has two thresholds.
	 */
	private Threshold thresholdMin;
	private Threshold thresholdMax;
	/**
	 * A branch can be labeled, labels are used to reference potential from other branches when that potential has more than one parents
	 * 
	 */
	private Variable topVariable;
	 
	/*private String label;*/
	/**
	 * A branch can reference a potential from other branch that has been labeled
	 */
	/*private String reference;*/
	
	/**
	 * Constructor for discretized and finite estates variables
	 * @param branchStates
	 * @param potential
	 * @param topVariable
	 * @param parentVariables
	 */
	public TreeADDBranch(ArrayList<State> branchStates, Potential potential, Variable topVariable, ArrayList<Variable> parentVariables) {
		this.states = branchStates;
		this.potential = potential;
		this.topVariable = topVariable;
		this.parentVariables = parentVariables;
		//this.treeADD = treeADD;
		
	}
	/**
	 * Constructor for the parser
	 * @param thresholds
	 * @param potential
	 */
	public TreeADDBranch (Threshold[] thresholds, Potential potential, Variable topVariable, ArrayList<Variable> parentVariables) {
		this.thresholdMax = thresholds[1];
		this.thresholdMin = thresholds[0];
		this.topVariable = topVariable;
		this.parentVariables = parentVariables;
		this.potential = potential;
		
	}
		
	/*public TreeADDBranch(ArrayList<State> branchStates, Potential potential, String label) {
		this.states = branchStates;
		this.potential = potential;
		this.label = label;
		//this.treeADD = treeADD;
	}*/
	
	/* 
	 * @argCondition reference must be one of the labels in this ADD
	 *  and the reference must not create a cycle in the ADD. If a branch has assigned a reference cannot has assigned a potential */
	/*public void setReference(String reference) {
		this.reference = reference;
	}*/
	
	/* 
	 * @argCondition label is incompatible with reference */
	/*public void setLabel(String label) {
		this.label = label;
	}*/
	
	/*public TreeADDBranch(ArrayList<State> branchStates, String reference) {
		this.states = branchStates;
		this.reference = reference;
		
	}*/
	/**
	 * Constructor for numeric variables
	 * @param thresholdMin
	 * @param thresholdMax
	 * @param potential
	 * @param topVariable
	 * @param parentVariables
	 */
	public TreeADDBranch(Threshold thresholdMin, Threshold thresholdMax, Potential potential, Variable topVariable, ArrayList<Variable> parentVariables) {
		this.thresholdMin = thresholdMin;
		this.thresholdMax = thresholdMax;
		this.potential = potential;
		this.topVariable = topVariable;
		this.parentVariables = parentVariables;
		
	}
	
	/*public TreeADDBranch(ArrayList<State> branchStates, Potential potential, String label) {
		this.states = branchStates;
		this.label = label;
		this.potentialsLabeled = new HashMap<String, Potential>();
		this.potential = potentialsLabeled.put(label, potential);
		
	}
	
	public TreeADDBranch(ArrayList<State> branchStates, String reference) {
		this.states = branchStates;
	
	}*/
	public void setThresholdMin(Threshold min) {
		this.thresholdMin = min;
		
	}
	
	public void setThresholdMax(Threshold max) {
		this.thresholdMax = max;
		
	}
	
	public ArrayList<State> getBranchStates() {
		return this.states;
	}
	
	public ArrayList<Variable> getParentVariables() {
		return this.parentVariables;
	}
	public void setParentVariables(ArrayList<Variable> parentVariables) {
		this.parentVariables = parentVariables;
	}
	
	
	public Variable getTopVariable() {
		return this.topVariable;
	}
	/*public String getLabel(){
		return this.label;
	}
	
	public String getReference(){
		return this.reference;
	}*/
	
	public Potential getPotential() {
		return this.potential;
	}
	
	public void setPotential(Potential potential){
		this.potential = potential;
	}
	public void setTopVariable(Variable topVariable){
		this.topVariable = topVariable;
	}
	public void setStates (ArrayList<State> states) {
		this.states = states;
	}
	public Threshold getMinThreshold () {
		return thresholdMin;
	}
	public Threshold getMaxThreshold () {
		return thresholdMax;
	}
	

}
