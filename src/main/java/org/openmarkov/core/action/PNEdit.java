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

}
