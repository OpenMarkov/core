package org.openmarkov.core.learning.algorithm;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.learning.editionsgenerator.EditionsGenerator;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.constraint.ModelNetworkConstraint;

/** This class implements the basic structure of any algorithm based on the
 * score-and-search approach.
 * The particular behavior of each algorithm is given by the 
 * <code>editionsGenerator</code> class.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public abstract class ScoreAndSearchAlgorithm extends LearningAlgorithm {
    
    /** Cache used to store the scores associated to editions */
    protected EditionsGenerator editionsGenerator;
    
    /** Final score of the evaluated net */
    protected double finalScore;
    
    /** Method invoked to run the algorithm
     * 
     * @return <code>ProbNet</code> learned.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws java.lang.Exception
     */
	public abstract ProbNet run() throws NotEnoughMemoryException,
			NormalizeNullVectorException;

	/** Takes a step in the algorithm
     * 
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws java.lang.Exception
     */
	public abstract ProbNet step(ProbNet learnedNet, PNEdit bestEdition,
			boolean doParametricLearning) throws NotEnoughMemoryException,
			NormalizeNullVectorException;
	
	/**
	 * Initializes the algorithm depending on the model net use selected
	 * by the user.
	 * @param boolean[] modelNetUse use of the model net selected by the user.
	 * @param <code>ProbNet</code> modelNet model network used to initialize
	 * the algorithm.
	 */
    public void init(boolean[] modelNetUse,
    		ProbNet modelNet) throws ProbNodeNotFoundException, 
    		NodeNotFoundException{
    	
    	/* If the option "Use only nodes" is not selected, we add
    	 * the links of the model net to the learnedNet we are going to 
    	 * learn.
    	 */
    	if(!modelNetUse[1] && (modelNet != null)){
    		for (Link link : modelNet.getGraph().getLinks()){
                learnedNet.addLink(learnedNet.getVariable(((ProbNode)link.getNode1().
                		getObject()).getVariable().getName()), 
                		learnedNet.getVariable(((ProbNode)link.getNode2().
                				getObject()).getVariable().getName()), 
                				link.isDirected()); 
            }
        	
        	try {
    			learnedNet.addConstraint(new ModelNetworkConstraint(modelNetUse, 
    					modelNet), false);
    		} catch (ConstraintViolationException e) {
    		}
    	}
    }

    public void setListeners(){
    	pNESupport.addUndoableEditListener(editionsGenerator);
    }
	
    public double getFinalScore(){
        return finalScore;
    }

}
