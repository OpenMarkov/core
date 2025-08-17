/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for TablePotential
 */
@PotentialType(name = "Exact") public class ExactDistrPotential extends Potential {
    
    
    // Attributes
    private TablePotential tablePotential;
    
    // Constructors
    public ExactDistrPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        if (this.role == null) {
            this.role = PotentialRole.CONDITIONAL_PROBABILITY;
        }
        tablePotential = new TablePotential(variables.subList(1, variables.size()), PotentialRole.UNSPECIFIED);
    }
    
    public ExactDistrPotential(List<Variable> variables) {
        this(variables, PotentialRole.CONDITIONAL_PROBABILITY);
    }
    
    public ExactDistrPotential(List<Variable> variables, PotentialRole role, double[] table) {
        this(variables, role);
        this.tablePotential.setValues(table);
    }
    
    public ExactDistrPotential(ExactDistrPotential potential) {
        super(potential);
        this.tablePotential = new TablePotential(potential.getTablePotential());
    }
    
    // Methods
    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions) throws NonProjectablePotentialException {
        // get the projected TablePotential, which will be returned inside a list
        List<TablePotential> projectedPotentials = tablePotential.tableProject(evidenceCase, inferenceOptions);
        projectedPotentials.get(0).setCriterion(getChildVariable().getDecisionCriterion());
        projectedPotentials.get(0).setPotentialRole(PotentialRole.UNSPECIFIED);
        return projectedPotentials;
    }
    
    @Override public ExactDistrPotential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
        List<TablePotential> projectedPotentials = tablePotential.tableProject(evidenceCase, null);
        List<Variable> newVariables = new ArrayList<>();
        newVariables.add(variables.get(0));
        newVariables.addAll(projectedPotentials.get(0).getVariables());
        ExactDistrPotential exactDistrPotential = new ExactDistrPotential(newVariables, PotentialRole.UNSPECIFIED);
        exactDistrPotential.setTablePotential(projectedPotentials.get(0));
        return exactDistrPotential;
    }
    
    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) {
        // get the projected TablePotential, which will be returned inside a list
        List<TablePotential> projectedPotentials = tablePotential
                .tableProject(evidenceCase, inferenceOptions, alreadyProjectedPotentials);
        projectedPotentials.get(0).setCriterion(getChildVariable().getDecisionCriterion());
        projectedPotentials.get(0).setPotentialRole(PotentialRole.UNSPECIFIED);
        return projectedPotentials;
    }
    
    @Override public Potential sample() {
        ExactDistrPotential sampled = (ExactDistrPotential) copy();
        sampled.tablePotential = (TablePotential) tablePotential.sample(true);
        return sampled;
    }
    
    @Override public Potential copy() {
        return new ExactDistrPotential(this);
    }
    
    @Override public boolean isUncertain() {
        return this.tablePotential.isUncertain();
    }
    
    @Override public void scalePotential(double scale) {
        this.tablePotential.scalePotential(scale);
    }
    
    public TablePotential getTablePotential() {
        return tablePotential;
    }
    
    public void setTablePotential(TablePotential tablePotential) {
        this.tablePotential = tablePotential;
    }
    
    public Variable getChildVariable() {
        return this.getVariable(0);
    }
    
    public void setChildVariable(Variable childVariable) {
        this.getVariables().set(0, childVariable);
    }
    
    public UncertainValue[] getUncertainValues() {
        return tablePotential.getUncertainValues();
    }
    
    public void setUncertainValues(UncertainValue[] uncertainValues) {
        tablePotential.setUncertainValues(uncertainValues);
    }
    
    public double[] getValues() {
        return tablePotential.getValues();
    }
    
    public void setValues(double[] values) {
        this.tablePotential.values = values;
    }
    
    @Override public List<Variable> getVariables() {
        return variables;
    }
    
    @Override public void setVariables(List<Variable> variables) {
        super.setVariables(variables);
        this.tablePotential.setVariables(variables.subList(1, variables.size()));
    }
    
    @Override public void setComment(String comment) {
        super.setComment(comment);
        this.tablePotential.setComment(comment);
    }
    
    @Override public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(variables.get(0).getName());
        if (variables.size() == 1) {
            buffer.append(" = ");
        } else if (variables.size() > 1) {
            buffer.append(" | ");
            // Print variables
            for (int i = 1; i < variables.size() - 1; i++) {
                buffer.append(variables.get(i));
                buffer.append(", ");
            }
            buffer.append(variables.get(variables.size() - 1));
            buffer.append(" = ");
        }
        
        if (tablePotential.values.length == 1) {
            buffer.append(tablePotential.values[0]);
        } else if (tablePotential.values.length > 1) {
            buffer.append("{");
            for (int i = 0; i < tablePotential.values.length; i++) {
                buffer.append(tablePotential.values[i]);
                if (i != tablePotential.values.length - 1) {
                    buffer.append(",");
                }
            }
            buffer.append("}");
        }
        // Comment these lines to fix issue 477: Wrong text in Tree/ADD potentials
		/*
		buffer.append("\n Role: " + this.getPotentialRole());
		if (criterion != null) {
			buffer.append("\n Criterion: " + criterion.toString());
		}*/
        return buffer.toString();
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given
     * the variables and the potential role.
     *
     * @param node      {@code Node}
     * @param variables {@code ArrayList} of {@code Variable}.
     * @param role      {@code PotentialRole}.
     *
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        List<Variable> parents = variables.subList(1, variables.size());
        boolean isValid = node.getNodeType() != NodeType.CHANCE || node.getVariable()
                                                                       .getVariableType() == VariableType.NUMERIC || parents.stream()
                                                                                                                            .anyMatch(parent -> parent.getVariableType() == VariableType.NUMERIC);
        return isValid;
    }
    
    
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        TablePotential auxPotential = (TablePotential) getTablePotential().reorder(newOrderOfVariables);
        List<Variable> newPotentialVariables = new ArrayList<>();
        newPotentialVariables.add(getVariables().get(0));
        newPotentialVariables.addAll(newOrderOfVariables);
        ExactDistrPotential potential = new ExactDistrPotential(newPotentialVariables, getPotentialRole());
        potential.setTablePotential(auxPotential);
        return potential;
    }
    
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        ExactDistrPotential copyPotential = (ExactDistrPotential) copy();
        copyPotential.setTablePotential(copyPotential.tablePotential.reorder(variable, newOrder));
        return copyPotential;
    }
    
}
