/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.exception.InvalidStateException;

/**
 * A finding is a variable and the value associated to it. The variable can be
 * discrete, continuous or hybrid
 * (<code>DiscretizedVariable</code>).
 *
 * @author Manuel
 * @author fjdiez
 * @version 1.0
 * @see Variable
 * @see EvidenceCase
 * @since OpenMarkov 1.0
 */
public class Finding {

	// Attributes
	/**
	 * The variable stored in this object.
	 */
	protected Variable variable;

	/**
	 * This attribute is used only when the associated variable is discrete or
	 * hybrid.
	 */
	protected int stateIndex = Integer.MAX_VALUE;

	/**
	 * This attribute is used only when the associated variable is continuous
	 * or hybrid.
	 */
	protected double numericalValue;

	// Constructors

	/**
	 * Creates a <code>Finding</code> associated to a discrete variable
	 *
	 * @param fsVariable <code>Variable</code>
	 * @param state      <code>int</code>
	 */
	public Finding(Variable fsVariable, State state) {
		this(fsVariable, fsVariable.getStateIndex(state));
	}

	/**
	 * Creates a <code>Finding</code> associated to a discrete variable
	 *
	 * @param fsVariable <code>Variable</code>
	 * @param state      <code>int</code>
	 */
	public Finding(Variable fsVariable, int state) {
		variable = fsVariable;
		this.stateIndex = state;
		if (variable.getVariableType() == VariableType.DISCRETIZED) {
			numericalValue = (
					variable.getPartitionedInterval().getLimit(state) + variable.getPartitionedInterval()
							.getLimit(state + 1)
			) / 2;
		} else {
			numericalValue = Double.NaN; // Default value
		}
	}

	/**
	 * Creates a <code>Finding</code> associated to a numerical or discretized
	 * variable.
	 *
	 * @param variable       <code>Variable</code>
	 * @param numericalValue <code>double</code>
	 * @throws InvalidStateException
	 */
	public Finding(Variable variable, double numericalValue) {
		// TODO Throw exception if numerical values is outside the domain of variable
		this.variable = variable;
		this.numericalValue = numericalValue;
		if (variable.getVariableType() == VariableType.DISCRETIZED) {
			try {
				this.stateIndex = variable.getStateIndex(numericalValue);
			} catch (InvalidStateException e) {
				// Unreachable code because we have checked that the value is 
				// inside the limits of the variable
				e.printStackTrace();
			}
		}
	}

	// Methods

	/**
	 * @param name <code>String</code>
	 * @return <code>true</code> if the parameter name is equal to the internal
	 * variable name. <code>boolean</code>
	 */
	public boolean match(String name) {
		return variable.getName().matches(name);
	}

	/**
	 * @return variable. <code>Variable</code>
	 */
	public Variable getVariable() {
		return variable;
	}

	/**
	 * @return stateIndex. <code>int</code>
	 */
	public int getStateIndex() {
		return stateIndex;
	}

	/**
	 * @param stateIndex <code>int</code>
	 */
	public void setStateIndex(int stateIndex) {
		this.stateIndex = stateIndex;
	}

	/**
	 * @return state name. <code>String</code>
	 */
	public String getState() {
		return variable.getStateName(stateIndex);
	}

	/**
	 * @return numericalValue. <code>double</code>
	 * @precondition This finding stores a hybrid or continuous variable
	 */
	public double getNumericalValue() {
		return Double.isNaN(numericalValue) ? stateIndex : numericalValue;
	}

	/**
	 * @param numericalValue <code>double</code>
	 * @throws InvalidStateException
	 */
	public void setNumericalValue(double numericalValue) throws InvalidStateException {
		this.numericalValue = numericalValue;
		stateIndex = variable.getStateIndex(numericalValue);
	}

	/**
	 * Overrides <code>toString</code> method. Mainly for test purposes.
	 */
	public String toString() {
		String string = new String(variable.getName() + ":");
		if (variable.getVariableType() == VariableType.FINITE_STATES) {
			string = string + ((Variable) variable).getStateName(stateIndex) + "(" + Integer.toString(stateIndex) + ")";
		} else {
			string = string + "(" + Double.toString(numericalValue) + ")";
		}
		return string;
	}

}