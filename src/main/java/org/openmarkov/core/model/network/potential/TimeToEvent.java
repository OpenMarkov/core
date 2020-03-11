package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.*;

import java.util.List;
import java.util.Random;

/**
 * Interface implemented by Potentials used in DES models to represent and  calculate Time To Event
 * @author cyago
 * @version 1.0 2019
 * @version 1.1 05/01/2020
 */
public interface TimeToEvent {
    /**
     * Generates a TTE since the event is "triggered" by initial event, another event or change in state
     *  The event will happend at triggering_time + TTE
     * @param configuration configuration for which TTE is computed
     * @param random Random engine for computing
     * @return the time to event since the event is triggered
     */
    double getTimeToEvent(Configuration configuration, Random random);

    /**
     * Generates a TTE since the event is "triggered" by initial event, another event or change in state
     *      *  The event will happend at triggering_time + TTE
     * @param findings one or more findings to create the Configuration for extracting TTE
     * @param random
     * @return
     */
    double getTimeToEvent(List<Finding> findings, Random random);

}
