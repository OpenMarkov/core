/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.oopn.action;

import org.openmarkov.core.action.base.CompoundPNEdit;
import org.openmarkov.core.action.core.NodeNameEdit;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.oopn.Instance;

import java.util.ArrayList;

@SuppressWarnings("serial") public class InstanceNameEdit extends CompoundPNEdit {

	private String newName;
	private String oldName;
	private Instance instance;

	public InstanceNameEdit(ProbNet probNet, Instance instance, String newName) {
		super(probNet);
		this.newName = newName;
		this.instance = instance;
		this.oldName = instance.getName();
	}
    
    @Override public ArrayList<PNEdit> generateEdits() {
        ArrayList<PNEdit> edits = new ArrayList<>();
		this.instance.setName(newName);
		for (Node instanceNode : instance.getNodes()) {
			String newNodeName = instanceNode.getName().replace(oldName, newName);
            edits.add(new NodeNameEdit(instanceNode, newNodeName));
		}
        return edits;
    }
    
    @Override public void undo() {
		super.undo();
		this.instance.setName(oldName);
	}

}
