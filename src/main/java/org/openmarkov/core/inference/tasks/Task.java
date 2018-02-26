/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.heuristic.HeuristicFactory;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.List;

/** This class represents a user action related to inference. */
public interface Task {

    void setPreResolutionEvidence(EvidenceCase preresolutionEvidence);

    void setConditioningVariables(List<Variable> conditioningVariables);
}
