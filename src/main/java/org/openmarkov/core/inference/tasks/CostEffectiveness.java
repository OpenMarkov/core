package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.ProbNet;

/**
 * Created by Jorge on 13/07/2015.
 */
public abstract class CostEffectiveness extends Task {
    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public CostEffectiveness(ProbNet probNet) throws NotEvaluableNetworkException {
        super(probNet);
    }
}
