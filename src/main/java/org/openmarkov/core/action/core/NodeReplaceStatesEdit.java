/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.action.base.PNEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code NodeReplaceStatesEdit} is a simple edit that allows modify the
 * states of node
 *
 * @author Miguel Palacios
 * @version 1.0 10/05/2011
 */

@SuppressWarnings("serial") public class NodeReplaceStatesEdit extends PNEdit {
    
    
    private State[] lastStates;
    
    private State[] newStates;

	private Node node;
    
    public NodeReplaceStatesEdit(Node node, State[] newStates) {
		super(node.getProbNet());
		this.node = node;
		this.lastStates = node.getVariable().getStates();
		this.newStates = newStates;
	}

	// Methods
	@Override protected void doEdit() {
		node.getVariable().replaceStates(node,newStates);
	}
    
    @Override public void undo() {
		super.undo();
        node.getVariable().replaceStates(node,lastStates);
	}


}
