package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;

/**
 * @author jperez-martin
 */
public interface CEAnalysis extends Task {

    public GTablePotential getGTablePotential() throws UnexpectedInferenceException, NotEvaluableNetworkException, IncompatibleEvidenceException;

    void setDecisionVariable(Variable decisionVariable);
}
