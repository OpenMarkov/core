package org.openmarkov.core.learning.metric;

import java.util.ArrayList;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/** This abstract class defines the basic elements of a metric.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public abstract class Metric implements PNUndoableEditListener {
	
    // Attributes
    /** A copy of the received <code>ProbNet</code>. */
    protected ProbNet probNet;
    
    /** Score of the associated net */
    protected double score;

    /** Database cases. */
    protected int[][] cases;

    // Constructor
    /** @param inputProbNet <code>ProbNet</code> The graph to which the 
     * algorithm will be applied.
     * @param cases <code>double[][]</code> database cases.  */
    public Metric(ProbNet inputProbNet, int[][] cases) {
            this.probNet = inputProbNet.copy();
            this.cases = cases;
    }
    
    //Methods
    /**
     * Scores the associated network with the given edition.
     * @param edition <code>PNEdit</code> 
     * @return <code>double</code> score of the net with the given edition
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public abstract double score(PNEdit edition) 
            throws NotEnoughMemoryException;

    /**
     * Scores the associated network
     * @return <code>double</code> score of the net
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public abstract double score() 
            throws NotEnoughMemoryException;
    
    /**
     * @return <code>double</code> score of the associated net.
     */
    public double getScore(){
       return score;
    }
    
    /** @return <code>String</code> with the class name */
    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
