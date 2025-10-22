/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.oopn.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.oopn.OOPNet;
import org.openmarkov.core.oopn.ReferenceLink;

/**
 * @author ibermejo
 */
@SuppressWarnings("serial") public class RemoveReferenceLinkEdit extends PNEdit {

	private ReferenceLink referenceLink;

	/**
	 * Constructor
	 *
	 * @param probNet Network
	 * @param referenceLink Reference link
	 */
	public RemoveReferenceLinkEdit(ProbNet probNet, ReferenceLink referenceLink) {
		super(probNet);
		this.referenceLink = referenceLink;
	}

	@Override public void doEdit() {
		((OOPNet) probNet).removeReferenceLink(referenceLink);
	}
    
    @Override public void undo() {
		super.undo();
		((OOPNet) probNet).addReferenceLink(referenceLink);
	}
}
