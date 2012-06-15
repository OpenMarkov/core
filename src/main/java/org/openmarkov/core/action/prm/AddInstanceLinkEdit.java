/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.action.prm;

import javax.swing.undo.CannotUndoException;

import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.prm.Instance;
import org.openmarkov.core.model.network.prm.InstanceLink;

@SuppressWarnings("serial")
public class AddInstanceLinkEdit extends SimplePNEdit{

	private InstanceLink instanceLink;
	
	public AddInstanceLinkEdit(ProbNet probNet, Instance sourceInstance, Instance destinationInstance,
			Instance destinationParameter) {
		super(probNet);
		
		instanceLink = new InstanceLink(sourceInstance, destinationInstance, destinationParameter);
	}
	
	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		probNet.addInstanceLink(instanceLink);
	}	
	

	@Override
	public void undo() throws CannotUndoException {
		// TODO Auto-generated method stub
		probNet.removeInstanceLink(instanceLink);
	}

}
