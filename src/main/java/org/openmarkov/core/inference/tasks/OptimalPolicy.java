package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author jperez-martin
 * @author artasom
 */
public abstract class OptimalPolicy extends Task {

    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public OptimalPolicy(ProbNet probNet) throws NotEvaluableNetworkException {
        super(probNet);
    }

    public abstract TablePotential getOptimalPolicy() throws UnexpectedInferenceException;

}
