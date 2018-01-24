package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;

/**
 * @author jperez-martin
 */
public interface SensAnMap extends Task {

    public abstract HashMap<UncertainParameter, TablePotential> getUncertainParametersPotentials();
}
