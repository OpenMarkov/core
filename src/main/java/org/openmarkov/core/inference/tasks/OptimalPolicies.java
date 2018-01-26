package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.HashMap;

/**
 * @author jperez-martin
 * @author artasom
 */
public interface OptimalPolicies extends Task {

    HashMap<Variable, Potential> getOptimalPolicies()
            throws UnexpectedInferenceException, NotEvaluableNetworkException, IncompatibleEvidenceException;

    Potential getOptimalPolicy(Variable decision) throws UnexpectedInferenceException, NotEvaluableNetworkException, IncompatibleEvidenceException;

}
