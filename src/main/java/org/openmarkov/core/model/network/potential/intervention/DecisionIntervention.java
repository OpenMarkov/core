package org.openmarkov.core.model.network.potential.intervention;

import org.openmarkov.core.model.network.Variable;

public class DecisionIntervention extends Intervention {
	
	// Attributes
	protected Variable decisionVariable;
	
	protected int decisionValue;
	
	/** Used in toString() */
	protected Intervention nextIntervention;

	// Constructors
	/**
	 * @param decisionVariable. <code>Variable</code>
	 * @param decisionValue. <code>int</code>
	 */
	public DecisionIntervention(Variable decisionVariable, int decisionValue) {
		this.decisionVariable = decisionVariable;
		this.decisionValue = decisionValue;
	}
	
	/**
	 * @param decisionVariable. <code>Variable</code>
	 * @param decisionValue. <code>int</code>
	 * @param previousIntervention. <code>Intervention</code>
	 */
	public DecisionIntervention(Variable decisionVariable, int decisionValue, Intervention nextIntervention) {
		this(decisionVariable, decisionValue);
		this.nextIntervention = nextIntervention;
	}

	// Methods
	@Override
	public boolean sameAs(Object object) {
		boolean sameIntervention = false;
		if (object != null && object.getClass() == this.getClass()) {
			DecisionIntervention intervention = (DecisionIntervention)object;
			if (intervention.getDecisionVariable() == decisionVariable && 
					intervention.getDecisionValue() == decisionValue) {
				sameIntervention = true;
			}
		}
		return sameIntervention;
	}

	public String toString() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(indent);
		strBuffer.append(decisionVariable.getName());
		strBuffer.append(" = '");
		strBuffer.append(decisionVariable.getStateName(decisionValue));
		strBuffer.append("'");
		if (hasAnySubIntervention()) {
			strBuffer.append("; ");
			if (getNumLeaves() == 1) {
				nextIntervention.setIndentLevel(0);
			} else {
				strBuffer.append("\n");
				nextIntervention.setIndentLevel(indentLevel);
			}
			strBuffer.append(nextIntervention.toString());
		} else {
			strBuffer.append("\n");
		}
		return strBuffer.toString();
	}

	@Override
	/** A decision has only one branch.
	 * @return <code>int</code> */
	protected int getNumBranches() {
		return 1;
	}

	/**
	 * @return <code>Variable</code>
	 */
	public Variable getDecisionVariable() {
		return decisionVariable;
	}

	/**
	 * @return <code>int</code>
	 */
	public int getDecisionValue() {
		return decisionValue;
	}

	@Override
	public boolean hasAnySubIntervention() {
		return nextIntervention != null;
	}

	@Override
	protected int getNumLeaves() {
		int numberOfLeaves = 1; // By default this is a leave
		if (nextIntervention != null) {
			numberOfLeaves = nextIntervention.getNumLeaves();
		}
		return numberOfLeaves;
	}


}
