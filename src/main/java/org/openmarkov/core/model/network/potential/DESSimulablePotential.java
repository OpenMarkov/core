package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.EvidenceCase;

/**
 * Interface implemented by Potentials which can be simulated in DES models (DESnets)
 * @author cmyago
 * @version 1.0 2019 - TimeToEvent only for events
 * @version 1.1 cmyago - 05/01/2020
 * @version 2 cmyago - 15/01/2023 refactored TimeToEvent from  to DESSimulablePotential because it is implemented by potentials which can be sampled in DES evaluation.
 */
public interface DESSimulablePotential {
    //CMI 11/04/2020; 14/08/2022 sampleConditionedVariable(Random randomGenerator, EvidenceCase parents) refactored to  sampleConditionedVariable(double randomNumber, EvidenceCase parents)  for dealing with nuisance variance
    //15/01/2023; moved from Potential

    /**
     * Gets a sample of this potential conditioned by its parents the using inverse cumulative distribution method. If this variable is finite-states, it returns the index of
     * the sampled state. If the variable is numeric, it returns the value sampled.
     * @param randomNumber number before 0 an 1 from which inverse cumulative value is computed
     * @param parents parents configuration with their values
     * @return  a sample of the potential when exists; Double.MAX_VALUE otherwise
     */
    public default double sampleConditionedVariable(double randomNumber, EvidenceCase parents)  {
        return Double.MAX_VALUE;
    }

    /**
     * Resets the potential for a new simulation
     */
    public default void resetSimulation(){
    }

//CMF
}
