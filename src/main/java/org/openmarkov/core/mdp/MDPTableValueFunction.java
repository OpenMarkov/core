package org.openmarkov.core.mdp;

/**
 * @author Jorge
 *
 */
public class MDPTableValueFunction extends MDPValueFunction {
	/**
	 * 
	 */
	double[] values;
	
	/**
	 * 
	 */
	MDPVariable mdpVariable;

	/**
	 * @param mdpVariable
	 */
	public MDPTableValueFunction (MDPVariable mdpVariable) {
		this.mdpVariable= mdpVariable;
		values= new double[mdpVariable.potEstado.getTableSize()];
	}

	/** Copy el estado interno de una VF
	 * 
	 * @param tableValueFunction VF a replicar
	 */
	public void copyFrom (MDPTableValueFunction tableValueFunction) {
		if (mdpVariable!=tableValueFunction.mdpVariable) {
			mdpVariable= tableValueFunction.mdpVariable;
			
			if (values.length!= tableValueFunction.values.length) {
				values= new double[mdpVariable.potEstado.getTableSize()];
			}
		}
		
		System.arraycopy(tableValueFunction.values, 0, values, 0, mdpVariable.potEstado.getTableSize());
	}
	
	/**
	 * @see org.openmarkov.core.mdp.MDPValueFunction#getValue(org.openmarkov.core.mdp.MDPVariableConfig)
	 */
	public double getValue (MDPVariableConfig configVbles) {
		assert (configVbles.variable==mdpVariable);
		
		return values[configVbles.iPos];
	}
	
	/**
	 * @see org.openmarkov.core.mdp.MDPValueFunction#setValue(org.openmarkov.core.mdp.MDPVariableConfig, double)
	 */
	public void setValue(MDPVariableConfig configVbles, double value) {
		assert (configVbles.variable==mdpVariable);
		
		values[configVbles.iPos]= value;
	}
	
	/**
	 * @see org.openmarkov.core.mdp.MDPValueFunction#calculateNormMax(org.openmarkov.core.mdp.MDPTableValueFunction)
	 */
	public double calculateNormMax (MDPTableValueFunction Jb_next) {
		// Tb habr�a q comprobar que tienen las mismas vbles y en el mismo orden
		assert (values.length==Jb_next.values.length);
		
		// Test de convergencia
		double dUtilE= Double.MIN_VALUE;
		for(int r=0; r< values.length; r++) {
			double dif= Math.abs(Jb_next.values[r]-values[r]);
			dUtilE= Math.max(dUtilE,dif);
		}

		return dUtilE;		
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s= "";
		
		for(double v : values) {
			s += v + ", ";
		}

		return s;
	}
}
