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
import org.openmarkov.core.oon.OOPNet;
import org.openmarkov.core.oon.ReferenceLink;

/**
 * @author ibermejo
 *
 */
@SuppressWarnings("serial")
public class RemoveReferenceLinkEdit extends SimplePNEdit {
	
	private ReferenceLink referenceLink;

	/**
	 * Constructor
	 * @param probNet
	 * @param referenceLink
	 */
	public RemoveReferenceLinkEdit(ProbNet probNet, ReferenceLink referenceLink) {
		super(probNet);
		this.referenceLink = referenceLink;
	}

	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		((OOPNet)probNet).removeReferenceLink(referenceLink);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		((OOPNet)probNet).addReferenceLink(referenceLink);
	}
}
