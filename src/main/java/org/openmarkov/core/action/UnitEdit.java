/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;

/**
 * Edit for variable´s unit
 *
 * @author myebra
 */
@SuppressWarnings("serial") public class UnitEdit extends SimplePNEdit {

	private Node node;
	private StringWithProperties lastUnit;
	private StringWithProperties newUnit;

	public UnitEdit(Node node, String newUnit) {
		super(node.getProbNet());
		this.node = node;
        this.lastUnit = node.getVariable().getUnit().clone();
		this.newUnit = new StringWithProperties(newUnit);
	}

	@Override public void doEdit() {
		node.getVariable().setUnit(newUnit);
	}
	
	@Override public void doEdit(ProbNet probNet) throws DoEditException.ConstraintViolated {
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}
	
	@Override public void undo() {
		super.undo();
		node.getVariable().setUnit(lastUnit);
	}
}
