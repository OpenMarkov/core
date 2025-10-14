/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.DistinctVariableNames;
import org.openmarkov.core.model.network.constraint.NoEmptyName;
import org.openmarkov.core.model.network.constraint.ValidName;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.SimplePNEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code NodeNameEdit} is a simple edit that allow modify the node
 * name.
 *
 * @author Miguel Palacios
 * @version 1.0 21/12/10
 */
@SuppressWarnings("serial") public class NodeNameEdit extends SimplePNEdit {
	/**
	 * Current node name
	 */
	private String previousName;
	/**
	 * New node name
	 */
	private String newName;
	/**
	 * The node edited
	 */
    private List<Variable> variables;

	/**
	 * Creates a new {@code NodeNameEdit} with the node and new name
	 * specified.
	 *
	 * @param node    the node that will be modified
	 * @param newName the new name of the node
	 */
	public NodeNameEdit(Node node, String newName) {
		super(node.getProbNet());
		variables = new ArrayList<>();
		for (Variable variable : node.getProbNet().getVariables()) {
			if (variable.getBaseName().equals(node.getVariable().getBaseName())) {
				variables.add(variable);
			}
		}
		this.previousName = node.getVariable().getBaseName();
		this.newName = newName;
	}
    
    @Override public void checkConstraintsWillBeMet() throws ConstraintViolatedException {
        if (probNet.getConstraintOfClass(DistinctVariableNames.class) instanceof DistinctVariableNames constraint) {
            if (probNet.getVariablesNames().contains(newName)) {
                throw new ConstraintViolatedException.VariableNameIsAlreadyPresent(constraint, this.newName);
            }
        }
        if (probNet.getConstraintOfClass(NoEmptyName.class) instanceof NoEmptyName constraint) {
            if ((this.newName == null) || (this.newName.contentEquals(""))) {
                throw new ConstraintViolatedException(constraint);
            }
        }
        if (probNet.getConstraintOfClass(ValidName.class) instanceof ValidName constraint) {
            if (!ValidName.checkName(this.getNewName(), this.getPreviousName(), probNet)) {
                throw new ConstraintViolatedException(constraint);
            }
        }
    }
    
    @Override public void doEdit() {
		for (Variable variable : variables) {
			variable.setBaseName(newName);
		}
	}
    
    @Override public void doEdit(ProbNet probNet) throws ConstraintViolatedException {
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}

	@Override public void undo() {
		super.undo();
		for (Variable variable : variables) {
			variable.setBaseName(previousName);
		}
	}

	/**
	 * Gets the new name of the node
	 *
	 * @return the new name of the node
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * Gets the previous name of the node
	 *
	 * @return the previous name of the node
	 */
	public String getPreviousName() {
		return previousName;
	}
}
