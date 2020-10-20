package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction.ParametrizedFunctionManager;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.openmarkov.core.model.network.NodeType.EVENT;


/**
 * Potential when the parameters of the probabilistic distribution which describe the TTE in an Event variable may vary in base of the Configuration of the parents.
 * This potential is described by the probabilistic distribution chosen and a Table where there are the values of the distribution parameters.
 * The rows of the Table represent the distribution parameters of the distribution, and may be indicated by a number or a Funcion of the parents with Numeric Variables.
 * Each configuration of the parents are formed by a state of every Chance Variable and a Event parent. There have to be at least one Event variable.
 * Parents with Numeric Variables are treated as "Function Parameters", and may be used to configure the values of the distribution parameters.
 * This potential may have impossible configuration of parents
 * TODO Rephrase.
 * @version 1.0 -24/03/2019- -cyago
 * @version 1.1 -13/09/2019  -cyago- inheritance from TransitionTablePotential changed for assotiation
 * @version 1.2 -14/12/2019 -cyago- using nodes with continuous variables as parameterVariable
 * @version 1.3 -05/01/2019 -cyago- adapted to Configuration version 1.1
 * @version 1.4 -07/10/2020 -cyago- added parametrizations
 *
*/

@PotentialType(family ="Event", name = "TimeToEventTable")
public class TimeToEventTablePotential extends Potential implements ImpossibleConfiguration {


    /**
     * Probabilistic distribution for computing TTE of every configuration
     */
    private String distributionName;

    /**
     * Parametrization of distributionName
     */
    private String parametrizationName;

    /**
     * Name of the parameters of the probabilistic distribution for computing TTE of every configuration
     */
    private String[] distributionParameters;

    /**
     * Variable in which each state is one parameter of the distribution
     */
    private Variable distributionVariable;

    /**
     * ArrayList of parents with continuous variables. Empty if there is not continuous variables
     */
    private ArrayList<Variable> numericVariables;

    /**
     * TableWithEvents which stores the values of the distribution parameters
     */
    private TableWithEvents tableWithEvents;


    /**
     * Constructor of a TimeToEventTablePotential with probabilistic distribution "Exact"
     * @param variables List of Variable whose first element is the node Variable and the rest are the Variable of the parents
     * @param role role assumed by the potential
     */
    public TimeToEventTablePotential(List<Variable> variables, PotentialRole role){
            this(variables,role,"Exact", "Nu");

    }

    /**
     * Constructor of TimeToEventTablePotential with probabilistic distribution given by distributionName
     * @param variables List of Variable whose first element is the node Variable and the rest are the Variable of the parents
     * @param role role assumed by the potential
     * @param distributionName name of the probabilistic distribution used to compute TTE of the node
     */
    public TimeToEventTablePotential(List<Variable> variables, PotentialRole role, String distributionName, String parametrizationName){
        super(variables,role);
        changeDistribution(distributionName, parametrizationName);
    }






    /**
     * Constructor of TimeToEventTablePotential which creates an object with the same values as potential.
     * @param potential TimeToEventTablePotential whose values are used to create the new object
     */
    public TimeToEventTablePotential(TimeToEventTablePotential potential) {
       this(potential.getVariables(),potential.getPotentialRole(), potential.getDistributionName(), potential.getParametrizationName());
        this.setTableWithEvents(potential.getTableWithEvents());
    }



    /**
     * Returns true if the node has type Event and one of its parents is an event
     * This potential makes sense when at least one of the parents
     * is an event.
     *
     * @param node      . <code>Node</code> where the potential is set
     * @param variables . <code>List</code> of <code>Variable</code>.
     * @param role      . <code>PotentialRole</code>.
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
//        boolean hasEventParent = node.getParents().stream().anyMatch(parent -> parent.getNodeType()==EVENT);
//        return ((node.getNodeType()== EVENT) && hasEventParent);
//When used in TreeWithEventsPotential it do not have and event parent.
        return (node.getNodeType()== EVENT);
    }


    /**
     * Changes the probabilistic distribution contained is this potential. The values of the distribution parameters are se to default.
     * @param distributionName
     * @param distributionParametrization name of the new probabilistic distribution
     */
    public void changeDistribution(String distributionName, String distributionParametrization){
        try {
            this.distributionName = distributionName;
            this.parametrizationName = distributionParametrization;
            List<String> distributionParametersList = (List<String>) ParametrizedFunctionManager.getUniqueInstance().getParameters(distributionName, distributionParametrization );
            distributionParameters = distributionParametersList.toArray(new String[distributionParametersList.size()]);

            State[] states = new State[distributionParameters.length];
            for (int i = 0; i < distributionParameters.length; i++) {
                states[i] = new State(distributionParameters[i]);
            }
            setDistributionVariable(new Variable("distributionVariable", states));
            List<Variable> tableParents = new ArrayList<Variable>();
            tableParents.add(getDistributionVariable());
            tableParents.addAll(variables.subList(1, variables.size()));
            tableParents.removeIf(v -> v.getVariableType() == VariableType.NUMERIC);
            numericVariables = new ArrayList<>();
            numericVariables.addAll(variables);
            numericVariables.removeIf(v -> v.getVariableType() != VariableType.NUMERIC);
            this.tableWithEvents = new TableWithEvents(tableParents, role, (numericVariables.size() > 0));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Returns true if this instance of TimeToEventTablePotential values may have functions as a value in a cell
     * @return true if this instance of TimeToEventTablePotential values may have functions as a value in a cell. False otherwise
     */
    public boolean hasFunctionValues(){
        return !(numericVariables.isEmpty());
    }

    /**
     * Returns the TableWithEvents object where the distribution parameters are stored
     * @return the TableWithEvents object where the distribution parameters are stored
     */
    public TableWithEvents getTableWithEvents() {
        return tableWithEvents;
    }

    /**
     * Sets a TableWithEvents with the values for the distribution parameters
     * TODO Throw an Exception if the table is not compatible with the probabilistic distribution
     * @param tableWithEvents the tableWithEvents to be stored
     */
    public void setTableWithEvents(TableWithEvents tableWithEvents) {
        this.tableWithEvents = tableWithEvents;
    }

    /**
     * Returns a String object with the name of the probabilistic distribution used for computing TTE
     * @return String with the name of the probabilistic distribution used for computing TTE
     */
    public String getDistributionName() {
        return distributionName;
    }

    /**
     * Sets the probabilistic distribution used for computing TTE to distributionName
     * @param distributionName String with the name of the distribution to be set
     */
    public void setDistributionName(String distributionName) {
        this.distributionName = distributionName;
    }

    /**
     * Returns a Variable object with the distribution parameters as states.
     * @return Variable with the distribution parameters as states
     */
    public Variable getDistributionVariable() {
        return distributionVariable;
    }

    /**
     * Sets a Variable object with the distribution parameters as states.
     * @param distributionVariable Variable to be set
     */
    public void setDistributionVariable(Variable distributionVariable) {
        this.distributionVariable = distributionVariable;
    }

    /**
     * List of Variable with the Numeric Parent Variables which can be used as function parameters
     * @return List of Variable with the Numeric Parent Variables which can be used as function parameters
     */
    public ArrayList<Variable> getNumericVariables() {
        return numericVariables;
    }

    /**
     * Sets List of Variable with the Numeric Parent Variables which can be used as function parameters.
     * @param numericVariables
     */
    public void setNumericVariables(ArrayList<Variable> numericVariables) {
        this.numericVariables = numericVariables;
    }

    @Override
    public double sampleConditionedVariable(Random randomGenerator, EvidenceCase parents) throws OpenMarkovException {
        return getTimeToEvent(randomGenerator,new Configuration(parents));
    }


    //TimeToEvent interface
    public double getTimeToEvent(Random random, Configuration configuration) {
        //Extract FINITE_STATES and EVENT Variables and convert to the format of a TableWithEvents to find the position in the table
        Configuration stateConfiguration = tableWithEvents.convert( configuration);
        //Extract Numeric Variables which are the Function Variables
        Configuration numericConfiguration= null;
        if (tableWithEvents.isUseTableWithFunctions()) {
            numericConfiguration = new Configuration();
            for (Variable numericVariable : numericVariables) {
                try {
                    Finding finding = configuration.getFinding(numericVariable);
                    numericConfiguration.addFinding(finding);
                } catch (OpenMarkovException e) {
                    e.printStackTrace();
                }
            }
        }
        //Extract parameters from the table
        ProbDensFunction distribution=null;
        int i=0;
        double[] paramValues = new double[distributionParameters.length];
        try {
            for (State sParam: distributionVariable.getStates())
            {  Finding f = new Finding(distributionVariable,sParam);

                stateConfiguration.addFinding(f);

                if (tableWithEvents.isUseTableWithFunctions()){
                     paramValues[i++] = tableWithEvents.getTableWithFunctions().getEvaluatedFunctionValue(stateConfiguration, numericConfiguration);
                }else {
                    paramValues[i++] = tableWithEvents.getTablePotential().getValue(stateConfiguration);
                }
                stateConfiguration.removeFinding(distributionVariable);
            }

            distribution = ParametrizedFunctionManager.getUniqueInstance().getParametrizedClass(distributionName,parametrizationName).newInstance();
            distribution.setParameters(paramValues);
        } catch (InstantiationException | IllegalAccessException | InvalidStateException | IncompatibleEvidenceException | NoFindingException e) {
            e.printStackTrace();
        }
        return distribution.getSample(random);
    }

//    /**
//     * Generates a TTE since the event is "triggered" by initial event, another event or change in state
//     * *  The event will happend at triggering_time + TTE
//     *
//     * @param findings one or more findings to create the Configuration for extracting TTE
//     * @param random
//     * @return
//     */
//    public double getTimeToEvent(List<Finding> findings, Random random) {
//
//        return getTimeToEvent(random, tableWithEvents.convert(findings));
//    }


    //ImpossibleConfiguration interface

    @Override
    public void addImpossibleConfiguration(Configuration configuration) {
        tableWithEvents.addImpossibleConfiguration(configuration);
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
    public boolean isImpossibleConfiguration(Configuration configuration) {
        return tableWithEvents.isImpossibleConfiguration(configuration);
    }

    /**
     * Returns true if the list of findings form an impossible configuration
     * Useful when having events treated internally in a TableWithEvents. Events are joined together in a TableWithEvents whereas treated as different variables in other cases
     *
     * @param findings - set of findings
     * @return
     */
    @Override
    public boolean isImpossibleConfiguration(List<Finding> findings) {
        return tableWithEvents.isImpossibleConfiguration(findings);
    }

    /**
     * Returns true if there is a possible configuration with finding
     *
     * @param finding finding to check a possible configuration with it
     * @return true if there is a possible configuration with finding
     */
    @Override
    public boolean hasCompatiblePossibleConfiguration(Finding finding) throws NoFindingException {
        Variable variable = finding.getVariable();
        if (variable.getVariableType() == VariableType.EVENT){

        } else{
            throw new NoFindingException("Only implemented with Event Variables");
        }

        return false;
    }

    @Override
    public ArrayList<Configuration> getImpossibleConfigurations() {
        return tableWithEvents.getImpossibleConfigurations();
    }

    @Override
    public void setImpossibleConfigurations(ArrayList<Configuration> impossibleConfigurations) {
        tableWithEvents.setImpossibleConfigurations(impossibleConfigurations);
    }


    @Override
    public Potential copy() {
        return new TimeToEventTablePotential(this);
    }

//Scale potential
 //TODO
    @Override public void scalePotential(double scale) {
    }


//Table projects
    @Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException, WrongCriterionException {
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }


    @Override public TimeToEventTablePotential project(EvidenceCase evidenceCase)
            throws WrongCriterionException, NonProjectablePotentialException {
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }


    @Override public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                       List<TablePotential> alreadyProjectedPotentials)
            throws NonProjectablePotentialException, WrongCriterionException {
        // get the projected TablePotential, which will be returned inside a list
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }


//Is uncertaing
    @Override public boolean isUncertain() {
        return false;
    }


    /**
     * Parametrization of distributionName
     */
    public String getParametrizationName() {
        return parametrizationName;
    }

    public void setParametrizationName(String parametrizationName) {
        this.parametrizationName = parametrizationName;
    }
}

