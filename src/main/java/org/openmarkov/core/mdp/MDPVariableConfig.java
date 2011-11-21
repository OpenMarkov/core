package org.openmarkov.core.mdp;

import org.openmarkov.core.model.network.Variable;

/**
 * @author Jorge
 *
 */
public class MDPVariableConfig {
	/**
	 * 
	 */
	MDPVariable variable;
	
	/**
	 * 
	 */
	int iPos= 0;
	
	/**
	 * @param variable
	 */
	MDPVariableConfig (MDPVariable variable) {
		this.variable= variable;
	}

	/** Constructor de copia: preferido al uso de clone
	 * @param vbleConfig
	 */
	MDPVariableConfig (MDPVariableConfig vbleConfig) {
		this.variable= vbleConfig.variable;
		this.iPos= vbleConfig.iPos;
	}
	
	/**
	 * 
	 */
	void reset() {
		iPos= 0;
	}
	
	/**
	 * @return
	 */
	boolean hasNext() {
		return iPos < variable.potEstado.getTableSize();
	}
	
	/**
	 * 
	 */
	void next() {
		if (iPos>= variable.potEstado.getTableSize()) {
			throw new IllegalStateException();
		}
		
		iPos++;
	}
	
	/**
	 * @param x
	 * @return
	 */
	int getStateValue(Variable x) {
		int pos= variable.potEstado.getVariables().indexOf(x);
		if (pos==-1) {
			throw new IllegalArgumentException();
		}
		
		return variable.potEstado.getConfiguration(iPos)[pos];		
	}
}
