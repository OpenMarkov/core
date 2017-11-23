/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.model.network.potential.TablePotential;

public abstract class Marginalization {
	private TablePotential utility;
	
	protected void setUtility(TablePotential utility) {
		this.utility = utility;
	}

	protected void setProbability(TablePotential probability) {
		this.probability = probability;
	}

	private TablePotential probability;

	public TablePotential getUtility() {
		return utility;
	}

	public TablePotential getProbability() {
		return probability;
	}

}
