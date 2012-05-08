package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNode;

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
	private ProbNode probNode = null;

	private boolean previousValue;

	private boolean newValue;

	public NodeAlwaysObservedEdit(ProbNode node, boolean alwaysObserved) {
		super(node.getProbNet());
		this.probNode = node;
		this.previousValue = node.getVariable().isAlwaysObserved();
		this.newValue = alwaysObserved;
	}

	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		probNode.getVariable().setAlwaysObserved(newValue);

	}
	
	public void undo() {
		super.undo();
		probNode.getVariable().setAlwaysObserved(previousValue);
	}

}
