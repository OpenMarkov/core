package org.openmarkov.core.learning.metric;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;

/** This class implements the K2 metric. Note that the K2 metric is
 * exactly the BayesianMetric with parameter alpha set to 1.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class K2Metric extends BayesianMetric {
    
    //Constructor
    /**
     * After constructing the metric, we evaluate the given net.
     * @param probNet <code>ProbNet</code> to evaluate.
     * @param cases <code>double[][]</code> database cases.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public K2Metric(ProbNet probNet, int[][] cases) 
            throws NotEnoughMemoryException {
        super(probNet, cases, 1);
    }
}
