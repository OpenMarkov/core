package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement a table with Events in the configuration of parents
 * @version 1.0 -14/00/2019- -cyago -
 *
 * @since OpenMarkov 3.0
*/


@PotentialType(family ="Event", name = "TableWithEvents")
public class TableWithEventsPotential extends Potential implements ImpossibleConfiguration {

    protected TablePotential tablePotential;

    protected Variable events;

    //Imcompatible configurations. Looking the better way of representing them.
    protected boolean hasImpossibleConfigurations;
    protected ArrayList<Configuration> impossibleConfigurations;



    /**
     *
     * @param variables
     * @param role
     */
    public TableWithEventsPotential(List<Variable> variables, PotentialRole role) {

        super(variables, role);
        List<Variable> parents = variables.subList(1,variables.size());
        setEventAsStates(parents);
        ArrayList<Variable> tablePotentialVariables=new ArrayList<>();
        tablePotentialVariables.add(variables.get(0));

        for (Variable variable:parents) {
            if (variable.getVariableType()!= VariableType.EVENT)
                    tablePotentialVariables.add(variable);
        }
        if (events !=null) {
            tablePotentialVariables.add(events);
        }
        setTablePotential(new TablePotential(tablePotentialVariables,role));
        impossibleConfigurations = new ArrayList<>();

    }

//    public EventTablePotential(List<Variable> variables, PotentialRole role, double[] table) {
//        this(variables, role);
//        this.tablePotential.setValues(table);
//    }

    //TODO
    public TableWithEventsPotential(TableWithEventsPotential potential) {
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

      return false;
    }


    public Variable getEvents() {
        return events;
    }

    public void setEvents(Variable events) {
        this.events = events;
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
        events = null;
        if (!states.isEmpty()) {
            events = new Variable("Events", states.toArray(new State[0]));
        }
    }

    @Override
    public void addImpossibleConfiguration(EvidenceCase eCiC){
        addImpossibleConfiguration(new Configuration(eCiC));
    }

    @Override
    public void addImpossibleConfiguration(Configuration configuration) {
        getImpossibleConfigurations().add(configuration);
    }

    @Override
    public void removeImpossibleConfiguration(EvidenceCase configuration) {
        removeImpossibleConfiguration(new Configuration(configuration));
    }

    @Override
    public void removeImpossibleConfiguration(Configuration configuration) {
        impossibleConfigurations.removeIf(configuration1 ->configuration1.equals(configuration));
    }

    @Override
    public boolean isImpossibleConfiguration(EvidenceCase configuration) {

        return  isImpossibleConfiguration( new Configuration(configuration));
    }

    @Override
    public boolean hasImpossibleConfiguration(){
        boolean hasIC = !(impossibleConfigurations.isEmpty());
        return hasIC;
    }

    @Override
    public boolean isImpossibleConfiguration(Configuration configuration) {
        boolean isThere = getImpossibleConfigurations().contains(configuration);
        return isThere;
    }






    //TODO
    @Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException, WrongCriterionException {
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }

    //TODO
    @Override public TableWithEventsPotential project(EvidenceCase evidenceCase)
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
        return new TableWithEventsPotential(this);
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


    /**
     * Combination of state_value and events that cannot be possible
     */
    public ArrayList<Configuration> getImpossibleConfigurations() {
        return impossibleConfigurations;
    }

    public void setImpossibleConfigurations(ArrayList<Configuration> impossibleConfigurations) {
        this.impossibleConfigurations = impossibleConfigurations;
    }


}

