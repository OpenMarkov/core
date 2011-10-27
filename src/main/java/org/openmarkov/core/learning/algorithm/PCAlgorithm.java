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

public class PCAlgorithm extends IndependenceRelationsAlgorithm{	

	private Logger logger;
	
    public PCAlgorithm(ProbNet learnedNet, ProbNet modelNet, 
    		EditionsGenerator editionsGenerator, double alpha, int cases[][]) {
        
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
        this.logger = Logger.getLogger(PCAlgorithm.class);
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
		
		/*Links orientation*/
		
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

        try{
            pNESupport.announceEdit(bestEdition);
            pNESupport.doEdit(bestEdition);
            logger.debug("Edition done: " + bestEdition.getPresentationName());
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