package org.openmarkov.core.model.network.potential;

import cern.jet.random.engine.RandomEngine;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Configuration;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Random;

/**
 * Interface implemented by Potentials used in DES models to represent and  calculate Time To Event behaviour
 */
public interface TimeToEvent {
    /**
     * Generate a TTE since the event is "triggered" by initial event, another event or change in state
     * The event will happend at triggering_time + TTE
     * @param  stateVariable
     * @param  stateValue
     * @param event
     * @return
     */
    public double getTimeToEvent(Variable stateVariable, State stateValue, String event);


    public double getTimeToEvent(EvidenceCase ev, Random random);


    double getTimeToEvent(Configuration eC, Random random);
}
