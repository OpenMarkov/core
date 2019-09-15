package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>EventTablePotential</code> is a type of relation with a list of
 * probabilistic nodes.
 *  Transition class to be merged with the new structure of tables
 * There have to be at least one Event variable
 * @version 1.0 -24/03/2019- -cyago -
 * @version 1.1 -13/09/2019  -cyago- inheritance from TransitionTablePotential changed for assotiation
 * @since OpenMarkov 3.0
*/


@PotentialType(family ="Event", name = "TimeToEventTable")
public class TimeToEventTablePotential extends Potential implements ImpossibleConfiguration, TimeToEvent {

    private ProbDensFunction distribution;
    private String distributionName;
    private Variable distributionVariable;
    private TransitionTablePotential tableWithEvents;

    public TimeToEventTablePotential(List<Variable> variables, PotentialRole role){
        this(variables,role, "Exact");

    }


    public TimeToEventTablePotential(List<Variable> variables, PotentialRole role, String distributionName){
        super(variables,role);
        this.setDistributionName(distributionName);
        try {
          distribution =  ProbDensFunctionManager.getUniqueInstance().getProbDensFunctionClass(distributionName).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String[] s=  ProbDensFunctionManager.getUniqueInstance().getParameters(distributionName);
        State[] states = new State[s.length];
        for (int i=0; i< s.length;i++){
            states[i] = new State(s[i]);
        }
        distributionVariable = new Variable("distributionVariable",states);
        List<Variable> v= new ArrayList<Variable>();
        v.add(distributionVariable);
        v.addAll(variables.subList(1,variables.size()));
        this.tableWithEvents = new TransitionTablePotential(v,role);
    }

    //TODO
    public TimeToEventTablePotential(TimeToEventTablePotential potential) {
        super(potential);

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


    public void changeDistribution(String distributionName){
        this.distributionName= distributionName;
        try {
            distribution =  ProbDensFunctionManager.getUniqueInstance().getProbDensFunctionClass(distributionName).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String[] s=  ProbDensFunctionManager.getUniqueInstance().getParameters(distributionName);
        State[] states = new State[s.length];
        for (int i=0; i< s.length;i++){
            states[i] = new State(s[i]);
        }
        distributionVariable = new Variable("distributionVariable",states);
        List<Variable> v= new ArrayList<Variable>();
        v.add(distributionVariable);
        v.addAll(variables.subList(1,variables.size()));
        this.tableWithEvents = new TransitionTablePotential(v,role);
    }


    @Override
    public double getTimeToEvent(Variable stateVariable, State stateValue, String event) {
        return 0;
    }


    /**
     * @param configuration
     * @return
     */
    @Override
    public double getTimeToEvent(Configuration configuration) {
         return 0;
    }

    @Override
    public double getTimeToEvent(EvidenceCase ev) {
        return 0;
    }

    public TableWithEventsPotential getTableWithEvents() {
        return tableWithEvents;
    }

    public void setTableWithEvents(TransitionTablePotential tableWithEvents) {
        this.tableWithEvents = tableWithEvents;
    }

    @Override
    public void addImpossibleConfiguration(EvidenceCase configuration) {
            addImpossibleConfiguration(new Configuration(configuration));
    }

    @Override
    public void addImpossibleConfiguration(Configuration configuration) {
        tableWithEvents.addImpossibleConfiguration(configuration);
    }

    @Override
    public void removeImpossibleConfiguration(EvidenceCase configuration) {
        removeImpossibleConfiguration(new Configuration(configuration));

    }

    @Override
    public void removeImpossibleConfiguration(Configuration configuration) {
        tableWithEvents.removeImpossibleConfiguration(configuration);
    }

    @Override
    public boolean hasImpossibleConfiguration() {
        return  tableWithEvents.hasImpossibleConfiguration();
    }

    @Override
    public boolean isImpossibleConfiguration(EvidenceCase configuration) {
        return tableWithEvents.isImpossibleConfiguration(configuration);
    }

    @Override
    public boolean isImpossibleConfiguration(Configuration configuration) {
        return tableWithEvents.isImpossibleConfiguration(configuration);
    }

    @Override
    public ArrayList<Configuration> getImpossibleConfigurations() {
        return tableWithEvents.getImpossibleConfigurations();
    }

    @Override
    public void setImpossibleConfigurations(ArrayList<Configuration> impossibleConfigurations) {
        tableWithEvents.setImpossibleConfigurations(impossibleConfigurations);
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
    }


    public String getDistributionName() {
        return distributionName;
    }

    public void setDistributionName(String distributionName) {
        this.distributionName = distributionName;
    }
}

