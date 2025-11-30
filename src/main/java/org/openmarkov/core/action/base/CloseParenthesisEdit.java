/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.model.network.ProbNet;

@SuppressWarnings("serial") public class CloseParenthesisEdit extends PNEdit {

	// Constant
	public static final String description = ")";

	// The open parenthesis of the close parenthesis
	private OpenParenthesisEdit openParenthesisEdit;

	// Constructor
    public CloseParenthesisEdit(ProbNet probNet, OpenParenthesisEdit openParenthesisEdit) {
        super(probNet);
		this.openParenthesisEdit = openParenthesisEdit;
	}

	// Methods
    @Override protected void doEdit() {
		//super.addEdit(this);
	}

	public String toString() {
		return description;
	}

	public OpenParenthesisEdit getOpenParenthesisEdit() {
		return openParenthesisEdit;
	}

}
