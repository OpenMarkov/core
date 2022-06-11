/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import java.util.*;

/**
 * This class implements a potential which is function of the values provided by the parents.
 * TODO Which parents???
 * Has they to be numeric or may we have "finites states" which have one value associated? For example male=1, female=0
 *
 * @author cyago
 * @version 1.1 06/12/2019
 * @version 1.2 11/06/2022 - set Evaluator to final and parsing in constructor for efficiency reasons (simulation ten times faster)
 */
@PotentialType(name = "Function") public class FunctionPotential extends GLMPotential {

	/**
	 * The default function
	 */
	public static final String DEFAULT_FUNCTION = "0";

	/**
	 * The coefficient
	 */
	protected static final double COEFFICIENT = 1;

	final Evaluator evaluator = new Evaluator();

	/**
	 * Creates a Function potential with the function by default
	 *
	 * @param variables - list with the node variable and their parents
	 * @param role
	 */
	public FunctionPotential(List<Variable> variables, PotentialRole role) {
		this(variables, role, DEFAULT_FUNCTION);
	}

	/**
	 * Creates a Function potential with the function given by {@code function}
	 *
	 * @param variables - list with the node variable and their parents
	 * @param role      - the role of the potential
	 * @param function  - A string representing the function
	 * @param role
	 */
	public FunctionPotential(List<Variable> variables, PotentialRole role, String function) {
		super(variables, role, new String[] { function }, new double[] { COEFFICIENT });
//		try {
//			evaluator.parse(this.processedCovariates[0]);
//		} catch (EvaluationException e) {
//			throw new RuntimeException(e);
//		}
	}

	/**
	 * Creates a Function potential equal to {@code potential}
	 *
	 * @param potential - potential copied
	 */
	public FunctionPotential(FunctionPotential potential) {
		super(potential);

	}

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role.
	 * UNCLEAR--> Should the parents be numeric
	 *
	 * @param node      . <code>Node</code>
	 * @param variables . <code>ArrayList</code> of <code>Variable</code>.
	 * @param role      . <code>PotentialRole</code>.
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
//CMI 17/10/2020
//		return (
//				!variables.isEmpty() && variables.get(0).getVariableType() == VariableType.NUMERIC
//		);
		return (
				!variables.isEmpty() && (variables.get(0).getVariableType() == VariableType.NUMERIC
						|| variables.get(0).getVariableType() == VariableType.EVENT)
		);
//CMF
	}

	/**
	 * Gets the unprocessed function of FunctionPotential
	 *
	 * @return the function contained in the FunctionPotential
	 */
	public String getFunction() {
		return unprocessCovariates(variables, processedCovariates)[0];
	}

	/**
	 * Process and sets  {@codefunction}
	 *
	 * @param function - The function (unprocessed) to be set
	 */

	public void setFunction(String function) {
		setCovariates(new String[] { function });
		try {
			evaluator.parse(this.processedCovariates[0]);
		} catch (EvaluationException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Only throws NonProjectablePotentialException because this potential cannot be projected to a table
	 *
	 * @throws NonProjectablePotentialException
	 * @throws WrongCriterionException
	 */
	@Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
			List<TablePotential> projectedPotentials) throws NonProjectablePotentialException, WrongCriterionException {
		throw new NonProjectablePotentialException("Function potential cannot be projected to a table");
	}

	/**
	 * Only throws NonProjectablePotentialException because this potential cannot be projected to a table
	 *
	 * @throws NonProjectablePotentialException
	 * @throws WrongCriterionException
	 */
	@Override protected List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
			double[] coefficients, String[] covariates, List<Variable> evidencelessVariables,
			Map<String, String> variableValues) throws NonProjectablePotentialException, WrongCriterionException {
		throw new NonProjectablePotentialException("Function potential cannot be projected to a table");

	}

	@Override public Potential copy() {
		return new FunctionPotential(this);
	}

	/**
	 * Multiplies function by {@code scale}
	 *
	 * @param scale - the scale factor
	 */
	@Override public void scalePotential(double scale) {
		String scaleString = new Double(scale).toString();
		String function = scaleString.concat("*").concat(processedCovariates[0]);
		processedCovariates[0] = function;
	}

	/**
	 * Adds the variable to the new potential. The function does not change
	 *
	 * @param variable - the variable to be added
	 * @returns a FunctionPotential with the new variabla
	 */
	@Override public Potential addVariable(Variable variable) {
		FunctionPotential newPotential = null;
		if (!variables.contains(variable)) {
			List<Variable> newVariables = new ArrayList<>(variables);
			newVariables.add(variable);
			newPotential = new FunctionPotential(newVariables, this.role);
			newPotential.setCovariates(processedCovariates);
			newPotential.setCoefficients(new double[] { 1 });
		} else {
			newPotential = new FunctionPotential(this);
		}
		return newPotential;
	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		return super.deepCopy(copyNet);
	}

	@Override public String toString() {
		return unprocessCovariates(variables, processedCovariates)[0];
	}

	/**
	 * Always returns false because there is no uncertainty
	 */

	@Override public boolean isUncertain() {
		return false;
	}
	
	
	/**
	 * @param values
	 * @return The value obtained by evaluation the function for the assignment of variables given by 'values'
	 * @throws EvaluationException
	 */
	public String getValue(Map<String,String> values) throws EvaluationException {

		evaluator.setVariables(values);
		// For TSD15; 10^6 21.424 sec
		return evaluator.evaluate(this.processedCovariates[0]);
		// For TSD15; 10^6 22.175 sec
		//		return evaluator.evaluate();
		// For TSD15; 10^6 16.392 sec
		//		return "12";
	}

//CMI 26/04/2020
	@Override
	public double sampleConditionedVariable(Random random, EvidenceCase parents) throws OpenMarkovException {
		List<Variable> parentVariables = parents.getVariables();

		Map<String, String> variablesMap = new HashMap();
		double result =0;

		for (Variable parentVariable:parentVariables){
			int index = variables.indexOf(parentVariable);
			String variableToAdd = "v" + index;
			variablesMap.put(variableToAdd, ""+parents.getFinding(parentVariable).getNumericalValue());
		}
		try {
			result = new Double(getValue(variablesMap)).doubleValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return  result;
	}



//CMF


}
