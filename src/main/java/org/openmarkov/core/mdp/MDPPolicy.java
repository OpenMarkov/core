package org.openmarkov.core.mdp;

/**
 * @author Jorge
 *
 */
public abstract class MDPPolicy {
	/**
	 * @param configVbles
	 * @return
	 */
	public abstract MDPVariableConfig getPolicy (MDPVariableConfig configVbles);
	
	/**
	 * @param configVbles
	 * @param configActions
	 */
	public abstract void setPolicy (MDPVariableConfig configVbles, MDPVariableConfig configActions);	
}
