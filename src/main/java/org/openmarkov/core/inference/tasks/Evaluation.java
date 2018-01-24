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
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author jperez-martin
 * @author artasom
 */
public interface Evaluation extends Task {

    /**
     * @return The global expected utility
     * @throws UnexpectedInferenceException
     * @throws IncompatibleEvidenceException
     * @throws NotEvaluableNetworkException
     * defined over the conditioning variables.
     */
    public TablePotential getProbability() throws IncompatibleEvidenceException, UnexpectedInferenceException, NotEvaluableNetworkException;

    /**
     * @return The global expected utility
     * defined over the conditioning variables.
     * @throws UnexpectedInferenceException
     * @throws IncompatibleEvidenceException
     * @throws NotEvaluableNetworkException
     */
    public TablePotential getUtility() throws UnexpectedInferenceException, IncompatibleEvidenceException, NotEvaluableNetworkException;

    /**
     *
     * @return The optimal strategy tree
     * @throws UnexpectedInferenceException
     * @throws IncompatibleEvidenceException
     * @throws NotEvaluableNetworkException
     */
    public StrategyTree getOptimalStrategyTree() throws UnexpectedInferenceException, IncompatibleEvidenceException, NotEvaluableNetworkException;


}