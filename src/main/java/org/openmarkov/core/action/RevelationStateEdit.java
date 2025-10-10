/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;

/*******
 * <p>
 * A simple edit which allows to add or remove a revealing state of a link.
 *
 */
@SuppressWarnings("serial") public class RevelationStateEdit extends SimplePNEdit {

	private Link<Node> link;

	private State newState;

	private boolean selected;

	public RevelationStateEdit(Link<Node> link, State state, boolean selected) {
		super(link.getNode1().getProbNet());
		this.link = link;
		this.selected = selected;
		this.newState = state;

	}

	@Override public void doEdit() {
		if (selected) {
			link.addRevealingState(newState);
		} else {
			link.removeRevealingState(newState);

		}

	}
	
	@Override public void doEdit(ProbNet probNet) throws DoEditException.ConstraintViolated {
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}
	
	@Override public void undo() {
		super.undo();
		if (selected) {
			link.removeRevealingState(newState);
		} else {
			link.addRevealingState(newState);

		}
	}

}
