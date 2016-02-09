package org.openmarkov.core.inference;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.OptimalIntervention;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author manolo
 * Tests class for models that contain decisions. Different subclasses share that they have to test the MEU and the strategy
 *
 */
public abstract class OptimalStrategyTaskDecTest extends InferenceTaskTest {
	public abstract OptimalIntervention buildInferenceTask(ProbNet probNet, EvidenceCase preResolutionEvidence)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException;
}
