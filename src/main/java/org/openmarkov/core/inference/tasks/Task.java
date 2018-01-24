package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.heuristic.HeuristicFactory;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.List;

/** This class represents a user action related to inference. */
public interface Task {

    public void setPreResolutionEvidence(EvidenceCase preresolutionEvidence);

    public void setConditioningVariables (List<Variable> conditioningVariables);
    
}
