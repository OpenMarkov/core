/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@PotentialType(name = "Hazard (Weibull)", family = "GLM") public class WeibullHazardPotential extends GLMPotential implements DESSimulablePotential {

	protected static final String GAMMA = "Gamma";
	protected static final String[] MANDATORY_COVARIATES = new String[] { GAMMA, CONSTANT };

	/**
	 * Determines whether it represents a log hazard
	 */
	protected boolean log = false;

	/**
	 * Time variable
	 */
	private Variable timeVariable = null;

	//CMI 21/11/2023 -For efficiency:  list of Variables sort the same way as the coefficients and evaluator as final.

	private final Evaluator desEvaluator = new Evaluator();


	//CMF

	public WeibullHazardPotential(List<Variable> variables, PotentialRole role, String[] covariates,
			double[] coefficients) {
		super(variables, role, covariates, coefficients);
	}

	public WeibullHazardPotential(List<Variable> variables, PotentialRole role, String[] covariates,
			double[] coefficients, double[] covarianceMatrix) {
		super(variables, role, covariates, coefficients, covarianceMatrix);
	}

	public WeibullHazardPotential(List<Variable> variables, PotentialRole role, double[] coefficients,
			double[] covarianceMatrix) {
		super(variables, role, getDefaultCovariates(variables, role, MANDATORY_COVARIATES), coefficients,
				covarianceMatrix);
	}

	public WeibullHazardPotential(List<Variable> variables, PotentialRole role, String[] covariates,
			double[] coefficients, double[] uncertaintyMatrix, MatrixType matrixType) {
		super(variables, role, covariates, coefficients, uncertaintyMatrix, matrixType);
	}

	public WeibullHazardPotential(List<Variable> variables, PotentialRole role, double[] coefficients,
			double[] uncertaintyMatrix, MatrixType matrixType) {
		super(variables, role, getDefaultCovariates(variables, role, MANDATORY_COVARIATES), coefficients,
				uncertaintyMatrix, matrixType);
	}

	public WeibullHazardPotential(List<Variable> variables, PotentialRole role) {
		this(variables, role, getDefaultCovariates(variables, role, MANDATORY_COVARIATES),
				new double[variables.size() + 1]);
	}

	public WeibullHazardPotential(WeibullHazardPotential potential) {
		super(potential);
		timeVariable = potential.timeVariable;
		log = potential.log;
	}

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role.
	 *
	 * @param node      . {@code Node}
	 * @param variables . {@code List} of {@code Variable}.
	 * @param role      . {@code PotentialRole}.
	 * @return True if it is valid
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		return !variables.isEmpty() && variables.get(0).isTemporal()
				&& variables.get(0).getVariableType() == VariableType.FINITE_STATES
				&& variables.get(0).getNumStates() == 2
				//CMI 21/11/2023 - for DESnets vs MIDs; FIXME currently only checks it is a DESnet
				|| node.getProbNet().getNetworkType() instanceof DESNetworkType;
				//CMF

	}

	public double getGamma() {
		return coefficients[getGammaIndex(processedCovariates)];
	}

	public void setGamma(double gamma) {
		this.coefficients[getGammaIndex(processedCovariates)] = gamma;
	}

	@Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
			double[] coefficients, String[] covariates, List<Variable> evidencelessVariables,
			Map<String, String> variableValues) throws NonProjectablePotentialException, WrongCriterionException {
		Variable conditionedVariable = getConditionedVariable();
		// Fill arrays numericValues and evidencelessVariables

		int gammaIndex = getGammaIndex(covariates);
		int constantIndex = getConstantIndex(covariates);

		evidencelessVariables.remove(timeVariable);

		int numConfigurations = 1;
		for (Variable evidencelessVariable : evidencelessVariables) {
			numConfigurations *= evidencelessVariable.getNumStates();
		}

		List<Variable> projectedPotentialVariables = new ArrayList<>(evidencelessVariables);
		projectedPotentialVariables.add(0, variables.get(0));
		if (timeVariable != null && timeVariable.getVariableType() != VariableType.NUMERIC && !evidenceCase
				.contains(timeVariable)) {
			projectedPotentialVariables.add(timeVariable);
		}
		TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, role);
		int[] offsets = projectedPotential.getOffsets();
		int[] dimensions = projectedPotential.getDimensions();

		double[] ts = null;
		if (timeVariable != null && timeVariable.getVariableType() != VariableType.NUMERIC) {
			ts = new double[timeVariable.getNumStates()];
			double timeDifference = conditionedVariable.getTimeSlice() - timeVariable.getTimeSlice();
			for (int i = 0; i < ts.length; ++i) {
				ts[i] = Double.parseDouble(timeVariable.getStates()[i].getName()) + timeDifference;
			}
		} else {
			ts = new double[1];
			double t = (conditionedVariable.getTimeSlice() >= 0) ? conditionedVariable.getTimeSlice() : 1;
			if (timeVariable != null) {
				if (!evidenceCase.contains(timeVariable)) {
					throw new NonProjectablePotentialException(
							"Can not project potential without evidence on timeVariable " + timeVariable.getName());
				}
				double timeDifference = conditionedVariable.getTimeSlice() - timeVariable.getTimeSlice();
				t = evidenceCase.getFinding(timeVariable).getNumericalValue() + timeDifference;
			}
			ts[0] = t;
		}
		double shape = Math.exp(coefficients[gammaIndex]);
		Evaluator evaluator = new Evaluator();
		for (int timeVariableState = 0; timeVariableState < ts.length; ++timeVariableState) {
			double t = ts[timeVariableState];
			for (int i = 0; i < numConfigurations; i++) {
				int configBaseIndex = (i + timeVariableState * numConfigurations) * 2;
				// Set the values of variables without evidence
				for (int j = 1; j < projectedPotentialVariables.size(); ++j) {
					int index = (configBaseIndex / offsets[j]) % dimensions[j];
					Variable variable = projectedPotentialVariables.get(j);
					State[] states = variable.getStates();
					double value = index;
					try {
						value = Double.parseDouble(states[index].getName());
					} catch (NumberFormatException e) {
						// ignore
					}
					variableValues.put("v" + variables.indexOf(variable), String.valueOf(value));
				}
				evaluator.setVariables(variableValues);
				double lambda = coefficients[constantIndex];
				for (int j = 0; j < coefficients.length; ++j) {
					double covariateValue = 0.0;
					if (j != gammaIndex && j != constantIndex) {
						try {
							covariateValue = Double.parseDouble(evaluator.evaluate(covariates[j]));
						} catch (NumberFormatException | EvaluationException e) {
							throw new NonProjectablePotentialException(e.getMessage());
						}
						lambda += covariateValue * coefficients[j];
					}
				}
				if (log) {
					lambda = Math.exp(lambda);
				}
				double probability = 0;
				if (t > 0) {
					double diff = Math.pow(t - 1, shape) - Math.pow(t, shape);
					probability = 1 - Math.exp(lambda * diff);
				}
				// p
				projectedPotential.values[configBaseIndex + 1] = probability;
				// Complement (1-p)
				projectedPotential.values[configBaseIndex] = 1 - probability;
			}
		}

		return Arrays.asList(projectedPotential);
	}


	//CMI 21/11/2023 implementing DESSimulablePotential; it also samples if failure occurs in the present cycle for comparing with MIDs
	//FIXME Consider using this potential for DESnets in general


//	@Override
//	public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) {
//
//		if (timeVariable == null) throw new RuntimeException("Weibull Hazard potential has no time variable; hazard cannot be computed");
//		//I always consider: first coefficient is lngamma; second is constant
//		//lambda = exp(constant + cov1*value_variable_1; cov2*value_variable_2 + covN*value_variable_N)
//		//Efficience problem--> getting findings .Are parents always in the same order through the simulations?
//		//parents.getFindings
//		String[] covariates = getCovariates();
//		double[] coefficients =getCoefficients();
//		//gamma in potential dialos  is lngamma stored in coefficients[0]
//		double gamma =Math.exp(coefficients[0]);
//		//lambda = exp(constant + cov1*value_variable_1; cov2*value_variable_2 + covN*value_variable_N_-1)
//		//last coefficient is the timeVariable; mandatory for sampling due to it is neccesary for lambda
//		double lnlambda = coefficients[1];
//		for (int i = 2; i < coefficients.length ; i++) {
//			int finalI = i;
//			Variable covariateVariable =  variables.stream().filter(variable -> variable.getName().equals(covariates[finalI])).findFirst().orElseThrow(RuntimeException::new);
//			Finding finding = parents.getFinding(covariateVariable);
//			if (covariateVariable.getVariableType().equals(VariableType.FINITE_STATES)){
//				double value = coefficients[i] * finding.getStateIndex();
//				lnlambda += value;
//			} else {
//				double value =coefficients[i] * finding.getNumericalValue();
//				lnlambda +=  value;
//			}
//
//		}
//		double timeVariableValue = parents.getFinding(timeVariable).getNumericalValue();
//		double lambda = Math.exp(lnlambda);
//		//hazard; for one year cycle; FIXME has to be revised
//		double transitionProbability = 1 - Math.exp(lambda*(Math.pow(timeVariableValue,gamma)-Math.pow(timeVariableValue-1,gamma) ));
//		if (randomNumbers[0]<= transitionProbability)
//			return 0;
//		else
//			return 1;
//	}

	@Override
	public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {

		if (timeVariable == null) throw new RuntimeException("Weibull Hazard potential has no time variable; hazard cannot be computed");
		double[] coefficients =getCoefficients();
		double gamma =Math.exp(coefficients[0]);
		Variable variableLambda =variables.stream().filter(variable -> variable.getName().equals("Lambda")).findFirst().orElse(null);
		double lambda = parents.getFinding(variableLambda).getNumericalValue();
		double timeVariableValue = parents.getFinding(timeVariable).getNumericalValue();
		//hazard; for one year cycle; FIXME has to be revised
		double transitionProbability = 1 - Math.exp(lambda*(Math.pow(timeVariableValue-1,gamma) -Math.pow(timeVariableValue,gamma) ));
//		System.out.println("Weibull Hazard: timeVariableValue "+ timeVariableValue +" transitionProbability "+ transitionProbability + " randomNumber " + randomNumbers[0]);
		if (randomNumbers[0]< transitionProbability) {
//			System.out.println("Weibull Hazard: state corresponding to 1: " + variables.get(0).getStateName(1));
			return 1;

		}
		else
			return 0;
	}





	//CMF



	@Override public Potential copy() {
		return new WeibullHazardPotential(this);
	}

	public Variable getTimeVariable() {
		return timeVariable;
	}

	public void setTimeVariable(Variable timeVariable) {
		this.timeVariable = timeVariable;
	}

	@Override public String toString() {
		return super.toString() + " = Hazard (Weibull)";
	}

	@Override public void shift(ProbNet probNet, int timeDifference) throws NodeNotFoundException {
		super.shift(probNet, timeDifference);
		if (timeVariable != null) {
			timeVariable = probNet.getShiftedVariable(timeVariable, timeDifference);
		}
	}

	@Override public void replaceNumericVariable(Variable convertedParentVariable) {
		super.replaceNumericVariable(convertedParentVariable);
		if (timeVariable != null && convertedParentVariable.getName().equals(timeVariable.getName())) {
			setTimeVariable(convertedParentVariable);
		}
	}

	protected int getGammaIndex(String[] covariates) {
		int gammaIndex = -1;
		int i = 0;
		while (i < covariates.length && gammaIndex == -1) {
			if (covariates[i].equals(GAMMA)) {
				gammaIndex = i;
			}
			++i;
		}
		return gammaIndex;
	}

	public boolean isLog() {
		return log;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	@Override public void scalePotential(double scale) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();

	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		WeibullHazardPotential potential = (WeibullHazardPotential) super.deepCopy(copyNet);

		potential.setLog(this.log);

		if (timeVariable != null) {
			try {
				potential.setTimeVariable(copyNet.getVariable(this.getTimeVariable().getName()));
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
		}

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
}
