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
import org.openmarkov.core.oon.Instance;
import org.openmarkov.core.oon.ParameterLink;
import org.openmarkov.core.oon.OOBNet;

@SuppressWarnings("serial")
public class AddInputParameterLinkEdit extends SimplePNEdit{

	private ParameterLink parameterLink;
	
	public AddInputParameterLinkEdit(ProbNet probNet, Instance sourceInstance, Instance destinationInstance,
			Instance destinationParameter) {
		super(probNet);
		
		parameterLink = new ParameterLink(sourceInstance, destinationInstance, destinationParameter);
	}
	
	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		((OOBNet)probNet).addParameterLink(parameterLink);
	}	
	

	@Override
	public void undo() throws CannotUndoException {
		// TODO Auto-generated method stub
	    ((OOBNet)probNet).removeInstanceLink(parameterLink);
	}

}
