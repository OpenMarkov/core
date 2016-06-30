package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.NoMixedParents;
import org.openmarkov.core.model.network.constraint.NoSuperValueNode;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author jperez-martin
 * @author artasom
 */
public abstract class TemporalEvolution extends Task {

    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public TemporalEvolution(ProbNet probNet) throws NotEvaluableNetworkException {
        super(probNet);
    }

    public abstract HashMap<Variable, TablePotential> getPosteriorValues() throws IncompatibleEvidenceException, UnexpectedInferenceException;

}