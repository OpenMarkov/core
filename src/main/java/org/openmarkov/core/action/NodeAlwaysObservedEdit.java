/*
* Copyright 2012 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;

/**
 * <code>NodeAlwaysObservedEdit</code> is a simple edit that allow modify the always observed property of a variable
 * name.
 *   
 */

@SuppressWarnings("serial")
public class NodeAlwaysObservedEdit extends SimplePNEdit {

	/**
	 * The node edited
	 */
	private Node node = null;

	private boolean previousValue;

	private boolean newValue;

	public NodeAlwaysObservedEdit(Node node, boolean alwaysObserved) {
		super(node.getProbNet());
		this.node = node;
		this.previousValue = node.isAlwaysObserved();
		this.newValue = alwaysObserved;
	}

	@Override
	public void doEdit() throws DoEditException {
		node.setAlwaysObserved(newValue);

	}
	
	public void undo() {
		super.undo();
		node.setAlwaysObserved(previousValue);
	}

}
