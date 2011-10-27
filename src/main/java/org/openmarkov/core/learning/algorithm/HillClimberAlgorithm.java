
package org.openmarkov.core.learning.algorithm;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.learning.editionsgenerator.EditAndScorePair;
import org.openmarkov.core.learning.editionsgenerator.EditionsGenerator;
import org.openmarkov.core.model.network.ProbNet;

/** This class implements the basic structure of the classic hill climber 
 * algorithm.
 * The particular behavior of each algorithm is given by the 
 * <code>editionsGenerator</code> class.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class HillClimberAlgorithm extends ScoreAndSearchAlgorithm{	
    
	
	private Logger logger;
	
    // Constructor
    /**
     * @param learnedNet <code>ProbNet</code> The graph to which the algorithm 
     * will be applied.
     * @param modelNet <code>ProbNet</code> Initial graph. 
     * @param alpha double parameter alpha
     * @param editionsGenerator <code>EditionsGenerator</code> The object that
     * gives the best operation in each iteration of the algorithm.
     * @param cases int[][] database cases for the parametric learning.
     **/
    public HillClimberAlgorithm(ProbNet learnedNet, ProbNet modelNet, 
    		double alpha, EditionsGenerator editionsGenerator, int cases[][]) {
        
        this.logger = Logger.getLogger(HillClimberAlgorithm.class);
        this.modelNet = modelNet;
        this.learnedNet = learnedNet;
        this.learnedNet.additionalProperties = learnedNet.additionalProperties;
        this.alpha = alpha;
        this.editionsGenerator = editionsGenerator;
        this.cases = cases;
        this.pNESupport = learnedNet.getPNESupport();
        if (pNESupport == null) {
            this.pNESupport = new PNESupport(this.learnedNet, false);
        }	               
    }
    
    /** Method invoked to run the algorithm
     * 
     * @return <code>ProbNet</code> learned.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws java.lang.Exception
     */
    public ProbNet run() throws NotEnoughMemoryException, 
		NormalizeNullVectorException {
		ArrayList<EditAndScorePair> bestEditions;
	
	    /* Principal loop */
	    bestEditions = editionsGenerator.getBestEditions(1,true,true,false);
	    while (!bestEditions.isEmpty()) {
	    	step(learnedNet, bestEditions.get(0).getEdition(), false);
	        bestEditions = editionsGenerator.getBestEditions(1,true,true,false);
	    } 
	    /* Parametric Learning */
	    parametricLearning();
	    
	    return learnedNet;
    }
    
    /** Takes a step in the algorithm
     * 
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws java.lang.Exception
     */
    public ProbNet step(ProbNet learnedNet, PNEdit bestEdition, 
    		boolean doParametricLearning) throws NotEnoughMemoryException, 
    		NormalizeNullVectorException {

	/* If there have been any improvements on the score, we update
     * the learnedNet. */
        try{
            pNESupport.announceEdit(bestEdition);
            pNESupport.doEdit(bestEdition);
            logger.debug("Edition done: " 
                    + bestEdition.getPresentationName());
        } catch (ConstraintViolationException ex){
        	/* If the edition was not allowed (ModelNetworkconstraint)
        	 * the algorithm just goes through the next iteration of the
        	 * loop, asking the cache for the next best edition.
        	 */
        }
        catch (Exception exception){
            exception.printStackTrace();
        }
	    /* Parametric Learning */
        if(doParametricLearning)
        	parametricLearning();
        
        return learnedNet;
    } 
}
