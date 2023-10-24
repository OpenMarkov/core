/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OutOfRangeException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.List;

@PotentialType(name = "Indicator")
public class IndicatorPotential extends Potential implements DESSimulablePotential {

	/**
	 * Time-to-event
	 */
	private double tte = 0;
	/**
	 * Probability of the event happening
	 */
	private double pOccurrence =1;

	public IndicatorPotential(List<Variable> variables, PotentialRole role, double tte, double pOccurrence) {
		this(variables, role);
		this.tte = tte;
		this.pOccurrence = pOccurrence;
	}

	public IndicatorPotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);
	}


	public IndicatorPotential(IndicatorPotential potential) {
		super(potential);
		this.tte = potential.getTte();
		this.pOccurrence = potential.getpOccurrence();
	}

	/**
	 * Returns whether this type of Potential is suitable for the list of
	 * variables and the potential role given.
	 *
	 * @param node      . {@code Node}
	 * @param variables . {@code List} of {@code Variable}.
	 * @param role      . {@code PotentialRole}.
	 * @return True the node is an event
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		return (variables.get(0).getVariableType() == VariableType.EVENT);
	}

	@Override
	public double sampleConditionedVariable(double randomNumber, EvidenceCase parents) {
		if (randomNumber <= pOccurrence) return tte;
		else return Double.NaN;
	}

	@Override
	public void resetSimulation() {
	}



	@Override
	public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException, WrongCriterionException {
		return null;
	}

	@Override public Potential copy() {
		return new IndicatorPotential(this);
	}

	@Override public boolean isUncertain() {
		return false;
	}

	@Override
	public void scalePotential(double scale) {

	}

	@Override public String toString()  {
		return variables.get(0) + "; probability = " + pOccurrence + "TTE = "+ tte;
	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		IndicatorPotential potential = (IndicatorPotential) super.deepCopy(copyNet);
		potential.pOccurrence =this.pOccurrence;
		potential.tte = this.tte;
		return potential;
	}

	@Override
	public Potential reorder(List<Variable> newOrderOfVariables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Potential reorder(Variable variable, State[] newOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * probability of the event happening
	 */
	public double getpOccurrence() {
		return pOccurrence;
	}

	public void setpOccurrence(double pOccurrence) throws OutOfRangeException {
		//"Probability of ocurrence has to be in [0,1]"
		if (pOccurrence <0 || pOccurrence >1) throw new OutOfRangeException();
		this.pOccurrence = pOccurrence;
	}

	/**
	 * Time-to-event
	 */
	public double getTte() {
		return tte;
	}

	public void setTte(double tte) throws OutOfRangeException {
		//"Time-to-event has to be >=0"
		if (tte <0) throw new OutOfRangeException();
		this.tte = tte;
	}


}
