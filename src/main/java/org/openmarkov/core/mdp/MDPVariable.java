package org.openmarkov.core.mdp;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;


/**
 * @author Jorge
 *
 */
public class MDPVariable {
	/**
	 * 
	 */
	TablePotential potEstado;
	
	/**
	 * @param list
	 * @throws NotEnoughMemoryException
	 */
	MDPVariable (ArrayList<Variable> list) throws NotEnoughMemoryException {
		potEstado = 
			new TablePotential(list, PotentialRole.CONDITIONAL_PROBABILITY);
	}
	
	/**
	 * @return
	 */
	MDPVariableConfig config_iterator() {
		return new MDPVariableConfig(this);
	}
}
