/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Potential which represents an increment in the value taken by a numeric variable.
 * TODO It does not represent a probability distribucion --???
 * @author cyago
 * @version 1.0 10/04/2020 - Adapted from Uniform
 */
@PotentialType(name = "Increment") public class IncrementPotential extends Potential {

	// Constructors

	/**
	 * Creates a new IncrementPotential with variables as its list of Variable and role as PotentialRole
	 * @param variables <code>ArrayList</code> of <code>Variable</code> . Variables of IncrementPotential
	 * @param role      <code>PotentialRole</code> of IncrementPotential
	 */
	public IncrementPotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);

	}


	/**
	 * Creates a new IncrementPotential with variables as its list of Variable and role as PotentialRole
	 * @param role      <code>PotentialRole</code> of IncrementPotential
	 * @param variables  Group of <code>Variable</code> which are the variables of the new IncrementPotential
	 */
	public IncrementPotential(PotentialRole role, Variable... variables) {
		this(toList(variables), role);
	}

	/**
	 * Creates a new IncrementPotential equal to potential
	 * Copy constructor for UniformPotential
	 *
	 * @param potential IncrementPotential from which the new IncrementPotential is created
	 */
	public IncrementPotential(IncrementPotential potential) {
		super(potential);
	}

	// Methods

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role
	 *
	 * @param node      <code>Node</code>
	 * @param variables <code>ArrayList</code> of <code>Variable</code>
	 * @param role      <code>PotentialRole</code>
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		//Currently 10/04/2020 it makes sense when the node variable is numeric and there is a self-loop
		return (variables.get(0).getVariableType() == VariableType.NUMERIC);
//				&& (variables.stream().filter(v -> v.equals(node.getVariable())).count()==2);//Check this use of equals
	}

	@Override
	public double sampleConditionedVariable(Random randomGenerator, EvidenceCase parents) throws OpenMarkovException {
//This is done until TreeWithEvents is completed
		List<Variable> events = parents.getVariables().stream().filter(v ->v.getVariableType()==VariableType.EVENT).collect(Collectors.toList());
		if (events.size()!=1) throw new OpenMarkovException("More than one event");
		if (events.get(0).getName().equalsIgnoreCase("Initial Event")){
			return 0;
		}
		return parents.getNumericalValue(getConditionedVariable())+1;
	}


	// Methods
	@Override

	public List<TablePotential> tableProject(
			EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials)
			throws NonProjectablePotentialException {
		//TODO
		return null;
	}


	@Override public Potential copy() {
		return new IncrementPotential(this);
	}

	@Override public int sampleConditionedVariable(Random randomGenerator, Map<Variable, Integer> parentStateIndexes) {

		return 1;
	}


	@Override public boolean isUncertain() {
		return false;
	}

	@Override public String toString() {
		return super.toString() + " = Increment";
	}

	@Override public void scalePotential(double scale) {

	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		IncrementPotential potential = (IncrementPotential) super.deepCopy(copyNet);
		return potential;

	}
}
