/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;

import javax.swing.undo.UndoableEdit;

/**
 * An edition is one action defined over a Probabilistic Network.
 */
public interface PNEdit extends UndoableEdit {

	/**
	 * Puts into effect the edition.
	 *
	 * @throws DoEditException DoEditException
	 */
	void doEdit() throws DoEditException;
	
	void setSignificant(boolean significant);

	ProbNet getProbNet();

	void setProbNet(ProbNet probNet);
	
	default void doEdit(ProbNet probNet) throws DoEditException{
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}
	
	static void startEdit(PNEdit edit, ProbNet probNet) throws DoEditException.ConstraintViolated {
		edit.setProbNet(probNet);
		startEdit(edit);
	}
	
	static void startEdit(PNEdit edit) throws DoEditException.ConstraintViolated {
		ProbNet probNet = edit.getProbNet();
		PNESupport pneSupport = probNet.getPNESupport();
        pneSupport.announceEdit(edit);
	}
	
	static void endEdit(PNEdit edit) {
		PNESupport pneSupport = edit.getProbNet().getPNESupport();
		if (pneSupport.isWithUndo()) {
			pneSupport.getUndoManager().addEdit(edit);
		}
		Class<? extends PNEdit> editClass = edit.getClass();
		boolean isParenthesis = editClass == OpenParenthesisEdit.class || editClass == CloseParenthesisEdit.class;
		if (!isParenthesis) {
			pneSupport.postEdit(edit);
		}
	}
	
}
