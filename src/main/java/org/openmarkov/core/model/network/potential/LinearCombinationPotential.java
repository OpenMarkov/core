/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@PotentialType(names = {"Linear combination", "Linear regression"})
public class LinearCombinationPotential extends GLMPotential {
    
    public LinearCombinationPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role, getDefaultCovariates(variables, role), new double[variables.size()]);
    }
    
    //    public LinearCombinationPotential(Variable utilityVariable, List<Variable> variables) {
    //        super(variables, PotentialRole.UTILITY, getDefaultCovariates(variables, PotentialRole.UTILITY), new double[variables.size()+1]);
    //        this.utilityVariable = utilityVariable;
    //    }
    
    public LinearCombinationPotential(List<Variable> variables, PotentialRole role, VariableExpression[] covariates,
                                      double[] coefficients) {
        super(variables, role, covariates, coefficients);
    }
    
    public LinearCombinationPotential(LinearCombinationPotential potential) {
        super(potential);
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     *
     * @param node      . {@code Node}
     * @param variables . {@code ArrayList} of {@code Variable}.
     * @param role      . {@code PotentialRole}.
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        return role == PotentialRole.UNSPECIFIED || (
                !variables.isEmpty() && variables.get(0).getVariableType() == VariableType.NUMERIC
        );
    }
    
    @Override
    public Potential project(EvidenceCase evidenceCase) throws NotSupportedOperationException {
        throw new NotSupportedOperationException();
    }
    
    @Override protected TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                    double[] coefficients, VariableExpression[] covariates, List<Variable> evidencelessVariables,
                                                    Map<Variable, String> variableValues) throws NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
        Variable conditionedVariable = getConditionedVariable();
        int numStates = conditionedVariable.getNumStates();
        // Fill arrays numericValues and evidencelessVariables
        
        int constantIndex = getConstantIndex(covariates);
        
        List<Variable> projectedPotentialVariables = new ArrayList<>(evidencelessVariables);
        projectedPotentialVariables.add(0, variables.get(0));
        TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, role);
        
        int[] offsets = projectedPotential.getOffsets();
        int[] dimensions = projectedPotential.getDimensions();
        int firstParentIndex = 1;
        for (int i = 0; i < projectedPotential.values.length; i += numStates) {
            // Set the values of variables without evidence
            for (int j = firstParentIndex; j < projectedPotentialVariables.size(); ++j) {
                Variable variable = projectedPotentialVariables.get(j);
                int index = (i / offsets[j]) % dimensions[j];
                double value = index;
                try {
                    value = Double.parseDouble(variable.getStates()[index].getName());
                } catch (NumberFormatException e) {
                    // ignore
                }
                variableValues.put(variable, String.valueOf(value));
            }
            double regression = coefficients[constantIndex];
            for (int j = 0; j < coefficients.length; ++j) {
                
                if (j != constantIndex) {
                    double covariateValue = Double.parseDouble(covariates[j].evaluateWith(variableValues));
                        regression += covariateValue * coefficients[j];
                }
            }
            if (getConditionedVariable().getVariableType() == VariableType.NUMERIC) {
                projectedPotential.values[i] = regression;
            } else {
                int stateIndex = getConditionedVariable().getStateIndex(regression);
                for (int j = 0; j < numStates; ++j) {
                    projectedPotential.values[i + j] = (j == stateIndex) ? 1 : 0;
                }
            }
        }
        return projectedPotential;
    }
    
    @Override public Potential copy() {
        return new LinearCombinationPotential(this);
    }
    
    @Override public void scalePotential(double scale) {
        // Multiply all the coefficients by the scale
        for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] *= scale;
        }
        
    }
    
    @Override public Potential addVariable(Variable variable) {
        LinearCombinationPotential newPotential;
        if (!variables.contains(variable)) {
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.add(variable);
            newPotential = new LinearCombinationPotential(newVariables, this.role);
            VariableExpression[] newCovariates = new VariableExpression[covariates.length + 1];
            for (int i = 0; i < covariates.length; ++i) {
                newCovariates[i] = covariates[i];
            }
            newCovariates[covariates.length] = new VariableExpression(newVariables, "{" + variable.getName() + "}");
            newPotential.setCovariates(newCovariates);
            
            double[] newCoefficients = new double[coefficients.length + 1];
            for (int i = 0; i < coefficients.length; ++i)
                newCoefficients[i] = coefficients[i];
            newCoefficients[coefficients.length] = 0.0;
            newPotential.setCoefficients(newCoefficients);
        } else {
            newPotential = new LinearCombinationPotential(this);
        }
        return newPotential;
        
    }
    
    @Override public Potential removeVariable(Variable variable) {
        LinearCombinationPotential newPotential;
        if (variables.contains(variable)) {
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.remove(variable);
            newPotential = new LinearCombinationPotential(newVariables, this.role);
            List<String> newCovariates = new ArrayList<>();
            List<Double> newCoefficients = new ArrayList<>();
            
            double[] newCoefficientsArray = new double[newCoefficients.size()];
            for (int i = 0; i < newCoefficients.size(); ++i) {
                newCoefficientsArray[i] = newCoefficients.get(i);
            }
            
            /// TODO: New potential should have the covariates.
            //newPotential.setCovariates(newCovariatesArray);
            newPotential.setCoefficients(newCoefficientsArray);
        } else {
            newPotential = new LinearCombinationPotential(this);
        }
        return newPotential;
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        return super.deepCopy(copyNet);
    }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder(super.toString() + " = ");
        VariableExpression[] covariates = this.covariates;
        boolean first = true;
        for (int i = 0; i < covariates.length; ++i) {
            if (this.coefficients[i] != 0.0) {
                if (!first)
                    sb.append(" + ");
                first = false;
                if (this.coefficients[i] != 1.0)
                    sb.append(this.coefficients[i] + "*");
                sb.append(covariates[i]);
            }
        }
        return sb.toString();
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
