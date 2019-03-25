package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>EventTablePotential</code> is a type of relation with a list of
 * probabilistic nodes.
 *  Transition class to be merged with the new structure of tables
 * There have to be at least one Event variable
 * @version 1.0 -24/03/2019- -cyago -
 * @since OpenMarkov 3.0
*/


@PotentialType(family ="Event", name = "EventTimeTable")
public class EventTimeTablePotential extends EventTablePotential {

    protected TablePotential tablePotential;

    protected Variable eventAsStates;

    public EventTimeTablePotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);

    }


    //TODO
    public EventTimeTablePotential(EventTimeTablePotential potential) {
        super(potential);
        this.setTablePotential(new TablePotential(potential.getTablePotential()));
    }



    /**
     * Returns true  if certain Potential type makes sense given the
     * variables and the potential role.
     * This potential makes sense when at least one of the parents
     * is an event and no event parents have Finite States or Discretized variables
     *
     * @param node      . <code>Node</code> where the potential is set
     * @param variables . <code>List</code> of <code>Variable</code>.
     * @param role      . <code>PotentialRole</code>.
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {

        boolean eventSuitable = variables.get(0).getVariableType() == VariableType.EVENT;
        boolean variableSuitable= true;

        //I'm supposing variable(0) always contains the node variable.
        for (Variable variable:variables.subList(1,variables.size())) {
            boolean isEvent= node.getProbNet().getNode(variable).getNodeType() == NodeType.EVENT;
            variableSuitable &= variable.getVariableType() == VariableType.FINITE_STATES
                    || variable.getVariableType() == VariableType.DISCRETIZED ||  variable.getVariableType() == VariableType.EVENT;
        }
        return (variableSuitable && eventSuitable);
    }




    //TODO
    @Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException, WrongCriterionException {
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }

    //TODO
    @Override public EventTimeTablePotential project(EvidenceCase evidenceCase)
            throws WrongCriterionException, NonProjectablePotentialException {
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }

    //TODO
    @Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                       List<TablePotential> alreadyProjectedPotentials)
            throws NonProjectablePotentialException, WrongCriterionException {
        // get the projected TablePotential, which will be returned inside a list
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }

    //TODO
    @Override public Potential copy() {
        return new EventTimeTablePotential(this);
    }

    //TODO
    @Override public boolean isUncertain() {
        return false;
    }

    //TODO
    @Override public void scalePotential(double scale) {
        this.getTablePotential().scalePotential(scale);
    }

}

