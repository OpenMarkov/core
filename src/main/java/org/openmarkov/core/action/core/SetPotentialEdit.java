/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.action.base.PNEdit;


public class SetPotentialEdit extends PNEdit {
	// unused - private PotentialType lastPotentialType;

	private Potential lastPotential;


	private String newPotentialType;


	private Variable variable;

	private Potential newPotential = null;

	private Node node;


	public SetPotentialEdit(Node node, String newPotentialType) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		lastPotential = node.getPotentials().get(0);
		this.newPotentialType = newPotentialType;

	}


	public SetPotentialEdit(Node node, Potential potential) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		   // If node is a decision node it may have no potential assigned yet.
        if (!node.getPotentials().isEmpty()) {
        	lastPotential = node.getPotentials().get(0);
		}

		newPotential = potential;
		this.newPotentialType = newPotential.getClass().getAnnotation(PotentialType.class).name();
	}


	public SetPotentialEdit(Node node,
                            Potential lastPotential,
                            Potential newPotential) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		this.lastPotential = lastPotential;
		this.newPotential = newPotential;
		this.newPotentialType = newPotential.getClass().getAnnotation(PotentialType.class).name();
	}


	// TODO al asignar un potencial tener en cuenta a los padres y a los
	// predecesores informativos que me los va a dar Manolo invocando a una
	// funcion
    
    @Override protected void doEdit() {
		node.setPotentialConsistently(newPotential);
	}

	@Override public void undo() {
		super.undo();
        node.setPotentialConsistently(lastPotential);
	}


	@Override public void redo() {
		super.redo();
        node.setPotentialConsistently(newPotential);
	}

	public Potential getNewPotential() {
		return newPotential;
	}

	public String getNewPotentialType() {
		return newPotentialType;
	}

	public Node getNode() {
		return node;
	}

}
