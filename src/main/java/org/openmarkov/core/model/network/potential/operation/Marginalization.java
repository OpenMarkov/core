/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import java.util.Collection;

import org.openmarkov.core.model.network.potential.Potential;
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
	
	/** 
     * Classifies potential from the first list between probability and utility and stores them 
     * in the second and third list
     * @param potentials <code>List</code> of <code>TablePotential</code>
     * @param probPotentials <code>List</code> of <code>TablePotential</code>
     * @param utilityPotentials <code>List</code> of <code>TablePotential</code>
     */
    public static void classifyProbAndUtilityPotentials(
    		Collection<? extends Potential> potentials,
    		Collection<TablePotential> probPotentials,
    		Collection<TablePotential> utilityPotentials) {
    	for (Potential potential : potentials) {
    		if (potential.isAdditive()) {
    			utilityPotentials.add((TablePotential)potential);
    		} else {
    			probPotentials.add((TablePotential)potential);
    		}
    	}
    }

}
