/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;

@PotentialType(name = "AugmentedProbTable") public class AugmentedProbTablePotential extends Potential {

    protected AugmentedProbTable AugmentedProbTable;
    private List<Variable> finiteStatesVariables;
    private List<Variable> parameterVariables;
    
    /*Note should be discrete variables*/
    public AugmentedProbTablePotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        setFiniteStatesVariables(new ArrayList<Variable>());
        setParameterVariables(new ArrayList<Variable>());
        getFiniteStatesVariables().add(variables.get(0));
        for (Variable variable : variables.subList(1, variables.size())) {
            if ((variable.getVariableType() == VariableType.FINITE_STATES) || (
                    variable.getVariableType() == VariableType.DISCRETIZED
            )) {
                finiteStatesVariables.add(variable);
            } else {
                parameterVariables.add(variable);
            }
        }
        
        setAugmentedProbTable(new AugmentedProbTable(getFiniteStatesVariables(), role));
    }
    
    public AugmentedProbTablePotential(AugmentedProbTablePotential AugmentedProbTablePotential) {
        this(AugmentedProbTablePotential.variables, AugmentedProbTablePotential.getPotentialRole());
        //UNCLEAR Should I copy Functions?
        this.AugmentedProbTable = new AugmentedProbTable(AugmentedProbTablePotential.getAugmentedProbTable());
    }
    
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        return (node.getVariable().getVariableType() == VariableType.FINITE_STATES);
        
    }
    
    public AugmentedProbTable getAugmentedProbTable() {
        return AugmentedProbTable;
    }
    
    public void setAugmentedProbTable(AugmentedProbTable AugmentedProbTable) {
        this.AugmentedProbTable = AugmentedProbTable;
    }
    
    /**
     * @return the finiteStatesVariables
     */
    public List<Variable> getFiniteStatesVariables() {
        return finiteStatesVariables;
    }
    
    /**
     * @param finiteStatesVariables the finiteStatesVariables to set
     */
    public void setFiniteStatesVariables(List<Variable> finiteStatesVariables) {
        this.finiteStatesVariables = finiteStatesVariables;
    }
    
    public List<Variable> getParameterVariables() {
        return parameterVariables;
    }
    
    public void setParameterVariables(List<Variable> parameterVariables) {
        this.parameterVariables = parameterVariables;
    }
    
    /**
     * Creates a functionPotential whose parents are the Numeric variables
     *
     * @param numericVariables Numeric variables
     * @param functionString   Function string
     *
     * @return a function potential whose parents are the Numeric variables
     */
    public FunctionPotential createFunctionPotential(List<Variable> numericVariables, String functionString) {
        return new FunctionPotential(numericVariables, this.role, functionString);
    }
    
    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Potential project(EvidenceCase evidenceCase) throws NotSupportedOperationException {
        throw new NotSupportedOperationException();
    }
    
    @Override public Potential copy() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override public boolean isUncertain() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override public void scalePotential(double scale) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        int size = newOrderOfVariables.size();
        //orderVariables has the order of the parents of the AugmentedProbTable, so parameterVariables should be added
        newOrderOfVariables.addAll(getParameterVariables());
        AugmentedProbTablePotential newPotential = new AugmentedProbTablePotential(newOrderOfVariables,
                                                                           getPotentialRole());
        AugmentedProbTable newDistributionTable = (AugmentedProbTable) getAugmentedProbTable().reorder(newOrderOfVariables.subList(0, size));
        newPotential.setAugmentedProbTable(newDistributionTable);
        return newPotential;
    }
    
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        // TODO Auto-generated method stub
        return null;
    }
    
}