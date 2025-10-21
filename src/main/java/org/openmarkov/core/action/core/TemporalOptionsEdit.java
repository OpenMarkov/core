/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.action.base.PNEdit;

public class TemporalOptionsEdit extends PNEdit {

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
    
    @Override public void undo() {
		super.undo();
		probNet.getInferenceOptions().setTemporalOptions(this.oldTemporalOptions);
	}

	@Override public void redo() {
		super.redo();
		doEdit();
	}

}
