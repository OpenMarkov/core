package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;

/**
 * Created by Jorge on 19/01/2016.
 */
public abstract class SensAnMap extends Task {
    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public SensAnMap(ProbNet probNet) throws NotEvaluableNetworkException {
        super(probNet);
    }

    @Override
    public boolean checkNetworkConsistency() throws NotEvaluableNetworkException {
        return false;
    }

    @Override
    public boolean checkEvidenceConsistency() {
        return false;
    }

    @Override
    public boolean checkPoliciesConsistency() {
        return false;
    }


    public abstract HashMap<UncertainParameter, TablePotential> getUncertainParametersPotentials();
}
