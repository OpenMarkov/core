/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.action.base.PNEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit that replaces the variable list of a node's first potential. Supports undo
 * by restoring the original variable list.
 */
@SuppressWarnings("serial") public class SetPotentialVariablesEdit extends PNEdit {

	private List<Variable> oldVariables;
	private List<Variable> newVariables;
	private Node node;

	public SetPotentialVariablesEdit(Node node, List<Variable> newVariables) {
		super(node.getProbNet());
		this.node = node;
		this.oldVariables = new ArrayList<>(node.getPotentials().get(0).getVariables());
		this.newVariables = newVariables;
	}
	
	@Override protected void doEdit() {
		node.getPotentials().get(0).setVariables(newVariables);
	}
    
    @Override public void undo() {
        node.getPotentials().get(0).setVariables(oldVariables);
    }
}