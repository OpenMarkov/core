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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

// TODO Documentar
public class SDAGIntervention extends Intervention {
	
	public SDAGIntervention(Variable topVariable) {
		super(topVariable);
		initializeParents();
	}
	
	private void updateParentsBranches(){
		if (branches != null){
			for (TreeADDBranch branch:branches){
				SDAGIntervention branchInterv = (SDAGIntervention) branch.getPotential();
				if (branchInterv != null){
					branchInterv.addParent(branch);
				}
			}
		}
	}

	private void initializeParents() {
		parents = new HashSet<>();
		
	}

	public SDAGIntervention(Variable decisionVariable, List<State> optimalStates,
			List<Intervention> optimalInterventions) {
		super(decisionVariable,optimalStates,optimalInterventions);
		initializeParents();
		updateParentsBranches();
	}

	public SDAGIntervention(Variable decisionVariable, List<State> optimalStates,
			Intervention intervention) {
		super(decisionVariable,optimalStates,intervention);
		initializeParents();
		updateParentsBranches();
	}

	public SDAGIntervention(Variable decisionVariable, List<State> optimalStates) {
		super(decisionVariable,optimalStates);
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
	
		return concatenate(null,(SDAGIntervention)intervention);
		
	}
	
	/**
	 * @param branchParent
	 * @param intervention
	 * @return It concatenates taking care of the coalescence
	 */
	public Intervention concatenate(TreeADDBranch branchParent, SDAGIntervention intervention) {

		SDAGIntervention result;
		Set<TreeADDBranch> auxParents = new HashSet<>();

		auxParents.addAll(parents);
		auxParents.remove(branchParent);

		//TODO Analize what happens if next line is uncommented, replacing its next line
		//The line "boolean hasOtherParents = true;" is correct, but it could be inefficient in huge networks 
		//boolean hasOtherParents = auxParents.size() > 0;
		boolean hasOtherParents = true;

		if (hasOtherParents) {
			result = copy().carefreeConcatenate(intervention);
		} else {
			for (TreeADDBranch branch : branches) {
				SDAGIntervention branchIntervention = getCoalescedInterventionBranch(branch);
				if (branchIntervention == null) {
					branch.setPotential(intervention);
					intervention.parents.add(branch);
				} else {
					branchIntervention.concatenate(branch, intervention);
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
	private SDAGIntervention carefreeConcatenate(SDAGIntervention intervention) {

		for (TreeADDBranch branch : branches) {
			SDAGIntervention branchIntervention = getCoalescedInterventionBranch(branch);
			if (branchIntervention == null) {
				branch.setPotential(intervention);
				intervention.parents.add(branch);
			} else {
				branchIntervention.carefreeConcatenate(intervention);
			}
		}
		return this;
	}

	@Override
	public SDAGIntervention copy() {
		SDAGIntervention newInt = new SDAGIntervention(this.getRootVariable());

		if (this.getBranches() != null) {
			// Create branches
			for (TreeADDBranch branch : getBranches()) {
				List<State> newStates = new ArrayList<>();
				newStates.addAll(branch.getStates());
				SDAGIntervention interv = getCoalescedInterventionBranch(branch);
				SDAGIntervention intervCopy;
				if (interv != null) {
					 intervCopy = interv.copy();
				}
				else{
					intervCopy = null;
				}
				TreeADDBranch newBranch = new TreeADDBranch(newStates, branch.getRootVariable(), intervCopy, null);
				newInt.addBranch(newBranch);
				if (intervCopy != null){
					intervCopy.addParent(newBranch);
				}
			}
		}
		return newInt;

	}
	
	public SDAGIntervention getCoalescedInterventionBranch(TreeADDBranch branch){
		return (SDAGIntervention)getInterventionBranch(branch);
	}
	
	
	
	public static Intervention buildIntervention(List<Intervention> optimalInterventions,Variable decisionVariable,
			List<State> optimalStates){
		Intervention intervention = (optimalInterventions.size() > 1)? 
				new SDAGIntervention(decisionVariable, optimalStates, optimalInterventions):
					new SDAGIntervention(decisionVariable, optimalStates, optimalInterventions.get(0));
		
    	return intervention;
	}


	private void addParent(TreeADDBranch newBranch) {
		parents.add(newBranch);
		
	}
	
	@Override
	public Potential deepCopy(ProbNet copyNet) {
		SDAGIntervention potential = (SDAGIntervention) super.deepCopy(copyNet);
		Set<TreeADDBranch> newParents = new HashSet<>();
		Iterator<TreeADDBranch> iterator = this.parents.iterator();

		while(iterator.hasNext()){
			newParents.add(iterator.next().deepCopy(copyNet));
		}

		potential.parents = newParents;

		return potential;
	}
}
