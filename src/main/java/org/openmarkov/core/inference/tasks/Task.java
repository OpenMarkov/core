package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.heuristic.HeuristicFactory;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.List;

/** This class represents a user action related to inference. */
public interface Task {

    public void setHeuristicFactory(HeuristicFactory heuristicFactory);

    public void checkNetworkConsistency (ProbNet probNet) throws NotEvaluableNetworkException;

    public List<PNConstraint> initializeAdditionalConstraints();

    /** @return An <code>ArrayList</code> of <code>NetworkType</code> where the algorithm can be applied. */
    public List<NetworkType> initializeNetworkTypesApplicable();

    public boolean checkEvidenceConsistency();
    
    public boolean checkPoliciesConsistency();
}
