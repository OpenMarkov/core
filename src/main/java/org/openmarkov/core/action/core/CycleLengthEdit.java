/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.SimplePNEdit;

import javax.swing.undo.CannotUndoException;

public class CycleLengthEdit extends SimplePNEdit {

	/**
	 * Default serial UID
	 */
	private static final long serialVersionUID = 1L;

	private CycleLength oldTemporalUnit;
	private CycleLength newTemporalUnit;

	public CycleLengthEdit(ProbNet probNet, CycleLength newTemporalUnit) {
		super(probNet);
		this.oldTemporalUnit = probNet.getCycleLength().clone();
		this.newTemporalUnit = newTemporalUnit;
	}

	@Override public void doEdit() {
		probNet.setCycleLength(this.newTemporalUnit);
	}
    
    @Override public void doEdit(ProbNet probNet) throws ConstraintViolatedException {
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}
	
	@Override public void undo() throws CannotUndoException {
		super.undo();
		probNet.setCycleLength(this.oldTemporalUnit);
	}

	@Override public void redo() {
		super.redo();
		doEdit();
	}

}
