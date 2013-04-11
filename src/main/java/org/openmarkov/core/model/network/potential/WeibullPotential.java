/*
 * Copyright 2013 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
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
    /**
     * Relative risk 
     */
    private double relativeRisk;    
    

    public WeibullPotential(List<Variable> variables, PotentialRole role,
            List<Double> coefficients, double gamma, double constant, double relativeRisk) {
        super(variables, role);
        this.coefficients = coefficients;
        this.gamma = gamma;
        this.constant = constant;
        this.relativeRisk = relativeRisk;
    }

    public WeibullPotential(List<Variable> variables, PotentialRole role) {
        this(variables, role, new ArrayList<Double>(variables.size()), 0.0, 0.0, 1.0);
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
                evidencelessVariablesIndex.add(i - 1);
                numericValues.add(0.0);
            } else {
                if(variable.isTemporal())
                {
                    numericValues.add(evidenceCase.getFinding(variable).getNumericalValue() - variable.getTimeSlice());
                }else
                {
                    numericValues.add(evidenceCase.getFinding(variable).getNumericalValue());
                }
            }
        }

        List<Variable> projectedPotentialVariables = new ArrayList<>(evidencelessVariables);
        projectedPotentialVariables.add(0,variables.get(0));
        TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, role);
        int[] offsets = projectedPotential.getOffsets();
        int timeSlice = variables.get(0).getTimeSlice();

        for (int i = 0; i < projectedPotential.values.length; i += 2) {
            // Set the values of variables of variables without evidence
            for (int j = 1; j < projectedPotentialVariables.size(); ++j) {
                int index = (j + 1 < projectedPotentialVariables.size()) ? (i/offsets[j]) % offsets[j + 1] : i/offsets[j];
                numericValues.set(evidencelessVariablesIndex.get(j-1), (double) index);
            }
            double lambda = constant;
            for (int j = 0; j < numericValues.size(); ++j) {
                lambda += numericValues.get(j) * coefficients.get(j);
            }
            lambda = Math.exp(lambda);
            lambda *= relativeRisk;
            if (timeSlice == 0) {
                // p
                projectedPotential.values[i] = 1;
                // Complement (1-p)
                projectedPotential.values[i+1] = 0;
            } else {
                // p
                projectedPotential.values[i] = Math.exp(lambda
                        * (Math.pow(timeSlice - 1, gamma) - Math.pow(timeSlice, gamma)));
                // Complement (1-p)
                projectedPotential.values[i+1] = 1 - projectedPotential.values[i];
            }
        }

        List<TablePotential> projectedPotentials = new ArrayList<>();
        projectedPotentials.add(projectedPotential);
        return projectedPotentials;
    }

    @Override
    public Potential shift(ProbNet probNet, int timeDifference) throws ProbNodeNotFoundException {
        return new WeibullPotential(getShiftedVariables(probNet, timeDifference), role,
                new ArrayList<>(coefficients), gamma, constant, relativeRisk);
    }

    @Override
    public Potential copy() {
        return new WeibullPotential(variables, role, new ArrayList<>(coefficients), gamma, constant, relativeRisk);
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

    public double getRelativeRisk() {
        return relativeRisk;
    }

    public void setRelativeRisk(double relativeRisk) {
        this.relativeRisk = relativeRisk;
    }

}
