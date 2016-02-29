package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.NoMixedParents;
import org.openmarkov.core.model.network.constraint.NoSuperValueNode;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jperez-martin
 */
public abstract class CEADecision extends Task {
    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public CEADecision(ProbNet probNet) throws NotEvaluableNetworkException {
        super(probNet);
    }

    public abstract GTablePotential getCEPPotential() throws UnexpectedInferenceException;
}
