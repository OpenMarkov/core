///*
// * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
// * Unless required by applicable law or agreed to in writing,
// * this code is distributed on an "AS IS" basis,
// * WITHOUT WARRANTIES OF ANY KIND.
// */
package org.openmarkov.core.model.network.potential;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a potential which is function of the values provided by the parents.
 * TODO Which parents???
 * Has they to be numeric or may we have "finites states" which have one value associated? For example male=1, female=0
 *
 * @author cmyago
 * @version 1.1 06/12/2019
 * @version 2 19/08/2022 - changed to mitigate nuisance variance and speed simulation creating only once the evaluator and the signature of sampling
 * @version 3 21/11/2023 - simplifying potential
 * 04/10/2023 FIXME Check license
 */
@PotentialType(name = "Function") public class FunctionPotential extends Potential implements DESSimulablePotential {

	/**
	 * The default function
	 */
	public static final String DEFAULT_FUNCTION = "1";

	/**
	 * The coefficient
	 */
	protected static final double COEFFICIENT = 1;

	/**
	 * Evaluates the function 19/08/2022 - changed to final field to speed the simulation
	 */
	private final Evaluator evaluator = new Evaluator();

	private String function = DEFAULT_FUNCTION;

	private String processedFunction = DEFAULT_FUNCTION;



	/**
	 * Creates a Function potential with the function by default
	 *
	 * @param variables - list with the node variable and their parents
	 * @param role Potential role
	 */
	public FunctionPotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);
	}

	/**
	 * Creates a Function potential with the function given by {@code function}
	 *
	 * @param variables - list with the node variable and their parents
	 * @param role      - the role of the potential
	 * @param function  - A string representing the function
	 */
	public FunctionPotential(List<Variable> variables, PotentialRole role, String function) {
		this(variables,role);
		this.function = function;
		this.processedFunction = processFunction(function);

	}

	/**
	 * Creates a Function potential equal to {@code potential}
	 *
	 * @param potential - potential copied
	 */
	public FunctionPotential(FunctionPotential potential) {
		super(potential);
		this.function = potential.getFunction();
		this.processedFunction = processFunction(potential.getFunction());
	}

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role.
	 * UNCLEAR--&#62; Should the parents be numeric
	 *
	 * @param node      . {@code Node}
	 * @param variables . {@code ArrayList} of {@code Variable}.
	 * @param role      . {@code PotentialRole}.
	 * @return True if it is valid
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
		return function;
	}

	/**
	 * Process and sets  {codefunction}
	 *
	 * @param function - The function (unprocessed) to be set
	 */

	public void setFunction(String function) {
		this.function = function;
		this.processedFunction = processFunction(function);
	}


	/**
	 * Only throws NonProjectablePotentialException because this potential cannot be projected to a table
	 *
	 * @throws NonProjectablePotentialException NonProjectablePotentialException
	 * @throws WrongCriterionException WrongCriterionException
	 */
	@Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
			List<TablePotential> projectedPotentials) throws NonProjectablePotentialException, WrongCriterionException {
	//15/01/2023 This method is called when removing a node with this potential;
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
// 24/10/2023 'Double(double)' is deprecated and marked for removal
//		String scaleString = new Double(scale).toString();
//
		String scaleString = Double.toString(scale);
		function = scaleString.concat("*").concat(function);
		processedFunction = scaleString.concat("*").concat(processedFunction);

	}

	//FIXME --> revise
	@Override public Potential deepCopy(ProbNet copyNet) {
		return super.deepCopy(copyNet);
	}

	@Override public String toString() {
		return function;
	}

	/**
	 * Always returns false because there is no uncertainty
	 */

	@Override public boolean isUncertain() {
		return false;
	}

// 19/08/2022 - used double instead of Random and evaluator object only create once
	/**
	 * @param values Values
	 * @return The value obtained by evaluation the function for the assignment of variables given by 'values'
	 * @throws EvaluationException EvaluationException
	 */
	public String getValue(Map<String,String> values) throws EvaluationException {
		evaluator.setVariables(values);
		return evaluator.evaluate(processedFunction);
	}

	private String processFunction(String function) {
		return function.replaceAll("\\{","#{");
	}
	@Override
	public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {
		List<Variable> parentVariables = parents.getVariables();
		Map<String, String> variablesMap = new HashMap();
		double result =0;
		for (Variable parentVariable:parentVariables){
			String variableToAdd = parentVariable.getName();
			variablesMap.put(variableToAdd, ""+parents.getFinding(parentVariable).getNumericalValue());
		}
		try {
//			'Double(double)' is deprecated and marked for removal
//			result = new Double(getValue(variablesMap)).doubleValue();
			result = Double.parseDouble(getValue(variablesMap));
		} catch (EvaluationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return  result;
	}



//

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

	public String getProcessedFunction() {
		return processedFunction;
	}

	public void setProcessedFunction(String processedFunction) {
		this.processedFunction = processedFunction;
	}
}
