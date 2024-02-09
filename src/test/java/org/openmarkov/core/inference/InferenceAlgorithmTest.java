/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.Tools;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author mluque
 */

/** @author ibermejo */
@Disabled public abstract class InferenceAlgorithmTest {

	/**
	 * Maximum error allowed in tests. It could be modified by subclasses
	 * if it is necessary (for example, approximate inference methods).
	 */
	protected static double maxError = 0.0001;

	/**
	 * @param network
	 * @param variableName
	 * @return The variable in 'network' whose name is 'variableName'. It also checks whether the variable is not null.
	 * @throws NodeNotFoundException
	 */
	public static Variable getVariableAndAssertNotNull(ProbNet network, String variableName)
			throws NodeNotFoundException {
		Variable variable;

		variable = network.getVariable(variableName);
		assertNotNull(variable);
		return variable;
	}

	/**
	 * @param pot
	 * Checks if 'pot' is a conditional probability potential correctly defined: the values in each column sum 1.0.
	 */
	public static void checkIsAConditionalProbability(TablePotential pot) {

		double[] potValues = pot.values;
		int numStates = pot.getVariable(0).getNumStates();
		double[] auxValues = new double[numStates];
		int numColumns = potValues.length / numStates;
		int posInValues = 0;
		for (int i = 0; i < numColumns; i++) {
			for (int j = 0; j < numStates; j++) {
				auxValues[j] = potValues[posInValues];
				posInValues++;
			}
			assertEquals(1.0, Tools.sum(auxValues), maxError);
		}
	}

	/**
	 * @param network
	 * @return An InferenceAlgorithm for 'network'. If the network is not evaluable
	 * with the algorithm then the test calling this method is skipped.
	 */
	protected InferenceAlgorithm buildInferenceAlgorithmAndSkipTestIfNotEvaluable(ProbNet network) {
		boolean isEvaluable;
		InferenceAlgorithm algorithm = null;

		//If the network is not evaluable then the test is skipped
		isEvaluable = true;
		try {
			algorithm = buildInferenceAlgorithm(network);
		} catch (NotEvaluableNetworkException e1) {
			isEvaluable = false;
		}
		assumeTrue(isEvaluable);
		return algorithm;
	}

	/**
	 * Builds an InferenceAlgorithm object with 'probNet'.
	 * This method must be implemented by each inference test class.
	 */
	public abstract InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet) throws NotEvaluableNetworkException;

	protected void setUp() throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @param potA
	 * @param potB
	 * @return true if potA and potB are equal (variables can be in different order)
	 */
	protected boolean areEqualPotentials(TablePotential potA, TablePotential potB) {
		boolean areEqual;

		List<Variable> varsA = potA.getVariables();
		List<Variable> varsB = potB.getVariables();

		areEqual = varsA.size() == varsB.size();

		if (areEqual) {
			for (int i = 0; i < varsB.size() && areEqual; i++) {
				areEqual = varsA.contains(varsB.get(i));
			}
			int size = potA.getTableSize();

			for (int i = 0; i < size && areEqual; i++) {
				double valueA = potA.values[i];
				double valueB = potB.getValue(varsA, potA.getConfiguration(i));
				areEqual = Math.abs(valueA - valueB) < maxError;

			}

		}
		return areEqual;

	}

	protected TablePotential getTablePotential(Potential potential) {
		TablePotential table = null;
		try {
			table = potential.tableProject(null, null).get(0);
		} catch (NonProjectablePotentialException | WrongCriterionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return table;
	}

	/**
	 * @param aPosterioriProbs
	 * @param variables
	 * @param expectedProbs
	 * The probabilities in expectedProbs are given as independent numbers for each variable:
	 * It means that, if n is the number of states of a variable, then n-1 probabilities are given
	 * for it in 'expectedProbs'. And the values are ordered according the order in 'variables'.
	 * In the particular case when all variables are binary then 'variables' and 'expectedProbs' have
	 * the same size.
	 * */
	protected void checkProbabilities(Map<Variable, TablePotential> aPosterioriProbs, ArrayList<Variable> variables,
			double[] expectedProbs) {

		int size = variables.size();

		int indexBaseProbs = 0;
		for (int i = 0; i < size; i++) {
			double auxExpectedProbs[];
			Variable auxVar = variables.get(i);
			int numStates = auxVar.getNumStates();
			int numProbsAux;
			numProbsAux = numStates - 1;
			auxExpectedProbs = new double[numProbsAux];
			for (int j = 0; j < numProbsAux; j++) {
				auxExpectedProbs[j] = expectedProbs[indexBaseProbs + j];
			}
			checkProbabilityPotential(aPosterioriProbs, variables.get(i), auxExpectedProbs);
			indexBaseProbs = indexBaseProbs + numProbsAux;
		}

	}

	/**
	 * @param e
	 */
	@SuppressWarnings("restriction") protected void printExceptionAndFailIfImplemented(Exception e) {
		if (e.getClass() != UnsupportedOperationException.class) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * @param probabilities
	 * @param variableX
	 * @param x
	 * Checks if the probability potential of 'variableX' in 'probabilities' is equal
	 * to x[0],..,x[n], where 'n' is the number of states of 'variableX'
	 */
	protected void checkProbabilityPotential(Map<Variable, TablePotential> probabilities, Variable variableX,
			double... x) {
		TablePotential X = (TablePotential) probabilities.get(variableX);
		checkProbabilities(X, x);

	}

	/**
	 * @param pot
	 * @param values
	 * Checks if the values of 'pot' (except the last one) is equal to 'values' and if the sum of
	 * the probabilities in 'pot' is 1.0.
	 */
	protected void checkProbabilities(TablePotential pot, double... values) {

		double[] potValues = pot.values;
		int potValuesLength = potValues.length;
		assertEquals(values.length + 1, potValuesLength);
		double sum;
		sum = 0.0;
		for (int i = 0; i < potValuesLength - 1; i++) {
			double expected = values[i];
			double actual = potValues[i];
			assertEquals(expected, actual, maxError);
			sum = sum + expected;
		}
		assertEquals(1.0 - sum, potValues[potValuesLength - 1], maxError);
	}

	protected boolean areEquals(double[] v1, double[] v2) {
		boolean areEquals = true;
		int v1length;
		int i;

		v1length = v1.length;

		areEquals = (v1length == v2.length);
		i = 0;
		while (areEquals && i < v1length) {
			areEquals = Math.abs(v1[i] - v2[i]) < maxError;
			i = i + 1;
		}
		return areEquals;

	}

}
