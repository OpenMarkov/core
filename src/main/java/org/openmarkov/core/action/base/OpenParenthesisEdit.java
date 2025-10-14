/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.ProbNet;

@SuppressWarnings("serial")

/*
  @author Manuel Arias
 * @see openmarkov.networks.edit.CloseParenthesisEdit
 */
public class OpenParenthesisEdit extends SimplePNEdit {

	// Constant
	public static final String description = "(";

	// Constructor

	/**
	 * Singleton pattern
	 */
	public OpenParenthesisEdit() {
		super(null);
	}

	// Methods
	@Override public void doEdit() {
		//super.addEdit(this);
	}
    
    @Override public void doEdit(ProbNet probNet) throws ConstraintViolatedException {
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}
	
	@Override public void undo() {
		super.undo();
	}

	@Override public String getUndoPresentationName() {
		return description + " " + getPresentationName();
	}

	@Override public String getRedoPresentationName() {
		return description + " " + getPresentationName();
	}

	public String toString() {
		return description;
	}

}
