package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.StringWithProperties;
/**
 * Edit for variable´s unit
 * @author myebra
 *
 */
@SuppressWarnings("serial")
public class UnitEdit extends SimplePNEdit {

	private Node probNode;
	private StringWithProperties lastUnit;
	private StringWithProperties newUnit;
	
	public UnitEdit(Node probNode, String newUnit) {
		super(probNode.getProbNet());
		this.probNode = probNode;
		this.lastUnit = probNode.getVariable().getUnit().copy();
		this.newUnit = new StringWithProperties(newUnit);
	}

	@Override
	public void doEdit() throws DoEditException {
		probNode.getVariable().setUnit(newUnit);
	}

	@Override
	public void undo() {
		super.undo();
		probNode.getVariable().setUnit(lastUnit);
	}
}
