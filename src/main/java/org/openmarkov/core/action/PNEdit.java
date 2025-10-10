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
     * This method acts as a contract saying no constraint will be violated after the edit is done.
     * <p>
     * If this method returns a {@link org.openmarkov.core.exception.DoEditException.ConstraintViolated}, then it means
     * this edit should not be applied, as it will violate that constraint.
     */
    default void checkConstraintsWillBeMet() throws DoEditException.ConstraintViolated {
    }
    
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
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}
    
    static void startEdit(PNEdit edit, ProbNet probNet) {
		edit.setProbNet(probNet);
        PNEdit.startEdit(edit);
	}
    
    static void startEdit(PNEdit edit) {
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
