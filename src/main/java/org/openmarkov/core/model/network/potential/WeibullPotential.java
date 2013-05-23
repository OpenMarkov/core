/*
 * Copyright 2013 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.modelUncertainty.NormalFunction;
import org.openmarkov.core.model.network.modelUncertainty.XORShiftRandom;
import org.openmarkov.core.model.network.potential.plugin.RelationPotentialType;

@RelationPotentialType(name = "WeibullDistribution", family = "")
public class WeibullPotential extends Potential {

    /**
     * Time variable
     */
    private Variable timeVariable = null;
    
    /**
     * Coefficients for the parameters of the function
     * In the first position of the array the shape parameter is stored
     * In the second position of the array the constant coefficient is stored
     */
    private double[] coefficients;
    /**
     * Coefficients sampled using Cholesky decomposition
     */
    private double[] sampledCoefficients;    
    /**
     * Covariance matrix
     */
    private double[] covarianceMatrix = null;
    /**
     * Colesky decomposition
     */
    private double[] cholesky = null; 

    public WeibullPotential(List<Variable> variables, PotentialRole role, double[] coefficients) {
        super(variables, role);
        this.coefficients = coefficients;
        this.sampledCoefficients = null; 
    }

    public WeibullPotential(List<Variable> variables, PotentialRole role,
            double[] coefficients, double[] covarianceMatrix) {
        this(variables, role, coefficients);
        this.covarianceMatrix = covarianceMatrix;
        this.cholesky = calculateCholesky(covarianceMatrix);
    }

    public WeibullPotential(List<Variable> variables, PotentialRole role) {
        this(variables, role, new double[variables.size()+1]);
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
            InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException, WrongCriterionException {

        double[] coefficients = (sampledCoefficients == null) ? this.coefficients
                : this.sampledCoefficients;
        int timeSlice = variables.get(0).getTimeSlice();
        double t = timeSlice;
        double tMinusOne = timeSlice -1;
        
        if (timeVariable != null) {
            if (!evidenceCase.contains(timeVariable)) {
                throw new NonProjectablePotentialException("Can not project potential without evidence on timeVariable "
                        + timeVariable.getName());
            }
            t = evidenceCase.getFinding(timeVariable).getNumericalValue();
            if (timeVariable != null
                    && inferenceOptions.probNet.containsShiftedVariable(timeVariable, -1)) {
                Variable previous;
                try {
                    previous = inferenceOptions.probNet.getShiftedVariable(timeVariable, -1);
                    tMinusOne = evidenceCase.getFinding(previous).getNumericalValue();
                } catch (ProbNodeNotFoundException e) {
                    // Unreachable code.
                }
            }else
            {
                tMinusOne = Double.NEGATIVE_INFINITY;
            }
        }
        
        List<Variable> evidencelessVariables = new ArrayList<>();
        List<Integer> evidencelessVariablesIndex = new ArrayList<>();
        double[] numericValues = new double[variables.size() - 1];

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
                    numericValues[i-1]=0.0;
                } else {
                    double numericValue = evidenceCase.getFinding(variable).getNumericalValue();
                    if (variable.isTemporal()) {
                        numericValue -= variable.getTimeSlice();
                    }
                    numericValues[i-1] = numericValue;
                }
            }
        }

        List<Variable> projectedPotentialVariables = new ArrayList<>(evidencelessVariables);
        projectedPotentialVariables.add(0, variables.get(0));
        TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, role);
        int[] offsets = projectedPotential.getOffsets();
        int[] dimensions = projectedPotential.getDimensions();
        double gamma = Math.exp(getShape());
        
        for (int i = 0; i < projectedPotential.values.length; i += 2) {
            // Set the values of variables without evidence
            for (int j = 1; j < projectedPotentialVariables.size(); ++j) {
                int index = (i / offsets[j]) % dimensions[j];
                numericValues[evidencelessVariablesIndex.get(j - 1)] = (double) index;
            }
            double lambda = getConstant();
            for (int j = 2; j < coefficients.length; ++j) {
                lambda += numericValues[j-2] * coefficients[j];
            }
            lambda = Math.exp(lambda);
            if (tMinusOne < 0) {
                // p
                projectedPotential.values[i] = 1;
                // Complement (1-p)
                projectedPotential.values[i + 1] = 0;
            } else {
                // p
                double diff = Math.pow(tMinusOne, gamma) - Math.pow(t, gamma);
                projectedPotential.values[i] = Math.exp(lambda * diff);
                // Complement (1-p)
                projectedPotential.values[i + 1] = 1 - projectedPotential.values[i];
            }
        }

        return Arrays.asList(projectedPotential);
    }

    @Override
    public Potential shift(ProbNet probNet, int timeDifference)
            throws ProbNodeNotFoundException {
        WeibullPotential copyPotential = new WeibullPotential(getShiftedVariables(probNet, timeDifference),
                role,
                coefficients.clone(),
                (covarianceMatrix != null) ? covarianceMatrix.clone() : covarianceMatrix);
        copyPotential.sampledCoefficients = sampledCoefficients;
        return copyPotential;
    }

    @Override
    public Potential copy() {
        WeibullPotential copyPotential = new WeibullPotential(variables,
                role,
                coefficients.clone(),
                (covarianceMatrix != null) ? covarianceMatrix.clone() : covarianceMatrix);
        copyPotential.sampledCoefficients = sampledCoefficients;
        return copyPotential;
    }

    @Override
    public Potential sample() {
        if(covarianceMatrix != null)
        {
            if(this.sampledCoefficients == null)
            {
                this.sampledCoefficients = new double[coefficients.length];
            }
            
            Random randomGenerator = new XORShiftRandom();
            NormalFunction normalDistribution = new NormalFunction(0, 1); 
            double[] normalSamples = new double[coefficients.length];
            for(int i=0; i < normalSamples.length; ++i)
            {
                double sample = normalDistribution.getSample(randomGenerator);
                normalSamples[i] = sample;
            }
        
            int index = 0;
            for(int i=0; i < coefficients.length; ++i)
            {
                double value = 0.0;
                for(int j=0; j <= i; ++j)
                {
                    value += cholesky[index] * normalSamples[j];
                    index++;
                }
                sampledCoefficients[i] = value + coefficients[i];
            }
        }
        return this;
    }

    @Override
    public boolean isUncertain() {
        return this.covarianceMatrix != null;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(double[] coefficients) {
        this.coefficients = coefficients;
    }

    public double getShape() {
        return coefficients[0];
    }

    public void setShape(double shape) {
        this.coefficients[0] = shape;
    }

    public double getConstant() {
        return coefficients[1];
    }

    public void setConstant(double constant) {
        this.coefficients[1] = constant;
    }

    public double[] getCovarianceMatrix() {
        return covarianceMatrix;
    }

    public void setCovarianceMatrix(double[] covarianceMatrix) {
        this.covarianceMatrix = covarianceMatrix;
        this.cholesky = calculateCholesky(covarianceMatrix);
    }
    
    public Variable getTimeVariable() {
        return timeVariable;
    }

    public void setTimeVariable(Variable timeVariable) {
        this.timeVariable = timeVariable;
    }
    
    public double[] getCholeskyDecomposition() {
        return cholesky;
    }

    private double[] calculateCholesky(double[] covarianceMatrix)
    {
        double[] cholesky = null;
        if(covarianceMatrix != null)
        {
            // Cholesky decomposition using the the Cholesky–Banachiewicz algorithm
            cholesky = new double[covarianceMatrix.length];
            // Solve quadratic equation to get n, the number of coefficients
            int n = (int)(Math.sqrt(covarianceMatrix.length * 8 + 1) - 1)/2;
            double[] diagonals = new double[n];
            int[] firstIndexOfRow = new int[n];
            int index = 0;
            for(int i=0; i < n; ++i)
            {
                double sumOfSquares = 0.0;
                firstIndexOfRow[i] = index;
                for(int j=0; j <= i; ++j)
                {
                    if(i == j)
                    {
                        diagonals[i] = Math.sqrt(covarianceMatrix[index] - sumOfSquares); 
                        cholesky[index] = diagonals[i];
                    }else
                    {
                        double sumOfMul = 0.0;
                        for(int k = 0; k < j; ++k)
                        {
                            sumOfMul += cholesky[firstIndexOfRow[i] + k] * cholesky[firstIndexOfRow[j] + k]; 
                        }
                        cholesky[index] = (covarianceMatrix[index] - sumOfMul) / diagonals[j];
                    }
                    sumOfSquares += Math.pow(cholesky[index], 2);
                    ++index;
                }
            }
        }
        return cholesky;
    }
}
