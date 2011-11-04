package org.openmarkov.core.mdp;

import java.util.Arrays;

/**
 * @author Jorge
 *
 */
public class MDPTablePolicy extends MDPPolicy {
	private int[] policy;
	private MDPVariable mdpVariable;
	private MDPVariable mdpActions;

	/**
	 * @param mdpVariable
	 * @param mdpActions
	 */
	public MDPTablePolicy (MDPVariable mdpVariable, MDPVariable mdpActions) {
		this.mdpVariable= mdpVariable;
		this.mdpActions= mdpActions;
		policy= new int [mdpVariable.potEstado.getTableSize()];
	}

	/**
	 * @param mdpVariable
	 * @param mdpActions
	 * @param myPolicy
	 */
	public MDPTablePolicy (MDPVariable mdpVariable, MDPVariable mdpActions, int []myPolicy) {
		this.mdpVariable= mdpVariable;
		this.mdpActions= mdpActions;
		
		assert (myPolicy.length==mdpVariable.potEstado.getTableSize());
		policy= myPolicy;
	}
	
	/**
	 * @see org.openmarkov.core.mdp.MDPPolicy#getPolicy(org.openmarkov.core.mdp.MDPVariableConfig)
	 */
	@Override
	public MDPVariableConfig getPolicy (MDPVariableConfig configVbles) {
		assert (configVbles.variable==mdpVariable);
		
		MDPVariableConfig configActions= mdpActions.config_iterator();		
		configActions.iPos= policy [configVbles.iPos];
		
		return configActions;
	}

	/**
	 * @see org.openmarkov.core.mdp.MDPPolicy#setPolicy(org.openmarkov.core.mdp.MDPVariableConfig, org.openmarkov.core.mdp.MDPVariableConfig)
	 */
	@Override
	public void setPolicy (MDPVariableConfig configVbles, MDPVariableConfig configActions) {
		assert (configVbles.variable==mdpVariable);
		assert (configActions.variable==mdpActions);
	
		policy [configVbles.iPos]= configActions.iPos;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s= "";
		
		for (int i=0; i< policy.length; i++) {
			s+= "Estado [" + i + "]: " + policy[i] + "\n";
		}
		
		return s;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj) {
		if (this==obj) {
			return true;
		}
		
		if (!(obj instanceof MDPTablePolicy)) {
			return false;
		}
		
		MDPTablePolicy pol= (MDPTablePolicy) obj;
		
		if (this.mdpActions != pol.mdpActions || this.mdpVariable != pol.mdpVariable) {
			return false;
		}
		
		return Arrays.equals(policy, ((MDPTablePolicy) obj).policy);
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return mdpActions.hashCode() + mdpVariable.hashCode() + policy.hashCode();
	}
}
