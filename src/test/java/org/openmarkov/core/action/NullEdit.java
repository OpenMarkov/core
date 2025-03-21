/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.model.network.ProbNet;

public class NullEdit extends SimplePNEdit {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 1L;
	private int numEdit;

	public NullEdit(ProbNet probNet, int numEdit) {
		super(probNet);
		this.numEdit = numEdit;
	}

	@Override public void doEdit() {

	}

	@Override public void undo() {
		super.undo();

	}

	public int getNumEdit() {
		return numEdit;
	}

}
