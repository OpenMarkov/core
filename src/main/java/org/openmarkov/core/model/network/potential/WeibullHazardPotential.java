/*
 * Copyright 2013 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

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

@RelationPotentialType(name = "Hazard (Weibull)", family = "Regression")
public class WeibullHazardPotential extends RegressionPotential {

    protected static final String GAMMA = "Gamma";
    /**
     * In the coefficients array of a Weibull function, in the first position of
     * the array the gamma (ln shape) parameter is stored and in the second
     * position of the array the constant coefficient is stored
     */
    protected int gammaIndex = 0;
    protected int constantIndex = 1;
    
    /**
     * Time variable
     */
    private Variable timeVariable = null;
    
    
    public WeibullHazardPotential(List<Variable> variables, PotentialRole role, String[] covariates, double[] coefficients) {
        super(variables, role, covariates, coefficients);
    }

    public WeibullHazardPotential(List<Variable> variables, PotentialRole role,
            String[] covariates, double[] coefficients, double[] covarianceMatrix) {
        super(variables, role, covariates, coefficients, covarianceMatrix);
    }
    
    public WeibullHazardPotential(List<Variable> variables, PotentialRole role,
            double[] coefficients, double[] covarianceMatrix) {
        super(variables, role, getDefaultCovariates(variables, role, getMandatoryCovariates()), coefficients, covarianceMatrix);
    }    
    
    public WeibullHazardPotential(List<Variable> variables, PotentialRole role,
            String[] covariates, double[] coefficients, double[] uncertaintyMatrix, MatrixType matrixType) {
        super(variables, role, covariates, coefficients, uncertaintyMatrix, matrixType);
    }
    
    public WeibullHazardPotential(List<Variable> variables, PotentialRole role,
            double[] coefficients, double[] uncertaintyMatrix, MatrixType matrixType) {
        super(variables, role, getDefaultCovariates(variables, role, getMandatoryCovariates()), coefficients, uncertaintyMatrix, matrixType);
    }

    public WeibullHazardPotential(List<Variable> variables, PotentialRole role) {
        this(variables, role, getDefaultCovariates(variables, role, getMandatoryCovariates()), new double[variables.size()+1]);
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     * 
     * @param probNode
     *            . <code>ProbNode</code>
     * @param variables
     *            . <code>List</code> of <code>Variable</code>.
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
            InferenceOptions inferenceOptions, double[] coefficients)
            throws NonProjectablePotentialException, WrongCriterionException {
        
        int timeSlice = variables.get(0).getTimeSlice();
        
        // Set value for t and tMinusOne
        double t = timeSlice;
        double tMinusOne = timeSlice - 1;
        if (timeVariable != null) {
            if (!evidenceCase.contains(timeVariable)) {
                throw new NonProjectablePotentialException("Can not project potential without evidence on timeVariable "
                        + timeVariable.getName());
            }
            t = evidenceCase.getFinding(timeVariable).getNumericalValue();
            if (timeVariable != null
                    && inferenceOptions.probNet.containsShiftedVariable(timeVariable, -1)) {
                Variable timeVariablePreviousSlice = null;
                try {
                    timeVariablePreviousSlice = inferenceOptions.probNet.getShiftedVariable(timeVariable, -1);
                    tMinusOne = evidenceCase.getFinding(timeVariablePreviousSlice).getNumericalValue();
                } catch (ProbNodeNotFoundException e) {
                    // Unreachable code.
                }
            }else
            {
                tMinusOne = Double.NEGATIVE_INFINITY;
            }
        }
        
        // Fill arrays numericValues and  evidencelessVariables
        List<Variable> evidencelessVariables = new ArrayList<>();
        List<Integer> evidencelessVariablesIndex = new ArrayList<>();
        Map<String, String> variableValues = new HashMap<>();

        for (int i = 1; i < variables.size(); ++i) {
            Variable variable = variables.get(i);
            if(!variables.get(i).equals(timeVariable))
            {
                if (!evidenceCase.contains(variable)) {
                    if (variable.getVariableType() == VariableType.NUMERIC) {
                        throw new NonProjectablePotentialException("Can not project potential with numeric variable "
                                + variable.getName());
                    }
                    evidencelessVariables.add(variable);
                    evidencelessVariablesIndex.add(i - 1);
                    variableValues.put(variable.getName(), "0.0");
                } else {
                    double numericValue = evidenceCase.getFinding(variable).getNumericalValue();
                    variableValues.put(variable.getName(), String.valueOf(numericValue));
                }
            }
        }

        List<Variable> projectedPotentialVariables = new ArrayList<>(evidencelessVariables);
        projectedPotentialVariables.add(0, variables.get(0));
        TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, role);
        int[] offsets = projectedPotential.getOffsets();
        int[] dimensions = projectedPotential.getDimensions();
        double shape = Math.exp(coefficients[gammaIndex]);
        Evaluator evaluator = new Evaluator();
        for (int i = 0; i < projectedPotential.values.length; i += 2) {
            // Set the values of variables without evidence
            for (int j = 1; j < projectedPotentialVariables.size(); ++j) {
                int index = (i / offsets[j]) % dimensions[j];
                variableValues.put(projectedPotentialVariables.get(j).getName(), String.valueOf(index));
            }
            evaluator.setVariables(variableValues);
            double lambda = coefficients[constantIndex];
            for (int j = 0; j < coefficients.length; ++j) {
                double covariateValue = 0.0;
                if(j!=gammaIndex && j!=constantIndex)
                {
                    try {
                        covariateValue = Double.parseDouble(evaluator.evaluate(processedCovariates[j]));
                    } catch (NumberFormatException | EvaluationException e) {
                        e.printStackTrace();
                    }
                    lambda += covariateValue * coefficients[j];
                }
            }
            lambda = Math.exp(lambda);
            double probability = 0;
            if (tMinusOne >= 0) {
                double diff = Math.pow(tMinusOne, shape) - Math.pow(t, shape);
                probability = 1 - Math.exp(lambda * diff);
            }
            // p
            projectedPotential.values[i + 1] = probability;
            // Complement (1-p)
            projectedPotential.values[i] = 1 - probability;
        }

        return Arrays.asList(projectedPotential);
    }

    @Override
    public void setCovariates(String[] covariates) {
        super.setCovariates(covariates);
        for(int i=0; i < covariates.length; ++i)
        {
            if(covariates[i].equals(GAMMA))
            {
                gammaIndex = i;
            }else if(covariates[i].equals(CONSTANT))
            {
                constantIndex = i;
            }
        }
    }

    @Override
    public Potential shift(ProbNet probNet, int timeDifference)
            throws ProbNodeNotFoundException {
        List<Variable> shiftedVariables = getShiftedVariables(probNet, timeDifference);
        WeibullHazardPotential copyPotential = new WeibullHazardPotential(shiftedVariables,
                role);
        copyPotential.setCovariates(shiftCovariates(covariates, variables, shiftedVariables));
        copyPotential.setCoefficients(coefficients.clone());
        if(covarianceMatrix != null)
        {
            copyPotential.setCovarianceMatrix(covarianceMatrix.clone());
        }else if(choleskyDecomposition != null)
        {
            copyPotential.setCholeskyDecomposition(choleskyDecomposition.clone());
        }
        copyPotential.sampledCoefficients = sampledCoefficients;
        copyPotential.timeVariable = timeVariable;
        return copyPotential;
    }
    
    @Override
    public Potential copy() {
        WeibullHazardPotential copyPotential = new WeibullHazardPotential(variables, role);
        copyPotential.setCovariates(covariates.clone());
        copyPotential.setCoefficients(coefficients.clone());
        if(covarianceMatrix != null)
        {
            copyPotential.setCovarianceMatrix(covarianceMatrix.clone());
        }else if(choleskyDecomposition != null)
        {
            copyPotential.setCholeskyDecomposition(choleskyDecomposition.clone());
        }
        copyPotential.sampledCoefficients = sampledCoefficients;
        copyPotential.timeVariable = timeVariable;
        return copyPotential;
    }

    public Variable getTimeVariable() {
        return timeVariable;
    }

    public void setTimeVariable(Variable timeVariable) {
        this.timeVariable = timeVariable;
    }
    
    public static String[] getMandatoryCovariates()
    {
        return new String[]{GAMMA, CONSTANT};
    }    
    
    @Override
    public String toString() {
        return super.toString() + " = Hazard (Weibull)";
    }       
}
