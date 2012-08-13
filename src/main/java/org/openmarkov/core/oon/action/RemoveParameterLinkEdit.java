/*
* Copyright 2012 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.oon.action;

import javax.swing.undo.CannotUndoException;

import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.oon.OOBNet;
import org.openmarkov.core.oon.ParameterLink;

/**
 * @author ibermejo
 *
 */
@SuppressWarnings("serial")
public class RemoveParameterLinkEdit extends SimplePNEdit {
	
	private ParameterLink parameterLink;

	/**
	 * Constructor
	 * @param probNet
	 * @param parameterLink
	 */
	public RemoveParameterLinkEdit(ProbNet probNet, ParameterLink parameterLink) {
		super(probNet);
		this.parameterLink = parameterLink;
	}

	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		((OOBNet)probNet).removeParameterLink(parameterLink);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		((OOBNet)probNet).addParameterLink(parameterLink);
	}
}
