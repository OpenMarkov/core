package org.openmarkov.core.model.network.potential.treeadd;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.List;

@PotentialType(name = "Tree/ADDDelta", family = "Tree")
public class TreeADDDeltaPotential extends Potential {

    private TreeADDPotential treeADDPotential;

    private Variable childVariable;

    /**
     * Constructor for the parser
     */
    public TreeADDDeltaPotential(List<Variable> variables, Variable topVariable, PotentialRole role,
                            List<TreeADDBranch> branches) {
        super(variables, role);
        this.childVariable = variables.remove(0);
        this.treeADDPotential = new TreeADDPotential(variables, topVariable, role, branches);
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
}
