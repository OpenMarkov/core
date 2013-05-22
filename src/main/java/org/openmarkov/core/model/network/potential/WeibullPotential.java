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
     * Ancillary parameter for the Weibull distribution
     */
    private double       shape;
    /**
     * Constant in survival analysis for baseline hazard.
     */
    private double       constant;

    public WeibullPotential(List<Variable> variables, PotentialRole role,
            double[] coefficients, double shape, double constant) {
        super(variables, role);
        this.coefficients = coefficients;
        this.shape = shape;
        this.constant = constant;
        this.sampledCoefficients = null; 
    }

    public WeibullPotential(List<Variable> variables, PotentialRole role,
            double[] coefficients, double[] covarianceMatrix, double shape,
            double constant) {
        this(variables, role, coefficients, shape, constant);
        this.covarianceMatrix = covarianceMatrix;
    }

    public WeibullPotential(List<Variable> variables, PotentialRole role) {
        this(variables, role, new double[variables.size()], 0.0, 0.0);
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
            if(inferenceOptions.probNet.containsShiftedVariable(timeVariable, -1))
            {
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
        List<Double> numericValues = new ArrayList<>(variables.size());

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
                    numericValues.add(0.0);
                } else {
                    double numericValue = evidenceCase.getFinding(variable).getNumericalValue();
                    if (variable.isTemporal()) {
                        numericValue -= variable.getTimeSlice();
                    }
                    numericValues.add(numericValue);
                }
            }
        }

        List<Variable> projectedPotentialVariables = new ArrayList<>(evidencelessVariables);
        projectedPotentialVariables.add(0, variables.get(0));
        TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, role);
        int[] offsets = projectedPotential.getOffsets();
        
        for (int i = 0; i < projectedPotential.values.length; i += 2) {
            // Set the values of variables of variables without evidence
            for (int j = 1; j < projectedPotentialVariables.size(); ++j) {
                int index = (j + 1 < projectedPotentialVariables.size()) ? (i / offsets[j])
                        % offsets[j + 1] : i / offsets[j];
                numericValues.set(evidencelessVariablesIndex.get(j - 1), (double) index);
            }
            double lambda = constant;
            for (int j = 0; j < coefficients.length; ++j) {
                lambda += numericValues.get(j) * coefficients[j];
            }
            lambda = Math.exp(lambda);
            if (tMinusOne < 0) {
                // p
                projectedPotential.values[i] = 1;
                // Complement (1-p)
                projectedPotential.values[i + 1] = 0;
            } else {
                // p
                double diff = Math.pow(tMinusOne, shape) - Math.pow(t, shape);
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
                (covarianceMatrix != null) ? covarianceMatrix.clone() : covarianceMatrix,
                shape,
                constant);
        copyPotential.sampledCoefficients = sampledCoefficients;
        return copyPotential;
    }

    @Override
    public Potential copy() {
        WeibullPotential copyPotential = new WeibullPotential(variables,
                role,
                coefficients.clone(),
                (covarianceMatrix != null) ? covarianceMatrix.clone() : covarianceMatrix,
                shape,
                constant);
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
            
            // Cholesky decomposition using the the Cholesky–Banachiewicz algorithm
            double[] cholesky = new double[covarianceMatrix.length];
            double[] diagonals = new double[coefficients.length];
            int index = 0;
            for(int i=0; i < coefficients.length; ++i)
            {
                double sumOfSquares = 0.0;
                double sumOfMul = 1.0;
                for(int j=0; j <= i; ++j)
                {
                    if(i == j)
                    {
                        diagonals[i] = Math.sqrt(covarianceMatrix[index] - sumOfSquares); 
                        cholesky[index] = diagonals[i];
                    }else
                    {
                        cholesky[index] = (covarianceMatrix[index] - sumOfMul) / diagonals[j];
                    }
                    sumOfSquares += Math.pow(cholesky[index], 2);
                    sumOfMul += cholesky[index] * diagonals[j];
                    ++index;
                }
            }
            
//            // Correlation matrix
//            double[] correlationMatrix = new double[covarianceMatrix.length];
//            index = 0;
//            for(int i=0; i < coefficients.length; ++i)
//            {
//                int firstIndexOfRow = index;
//                double diagonal = covarianceMatrix[firstIndexOfRow + i];
//                for(int j=0; j <= i; ++j)
//                {
//                    if(i==j)
//                    {
//                        diagonals[j] = covarianceMatrix[index];
//                    }
//                    correlationMatrix[index] = covarianceMatrix[index] / (diagonals[j] * diagonal);  
//                }
//                ++index;
//            }
            
            Random randomGenerator = new XORShiftRandom();
            NormalFunction normalDistribution = new NormalFunction(0, 1); 
            double[] normalSamples = new double[coefficients.length];
            for(int i=0; i < normalSamples.length; ++i)
            {
                double sample = normalDistribution.getSample(randomGenerator);
                normalSamples[i] = sample;
            }
        
            index = 0;
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
        return shape;
    }

    public void setShape(double shape) {
        this.shape = shape;
    }

    public double getConstant() {
        return constant;
    }

    public void setConstant(double constant) {
        this.constant = constant;
    }

    public double[] getCovarianceMatrix() {
        return covarianceMatrix;
    }

    public void setCovarianceMatrix(double[] covarianceMatrix) {
        this.covarianceMatrix = covarianceMatrix;
    }
    
    public Variable getTimeVariable() {
        return timeVariable;
    }

    public void setTimeVariable(Variable timeVariable) {
        this.timeVariable = timeVariable;
    }

}
