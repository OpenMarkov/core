package org.openmarkov.core.action;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;

@SuppressWarnings("serial")
public class SetPotentialVariablesEdit extends SimplePNEdit{

	private List<Variable> oldVariables;
	private List<Variable> newVariables;
	private Node probNode;
	
	public SetPotentialVariablesEdit(Node probNode, List<Variable> newVariables) {
		super(probNode.getProbNet());
		this.probNode = probNode;
		this.oldVariables = new ArrayList<Variable>(probNode.getPotentials().get(0).getVariables());
		this.newVariables = newVariables;
	}

	@Override
	public void doEdit() throws DoEditException {
		probNode.getPotentials().get(0).setVariables(newVariables);
	}
	public void undoEdit() throws DoEditException {
		probNode.getPotentials().get(0).setVariables(oldVariables);
	}
}