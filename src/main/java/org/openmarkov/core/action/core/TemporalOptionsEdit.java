/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.SimplePNEdit;

import javax.swing.undo.CannotUndoException;

public class TemporalOptionsEdit extends SimplePNEdit {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	private TemporalOptions oldTemporalOptions;
	private TemporalOptions newTemporalOptions;

	public TemporalOptionsEdit(ProbNet probNet, TemporalOptions options) {
		super(probNet);
		this.oldTemporalOptions = probNet.getInferenceOptions().getTemporalOptions().clone();
		this.newTemporalOptions = options;
	}

	@Override public void doEdit() {
		probNet.getInferenceOptions().setTemporalOptions(this.newTemporalOptions);

	}
    
    @Override public void doEdit(ProbNet probNet) throws ConstraintViolatedException {
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}
	
	@Override public void undo() throws CannotUndoException {
		super.undo();
		probNet.getInferenceOptions().setTemporalOptions(this.oldTemporalOptions);
	}

	@Override public void redo() {
		super.redo();
		doEdit();
	}

}
