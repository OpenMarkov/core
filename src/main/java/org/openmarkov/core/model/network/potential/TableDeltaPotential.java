package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.List;

@PotentialType(name = "TableDelta")
public class TableDeltaPotential extends Potential {
	
    private TablePotential tablePotential;

    private Variable childVariable;

    public TableDeltaPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        if(this.role == null) {
            this.role = PotentialRole.CONDITIONAL_PROBABILITY;
        }
        childVariable = this.variables.remove(0);
        tablePotential = new TablePotential(variables, PotentialRole.UNSPECIFIED);
    }

    public TableDeltaPotential(List<Variable> variables) {
        this(variables, PotentialRole.CONDITIONAL_PROBABILITY);
    }

    public TableDeltaPotential(List<Variable> variables, PotentialRole role, double[] table) {
        super(variables, role);
        if(this.role == null) {
            this.role = PotentialRole.CONDITIONAL_PROBABILITY;
        }
        childVariable = variables.remove(0);
        tablePotential = new TablePotential(variables, PotentialRole.UNSPECIFIED, table);
    }

    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions) throws NonProjectablePotentialException, WrongCriterionException {
        tablePotential.setCriterion(childVariable.getDecisionCriterion());
        tablePotential.setPotentialRole(PotentialRole.UTIL_2);
        List<TablePotential> projectedPotentials = tablePotential.tableProject(evidenceCase, inferenceOptions);
        return projectedPotentials;
    }

    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException, WrongCriterionException {
        tablePotential.setCriterion(childVariable.getDecisionCriterion());
        tablePotential.setPotentialRole(PotentialRole.UTIL_2);
        List<TablePotential> projectedPotentials = tablePotential.tableProject(evidenceCase, inferenceOptions, alreadyProjectedPotentials);
        return projectedPotentials;
    }

    @Override
    public Potential copy() {
        return null;
    }

    @Override
    public boolean isUncertain() {
        return false;
    }

    @Override
    public void scalePotential(double scale) {

    }

    public TablePotential getTablePotential() {
        return tablePotential;
    }

    public void setTablePotential(TablePotential tablePotential) {
        this.tablePotential = tablePotential;
    }

    public Variable getChildVariable() {
        return childVariable;
    }

    public void setChildVariable(Variable childVariable) {
        this.childVariable = childVariable;
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
    public void setComment(String comment) {
        super.setComment(comment);
        this.tablePotential.setComment(comment);
    }
}
