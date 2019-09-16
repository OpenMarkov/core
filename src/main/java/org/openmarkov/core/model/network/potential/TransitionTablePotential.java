package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>TransitionTablePotential</code> is a type of relation with a list of
 * probabilistic nodes.
 *  Transition class to be merged with the new structure of tables
 * There have to be at least one Event variable
 * @version 1.0 -24/03/2019- -cyago -
 * @version 1.1 -24/08/2019 - renamed to TransitionTable and added the possibility of incompatible combinations
 * @since OpenMarkov 3.0
*/


@PotentialType(family ="Event", name = "TransitionTable")
public class TransitionTablePotential extends TableWithEventsPotential  {



    /**
     *
     * @param variables
     * @param role
     */
    public TransitionTablePotential(List<Variable> variables, PotentialRole role) {

        super(variables, role);

    }


    //TODO
    public TransitionTablePotential(TransitionTablePotential potential) {
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
        //return (variableSuitable && eventSuitable);
        return variableSuitable;
    }
    /**
     * Get a sample for a column given by ev
     * TODO This initial state may have parents
     * @param ev - configuration of the column
     *
     * @return A State sample of the potential in base of its parents
     */
    public State sample(EvidenceCase ev) {
        double r = Math.random();
        double countWeight = 0;
        List<Variable> lV = new ArrayList<>();
        Variable cVariable = getConditionedVariable();
        State[] columnStates  = cVariable.getStates();
        if (ev==null) ev = new EvidenceCase();

        for (int i = 0; i < columnStates.length; i++) {
            Finding f = new Finding(cVariable,columnStates[i]);
            try {
                ev.addFinding(f);
                countWeight +=tablePotential.getValue(ev);
                if (r < countWeight) {
                    return columnStates[i] ;
                }
                ev.removeFinding(cVariable);
            } catch (InvalidStateException | IncompatibleEvidenceException | NoFindingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    /**
     * Returns the state after an event has happened
     * version 1.0 - this version only considers Event nodes and State[0] as parents
     * TODO
     */
    public State newState(Configuration configuration, State currentState){
        State eState = null;
        Configuration eventAdaptedConfiguration = new Configuration();
        for (Finding f:configuration.getConfiguration()){
            if (f.getVariable().getVariableType() == VariableType.EVENT){
                try {
                    State eventState = events.getState(f.getVariable().getName());
                    eventAdaptedConfiguration.add(new Finding(events,eventState));
                } catch (InvalidStateException e) {
                    e.printStackTrace();
                }
            } else {
                eventAdaptedConfiguration.add(f);
            }

        }
        if (isImpossibleConfiguration(eventAdaptedConfiguration)){
        }
        eState=sample(eventAdaptedConfiguration.convertToEvidenceCase());

        return eState;
    }
}

