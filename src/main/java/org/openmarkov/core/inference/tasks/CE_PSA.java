package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;

import java.util.Collection;

/**
 * @author jperez-martin
 */
public interface CE_PSA extends Task {

    Collection<GTablePotential> getCEPPotentials() throws NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException;

    void setDecisionVariable(Variable decisionSelected);
}
