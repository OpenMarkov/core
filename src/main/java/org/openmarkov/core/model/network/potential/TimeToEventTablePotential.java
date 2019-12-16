package org.openmarkov.core.model.network.potential;

import cern.jet.random.engine.RandomEngine;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A <code>EventTablePotential</code> is a type of relation with a list of
 * probabilistic nodes.
 *  Transition class to be merged with the new structure of tables
 * There have to be at least one Event variable
 * @version 1.0 -24/03/2019- -cyago -
 * @version 1.1 -13/09/2019  -cyago- inheritance from TransitionTablePotential changed for assotiation
 * @version 1.2 -14/12/2019 -cyago- using nodes with continuous variables as parameterVariable
 * @since OpenMarkov 3.0
*/


@PotentialType(family ="Event", name = "TimeToEventTable")
public class TimeToEventTablePotential extends Potential implements ImpossibleConfiguration, TimeToEvent {

    private String distributionName;
    private String[] distributionParameters;

    //Variable in which each state is one parameter of the distribution
    private Variable distributionVariable;

    //Array of parents with continuous variables. Empty if there is not continuous variables
    private ArrayList<Variable> functionVariables;


    private TableWithEvents tableWithEvents;
    RandomEngine random= RandomEngine.makeDefault();

    public TimeToEventTablePotential(List<Variable> variables, PotentialRole role){
            this(variables,role, "Exact");

    }

    /**
     *
     * @param variables
     * @param role
     * @param distributionName
     */
    public TimeToEventTablePotential(List<Variable> variables, PotentialRole role, String distributionName){
        super(variables,role);
        changeDistribution(distributionName);
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
        // Using continuous variables as parameters
        //return (variableSuitable && eventSuitable);
        return eventSuitable;
    }


    public void changeDistribution(String distributionName){
        this.distributionName= distributionName;
        distributionParameters=  ProbDensFunctionManager.getUniqueInstance().getParameters(distributionName);
        State[] states = new State[distributionParameters.length];
        for (int i=0; i< distributionParameters.length;i++){
            states[i] = new State(distributionParameters[i]);
        }
        setDistributionVariable(new Variable("distributionVariable",states));
        List<Variable> tableParents= new ArrayList<Variable>();
        tableParents.add(getDistributionVariable());
        tableParents.addAll(variables.subList(1,variables.size()));
        tableParents.removeIf(v ->v.getVariableType() == VariableType.NUMERIC );
        functionVariables = new ArrayList<>();
        functionVariables.addAll(variables);
        functionVariables.removeIf(v ->v.getVariableType() != VariableType.NUMERIC );
        this.tableWithEvents = new TableWithEvents(tableParents,role, (functionVariables.size()>0) );

    }

    /**
     * True if this instance of TimeToEventTablePotential values may have functions as a value in a cell
     * @return
     */
    public boolean hasFunctionValues(){
        return !(functionVariables.isEmpty());
    }


    @Override
    public double getTimeToEvent(Variable stateVariable, State stateValue, String event) {
        return 0;
    }


    @Override
    public double getTimeToEvent(Configuration eC, Random random) {
        return getTimeToEvent(eC.convertToEvidenceCase(), random);
    }

    public double getTimeToEvent(EvidenceCase ev, Random random) {
        ProbDensFunction distribution=null;
        int i=0;
        double[] paramValues = new double[distributionParameters.length];
        try {
            for (State sParam: getDistributionVariable().getStates())
            {  Finding f = new Finding(getDistributionVariable(),sParam);
                ev.addFinding(f);
                paramValues[i++] = tableWithEvents.getTablePotential().getValue(ev);
            }

            distribution = ProbDensFunctionManager.getUniqueInstance().getProbDensFunctionClass(distributionName).newInstance();
            distribution.setParameters(paramValues);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }    catch (InvalidStateException e) {
            e.printStackTrace();
        } catch (IncompatibleEvidenceException e) {
            e.printStackTrace();
        }

        return distribution.getSample(random);
    }









    public TableWithEvents getTableWithEvents() {
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

    public Variable getDistributionVariable() {
        return distributionVariable;
    }

    public void setDistributionVariable(Variable distributionVariable) {
        this.distributionVariable = distributionVariable;
    }

    public ArrayList<Variable> getFunctionVariables() {
        return functionVariables;
    }

    public void setFunctionVariables(ArrayList<Variable> functionVariables) {
        this.functionVariables = functionVariables;
    }
}

