package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author jperez-martin
 * @author artasom
 */
public interface OptimalPolicy extends Task {

    public abstract TablePotential getOptimalPolicy() throws UnexpectedInferenceException, NotEvaluableNetworkException, IncompatibleEvidenceException;

}
