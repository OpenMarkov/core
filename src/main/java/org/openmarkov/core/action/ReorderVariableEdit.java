package org.openmarkov.core.action;

import java.util.ArrayList;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;

public class ReorderVariableEdit  extends SimplePNEdit {

	private Object [][]dataTable;
	private ArrayList<Variable> oldVariables;
	private ArrayList<Variable> newVariables;
	private StateAction stateAction;
	private ProbNode probNode;
	
	public ReorderVariableEdit(ProbNode probNode, Object data[][], StateAction stateAction) {
		super(probNode.getProbNet());
		this.probNode = probNode;
		this.dataTable = data;
		this.stateAction = stateAction;
		if (probNode.getPotentials().get(0).getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
			ArrayList<Variable> variables = (ArrayList<Variable>) probNode.getPotentials().get(0).getVariables().clone();
			variables.remove(0);
			this.oldVariables = variables;
		} else if (probNode.getPotentials().get(0).getPotentialRole() == PotentialRole.UTILITY) {
			this.oldVariables = (ArrayList<Variable>) probNode.getPotentials().get(0).getVariables().clone();
		}
		
	}

	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		switch (stateAction){
		case DOWN:
			ArrayList<Variable> newVariablesDown = new ArrayList<Variable>();
			for (int i = 0; i < dataTable.length; i++) {
				for (int j = 0; i < oldVariables.size(); j++) {
					if ((String)dataTable[i][0] == oldVariables.get(j).getName()) {
						newVariablesDown.add(oldVariables.get(j));
					}
				}
			}
			probNode.getPotentials().get(0).setVariables(newVariablesDown);
			break;
		case UP:
			ArrayList<Variable> newVariablesUp = new ArrayList<Variable>();
			for (int i = 0; i < dataTable.length; i++) {
				for (int j = 0; i < oldVariables.size(); j++) {
					if ((String)dataTable[i][0] == oldVariables.get(j).getName()) {
						newVariablesUp.add(oldVariables.get(j));
					}
				}
			}
			probNode.getPotentials().get(0).setVariables(newVariablesUp);
			break;

		}
		
	}
	@Override
	public void undo() {
		super.undo();
		probNode.getPotentials().get(0).setVariables(oldVariables);
	}

}
