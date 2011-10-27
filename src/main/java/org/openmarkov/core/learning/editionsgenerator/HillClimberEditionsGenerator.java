package org.openmarkov.core.learning.editionsgenerator;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.BaseLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.LinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.learning.cache.Cache;
import org.openmarkov.core.learning.metrics.Metric;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

/** This class implements the EditionsGenerator interface for the Hill Climber 
 * algorithm. 
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class HillClimberEditionsGenerator implements EditionsGenerator {
	
    /**
     * A copy of the learnedNet that is being learned.
     */
    protected ProbNet learnedNet;
    
    /** Cache used to speed up the search of the best editions */
    protected Cache cache;
        
    public HillClimberEditionsGenerator(ProbNet learnedNet, ProbNet modelNet,
    		Metric metric) 
			throws ProbNodeNotFoundException, NotEnoughMemoryException{
        this.learnedNet = learnedNet;
        
        learnedNet.getPNESupport().addUndoableEditListener(metric);
        
        cache = new Cache(learnedNet, metric);
        cache.initCache();
    }

    @Override
    public void undoableEditWillHappen(PNUndoableEditEvent event)
                    throws ConstraintViolationException, CanNotDoEditException {
            // TODO Auto-generated method stub
    }

    @Override
    public void undoEditHappened(PNUndoableEditEvent event) {
            // TODO Auto-generated method stub
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent event) {
        UndoableEdit pNEdit = event.getEdit();
        Class pNEditClass = ((PNEdit)pNEdit).getClass();

        //
        if(pNEditClass == LinkEdit.class)
        {
        	Variable tail = null;
    		for (Variable node : learnedNet.getVariables()){
    			if(((LinkEdit)pNEdit).getProbNode1().getName().equals(node.getBaseName()))
    				tail = node;
    		}
        	Variable head = null;
    		for (Variable node : learnedNet.getVariables()){
    			if(((LinkEdit)pNEdit).getProbNode2().getName().equals(node.getBaseName()))
    				head = node;
    		}
    		
    		if(((LinkEdit)pNEdit).isAdd())
    			pNEdit = new AddLinkEdit(learnedNet, head, tail, ((LinkEdit)pNEdit).isDirected());
    		else
    			pNEdit = new RemoveLinkEdit(learnedNet, head, tail, ((LinkEdit)pNEdit).isDirected());
    		
    		pNEditClass = ((PNEdit)pNEdit).getClass();
        }
        
        if((pNEditClass == AddLinkEdit.class) ||
                (pNEditClass == RemoveLinkEdit.class) ||
                (pNEditClass == InvertLinkEdit.class))
        try {
        	cache.updateCache((PNEdit) pNEdit);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
}

    /**
	 * This method returns the best n editions (and their associated scores)
	 * that can be done to the network that is being learnt. 
	 * 
	 * @param numEdits Number of editions that should be returned. 
	 * @param onlyAllowedEditions If this parameter is true, only those editions
	 * that do not provoke a modelNetworkConstraintViolation are returned
	 * @param onlyPositiveEditions If this parameter is true, only those 
	 * editions with a positive associated score are returned.
	 * @param reset If this parameter is true, the results provided will be  
	 * computed again forgetting previous calculations
	 * @return <code>ArrayList</code> of <code>EditAndScorePair</code> with the
	 * editions and scores requested. 
	 */
    public ArrayList<EditAndScorePair> getBestEditions(int numEdits,
                boolean onlyAllowedEdits, boolean onlyPositiveEdits,
                boolean reset) {

        ArrayList<EditAndScorePair> bestEditions =
                new ArrayList<EditAndScorePair>();
        EditAndScorePair editAndScore;
        BaseLinkEdit bestEdition;
        int numBestEditions = 0;
        
        /* If one of the parameters changed, we should compute the  
         * best editions again. (Note that this fact can occur only in the
         * interactive learning).
         */
        if (reset)
        	cache.resetParameters();
        
        while (numBestEditions < numEdits){
        	bestEdition = cache.getOptimalEdition();
            editAndScore = new EditAndScorePair(bestEdition,
                            cache.get(bestEdition));
            /* If we want only positive editions and this edition has a
             * negative score (or 0), we finish.
             */
            if ((onlyPositiveEdits) && (editAndScore.getScore() <= 0)){
                break;
            }

            /* We have to announce the edition to check whether it is allowed
             * or not
             */
            try {
                learnedNet.getPNESupport().announceEdit(bestEdition);
            } catch (Exception e) {
                if(onlyAllowedEdits){
                    continue;
                }
                else{
                    editAndScore.setViolatedConstraint(e);
                }
            }
            bestEditions.add(editAndScore);
            numBestEditions++;
        }
        return bestEditions;
	}
}
