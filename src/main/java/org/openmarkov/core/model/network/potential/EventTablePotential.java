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


@PotentialType(family ="Event", name = "EventTable")
public class EventTablePotential extends Potential {

    protected TablePotential tablePotential;

    protected Variable eventAsStates;

    public EventTablePotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        List<Variable> parents = variables.subList(1,variables.size());

        setEventAsStates(parents);
        ArrayList<Variable> tablePotentialVariables=new ArrayList<>();
        tablePotentialVariables.add(variables.get(0));

        for (Variable variable:parents) {
            if (variable.getVariableType()!= VariableType.EVENT)
                    tablePotentialVariables.add(variable);
        }
        if (eventAsStates !=null) {
            tablePotentialVariables.add(eventAsStates);
        }
        setTablePotential(new TablePotential(tablePotentialVariables,role));

    }

//    public EventTablePotential(List<Variable> variables, PotentialRole role, double[] table) {
//        this(variables, role);
//        this.tablePotential.setValues(table);
//    }

    //TODO
    public EventTablePotential(EventTablePotential potential) {
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

        boolean eventSuitable = false;
        boolean variableSuitable= variables.get(0).getVariableType()==VariableType.FINITE_STATES;
        //I'm supposing variable(0) always contains the node variable.
        for (Variable variable:variables.subList(1,variables.size())) {
            boolean isEvent= node.getProbNet().getNode(variable).getNodeType() == NodeType.EVENT;
            if (isEvent) {
                eventSuitable= true;
            } else {
                variableSuitable &= variable.getVariableType() == VariableType.FINITE_STATES
                        || variable.getVariableType() == VariableType.DISCRETIZED;
            }
        }
        return (variableSuitable && eventSuitable);
    }


    public Variable getEventAsStates() {
        return eventAsStates;
    }

    public void setEventAsStates(Variable eventAsStates) {
        this.eventAsStates = eventAsStates;
    }

    /**
     * Extract the event parents and fill the states of eventAsStates with its names. For example,
     * if node A has as parents events E1, E2 and E3 eventAsStates will be a Finite-States variable with
     * three states: E1, E2, E3
     * @param variables
     */
    public void setEventAsStates(List<Variable> variables) {
        ArrayList<State> states = new ArrayList<>();
        int i=0;
        for (Variable variable:variables) {
            if (variable.getVariableType()==VariableType.EVENT) {
                states.add(new State(variable.getName()));
            }
        }
        eventAsStates = null;
        if (!states.isEmpty()) {
            eventAsStates = new Variable("Events", states.toArray(new State[0]));
        }
    }




    //TODO
    @Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException, WrongCriterionException {
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }

    //TODO
    @Override public EventTablePotential project(EvidenceCase evidenceCase)
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
        return new EventTablePotential(this);
    }

    //TODO
    @Override public boolean isUncertain() {
        return false;
    }

    //TODO
    @Override public void scalePotential(double scale) {
        this.getTablePotential().scalePotential(scale);
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
        return getTablePotential().getUncertainValues();
    }

    public void setUncertainValues(UncertainValue[] uncertainValues) {
        getTablePotential().setUncertainValues(uncertainValues);
    }

    public double[] getValues() {
        return getTablePotential().getValues();
    }

    public void setValues(double[] values) {
        this.getTablePotential().values = values;
    }

    @Override public List<Variable> getVariables() {
        return variables;
    }

    //TODO
    @Override public void setVariables(List<Variable> variables) {
        super.setVariables(variables);
        this.getTablePotential().setVariables(variables.subList(1, variables.size()));
    }
    //TODO
    @Override public void setComment(String comment) {
        super.setComment(comment);
        this.getTablePotential().setComment(comment);
    }

    //TODO
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

        if (getTablePotential().values.length == 1) {
            buffer.append(getTablePotential().values[0]);
        } else if (getTablePotential().values.length > 1) {
            buffer.append("{");
            for (int i = 0; i < getTablePotential().values.length; i++) {
                buffer.append(getTablePotential().values[i]);
                if (i != getTablePotential().values.length - 1) {
                    buffer.append(",");
                }
            }
            buffer.append("}");
        }
        buffer.append("\n Role: " + this.getPotentialRole());
        buffer.append("\n Criterion: " + ((criterion == null) ? "null" : criterion.toString()));
        return buffer.toString();
    }
}

