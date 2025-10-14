/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.SimplePNEdit;

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
    
    @Override public void doEdit(ProbNet probNet) throws ConstraintViolatedException {
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
