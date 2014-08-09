/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.sdag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

public class CoalescedIntervention extends Intervention {
	
	public CoalescedIntervention(Variable topVariable) {
		super(topVariable);
		initializeParents();
	}
	
	private void updateParentsBranches(){
		if (branches != null){
			for (TreeADDBranch branch:branches){
				CoalescedIntervention branchInterv = (CoalescedIntervention) branch.getPotential();
				if (branchInterv != null){
					branchInterv.addParent(branch);
				}
			}
		}
	}

	private void initializeParents() {
		parents = new HashSet<>();
		
	}

	public CoalescedIntervention(Variable decisionVariable, List<State> optimalStates,
			List<Intervention> optimalInterventions) {
		super(decisionVariable,optimalStates,optimalInterventions);
		initializeParents();
		updateParentsBranches();
	}

	public CoalescedIntervention(Variable decisionVariable, List<State> optimalStates,
			Intervention intervention) {
		super(decisionVariable,optimalStates,intervention);
		initializeParents();
		updateParentsBranches();
	}

	protected Set<TreeADDBranch> parents;

	/** 
	 * Add <code>Intervention</code> to edges of this intervention
	 * @param intervention
	 * @throws Exception 
	 * It concatenates taking care of the coalescence
	 */
	@Override
	public Intervention concatenate(Intervention intervention) {
	
		return concatenate(null,intervention);
		
	}
	
	/**
	 * @param branchParent
	 * @param intervention
	 * @return It concatenates taking care of the coalescence
	 */
	public Intervention concatenate(TreeADDBranch branchParent,Intervention intervention) {
		
	CoalescedIntervention result;
	Set<TreeADDBranch> auxParents;
	
	auxParents = new HashSet<>();
	auxParents.addAll(parents);
	auxParents.remove(branchParent);
	
	boolean hasOtherParents = auxParents.size()>0;
	
	if (hasOtherParents){
		result = this.copy().carefreeConcatenate(intervention);
	}
	else{
		for (TreeADDBranch branch : branches) {
			CoalescedIntervention branchIntervention = (CoalescedIntervention)branch.getPotential();
			if (branchIntervention == null) {
				branch.setPotential(intervention);
				((CoalescedIntervention)intervention).parents.add(branch);
			} else {
				branchIntervention.concatenate(branch,intervention);
			}
		}
		result = this;
	}
	
	
	return result;
}
		

	/**
	 * @param intervention
	 * @return It concatenates not taking care of the coalescence, because the receiving object is a copy
	 */
	private CoalescedIntervention carefreeConcatenate(Intervention intervention) {

		for (TreeADDBranch branch : branches) {
			CoalescedIntervention branchIntervention = (CoalescedIntervention)branch.getPotential();
			if (branchIntervention == null) {
				branch.setPotential(intervention);
				((CoalescedIntervention)intervention).parents.add(branch);
			} else {
				branchIntervention.carefreeConcatenate(intervention);
			}
		}
		return this;
	}

	@Override
	public CoalescedIntervention copy() {
		CoalescedIntervention newInt = new CoalescedIntervention(this.getRootVariable());

		if (this.getBranches() != null) {
			// Create branches
			for (TreeADDBranch branch : this.getBranches()) {
				List<State> newStates = new ArrayList<State>();
				newStates.addAll(branch.getStates());
				CoalescedIntervention interv = (CoalescedIntervention) branch.getPotential();
				if (interv != null) {
					CoalescedIntervention intervCopy = interv.copy();
					TreeADDBranch newBranch = new TreeADDBranch(newStates, branch.getRootVariable(), intervCopy, null);
					newInt.addBranch(newBranch);
					intervCopy.addParent(newBranch);
				}
			}
		}
		return newInt;

	}
	
	public static Intervention buildIntervention(List<Intervention> optimalInterventions,Variable decisionVariable,
			List<State> optimalStates){
		Intervention intervention = (optimalInterventions.size() > 1)? 
				new CoalescedIntervention(decisionVariable, optimalStates, optimalInterventions):
					new CoalescedIntervention(decisionVariable, optimalStates, optimalInterventions.get(0));
		
    	return intervention;
	}


	private void addParent(TreeADDBranch newBranch) {
		parents.add(newBranch);
		
	}
}
