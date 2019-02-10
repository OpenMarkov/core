package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.List;

//@PotentialType(name = "EventTable")
public class EventTablePotential extends TablePotential {


    public EventTablePotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
    }

    /**
     * Returns if certain Potential type makes sense given the
     * variables and the potential role.
     * In this case the potential makes sense when at least one of the parents is an Event and the rest are
     *  FINITE_STATES or DISCRETIZED
     *
     * @param node      . <code>Node</code>
     * @param variables . <code>List</code> of <code>Variable</code>.
     * @param role      . <code>PotentialRole</code>.
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        boolean eventSuitable = false;
        boolean finiteSuitable = true;
        int i = 0;
        while (finiteSuitable && i < variables.size()) {
            eventSuitable |= node.getNodeType() == NodeType.EVENT;
            finiteSuitable &= variables.get(i).getVariableType() == VariableType.FINITE_STATES
                    || variables.get(i).getVariableType() == VariableType.DISCRETIZED;
            ++i;
        }
        return (finiteSuitable && eventSuitable);
    }

}
