package org.openmarkov.core.inference;

import java.util.Hashtable;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

public class StrategyUtilities {

	Hashtable<Variable,TablePotential> utilities;

	/**
	 * @return the utilities
	 */
	public Hashtable<Variable, TablePotential> getUtilities() {
		return utilities;
	}

	/**
	 * @param utilities the utilities to set
	 */
	public void setUtilities(Hashtable<Variable, TablePotential> utilities) {
		this.utilities = utilities;
	}
	
	/**
	 * @return the utilities
	 */
	public TablePotential getUtilities(Variable decision) {
		return utilities.get(decision);
	}
	
	
	
	
}
