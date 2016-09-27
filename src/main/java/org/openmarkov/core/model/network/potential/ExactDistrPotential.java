package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;

@PotentialType(name = "Exact")
public class ExactDistrPotential extends Potential {
	
    private TablePotential tablePotential;

    public ExactDistrPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        if(this.role == null) {
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

    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions) throws NonProjectablePotentialException, WrongCriterionException {
        // get the projected TablePotential, which will be returned inside a list
        List<TablePotential> projectedPotentials = tablePotential.tableProject(evidenceCase, inferenceOptions);
        projectedPotentials.get(0).setCriterion(getChildVariable().getDecisionCriterion());
        projectedPotentials.get(0).setPotentialRole(PotentialRole.UTIL_2);
        return projectedPotentials;
    }

    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException, WrongCriterionException {
        // get the projected TablePotential, which will be returned inside a list
        List<TablePotential> projectedPotentials = tablePotential.tableProject(evidenceCase, inferenceOptions, alreadyProjectedPotentials);
        projectedPotentials.get(0).setCriterion(getChildVariable().getDecisionCriterion());
        projectedPotentials.get(0).setPotentialRole(PotentialRole.UTIL_2);
        return projectedPotentials;
    }

    @Override
    public Potential copy() {
        return new ExactDistrPotential(this);
    }

    @Override
    public boolean isUncertain() {
        return false;
    }

    @Override
    public void scalePotential(double scale) {
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

    public void setUncertainValues(UncertainValue[] uncertainValues) {
        tablePotential.setUncertainValues(uncertainValues);
    }

    public UncertainValue[] getUncertainValues () {
        return tablePotential.getUncertainValues();
    }

    public void setValues (double[] values) {
        this.tablePotential.values = values;
    }

    public double[] getValues() {
        return tablePotential.getValues();
    }

    @Override
    public List<Variable> getVariables() {
        return variables;
    }

    @Override
    public void setComment(String comment) {
        super.setComment(comment);
        this.tablePotential.setComment(comment);
    }

    @Override
    public void setVariables(List<Variable> variables) {
        super.setVariables(variables);
        this.tablePotential.setVariables(variables.subList(1, variables.size()));
    }
}
