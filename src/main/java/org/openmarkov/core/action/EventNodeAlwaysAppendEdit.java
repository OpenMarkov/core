/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;

/**
 * Simple edit that allow modify the always append property of an event variable
 * @author cyago
 * @version 1.0 25/10/2020
 */

@SuppressWarnings("serial") public class EventNodeAlwaysAppendEdit extends SimplePNEdit {

	/**
	 * The last 'alwaysAppend' before the edition
	 */
	private boolean lastAlwaysAppend;
	/**
	 * The new 'alwaysAppend' after the edition
	 */
	private boolean newAlwaysAppend;
	/**
	 * The edited node
	 */
	private Node node = null;

	/**
	 * Creates a new <code>AlwaysAppendEdit</code> with the node and new 'alwaysObserved'
	 * specified.
	 *
	 * @param node              the node that will be edited
	 * @param newAlwaysAppend the new alwaysObserved
	 */
	public EventNodeAlwaysAppendEdit(Node node, boolean newAlwaysAppend) {
		super(node.getProbNet());
		this.lastAlwaysAppend = node.isAlwaysObserved();
		this.newAlwaysAppend = newAlwaysAppend;
		this.node = node;
	}

	/**
	 * Updates the event node with the new value for alwaysAppend
	 * @throws DoEditException if edit cannot be done
	 */
	@Override public void doEdit() throws DoEditException {
		node.setAlwaysAppend(newAlwaysAppend);
	}

	/**
	 * Undo the alwaysAppend change
	 */
	@Override public void undo() {
		super.undo();
		node.setAlwaysAppend(lastAlwaysAppend);
	}


}
