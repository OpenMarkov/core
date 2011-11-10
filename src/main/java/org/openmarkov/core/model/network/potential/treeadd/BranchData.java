package org.openmarkov.core.model.network.potential.treeadd;

import java.util.HashSet;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

public class BranchData implements Cloneable {
	protected Variable branchVariable;
	protected HashSet<State> branchStates= new HashSet<State>();
	protected HashSet<BranchInterval> branchIntervals= new HashSet<BranchInterval>();
	
	public BranchData (Variable branchVariable) {
		this.branchVariable= branchVariable;
	}
	
	public Variable getBranchVariable() {
		return branchVariable;
	}
	
	public void add (State newState) {
		if (branchVariable.getVariableType()==VariableType.NUMERIC) {
			throw new UnsupportedOperationException();
		}
		
		branchStates.add(newState);
	}
	
	public void remove (State state) {
		if (branchVariable.getVariableType()==VariableType.NUMERIC) {
			throw new UnsupportedOperationException();
		}
		
		branchStates.remove (state);
	}

	public void add (BranchInterval newInterval) {
		if (branchVariable.getVariableType()!=VariableType.NUMERIC) {
			throw new UnsupportedOperationException();
		}
		
		branchIntervals.add(newInterval);
	}
	
	public void remove (BranchInterval interval) {
		if (branchVariable.getVariableType()!=VariableType.NUMERIC) {
			throw new UnsupportedOperationException();
		}
		
		branchIntervals.remove (interval);
	}
	
	public HashSet<State> getBranchStates() {
		return this.branchStates;
	}
	
	public HashSet<BranchInterval> getBranchIntervals() {
		return this.branchIntervals;
	}
	
	@SuppressWarnings("unchecked")
	public Object clone() throws CloneNotSupportedException {

		BranchData clone= (BranchData) super.clone();

		clone.branchVariable= branchVariable;
	    
		clone.branchStates= (HashSet<State>) branchStates.clone();
		clone.branchIntervals= (HashSet<BranchInterval>) branchIntervals.clone();

	    return clone;
	  }

	public boolean containsStateName(String stateName) {
		for (State state : branchStates) {
			if (state.getName().contentEquals(stateName)) {
				return true;
			}
		}
		return false;
	}	
}
