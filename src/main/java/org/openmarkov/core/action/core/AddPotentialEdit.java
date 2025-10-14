/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.SimplePNEdit;

@SuppressWarnings("serial") public class AddPotentialEdit extends SimplePNEdit {

	protected Potential potential;

	// Constructor

	/**
	 * @param probNet   {@code ProbNet}
	 * @param potential {@code Potential}
	 */
	public AddPotentialEdit(ProbNet probNet, Potential potential) {
		super(probNet);
		this.potential = potential;
	}

	// Methods
	@Override public void doEdit() {
		probNet.addPotential(potential);
	}
    
    @Override public void doEdit(ProbNet probNet) throws ConstraintViolatedException {
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}
	
	@Override public void undo() {
		super.undo();
		probNet.removePotential(potential);
	}

	/**
	 * @return potential {@code Potential}
	 */
	public Potential getPotential() {
		return potential;
	}

	/**
	 * @return A {@code String} with the potential variables.
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder("AddPotentialEdit: ");
		if (potential != null) {
			buffer.append(potential.getVariables());
		} else {
			buffer.append("null !!!!");
		}
		return buffer.toString();
	}

}
