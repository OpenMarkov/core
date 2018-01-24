/*
 * Copyright 2015 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.potential.StrategyTree;

/**
 * @author jperez-martin
 * @author artasom
 */
public interface OptimalIntervention extends Task {

    /**
     * @return The optimal intervention
     */
    public abstract StrategyTree getOptimalIntervention() throws IncompatibleEvidenceException, UnexpectedInferenceException, NotEvaluableNetworkException;

}