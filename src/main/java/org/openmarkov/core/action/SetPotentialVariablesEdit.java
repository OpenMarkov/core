package org.openmarkov.core.action;

import java.util.ArrayList;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

@SuppressWarnings("serial")
public class SetPotentialVariablesEdit extends SimplePNEdit{

	private ArrayList<Variable> oldVariables;
	private ArrayList<Variable> newVariables;
	private ProbNode probNode;
	
	@SuppressWarnings("unchecked")
	public SetPotentialVariablesEdit(ProbNode probNode, ArrayList<Variable> newVariables) {
		super(probNode.getProbNet());
		this.probNode = probNode;
		this.oldVariables = (ArrayList<Variable>) probNode.getPotentials().get(0).getVariables().clone();
		this.newVariables = newVariables;
	}

	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		probNode.getPotentials().get(0).setVariables(newVariables);
	}
	public void undoEdit() throws DoEditException, NotEnoughMemoryException {
		probNode.getPotentials().get(0).setVariables(oldVariables);
	}

}
