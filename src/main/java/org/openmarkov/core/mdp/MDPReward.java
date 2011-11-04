package org.openmarkov.core.mdp;

/**
 * @author Jorge
 *
 */
public abstract class MDPReward {

	/**
	 * @param configPriori
	 * @param configActions
	 * @return
	 */
	public abstract double eval (MDPVariableConfig configPriori, MDPVariableConfig configActions);	
}
