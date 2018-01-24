package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.GTablePotential;

/**
 * @author jperez-martin
 */
public interface CEADecision extends Task {

    public abstract GTablePotential getCEPPotential() throws UnexpectedInferenceException;
}
