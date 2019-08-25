package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
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


@PotentialType(family ="Event", name = "TimeToEventTable")
public class TimeToEventTablePotential extends TransitionTablePotential implements TimeToEventPotentialInterface {

    protected TablePotential tablePotential;

    protected Variable eventAsStates;

    /**
     * Configurations of parents when this event cannot occur
     */
    List<EvidenceCase> impossibleConfigurations;


    public TimeToEventTablePotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        impossibleConfigurations = new ArrayList<EvidenceCase>();
    }


    //TODO
    public TimeToEventTablePotential(TimeToEventTablePotential potential) {
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

    /**
     * Returns true is this event can happen with the current configuration (stateValue, previous event)
     * @return true if the event is possible with the stateValue and the event w
     */
    public boolean eventPossible(Variable stateVariable, State stateValue, String event){
        Finding findingState = new Finding(stateVariable,stateValue);
        Finding findingEvent;
        try {
            findingEvent = new Finding(this.eventAsStates, eventAsStates.getState(event));
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public double getTimeToEvent(Variable stateVariable, State stateValue, String event) {
        Finding finding = new Finding(stateVariable,stateValue);

        return 0;
    }


    //TODO
    @Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException, WrongCriterionException {
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }

    //TODO
    @Override public TimeToEventTablePotential project(EvidenceCase evidenceCase)
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
        return new TimeToEventTablePotential(this);
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

