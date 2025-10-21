/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.oopn.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.oopn.Instance;
import org.openmarkov.core.oopn.Instance.ParameterArity;

@SuppressWarnings("serial") public class ChangeParameterArityEdit extends PNEdit {
    
    private Instance instance;
    private ParameterArity arity;
    private ParameterArity previousArity;

	public ChangeParameterArityEdit(ProbNet probNet, Instance instance, ParameterArity arity) {
		super(probNet);
		this.instance = instance;
		this.arity = arity;
		this.previousArity = instance.getArity();
	}

	@Override public void doEdit() {
		instance.setArity(arity);
	}
    
    @Override public void undo() {
		super.undo();
		instance.setArity(previousArity);
	}

}
