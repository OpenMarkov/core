package org.openmarkov.core.model.network.potential.treeadd;

import java.util.ArrayList;
import java.util.HashMap;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.potential.Potential;

public class TreeADDBranch {
	/**
	 * Each branch belongs to a treeADD
	 */
	private TreeADDPotential2 treeADD;
	/**
	 * Each treeADDBranch has a potential associated
	 */
	private Potential potential;
	/**
	 * If the topVariable of the tree is a finite states or a discretized variable each branch has an associated state.
	 */
	private ArrayList<State> states;
	/**
	 * If the topVariable of the tree is a continuous variable it is defined in a continuous interval which has two thresholds.
	 */
	private Threshold thresholdMin;
	private Threshold thresholdMax;
	/**
	 * A branch can be labeled, labels are used to reference potential from other branches when that potential has more than one parents
	 * 
	 */
	private String label;
	/**
	 * A branch can reference a potential from other branch that has been labeled
	 */
	private String reference;
	
	
	public TreeADDBranch(ArrayList<State> branchStates, Potential potential, TreeADDPotential2 treeADD) {
		this.states = branchStates;
		this.potential = potential;
		this.treeADD = treeADD;
		
	}
	
	public TreeADDBranch(ArrayList<State> branchStates, Potential potential, String label, TreeADDPotential2 treeADD) {
		this.states = branchStates;
		this.potential = potential;
		this.label = label;
		this.treeADD = treeADD;
	}
	
	/* 
	 * @argCondition reference must be one of the labels in this ADD
	 *  and the reference must not create a cycle in the ADD. If a branch has assigned a reference cannot has assigned a potential */
	public void setReference(String reference) {
		this.reference = reference;
	}
	
	/* 
	 * @argCondition label is incompatible with reference */
	public void setLabel(String label) {
		this.label = label;
	}
	
	public TreeADDBranch(ArrayList<State> branchStates, String reference, TreeADDPotential2 treeADD) {
		this.states = branchStates;
		this.reference = reference;
		this.treeADD = treeADD;
	}
	
	public TreeADDBranch(Threshold thresholdMin, Threshold thresholdMax, Potential potential, TreeADDPotential2 treeADD) {
		this.thresholdMin = thresholdMin;
		this.thresholdMax = thresholdMax;
		this.potential = potential;
		this.treeADD = treeADD;
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
	
	public ArrayList<State> getBranchStates() {
		return this.states;
	}
	
	public TreeADDPotential2 getTreeADDParent () {
		return this.treeADD;
	}
	
	public String getLabel(){
		return this.label;
	}
	
	public String getReference(){
		return this.reference;
	}
	
	public Potential getPotential() {
		return this.potential;
	}
	
	public void setPotential(Potential potential){
		this.potential = potential;
	}
	

}
