/*
* Copyright 2012 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.oon.action;

import org.openmarkov.core.action.SimplePNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;

@SuppressWarnings("serial")
public class ChangeParameterArityEdit extends SimplePNEdit{

	public ChangeParameterArityEdit(ProbNet probNet) {
		super(probNet);
		// TODO implement
	}

	@Override
	public void doEdit() throws DoEditException, NotEnoughMemoryException {
		// TODO Auto-generated method stub
	}

}
