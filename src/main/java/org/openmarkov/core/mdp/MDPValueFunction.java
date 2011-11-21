package org.openmarkov.core.mdp;

/**
 * @author Jorge
 *
 */
public abstract class MDPValueFunction {
	/**
	 * @param configVbles
	 * @return
	 */
	public abstract double getValue (MDPVariableConfig configVbles);
	
	/**
	 * @param configVbles
	 * @param value
	 */
	public abstract void setValue (MDPVariableConfig configVbles, double value);
	
	/**
	 * @param Jb_next
	 * @return
	 */
	public abstract double calculateNormMax (MDPTableValueFunction Jb_next);
}
