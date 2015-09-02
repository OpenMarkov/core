package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.ProbNet;

/**
 * @author jorgepmartin
 * @author artasom
 */
public abstract class TemporalEvolution extends Task {

    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public TemporalEvolution(ProbNet probNet) throws NotEvaluableNetworkException {
        super(probNet);
    }
}