/*
* Copyright 2011 CISIAD, UNED, Spain
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
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.oon.Instance;
import org.openmarkov.core.oon.InstanceReferenceLink;
import org.openmarkov.core.oon.NodeReferenceLink;
import org.openmarkov.core.oon.OOBNet;
import org.openmarkov.core.oon.ReferenceLink;

@SuppressWarnings("serial")
public class AddReferenceLinkEdit extends SimplePNEdit{

	private ReferenceLink referenceLink;
	
	public AddReferenceLinkEdit(ProbNet probNet, Instance sourceInstance, Instance destinationInstance,
			Instance destinationParameter) {
		super(probNet);
		
		referenceLink = new InstanceReferenceLink(sourceInstance, destinationInstance, destinationParameter);
	}
	
	public AddReferenceLinkEdit(ProbNet probNet, ProbNode sourceNode, ProbNode destinationNode) {
		super(probNet);
		
		referenceLink = new NodeReferenceLink(sourceNode, destinationNode);
	}	
	
	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		((OOBNet)probNet).addReferenceLink(referenceLink);
	}	
	

	@Override
	public void undo() throws CannotUndoException {
		// TODO Auto-generated method stub
	    ((OOBNet)probNet).removeReferenceLink(referenceLink);
	}

}
