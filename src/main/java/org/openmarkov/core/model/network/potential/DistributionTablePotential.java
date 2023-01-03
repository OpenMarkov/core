package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction.ParametrizedFunctionManager;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionWithKnownInverseCDF;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;


/**
 * Potential when the parameters of the probabilistic distribution which describe the TTE in an Event variable may vary in base of the Configuration of the parents.
 * This potential is described by the probabilistic distribution chosen and a Table where there are the values of the distribution parameters.
 * The rows of the Table represent the distribution parameters of the distribution, and may be indicated by a number or a Funcion of the parents with Numeric Variables.
 * Each configuration of the parents are formed by a state of every Chance Variable and a Event parent. There have to be at least one Event variable.
 * Parents with Numeric Variables are treated as "Function Parameters", and may be used to configure the values of the distribution parameters.
 * This potential may have impossible configuration of parents
 * TODO Rephrase.
 *
 * @author cmyago
 * @version 1.5 -07/10/2022 -cmyago- changed to be used also with continuous variables (UTILITY and CHANCE nodes)
 */

@PotentialType(family = "Event", name = "DistributionTable")
public class DistributionTablePotential extends Potential {
    //14/08/2022 ImpossibleConfiguration removed
//        implements ImpossibleConfiguration {


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
     * Constructor of a DistributionTablePotential with probabilistic distribution "Exact"
     *
     * @param variables List of Variable whose first element is the node Variable and the rest are the Variable of the parents
     * @param role      role assumed by the potential
     */
    public DistributionTablePotential(List<Variable> variables, PotentialRole role) {
        this(variables, role, "Exact", "Nu");

    }

    /**
     * Constructor of DistributionTablePotential with probabilistic distribution given by distributionName
     *
     * @param variables        List of Variable whose first element is the node Variable and the rest are the Variable of the parents
     * @param role             role assumed by the potential
     * @param distributionName name of the probabilistic distribution used to compute TTE of the node
     */
    public DistributionTablePotential(List<Variable> variables, PotentialRole role, String distributionName, String parametrizationName) {
        super(variables, role);
        changeDistribution(distributionName, parametrizationName);
    }


    /**
     * Constructor of DistributionTablePotential which creates an object with the same values as potential.
     *
     * @param potential DistributionTablePotential whose values are used to create the new object
     */
    public DistributionTablePotential(DistributionTablePotential potential) {
        this(potential.getVariables(), potential.getPotentialRole(), potential.getDistributionName(), potential.getParametrizationName());
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

//18/05/2022. To be used with numeric variables
        //       return (node.getNodeType()== EVENT);
        VariableType variableType = variables.get(0).getVariableType();
        return ((variableType == VariableType.EVENT) || (variableType == VariableType.NUMERIC));

    }


    /**
     * Changes the probabilistic distribution contained is this potential. The values of the distribution parameters are se to default.
     *
     * @param distributionName
     * @param distributionParametrization name of the new probabilistic distribution
     */
    public void changeDistribution(String distributionName, String distributionParametrization) {
        try {
            this.distributionName = distributionName;
            this.parametrizationName = distributionParametrization;
            List<String> distributionParametersList = ParametrizedFunctionManager.getUniqueInstance().getParameters(distributionName, distributionParametrization);
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
            //Adding numeric parents
            numericVariables = new ArrayList<>();
            numericVariables.addAll(variables.subList(1, variables.size()));
            numericVariables.removeIf(v -> v.getVariableType() != VariableType.NUMERIC);
            this.tableWithEvents = new TableWithEvents(tableParents, role, (numericVariables.size() > 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns true if this instance of DistributionTablePotential values may have functions as a value in a cell
     *
     * @return true if this instance of DistributionTablePotential values may have functions as a value in a cell. False otherwise
     */
    public boolean hasFunctionValues() {
        return !(numericVariables.isEmpty());
    }

    /**
     * Returns the TableWithEvents object where the distribution parameters are stored
     *
     * @return the TableWithEvents object where the distribution parameters are stored
     */
    public TableWithEvents getTableWithEvents() {
        return tableWithEvents;
    }

    /**
     * Sets a TableWithEvents with the values for the distribution parameters
     * TODO Throw an Exception if the table is not compatible with the probabilistic distribution
     *
     * @param tableWithEvents the tableWithEvents to be stored
     */
    public void setTableWithEvents(TableWithEvents tableWithEvents) {
        this.tableWithEvents = tableWithEvents;
    }

    /**
     * Returns a String object with the name of the probabilistic distribution used for computing TTE
     *
     * @return String with the name of the probabilistic distribution used for computing TTE
     */
    public String getDistributionName() {
        return distributionName;
    }

    /**
     * Sets the probabilistic distribution used for computing TTE to distributionName
     *
     * @param distributionName String with the name of the distribution to be set
     */
    public void setDistributionName(String distributionName) {
        this.distributionName = distributionName;
    }

    /**
     * Returns a Variable object with the distribution parameters as states.
     *
     * @return Variable with the distribution parameters as states
     */
    public Variable getDistributionVariable() {
        return distributionVariable;
    }

    /**
     * Sets a Variable object with the distribution parameters as states.
     *
     * @param distributionVariable Variable to be set
     */
    public void setDistributionVariable(Variable distributionVariable) {
        this.distributionVariable = distributionVariable;
    }

    /**
     * List of Variable with the Numeric Parent Variables which can be used as function parameters
     *
     * @return List of Variable with the Numeric Parent Variables which can be used as function parameters
     */
    public ArrayList<Variable> getNumericVariables() {
        return numericVariables;
    }

    /**
     * Sets List of Variable with the Numeric Parent Variables which can be used as function parameters.
     *
     * @param numericVariables
     */
    public void setNumericVariables(ArrayList<Variable> numericVariables) {
        this.numericVariables = numericVariables;
    }

    //14/08/2022 changed for nuisance variable
    //14/08/2022 FIXME revise functions use
    @Override
    public double sampleConditionedVariable(double randomNumber, EvidenceCase parents) {
        //Extract FINITE_STATES and EVENT Variables and convert to the format of a TableWithEvents to find the position in the table
        EvidenceCase stateConfiguration = tableWithEvents.convert(parents);
        //Extract Numeric Variables which are the Function Variables
        EvidenceCase numericConfiguration = null;
        if (tableWithEvents.isUseTableWithFunctions()) {
            numericConfiguration = new Configuration();
            for (Variable numericVariable : numericVariables) {
                Finding finding = parents.getFinding(numericVariable);
                try {
                    numericConfiguration.addFinding(finding);
                } catch (InvalidStateException | IncompatibleEvidenceException e) {
                    e.printStackTrace();

                }
            }
        }
        //Extract parameters from the table
        ProbDensFunctionWithKnownInverseCDF distribution = null;
        int i = 0;
        double[] paramValues = new double[distributionParameters.length];
        try {
            for (State sParam : distributionVariable.getStates()) {
                Finding f = new Finding(distributionVariable, sParam);

                stateConfiguration.addFinding(f);

                if (tableWithEvents.isUseTableWithFunctions()) {
                    paramValues[i++] = tableWithEvents.getTableWithFunctions().getEvaluatedFunctionValue(stateConfiguration, numericConfiguration);
                } else {
                    paramValues[i++] = tableWithEvents.getTablePotential().getValue(stateConfiguration);
                }
                stateConfiguration.removeFinding(distributionVariable);
            }

            distribution = ParametrizedFunctionManager.getUniqueInstance().getParametrizedClass(distributionName, parametrizationName).newInstance();
            distribution.setParameters(paramValues);
        } catch (InstantiationException | IllegalAccessException | InvalidStateException |
                 IncompatibleEvidenceException | NoFindingException e) {
            e.printStackTrace();
        }
        //11/12/2022 I need to control the randomNumber sequence in order to avoid nuisance variance
        return distribution.getInverseCumulativeDistributionFunction(randomNumber);
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


//    /**
//     * Generates a TTE since the event is "triggered" by initial event, another event or change in state
//     * *  The event will happend at triggering_time + TTE
//     *
//     * @param findings one or more findings to create the Configuration for extracting TTE
//     * @param random
//     * @return
//     */
//    public double sampleVariable(List<Finding> findings, Random random) {
//
//        return sampleVariable(random, tableWithEvents.convert(findings));
//    }


    @Override
    public Potential copy() {
        return new DistributionTablePotential(this);
    }

    //Scale potential
    //TODO
    @Override
    public void scalePotential(double scale) {
    }

//ImpossibleConfiguration interface


    //Table projects
    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions)
            throws NonProjectablePotentialException, WrongCriterionException {
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }


    @Override
    public DistributionTablePotential project(EvidenceCase evidenceCase)
            throws WrongCriterionException, NonProjectablePotentialException {
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }


    @Override
    public List<TablePotential> tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                             List<TablePotential> alreadyProjectedPotentials)
            throws NonProjectablePotentialException, WrongCriterionException {
        // get the projected TablePotential, which will be returned inside a list
        throw new NonProjectablePotentialException("EventTablePotential cannot be projected");
    }


    //Is uncertaing
    @Override
    public boolean isUncertain() {
        return false;
    }


}

