package org.openmarkov.core.model.network.potential.treeadd;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@PotentialType(name = "Tree/ADDDelta", family = "Tree")
public class TreeADDDeltaPotential extends Potential {

    private TreeADDPotential treeADDPotential;

    private Variable childVariable;

    /**
     * Copy constructor
     *
     * @param treeADDDelta
     */
    public TreeADDDeltaPotential(TreeADDDeltaPotential treeADDDelta) {
        super(treeADDDelta);
        this.treeADDPotential = new TreeADDPotential(treeADDDelta.getTreeADDPotential());
        this.childVariable = treeADDDelta.getChildVariable();
    }

    /**
     * Constructor for the parser
     */
    public TreeADDDeltaPotential(List<Variable> variables, Variable topVariable, PotentialRole role,
                            List<TreeADDBranch> branches) {
        super(variables, role);
        this.childVariable = variables.remove(0);
        this.treeADDPotential = new TreeADDPotential(variables, topVariable, role, branches);
    }

    public TreeADDDeltaPotential(List<Variable> variables, PotentialRole role) {
        this(variables, variables.get(1), role);
    }

    public TreeADDDeltaPotential(List<Variable> variables, Variable topVariable, PotentialRole role) {
        this(variables, topVariable, topVariable.getStates(), topVariable.getPartitionedInterval(), role);
    }

    public TreeADDDeltaPotential(List<Variable> variables, Variable topVariable,
                            State[] branchingStates, PartitionedInterval interval, PotentialRole role) {
        super(variables, role);
        if(this.role == null) {
            this.role = PotentialRole.CONDITIONAL_PROBABILITY;
        }
        childVariable = this.variables.get(0);
        treeADDPotential = new TreeADDPotential(variables.subList(1, variables.size()), topVariable, branchingStates, interval, role);
    }

    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException, WrongCriterionException {
        List<TablePotential> projectedPotentials = this.treeADDPotential.tableProject(evidenceCase, inferenceOptions, alreadyProjectedPotentials);
        projectedPotentials.get(0).setCriterion(childVariable.getDecisionCriterion());
        projectedPotentials.get(0).setPotentialRole(PotentialRole.UTIL_2);
        return projectedPotentials;
    }

    @Override
    public Potential copy() {
        return new TreeADDDeltaPotential(this);
    }

    @Override
    public boolean isUncertain() {
        return false;
    }

    @Override
    public void scalePotential(double scale) {
        treeADDPotential.scalePotential(scale);
    }

    public TreeADDPotential getTreeADDPotential() {
        return treeADDPotential;
    }

    public void setTreeADDPotential(TreeADDPotential treeADDPotential) {
        this.treeADDPotential = treeADDPotential;
    }

    public Variable getChildVariable() {
        return childVariable;
    }

    public void setChildVariable(Variable childVariable) {
        this.childVariable = childVariable;
    }

    @Override
    public void replaceVariable(int position, Variable variable) {
        super.replaceVariable(position, variable);
        treeADDPotential.replaceVariable(position, variable);
    }

    @Override
    public void replaceNumericVariable(Variable convertedParentVariable) {
        super.replaceNumericVariable(convertedParentVariable);
        treeADDPotential.replaceNumericVariable(convertedParentVariable);
    }

    @Override
    public void setComment(String comment) {
        super.setComment(comment);
        this.treeADDPotential.setComment(comment);
    }

    public List<TreeADDBranch> getBranches() {
        return treeADDPotential.getBranches();
    }
}
