/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.model.network.ProbNet;

@SuppressWarnings("serial")

/*
  @author Manuel Arias
 * @see openmarkov.networks.edit.CloseParenthesisEdit
 */
public class OpenParenthesisEdit extends PNEdit {

	// Constant
	public static final String description = "(";

	// Constructor

	/**
	 * Singleton pattern
	 */
    public OpenParenthesisEdit(ProbNet probNet) {
        super(probNet);
	}

	// Methods
	@Override protected void doEdit() {
		//super.addEdit(this);
	}
	
	@Override public void undo() {
		super.undo();
	}

	public String toString() {
		return description;
	}

}
