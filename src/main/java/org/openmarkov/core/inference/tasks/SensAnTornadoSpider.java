package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;

/**
 * @author jperez-martin
 */
public abstract class SensAnTornadoSpider extends Task {
    /**
     * @param probNet The network used in the inference
     * @throws NotEvaluableNetworkException
     */
    public SensAnTornadoSpider(ProbNet probNet) throws NotEvaluableNetworkException {
        super(probNet);
    }

    public abstract HashMap<UncertainParameter, TablePotential> getUncertainParametersPotentials();
}
