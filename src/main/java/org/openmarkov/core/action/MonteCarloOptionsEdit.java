/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.ProbNet;

import javax.swing.undo.CannotUndoException;

/**
 * This class contains the information for doEdit() of MonteCarloOptions
 * @author cyago
 * @version 1.0 25/08/2019
 */
public class MonteCarloOptionsEdit extends SimplePNEdit {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	private MonteCarloOptions oldMonteCarloOptions;
	private MonteCarloOptions newMonteCarloOptions;

	public MonteCarloOptionsEdit(ProbNet probNet, MonteCarloOptions options) {
		super(probNet);
		this.oldMonteCarloOptions = probNet.getInferenceOptions().getMonteCarloOptions().clone();
		this.newMonteCarloOptions = options;
	}

	@Override public void doEdit() throws DoEditException {
		probNet.getInferenceOptions().setMonteCarloOptions(this.newMonteCarloOptions);

	}

	@Override public void undo() throws CannotUndoException {
		super.undo();
		probNet.getInferenceOptions().setMonteCarloOptions(this.oldMonteCarloOptions);
	}

	@Override public void redo() {
		super.redo();
		try {
			doEdit();
		} catch (DoEditException e) {
			e.printStackTrace();
		}
	}

}
