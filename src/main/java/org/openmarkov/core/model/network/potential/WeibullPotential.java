/*
 * Copyright 2013 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.RelationPotentialType;

@RelationPotentialType(name = "Weibull", family = "")
public class WeibullPotential extends Potential {

	/**
	 * Coefficients for the parameters of the function
	 */
	private List<Double> coefficients;
	/**
	 * Ancillary parameter for the Weibull distribution
	 */
	private double gamma;
	/**
	 * Constant in survival analysis for baseline hazard.
	 */
	private double constant;

	public WeibullPotential(List<Variable> variables, PotentialRole role,
			List<Double> coefficients, double gamma, double constant) {
		super(variables, role);
		this.coefficients = coefficients;
		this.gamma = gamma;
		this.constant = constant;
	}

	public WeibullPotential(List<Variable> variables, PotentialRole role) {
		this(variables, role, Arrays.asList (-0.04, 0.77), 1.45, -5.49);
	}

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role.
	 * 
	 * @param probNode
	 *            . <code>ProbNode</code>
	 * @param variables
	 *            . <code>ArrayList</code> of <code>Variable</code>.
	 * @param role
	 *            . <code>PotentialRole</code>.
	 */
	public static boolean validate(ProbNode probNode, List<Variable> variables, PotentialRole role) {
		return variables.get(0).isTemporal()
				&& variables.get(0).getVariableType() == VariableType.FINITE_STATES
				&& variables.get(0).getNumStates() == 2;
	}

	@Override
	public List<TablePotential> tableProject(EvidenceCase evidenceCase,
			InferenceOptions inferenceOptions) throws NonProjectablePotentialException,
			WrongCriterionException {
		List<Variable> evidencelessVariables = new ArrayList<>();
		List<Integer> evidencelessVariablesIndex = new ArrayList<>();
		List<Double> numericValues = new ArrayList<>(variables.size());

		for (int i = 1; i < variables.size(); ++i) {
			Variable variable = variables.get(i);
			if (!evidenceCase.contains(variable)) {
				if (variable.getVariableType() == VariableType.NUMERIC) {
					throw new NonProjectablePotentialException(
							"Can not project potential with numeric variable " + variable.getName());
				}
				evidencelessVariables.add(variable);
				evidencelessVariablesIndex.add(i-1);
				numericValues.add(0.0);
			} else {
				numericValues.add(evidenceCase.getFinding(variable).getNumericalValue());
			}
		}

		TablePotential projectedPotential = new TablePotential(evidencelessVariables, role);
		int[] offsets = projectedPotential.getOffsets();
		int timeSlice = variables.get(0).getTimeSlice();

		for (int i = 0; i < projectedPotential.values.length; i += 2) {
			for (int j = 0; j < evidencelessVariables.size(); ++j) {
				numericValues.set(evidencelessVariablesIndex.get(j), (double)i % offsets[j+1]);
			}
			double lambda = constant;
			for (int j = 0; j < numericValues.size(); ++j) {
				lambda += numericValues.get(j) * coefficients.get(j);
			}
			lambda = Math.exp(lambda);
			projectedPotential.values[i + 1] = Math.exp(lambda
					* (Math.pow(timeSlice - 1, gamma) - Math.pow(timeSlice, gamma)));
			projectedPotential.values[i] = 1 - projectedPotential.values[i + 1];
		}

		List<TablePotential> projectedPotentials = new ArrayList<>();
		projectedPotentials.add(projectedPotential);
		return projectedPotentials;
	}

	@Override
	public Potential shift(ProbNet probNet, int timeDifference) throws ProbNodeNotFoundException {
		return new WeibullPotential(getShiftedVariables(probNet, timeDifference), role,
				coefficients, gamma, constant);
	}

	@Override
	public Potential copy() {
		return new WeibullPotential(variables, role, coefficients, gamma, constant);
	}

	@Override
	public boolean isUncertain() {
		return false;
	}

	public List<Double> getCoefficients() {
		return coefficients;
	}

	public void setCoefficients(List<Double> coefficients) {
		this.coefficients = coefficients;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public double getConstant() {
		return constant;
	}

	public void setConstant(double constant) {
		this.constant = constant;
	}

}
