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
 * independence relations approach.
 * The particular behavior of each algorithm is given by the 
 * <code>editionsGenerator</code> class.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public abstract class IndependenceRelationsAlgorithm extends LearningAlgorithm {

    protected EditionsGenerator editionsGenerator;
    
    protected boolean undirectedStructureFound = false;
    
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
		
		/* There is no model Net. The initial graph is the complete graph*/
		if (modelNet == null){
			learnedNet.getGraph().marry(ProbNet.getNodesOfProbNodes(
					learnedNet.getProbNodes()));
		}
		
		else{
			/* If the option "Use only nodes" or "Allow add links" are selected, 
			 * we just have to initialize the graph to the complete graph.
			 */
			if ((modelNetUse[1]) || (modelNetUse[2])){
				learnedNet.getGraph().marry(ProbNet.getNodesOfProbNodes(
					learnedNet.getProbNodes()));
			}
			
			// Addition of links not allowed
			else if (!modelNetUse[2]){
				/* If the algorithm cannot add links, the initial graph is 
				 * the one of the model net. (Note that this algorithms only 
				 * remove links)
				 */
				for (Link link : modelNet.getGraph().getLinks()){
	                learnedNet.addLink(learnedNet.getVariable(((ProbNode)link.
	                		getNode1().getObject()).getVariable().getName()), 
	                		learnedNet.getVariable(((ProbNode)link.getNode2().
	                				getObject()).getVariable().getName()), 
	                				false); 
	            }
				/*Removal of links is not allowed. At this point we have the
				 * structure of the undirected graph. thus, we just have to skip
				 * the first part of the algorithm (we use a flag to show this).
				 */
				if (!modelNetUse[3]){
					undirectedStructureFound = true;
				}
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

}
