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

	private Node node;
	private StringWithProperties lastUnit;
	private StringWithProperties newUnit;
	
	public UnitEdit(Node node, String newUnit) {
		super(node.getProbNet());
		this.node = node;
		this.lastUnit = node.getVariable().getUnit().copy();
		this.newUnit = new StringWithProperties(newUnit);
	}

	@Override
	public void doEdit() throws DoEditException {
		node.getVariable().setUnit(newUnit);
	}

	@Override
	public void undo() {
		super.undo();
		node.getVariable().setUnit(lastUnit);
	}
}
