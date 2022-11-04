/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;

/**
 * @author jperez-martin
 * @author artasom
 * @version 1.1 cmyago 03/11/2022; added method getTemporalEvolutionWithDiscount
 */
public interface TemporalEvolution extends Task {

	HashMap<Variable, TablePotential> getTemporalEvolution()
			throws IncompatibleEvidenceException, UnexpectedInferenceException, NotEvaluableNetworkException;
	//cmyago 04/11/2022
	/**
	 * Returns the temporal evolution of a node or a set of utility nodes where discounting is applied.
	 * If there is no discounting non-discounted temporal evolution is returned
	 *
	 * @return temporal evolution of a node or a set of utility nodes where discounting is applied
	 * @throws IncompatibleEvidenceException
	 * @throws UnexpectedInferenceException
	 * @throws NotEvaluableNetworkException
	 */
	default HashMap<Variable, TablePotential> getTemporalEvolutionWithDiscount()
			throws IncompatibleEvidenceException, UnexpectedInferenceException, NotEvaluableNetworkException {
		return getTemporalEvolution();
	}
//cmyago end 04/11/2022

	void setDecisionVariable(Variable decisionSelected);

	ProbNet getExpandedNetwork();
}