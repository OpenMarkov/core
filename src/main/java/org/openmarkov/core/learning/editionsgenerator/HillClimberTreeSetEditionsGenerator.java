package org.openmarkov.core.learning.editionsgenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.BaseLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.learning.cache.Cache;
import org.openmarkov.core.learning.metrics.Metric;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

/** This class implements the EditionsGenerator interface. It contains a 
 * <code>HashMap</code> to store, for each node, the information of the possible
 * editions (i.e., with positive or negative associated score) that have that 
 * node as destiny. Also it contains a <code>TreeSet</code> with the editions 
 * that can be done to the <code>ProbNet</code>, ordered by the score associated 
 * to each one. 
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class HillClimberTreeSetEditionsGenerator implements EditionsGenerator {

	/**
	 * A copy of the learnedNet that is beeing learnt.
	 */
	protected ProbNet learnedNet;
    
    /** This net is used as the initial net from wich to start the algorithm
     * with the possibility to fix its links or not.
     */
    protected ProbNet modelNet;
    
    /** Cache used to speed up the search of the best editions */
    protected Cache cache;
    
    /** Possible editions that were impossible before the last update of the
     * network.
     */
    protected ArrayList<PNEdit> possibleEditions = new ArrayList<PNEdit>();
    
    /** Impossible editions that were possible before the last update of the
     * network.
     */
    protected ArrayList<PNEdit> impossibleEditions = new ArrayList<PNEdit>();
    
    /** Editions stored attending to its destiny node */
    protected HashMap<String, HashMap<PNEdit, EditAndScorePair>> 
    	nodeEditionsMap;
    
    /** Positive editions ordered by their respective associated scores */
    protected TreeSet<EditAndScorePair> editionsTree;
    
	public HillClimberTreeSetEditionsGenerator(ProbNet modelNet, 
			ProbNet learnedNet, Metric metric) 
			throws ProbNodeNotFoundException, NotEnoughMemoryException{
        this.modelNet = modelNet;
        this.learnedNet = learnedNet;
        nodeEditionsMap = new HashMap<String, HashMap<PNEdit,
        	EditAndScorePair>>();
        editionsTree = new TreeSet<EditAndScorePair>
            (new EditAndScorePairComparator());
        learnedNet.getPNESupport().addUndoableEditListener(metric);
        cache = new Cache(learnedNet, metric);
        cache.initCache();
        initPossibleEditions();  
        initTreeStructures();
	}
	
	/**
     * This method is called in the constructor of this object in order to 
     * initialize the list of possible editions. 
	 * @throws ProbNodeNotFoundException 
     */
    private void initPossibleEditions() throws ProbNodeNotFoundException{
        ProbNet auxiliarNet = null;
        
        auxiliarNet = learnedNet.copy();
        
        /* calculate possibleEditions */
        possibleEditions.clear();
        for (ProbNode node1 : learnedNet.getProbNodes()){
            for (ProbNode node2 : learnedNet.getProbNodes()){
                /*if node2 is a child of node1 we can remove and maybe invert 
                 * the link*/
                if(node1.getNode().isChild(node2.getNode())){
                    if(modelNet.getGraph().
                            getLink(node1.getNode(), node2.getNode(), true) 
                            == null){
                        possibleEditions.add(new RemoveLinkEdit(learnedNet, 
                            node1.getVariable(), node2.getVariable(), true));
                        /* check whether the inversion is possible or not*/
                        auxiliarNet.removeLink(node1.getVariable(), 
                        		node2.getVariable(), true);
                        if(!auxiliarNet.getGraph().existsPath(auxiliarNet.
                        		getProbNode(node1.getName()).
                        		getNode(), auxiliarNet.
    	                        		getProbNode(node2.getName())
    	                        		.getNode(), true)){
                        	possibleEditions.add(new InvertLinkEdit(learnedNet,
                        			node1.getVariable(), node2.
                        			getVariable(), true));
                        }
                        try {
							auxiliarNet.addLink(node1.getVariable(), 
									node2.getVariable(), true);
						} catch (Exception e) {
							e.printStackTrace();
						}
                    }
                }
                // In other case, we can add the link unless it makes a cycle
                else {
                    if (!(node1.getVariable().getName().equals(node2.
                            getVariable().getName())) && (!learnedNet.getGraph().
                            		existsPath(node2.getNode(), 
                            				node1.getNode(), true))){
                    	possibleEditions.add(new AddLinkEdit(learnedNet, 
                    			node1.getVariable(), node2.getVariable(), 
                    			true));
                    }
                }
            }
        }
    }
    
    private void initTreeStructures(){
    	Iterator<PNEdit> it = possibleEditions.iterator();
    	PNEdit edition;
    	EditAndScorePair editAndScore;
    	Class pNEditClass;
    	String destinationVariable = "", destinationVariable2 = "",
    		destinationVariableAux;
    	HashMap<PNEdit, EditAndScorePair> editionsList = 
            new HashMap<PNEdit, EditAndScorePair>();
    	
    	while(it.hasNext()){
    		edition = it.next();
    		
    		pNEditClass = edition.getClass();
            try{
                if (pNEditClass == AddLinkEdit.class) {
                    destinationVariable = ((AddLinkEdit)edition).getVariable2().
                            getName(); 
                }
                else if (pNEditClass == RemoveLinkEdit.class) {
                    destinationVariable = ((RemoveLinkEdit)edition).
                    	getVariable2().getName(); 
                } 
                else{
                	destinationVariable = ((InvertLinkEdit)edition).
                		getVariable1().getName(); 
                    destinationVariable2 = ((InvertLinkEdit)edition).
                    	getVariable2().getName();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            
            editAndScore = new EditAndScorePair((BaseLinkEdit)edition, cache.get(edition));
            /*search the edition in the nodeEditionsMap of 
            the destination variable */
            destinationVariableAux = destinationVariable;
            while (destinationVariableAux != null){
                if(nodeEditionsMap.containsKey(destinationVariableAux)){
                    editionsList = nodeEditionsMap.get(destinationVariableAux);
                    if(editionsList.containsKey(edition)){
                        editionsList.put(edition,editAndScore);
                    }
                    /* If the edition was not present in the nodeEditionsMap, we
                     * add it there and in the editionsTree.
                     */
                    else{
                        editionsTree.add(editAndScore);
                        editionsList.put(edition,editAndScore);
                    }
                }
                else{
                    editionsList.put(edition, editAndScore);
                    nodeEditionsMap.put(destinationVariableAux, editionsList);
                    editionsTree.add(editAndScore);
                }
                if(destinationVariableAux.equals(destinationVariable2))
                    break;
                
                destinationVariableAux = destinationVariable2;
            }
    	}
    }
    
    /**
     * This method is called after constructing this object and everytime
     * the network is updated to update the list of possible editions. This
     * method is only called when cache use is activated.
     * @param pNEdit <code>UndoableEdit</code> last edition done.
     */
    private void updatePossibleEditions(UndoableEdit pNEdit){
        Class pNEditClass = ((PNEdit)pNEdit).getClass();
        impossibleEditions.clear();
        possibleEditions.clear();
        ArrayList<PNEdit> possibleEditionsRemove = new ArrayList<PNEdit>();
        ProbNet auxiliarNet;
        Variable var1, var2;
        
        /* update the possible editions */
        try{
            if (pNEditClass == AddLinkEdit.class) {
            	auxiliarNet = learnedNet.copy();
            	var1 = ((AddLinkEdit)pNEdit).getVariable1();
            	var2 = ((AddLinkEdit)pNEdit).getVariable2();            	
                /* When adding a link, we have to remove from cache the links
                 * that make a cycle */
                impossibleEditions = getCycleLinksAdd(learnedNet.
                        getProbNode(var1), learnedNet.getProbNode(var2));
                impossibleEditions.add((AddLinkEdit)pNEdit);
                possibleEditions.add(new RemoveLinkEdit(learnedNet, var1, var2, 
                		true));
                /* Check whether we can invert the added link */
                auxiliarNet.removeLink(((AddLinkEdit)pNEdit).getVariable1(),
                ((AddLinkEdit)pNEdit).getVariable2(), true);
                if(!auxiliarNet.getGraph().existsPath(auxiliarNet.
                		getProbNode(var1).getNode(), 
                		auxiliarNet.getProbNode(var2).getNode(), true)){
                	possibleEditions.add(new InvertLinkEdit(learnedNet,
                			var1, var2, true));
                }
            }
            else if (pNEditClass == RemoveLinkEdit.class) {
                /* When removing a link, we have to add to cache the links
                 * that made a cycle */
            	var1 = ((RemoveLinkEdit)pNEdit).getVariable1();
            	var2 = ((RemoveLinkEdit)pNEdit).getVariable2();  
                possibleEditions = getCycleLinksRemove(learnedNet.
                        getProbNode(var1), learnedNet.getProbNode(var2));
                possibleEditions.add(new AddLinkEdit(learnedNet, var1, var2, 
                		true));
                impossibleEditions.add(new RemoveLinkEdit(learnedNet,
                        var1, var2, true));
            	impossibleEditions.add(new InvertLinkEdit(learnedNet,
                    var1, var2, true));
            } 
            else if (pNEditClass == InvertLinkEdit.class){
            	auxiliarNet = learnedNet.copy();
            	/* to this function, invert the link between var1 and var2 is
            	 * almost like removing the link between var1 and var2 and
            	 * adding the link between var2 and var1
            	 */
            	var1 = ((InvertLinkEdit)pNEdit).getVariable1();
            	var2 = ((InvertLinkEdit)pNEdit).getVariable2();            	
                
            	/* Possible and impossible editions after removing the link */
            	learnedNet.removeLink(var2, var1, true);
            	possibleEditionsRemove = getCycleLinksRemove(learnedNet.
                        getProbNode(var1), learnedNet.getProbNode(var2));
                
                /* Possible and impossible editions after adding the inverse
                 * link.
                 */
                learnedNet.addLink(var2, var1, true);
                impossibleEditions = getCycleLinksAdd(learnedNet.
                        getProbNode(var2), learnedNet.getProbNode(var1));
                impossibleEditions.add(new AddLinkEdit(learnedNet, var2, var1, 
                		true));
                impossibleEditions.add(new AddLinkEdit(learnedNet, var1, var2, 
                		true));
                impossibleEditions.add(new RemoveLinkEdit(learnedNet,
                        var1, var2, true));
                possibleEditions.add(new RemoveLinkEdit(learnedNet, var2, var1, 
                		true));
                
                for (PNEdit edition : possibleEditionsRemove){
                	if (!impossibleEditions.contains(edition)){
                		possibleEditions.add(edition);
                	}
                }
                impossibleEditions.add((InvertLinkEdit)pNEdit);
            }
        } catch (Exception e){
            e.printStackTrace();
        } 
    }
    
    /** Method to get the editions that make a cycle after adding a link.
     * If the edition is from node A to node B, the links returned are those 
     * from B and its descendants to A and its ancestors. For these kind of
     * links that already exist we add an invertLinkEdit, for the rest, an
     * addLinkEdit.
     * @param sourceNode <code>ProbNode</code> source node of the added link
     * @param destinationNode <code>ProbNode</code> destination node of the 
     * added link
     * @return <code>ArrayList<PNEdit></code> list of editions that make a
     * cycle after adding the link between sourceNode and destinationNode
     */
    private ArrayList<PNEdit> getCycleLinksAdd(ProbNode sourceNode, 
            ProbNode destinationNode){
        ArrayList<ProbNode> descendants = new ArrayList<ProbNode>();
        ArrayList<ProbNode> ancestors = new ArrayList<ProbNode>();
        ArrayList<PNEdit> cycleEditions = new ArrayList<PNEdit>();
         
        for(ProbNode node : ProbNet.getProbNodesOfNodes(
                getDescendants(destinationNode.getNode()))){
            descendants.add(node);
        }
        
        for(ProbNode node : ProbNet.getProbNodesOfNodes(
                getAncestors(sourceNode.getNode()))){
            ancestors.add(node);
        }
        
        for(ProbNode source : descendants){
            for (ProbNode destination : ancestors){
            	if ((source != destinationNode) || (destination != sourceNode)){
					cycleEditions.add(new AddLinkEdit(learnedNet,source.
							getVariable(), destination.getVariable(), true));
					if (destination.getNode().isChild(source.getNode()))
						cycleEditions.add(new InvertLinkEdit(learnedNet,destination.
							getVariable(), source.getVariable(), true));
            	}
            }
        }
        
        cycleEditions.add(new AddLinkEdit(learnedNet, destinationNode.
        		getVariable(), sourceNode.getVariable(), true));
        return cycleEditions;
    }
    
    /** Method to get the editions that made a cycle before removing a link.
     * If the edition is from node A to node B, the links returned are those 
     * from B and its descendants to A and its ancestors. The returned list
     * includes also the inversion on links that before removing the link made
     * a cycle.
     * @param sourceNode <code>ProbNode</code> source node of the removed link
     * @param destinationNode <code>ProbNode</code> destination node of the 
     * removed link
     * @return <code>ArrayList<PNEdit></code> list of editions that made a
     * cycle before removing the link between sourceNode and destinationNode
     * @throws ProbNodeNotFoundException 
     */
    private ArrayList<PNEdit> getCycleLinksRemove(ProbNode sourceNode, 
            ProbNode destinationNode) throws ProbNodeNotFoundException{
    	ProbNet auxiliarNet = null;
        ArrayList<ProbNode> descendants = new ArrayList<ProbNode>();
        ArrayList<ProbNode> ancestors = new ArrayList<ProbNode>();
        ArrayList<PNEdit> cycleEditions = new ArrayList<PNEdit>();
        
    	auxiliarNet = learnedNet.copy();
        
        for(ProbNode node : ProbNet.getProbNodesOfNodes(
                getDescendants(destinationNode.getNode()))){
            descendants.add(node);
        }
        
        for(ProbNode node : ProbNet.getProbNodesOfNodes(
                getAncestors(sourceNode.getNode()))){
            ancestors.add(node);
        }
        
        for(ProbNode source : descendants){
            for (ProbNode destination : ancestors){
            	if (destination.getNode().isChild(source.getNode())){
        			auxiliarNet.removeLink(destination.getVariable(), 
                    		source.getVariable(), true);
                    if(!auxiliarNet.getGraph().existsPath(auxiliarNet.
                    		getProbNode(destination.getName()).getNode(), 
                    		auxiliarNet.getProbNode(source.getName()).
                    			getNode(), true)){
                    	cycleEditions.add(new InvertLinkEdit(learnedNet,
                    			destination.getVariable(), 
                    			source.getVariable(), true));
                    }
                    try {
						auxiliarNet.addLink(destination.getVariable(), 
								source.getVariable(), true);
					} catch (Exception e) {
						e.printStackTrace();
					}
            	}
            	else{
            		if (!learnedNet.getGraph().existsPath(destination.getNode(), 
            				source.getNode(), true))
            		cycleEditions.add(new AddLinkEdit(learnedNet,source.
            				getVariable(), destination.getVariable(), true));
            	}
            }
        }
        
        return cycleEditions;
    }
    
    /** Recursive function to obtain the descendants of a node in the network.
     * 
     * @param parent <code>Node</code> node whose descendants we want to find.
     * @return <code>ArrayList<Node></code> list of descendants
     */
    private ArrayList<Node> getDescendants(Node parent){
        ArrayList<Node> descendants = new ArrayList<Node>();
        ArrayList<Node> children = parent.getChildren();
        
        if ((children == null) || (children.isEmpty())){
            descendants.add(parent);
            return descendants;
        }
            
        for(Node child : children){
            for(Node node : getDescendants(child)){
                descendants.add(node);
            }
        }
        descendants.add(parent);
        
        return descendants;
    }
    
    /** Recursive function to obtain the ancestors of a node in the network.
     * 
     * @param child <code>Node</code> node whose ancestors we want to find.
     * @return <code>ArrayList<Node></code> list of ancestors
     */
    private ArrayList<Node> getAncestors(Node child){
        ArrayList<Node> ancestors = new ArrayList<Node>();
        ArrayList<Node> parents = child.getParents();
        
        if ((parents == null) || (parents.isEmpty())){
            ancestors.add(child);
            return ancestors;
        }
            
        for(Node parent : parents){
            for(Node node : getAncestors(parent)){
                ancestors.add(node);
            }
        }
        ancestors.add(child);
        return ancestors;
    }
    
    
	
	@Override
	/**
	 * This method returns the best n editions (and their associated scores)
	 * that can be done to the network that is being learnt. It uses the
	 * <code> TreeSet </code> structure of possible editions.
	 * 
	 * @param n Number of editions that should be returned. 
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
			boolean onlyAllowedEdits, boolean onlyPositiveEdits, boolean reset) {
		ArrayList<EditAndScorePair> bestEditions = 
			new ArrayList<EditAndScorePair>();
		Iterator<EditAndScorePair> it = editionsTree.descendingIterator();
		EditAndScorePair editAndScore;
		int numberOfEditions = 0;
		
		while ((it.hasNext()) && (numberOfEditions < numEdits)){
			editAndScore = it.next();
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
				learnedNet.getPNESupport().announceEdit(editAndScore.getEdition());
			} catch (Exception e) {
				if(onlyAllowedEdits){
					continue;
				}
				else{
					editAndScore.setViolatedConstraint(e);
				}
			}
			bestEditions.add(editAndScore);
			numberOfEditions++;
		}
		
		return bestEditions;
	}
	
	/**
     * Adds an edition to the the editionsTree and the editionsMap. 
     * @param edition <code>PNEdit</code> edition to store in the cache.
     * @param score double score associated to the edition
     */
    public void addEdition(PNEdit edition, double score){
        double oldScore;
        HashMap<PNEdit, EditAndScorePair> editionsList = 
                new HashMap<PNEdit, EditAndScorePair>();
        String destinationVariable = "", destinationVariable2 = null,
                destinationVariableAux;
        EditAndScorePair editAndScore, oldEditAndScore;
        
        /* Extract the destination variable */
        Class pNEditClass = edition.getClass();
        try{
            if (pNEditClass == AddLinkEdit.class) {
                destinationVariable = ((AddLinkEdit)edition).getVariable2().
                        getName(); 
            }
            else if (pNEditClass == RemoveLinkEdit.class) {
                destinationVariable = ((RemoveLinkEdit)edition).getVariable2().
                        getName(); 
            } 
            else{
            	destinationVariable = ((InvertLinkEdit)edition).getVariable1().
                	getName(); 
                destinationVariable2 = ((InvertLinkEdit)edition).getVariable2().
                	getName();
            }
        } catch (Exception e){
            e.printStackTrace();
        } 
        
        editAndScore = new EditAndScorePair((BaseLinkEdit)edition, score);
        /*search the edition in the nodeEditionsMap of 
         the destination variable */
        destinationVariableAux = destinationVariable;
        while (destinationVariableAux != null){
            if(nodeEditionsMap.containsKey(destinationVariableAux)){
                editionsList = nodeEditionsMap.get(destinationVariableAux);
                if(editionsList.containsKey(edition)){
                    oldScore = editionsList.get(edition).getScore();
                    oldEditAndScore = new EditAndScorePair((BaseLinkEdit)edition, oldScore);
                    editionsTree.remove(oldEditAndScore);
                    editionsTree.add(editAndScore);
                    editionsList.put(edition,editAndScore);
                }
                /* If the edition was not present in the nodeEditionsMap, we
                 * add it there and in the editionsTree.
                 */
                else{
                    editionsTree.add(editAndScore);
                    editionsList.put(edition,editAndScore);
                }
            }
            else{
                editionsList.put(edition, editAndScore);
                nodeEditionsMap.put(destinationVariableAux, editionsList);
                editionsTree.add(editAndScore);
            }
            if(destinationVariableAux.equals(destinationVariable2))
                break;
            
            destinationVariableAux = destinationVariable2;
        }
    }
    
	 /**
     * Removes an edition from the cache, updating
     * the editionsTree and the editionsMap.
     * @param edition <code>PNEdit</code> edition to remove from the cache.
     */
	public void removeEdition(PNEdit edition){
        String destinationVariable = "", destinationVariable2 = null;
        EditAndScorePair editAndScoreToRemove =  null;

        /* Extract the destination variable */
        Class pNEditClass = edition.getClass();
        try{
            if (pNEditClass == AddLinkEdit.class) {
                destinationVariable = ((AddLinkEdit)edition).getVariable2().
                        getName(); 
            }
            else if (pNEditClass == RemoveLinkEdit.class) {
                destinationVariable = ((RemoveLinkEdit)edition).getVariable2().
                        getName(); 
            } 
            else{
            	destinationVariable = ((InvertLinkEdit)edition).getVariable1().
                	getName(); 
                destinationVariable2 = ((InvertLinkEdit)edition).getVariable2().
                	getName();
            }
        } catch (Exception e){
            e.printStackTrace();
        } 
        
        editAndScoreToRemove = nodeEditionsMap.get(destinationVariable).
                get(edition);
        
        /* If the edition to remove was impossible before this edition (i.e., it
         * is not present in the nodeEditionsMap of the destination variable), 
         * there is nothing to do*/
        if (editAndScoreToRemove != null){
            editionsTree.remove(editAndScoreToRemove);
            nodeEditionsMap.get(destinationVariable).remove
                    (edition);
        }
        
        if (destinationVariable2 != null){
            editAndScoreToRemove = nodeEditionsMap.get(destinationVariable2).
                get(edition);
        
            /* If the edition to remove was impossible before this edition (i.e., it
             * is not present in the nodeEditionsMap of the destination variable), 
             * there is nothing to do*/
            if (editAndScoreToRemove != null){
                editionsTree.remove(editAndScoreToRemove);
                nodeEditionsMap.get(destinationVariable2).remove
                        (edition);
            }
        }  
    }
	
	/** Gives the editions associated to the destination node of the last
     * edition done 
     * @param edition <code>PNEdit</code> edition to extract the destination
     * variable whose associated editions are wanted.
 	 */
    public HashMap<PNEdit, EditAndScorePair> getNodeEditions(
    		UndoableEdit edition){
        String destinationVariable = "";
        String destinationVariable2 = null;
        HashMap<PNEdit, EditAndScorePair> result;
        
        /* Extract the destination variable */
        Class pNEditClass = edition.getClass();
        try{
            if (pNEditClass == AddLinkEdit.class) {
                destinationVariable = ((AddLinkEdit)edition).getVariable2().
                        getName(); 
            }
            else if (pNEditClass == RemoveLinkEdit.class) {
                destinationVariable = ((RemoveLinkEdit)edition).getVariable2().
                        getName(); 
            } 
            else{
            	destinationVariable = ((InvertLinkEdit)edition).getVariable1().
                	getName(); 
                destinationVariable2 = ((InvertLinkEdit)edition).getVariable2().
                	getName();
            }
        } catch (Exception e){
            e.printStackTrace();
        } 
        
        result = nodeEditionsMap.get(destinationVariable);
        if (destinationVariable2 != null)
            result.putAll(nodeEditionsMap.get(destinationVariable2));
        return result;
    }

	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
			throws ConstraintViolationException, CanNotDoEditException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent event) {
		UndoableEdit pNEdit = event.getEdit();
		HashMap<PNEdit, EditAndScorePair> editionsToUpdate;

		try {
            cache.updateCache((PNEdit) event.getEdit());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        updatePossibleEditions(pNEdit);
        
		/* Take the editions that are not possible after the last edition, 
         * and remove them form cache */
        for(PNEdit edition : impossibleEditions){
            removeEdition(edition);
        }
         
        /* Update the scores of the editions associated to the destination
         * node of the last edition done updating the cache */
        editionsToUpdate = (HashMap<PNEdit, EditAndScorePair>) 
        	getNodeEditions(pNEdit).clone();
        for(PNEdit editionToScore : editionsToUpdate.keySet()){
        	addEdition(editionToScore,cache.get(editionToScore));
         }
        
        /* Take the editions that can be done after the last edition, score
         * and store the ones with a positive score. */
         for(PNEdit edition : possibleEditions){
        	 addEdition(edition, cache.get(edition));
         }
	}

}
