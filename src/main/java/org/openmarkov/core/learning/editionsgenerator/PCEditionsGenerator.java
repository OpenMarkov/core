package org.openmarkov.core.learning.editionsgenerator;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openmarkov.core.action.BaseLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.learning.independencetester.IndependenceTester;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;

/** This class implements the EditionsGenerator interface for the PC 
 * algorithm. 
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class PCEditionsGenerator implements EditionsGenerator {
	
    /**
     * A copy of the learnedNet that is being learned.
     */
    protected ProbNet learnedNet;
    
    protected IndependenceTester independenceTester;
    
    protected double degreeOfAccuracy;
    
    protected ArrayList<HashMap<Node, ArrayList<Node>>> sepSets = null;
    
    protected ProbNode lastNode = null;
    
    protected int lastTamAdjacency = 0;
    
    ArrayList<EditAndScorePair> bestEditions =
            new ArrayList<EditAndScorePair>();
    
    int numBestEditions = 0;
        
    public PCEditionsGenerator(ProbNet learnedNet, ProbNet modelNet,
    		IndependenceTester independenceTester, double degreeOfAccuracy) 
			throws ProbNodeNotFoundException, NotEnoughMemoryException{
        this.learnedNet = learnedNet;
        this.independenceTester = independenceTester;
        this.degreeOfAccuracy = degreeOfAccuracy;
        learnedNet.getPNESupport().addUndoableEditListener(independenceTester);
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
                boolean onlyAllowedEdits, boolean onlyIndependentEdits,
                boolean reset) {

		int tamAdjacency = 0, indexNodeX = 0;
		EditAndScorePair linkTested;
		ArrayList<Node> adjacencySubset;
		
		if (numBestEditions == numEdits){		
			return bestEditions;
		}
        
        if (lastNode != null){
        	indexNodeX = learnedNet.getProbNodes().indexOf(lastNode);
        }
        else{
        	//init the separation sets
        	sepSets = new ArrayList<HashMap<Node, ArrayList<Node>>>();
      		for (int i = indexNodeX; i < learnedNet.getNumNodes(); i++){
      			sepSets.add(new HashMap<Node, ArrayList<Node>>());
      		}
        }
        tamAdjacency = lastTamAdjacency;
        
  		try{
  			while (maxOfAdjacencies() > tamAdjacency){
  				
  				/* We read the list of nodes from the last node we read to the
  				 * end of the list.
  				 */
  				for (ProbNode nodeX : learnedNet.getProbNodes().subList(
  						indexNodeX, learnedNet.getNumNodes())){
  					lastNode = nodeX;
					lastTamAdjacency = tamAdjacency;

  					for (Node nodeY : neighborsNotChecked(nodeX, indexNodeX)){
  						adjacencySubset = nodeX.getNode().getNeighbors();
  						adjacencySubset.remove(nodeY);
  						 						
  						for(ArrayList<Node> subSet : subSetsOfSize(
  								adjacencySubset, tamAdjacency)){
  							linkTested = independenceTester.independents(
  									nodeX.getNode(), nodeY, subSet);
  						
  							if (((!onlyIndependentEdits) || 
								(degreeOfAccuracy >= linkTested.getScore()))
								&& !bestEditions.contains(linkTested)){
  								/* We have to announce the edition to check 
  								 * whether it is allowed or not
  					             */
  					            try {
  					                learnedNet.getPNESupport().
  					                	announceEdit(linkTested.getEdition());
  					            } catch (Exception e) {
  					                if(onlyAllowedEdits){
  					                    continue;
  					                }
  					                else{
  					                    linkTested.setViolatedConstraint(e);
  					                }
  					            }
  								sepSets.get(indexNodeX).put(nodeY, 
  										adjacencySubset);
  								bestEditions.add(linkTested);
  								numBestEditions++;
  								if (numBestEditions == numEdits){		
  									return bestEditions;
  								}
  								break;
  							}
  						}
  					}
  				}
  				tamAdjacency++;
  				indexNodeX = 0;
  			}
  		} catch (ProbNodeNotFoundException e){
  			Logger.getLogger(PCEditionsGenerator.class.getName()).
  				log(Level.WARN, null, e);
  		}
        return bestEditions;
	}
    
    /**
     * This method returns the maximum number of neighbors of a node in 
     * the probNet that is being learnt.
     */
    private int maxOfAdjacencies(){
    	int max, adjacents;
	  
    	max = 0;
    	for (ProbNode node : learnedNet.getProbNodes()) {
    		adjacents = node.getNode().getNeighbors().size();
	    
    		if (adjacents > max)
    			max = adjacents;
    	}
	
    	return max;
    }
    
    /**
     * Returns a list of the subsets of size n of the given set   
     * @param set <code>ArrayList</code> of 
     * <code>Node</code> from which extract the subsets.
     * @param subSetsSize size of the subsets.
     * @return <code>ArrayList</code> of <code>ArrayList</code> of 
     * <code>Node</code>. Each <code>ArrayList</code> of <code>Node</code> 
     * is one of the subsets of size n.
     */
    public ArrayList<ArrayList<Node>> subSetsOfSize(ArrayList<Node> set, 
    		int subSetsSize) {

    	ArrayList<ArrayList<Node>> subSets = new ArrayList<ArrayList<Node>>();
    	ArrayList<Node> subSet = new ArrayList<Node>();
    	int indexSubSet[];
    	boolean found = true;

    	indexSubSet = new int[subSetsSize];
    	
    	//Add the empty set
    	if (subSetsSize == 0){
    		subSets.add(new ArrayList<Node>());
    	}

    	if ((subSetsSize > 0) & (subSetsSize <= set.size())) {
    		for (int i = 0 ; i < subSetsSize ; i++) {
    			indexSubSet[i] = i;
    			subSet.add(set.get(i));
		  	}
    		subSets.add(subSet);

    		if (subSetsSize < set.size()) {
    			while (found) {
    				found = false;

    				for (int i = subSetsSize-1 ; i >= 0 ; i--){
    					if (indexSubSet[i] < (set.size() + (i - subSetsSize))) {
    						indexSubSet[i] = indexSubSet[i] + 1;

    						if (i < (subSetsSize-1)) {
    							for (int j = i+1 ; j <subSetsSize ; j++){
    								indexSubSet[j] = indexSubSet[j-1] + 1;
    							}
    						}

    						found = true;
    						break;
    					}
    				}

    				if (found) {
    					subSet = new ArrayList<Node>();
    					for (int k = 0 ; k < subSetsSize ; k++){
    						subSet.add(set.get(indexSubSet[k]));
    					}

    					subSets.add(subSet);
    				}
    			}
    	    }   
    	}

    	return subSets;
	}
    
    /**
     * Returns a list of nodes that contains the neighbours that are after
     * nodeX in the list of nodes of the Probnet(i.e., those nodes that have
     * not been checked yet). It has no sense to check the independency of Y,X
     * if we previously tested the independence of X,Y.
     * @param nodeX
     * @return
     */
    public ArrayList<Node> neighborsNotChecked(ProbNode nodeX, int indexNodeX){
    	ArrayList<Node> neighborsNotChecked = new ArrayList<Node>();
    	
    	for (Node neighbor : nodeX.getNode().getNeighbors()){
    		neighborsNotChecked.add(neighbor);
    	}
    	
    	for (ProbNode checkedNode : learnedNet.getProbNodes().subList(0, 
    			indexNodeX)){ 
			neighborsNotChecked.remove(checkedNode.getNode());
    	}
    	return neighborsNotChecked;
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
    	ArrayList<EditAndScorePair> editionsToRemove = 
    			new ArrayList<EditAndScorePair>();
    	ProbNode probNodeX, probNodeY, originNode = null;
    	Node separationNode = null;
    	int indexOriginNode;
    	UndoableEdit pNEdit = event.getEdit();
    	BaseLinkEdit bestEdition;
    	Class pNEditClass = ((PNEdit)pNEdit).getClass();

        if(pNEditClass == RemoveLinkEdit.class)
        {
    	
	    	probNodeX = learnedNet.getProbNode(((RemoveLinkEdit)pNEdit).
	    			getVariable1());
	    	probNodeY = learnedNet.getProbNode(((RemoveLinkEdit)pNEdit).
	    			getVariable2());
	    	
	    	/* Remove the edition from the bestEditions list (and the
	    	 * inverse edition  
	    	 */
	    	for (EditAndScorePair editAndScore : bestEditions){
	    		originNode = null;
	    		bestEdition = editAndScore.getEdition();
	    		if (bestEdition.equals(pNEdit)){
	    			editionsToRemove.add(editAndScore);
	    		}
	    		
	    		//inverse edition
	    		bestEdition = new RemoveLinkEdit(
	    				learnedNet, editAndScore.getEdition().getVariable2(), 
	    				editAndScore.getEdition().getVariable1(), false);
	    		if (bestEdition.equals(pNEdit)){
	    			editionsToRemove.add(editAndScore);
	    		}
	    		
	    		/* We also have to remove the editions that have one of the 
	    		 * variables of the edition as origin node and the other variable
	    		 * in the separation set.
	        	 */
	    		if (bestEdition.getVariable1().equals(probNodeX.getVariable())){
	    			originNode = probNodeX;
	    			separationNode = probNodeY.getNode();
	    		}
	    		else if (bestEdition.getVariable2().equals(probNodeX.
	    				getVariable())){
	    			originNode = probNodeX;
	    			separationNode = probNodeY.getNode();
	    		}
	    		else if (bestEdition.getVariable1().equals(
	    				probNodeY.getVariable())){
	    			originNode = probNodeY;
	    			separationNode = probNodeX.getNode();
	    		}
	    		else if (bestEdition.getVariable2().equals(
	    				probNodeY.getVariable())){
	    			originNode = probNodeY;
	    			separationNode = probNodeX.getNode();
	    		}
	    		
	    		if (originNode != null){
		    		indexOriginNode = learnedNet.getProbNodes().indexOf(originNode);
		    		if (sepSets.get(indexOriginNode).get(separationNode) != null){
		    			editionsToRemove.add(new EditAndScorePair(bestEdition,0));
		    			sepSets.get(indexOriginNode).remove(separationNode);
		    		}
	    		}
	    	}
    	
	    	// Remove the editions
	    	bestEditions.removeAll(editionsToRemove);
	    	numBestEditions = bestEditions.size();
        }
    }
}
