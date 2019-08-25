package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

/**
 * Interface implemented by Potentials used in DES models to represent and  calculate Time To Event behaviour
 */
public interface TimeToEventPotentialInterface {
    /**
     * Generate a TTE since the event is "triggered" by initial event, another event or change in state
     * The event will happend at triggering_time + TTE
     * @param  stateVariable
     * @param  stateValue
     * @param event
     * @return
     */
    public double getTimeToEvent(Variable stateVariable, State stateValue, String event);


}
