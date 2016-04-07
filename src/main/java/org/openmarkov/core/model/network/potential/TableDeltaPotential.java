package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.List;

@PotentialType(name = "TableDelta")
public class TableDeltaPotential extends Potential {
    private TablePotential tablePotential;

    private Variable childVariable;

    public TableDeltaPotential(Potential potential) {
        super(potential);
    }

    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException, WrongCriterionException {
        return null;
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
}
