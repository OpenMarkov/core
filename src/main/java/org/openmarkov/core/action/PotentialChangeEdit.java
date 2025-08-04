/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.List;

/**
 * Changes an old potential for a new potential
 */
public class PotentialChangeEdit extends SimplePNEdit {

	// Attribute
	private Potential newPotential;

	private Potential oldPotential;

	private Node node;

	// Constructor

	/**
	 * @param probNet      {@code ProbNet}
	 * @param oldPotential {@code Potential}
	 * @param newPotential {@code Potential}
	 */
	public PotentialChangeEdit(ProbNet probNet, Potential oldPotential, Potential newPotential) {
		super(probNet);
		this.newPotential = newPotential;
		this.oldPotential = oldPotential;
	}

	public PotentialChangeEdit(Node node, Potential oldPotential, Potential newPotential){
		super(node.getProbNet());
		this.newPotential = newPotential;
		this.oldPotential = oldPotential;
		this.node = node;
	}

	@Override public void doEdit() throws DoEditException.CannotRemovePotential {
		if(node == null){
			if (probNet.removePotential(oldPotential) == null) {
				throw new DoEditException.CannotRemovePotential(probNet, oldPotential);
			}
			probNet.addPotential(newPotential);
		}else{
			List<Potential> potentials = new ArrayList<>();
			potentials.add(newPotential);
			potentials.add(oldPotential);
			node.setPotentials(potentials);
		}
	}

    @Override public void doEdit(ProbNet probNet) throws DoEditException.ConstraintViolated, DoEditException.CannotRemovePotential {
        PNEdit.startEdit(this, probNet);
        this.doEdit();
        PNEdit.endEdit(this);
    }

	@Override
	public void undo() {
		super.undo();
		probNet.removePotential(newPotential);
		if (probNet.getNode(oldPotential.getVariable(0)).getNodeType() == NodeType.CHANCE) {
			probNet.addPotential(oldPotential);
		}else if (probNet.getNode(oldPotential.getVariable(0)).hasPolicy()) {
			probNet.addPotential(oldPotential);
		}
	}

	@Override
	public void redo() {
		super.redo();
		probNet.removePotential(oldPotential);
		probNet.addPotential(newPotential);
	}

	/**
	 * @return A {@code String} with the variables of both potentials.
	 */
	public String toString() {
		return "ChangePotentialEdit: " + oldPotential.getVariables() + " --> " + newPotential.getVariables();
	}

	public Potential getNewPotential() {
		return newPotential;
	}

	public Potential getOldPotential() {
		return oldPotential;
	}

}
