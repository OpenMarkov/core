package org.openmarkov.core.action;

import java.util.ArrayList;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;

@SuppressWarnings("serial")
public class ReorderVariableEdit  extends SimplePNEdit {

	private Object [][]dataTable;
	private ArrayList<Variable> oldVariables;
	private ArrayList<Variable> newVariables = new ArrayList<Variable>();
	private StateAction stateAction;
	private ProbNode probNode;
	private ArrayList<Variable> variables = new ArrayList<Variable>();
	
	public ReorderVariableEdit(ProbNode probNode, Object data[][], StateAction stateAction) {
		super(probNode.getProbNet());
		this.probNode = probNode;
		this.dataTable = data;
		this.stateAction = stateAction;
		this.oldVariables = (ArrayList<Variable>) probNode.getPotentials().get(0).getVariables().clone();
		if (probNode.getPotentials().get(0).getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
			variables = (ArrayList<Variable>) probNode.getPotentials().get(0).getVariables().clone();
			variables.remove(0);
			
		} else if (probNode.getPotentials().get(0).getPotentialRole() == PotentialRole.UTILITY) {
			variables = (ArrayList<Variable>) probNode.getPotentials().get(0).getVariables().clone();
		}
		
	}

	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		switch (stateAction){
		case DOWN:
			ArrayList<Variable> newVariablesDown = new ArrayList<Variable>();
			for (int i = 0; i < dataTable.length; i++) {
				for (int j = 0; j < variables.size(); j++) {
					if ((String)dataTable[i][0] == variables.get(j).getName()) {
						newVariablesDown.add(variables.get(j));
					}
				}
			}
			if (probNode.getPotentials().get(0).getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
				this.newVariables.add(probNode.getPotentials().get(0).getVariables().get(0));
				this.newVariables.addAll(newVariablesDown);
			} else if (probNode.getPotentials().get(0).getPotentialRole() == PotentialRole.UTILITY) {
				this.newVariables.addAll(newVariablesDown);
			}
			probNode.getPotentials().get(0).setVariables(newVariables);
			break;
		case UP:
			ArrayList<Variable> newVariablesUp = new ArrayList<Variable>();
			for (int i = 0; i < dataTable.length; i++) {
				for (int j = 0; j < variables.size(); j++) {
					if ((String)dataTable[i][0] == variables.get(j).getName()) {
						newVariablesUp.add(variables.get(j));
					}
				}
			}
			if (probNode.getPotentials().get(0).getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
				this.newVariables.add(probNode.getPotentials().get(0).getVariables().get(0));
				this.newVariables.addAll(newVariablesUp);
			} else if (probNode.getPotentials().get(0).getPotentialRole() == PotentialRole.UTILITY) {
				this.newVariables.addAll(newVariablesUp);
			}
			probNode.getPotentials().get(0).setVariables(newVariables);
			break;

		}
		
	}
	@Override
	public void undo() {
		super.undo();
		probNode.getPotentials().get(0).setVariables(oldVariables);
	}

}
