/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.action.prm;

import java.util.Iterator;

import javax.swing.undo.CannotUndoException;

import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.prm.Instance;
import org.openmarkov.core.model.network.prm.InstanceLink;

@SuppressWarnings("serial")
public class AddInstanceLinkEdit extends SimplePNEdit{

	private InstanceLink instanceLink;
	
	public AddInstanceLinkEdit(ProbNet probNet, Instance sourceInstance,
			Instance destinationInstance) {
		super(probNet);
		
		boolean found = false;
		Iterator<Instance> subInstances = destinationInstance.getSubInstances().values().iterator();
		Instance destinationSubinstance = null;
		while(!found && subInstances.hasNext())
		{
			destinationSubinstance = subInstances.next();
			found = sourceInstance.getClassNet().equals(destinationSubinstance.getClassNet());
		}
		instanceLink = new InstanceLink(sourceInstance, destinationInstance, destinationSubinstance);
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
