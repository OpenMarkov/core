package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.Configuration;
import org.openmarkov.core.model.network.Finding;

import java.util.List;
import java.util.Random;

/**
 * Interface implemented by Potentials used in DES models to represent and  calculate Time To Event
 * @author cmyago
 * @version 1.0 2019
 * @version 1.1 cnyago - 05/01/2020
 * 04/10/2023 FIXME Keep it or not?
 */
public interface TimeToEvent {
    /**
     * Generates a TTE since the event is "triggered" by initial event, another event or change in state
     *  The event will happend at triggering_time + TTE
     * @param random Random engine for computing
     * @param configuration configuration for which TTE is computed
     * @return the time to event since the event is triggered
     */
    double getTimeToEvent(Random random, Configuration configuration);

    /**
     * Generates a TTE since the event is "triggered" by initial event, another event or change in state
     *      *  The event will happend at triggering_time + TTE
     * @param findings one or more findings to create the Configuration for extracting TTE
     * @param random
     * @return
     */
    double getTimeToEvent(List<Finding> findings, Random random);

}
