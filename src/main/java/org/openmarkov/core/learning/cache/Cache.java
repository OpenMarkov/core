package org.openmarkov.core.learning.cache;

import java.util.ArrayList;
import java.util.HashMap;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.BaseLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.learning.metric.Metric;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

/** This class implements a cache for the scores of the different possible
 * editions during the learning process following the approach of Weka.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class Cache{

	/** change in score due to adding an arc **/
	protected double [][] deltaScoreAdd;
	/** change in score due to deleting an arc **/
	protected double [][] deltaScoreDel;
	
	/** number assigned to each variable */
	protected HashMap<Variable, Integer> orderedVariables;

	protected int numNodes;

    /** Best score found in cache while searching for the best edition */
	protected double bestPartialScore;
	
	BaseLinkEdit bestEdition;
	
	/** Score of the last edition returned to the algorithm. We use
	 * this attribute in order to return to the algorithm another edition
	 * when the best one is not allowed.
	 */
	protected double lastBestPartialScore = Double.POSITIVE_INFINITY;
	
	/** Array with the best editions that have been not been done by the
	 * algorithm because they violate the ModelNetworkConstraint
	 */
	protected ArrayList<BaseLinkEdit> lastBestEditions = new ArrayList<BaseLinkEdit>();

    /** A copy of the received <code>ProbNet</code>. */
    protected ProbNet learnedNet;

    /** Metric used by the algorithm */
    protected Metric metric;
	
	/**
     *
     * @param learnedNet learnedNet it is going to be learnt.
     * @param metric metric used by the algorithm.
     * @param numNodes number of nodes of the learnedNet.
     * @param fixInitialLinks Flag that shows whether the links of the initial
     * net should be kept during the learning process.
     */
	public Cache(ProbNet learnedNet, Metric metric) {
		int numNodes = learnedNet.getNumNodes();

		orderedVariables = new HashMap<Variable,Integer>();
        deltaScoreAdd = new double [numNodes][numNodes];
		deltaScoreDel = new double [numNodes][numNodes];
		
		int i = 0;
		for (Variable var : learnedNet.getVariables()){
			orderedVariables.put(var, i);
			i++;
		}
		this.numNodes = numNodes;
        this.learnedNet = learnedNet;
        this.metric = metric;
	}

    /**
     * Method to initialize the cache scoring the possible editions to the
     * initial net.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public void initCache() throws NotEnoughMemoryException{
        metric.getScore();
        AddLinkEdit scoredAddEdition;
        RemoveLinkEdit scoredRemovedEdition;

        for (Variable var1 : learnedNet.getVariables()){
            for (Variable var2 : learnedNet.getVariables()){
                if ((!var1.equals(var2)) && (!isFixedLink(var1, var2))){
                    if (!learnedNet.getProbNode(var2).getNode().isParent(
    					learnedNet.getProbNode(var1).getNode())){
                        scoredAddEdition = new AddLinkEdit(learnedNet, var1, 
                        		var2, true);
                        put(scoredAddEdition, metric.score(
                                scoredAddEdition));
                    }
                    else{
                        scoredRemovedEdition = new RemoveLinkEdit(learnedNet, 
                        		var1, var2, true);
                        put(scoredRemovedEdition, metric.score(
                                scoredRemovedEdition));
                    }
                }
            }
        }
    }

	/** Set the score of a given edition.
	 * @param PNEdit edition whose value is going to be put in the cache
	 * @param fValue value to put in cache
	 */
	public void put(PNEdit edit, double fValue) {
		if(edit.getClass() == AddLinkEdit.class) 
			deltaScoreAdd[orderedVariables.get(((AddLinkEdit) edit).
                getVariable1())][orderedVariables.get(((AddLinkEdit) edit).
                        getVariable2())] = fValue;
        else 
        	deltaScoreDel[orderedVariables.get(((RemoveLinkEdit) edit).
                getVariable1())][orderedVariables.get(
                        ((RemoveLinkEdit) edit).getVariable2())] = fValue;
	} // put 

	/** Obtain the score of a given edition.
	 * @param PNEdit edition whose score we want to get.
	 * @return cache value
	 */
	public double get(PNEdit edit) {
		
		if(edit.getClass() == AddLinkEdit.class)
			return deltaScoreAdd[orderedVariables.get(((AddLinkEdit) edit).
				getVariable1())][orderedVariables.get(((AddLinkEdit) edit).
				getVariable2())];
		else if(edit.getClass() == RemoveLinkEdit.class)
			return deltaScoreDel[orderedVariables.get(((RemoveLinkEdit)
                    edit).getVariable1())][orderedVariables.get(
				((RemoveLinkEdit) edit).getVariable2())];
		else
			return (deltaScoreDel[orderedVariables.get(((InvertLinkEdit)
                edit).getVariable1())][orderedVariables.get(
				((InvertLinkEdit) edit).getVariable2())] + 
				deltaScoreAdd[orderedVariables.get(((InvertLinkEdit) edit).
				getVariable2())][orderedVariables.get(
				((InvertLinkEdit) edit).getVariable1())]);
	} // get

    /**
     * Method to check whether the link from var1 to var2 is a fixed link.
     * @param var1 origin variable
     * @param var2 destination variable
     * @return true if the link is fixed, otherwise false.
     */
    private boolean isFixedLink(Variable var1, Variable var2){
        if (deltaScoreDel[orderedVariables.get(var1)]
                [orderedVariables.get(var2)] == Double.NEGATIVE_INFINITY){
            return true;
        }
        return false;
    }

    /**
     * Method to obtain the edition with the highest associated score.
     * @param learnedNet learnedNet learnt.
     * @return <code>PNEdit</code> edition with the highest associated score.
     */
	public BaseLinkEdit getOptimalEdition(){
    	bestPartialScore = Double.NEGATIVE_INFINITY;
    	bestEdition = null;
    	
    	// Add???
		findBestArcToAdd();
		// Delete???
		findBestArcToDelete();
		// Reverse???
		findBestArcToReverse();
			
		lastBestPartialScore = bestPartialScore;
		lastBestEditions.add(bestEdition);
        return bestEdition;
    }

    /**
     * Method to obtain the <code>AddLinkEdition</code> with a highest score
     * that can be done to the learnedNet.
     * @return <code>PNEdit</code> <code>AddLinkEdition</code> with a highest
     * score that can be done to the learnedNet.
     */
	private void findBestArcToAdd(){
		double score;
		
		for (Variable head : learnedNet.getVariables()){
			for (Variable tail : learnedNet.getVariables()){
				score = deltaScoreAdd[orderedVariables.get(tail)]
					                     [orderedVariables.get(head)];
				/* Check whether the score is the best to the moment and
				 * whether this edition has not been rejected by the algorithm
				 * because it violates the ModelNetworkconstraint.
				 */
				if ((score > bestPartialScore)
						&& (score <= lastBestPartialScore) 
						&& !lastBestEditions.contains(new AddLinkEdit(learnedNet, 
								tail, head, true))){
					if (!(learnedNet.getProbNode(head).getNode().isParent(
    					learnedNet.getProbNode(tail).getNode())) &&
                        (!learnedNet.getGraph().existsPath(learnedNet.
							getProbNode(head).getNode(), learnedNet.
							getProbNode(tail).getNode(), true))){
						bestEdition = new AddLinkEdit(learnedNet, tail, head, 
								true);
						bestPartialScore = score;
					}
				}
			}
		}
	}

    /**
     * Method to obtain the <code>RemoveLinkEdition</code> with a highest score
     * that can be done to the learnedNet. This <code>RemoveLinkEdition</code> 
     * is returned only in case that its associated score is higher than the 
     * score of the bestEdition passed as argument.
     * @return <code>PNEdit</code> <code>RemoveLinkEdition</code> with a highest
     * score that can be done to the learnedNet.
     */
	private void findBestArcToDelete(){
		double score;
		
		for (Variable head : learnedNet.getVariables()){
			for (Variable tail : learnedNet.getVariables()){
				score = deltaScoreDel[orderedVariables.get(tail)]
				                      [orderedVariables.get(head)];
				/* Check whether the score is the best to the moment and
				 * whether this edition has not been rejected by the algorithm
				 * because it violates the ModelNetworkconstraint.
				 */
				if ((score > bestPartialScore) 
						&& (score <= lastBestPartialScore) 
						&& !lastBestEditions.contains(new RemoveLinkEdit(
								learnedNet, tail, head, true))){
                    if (learnedNet.getProbNode(head).getNode().isParent(
    					learnedNet.getProbNode(tail).getNode())){
                        bestEdition = new RemoveLinkEdit(learnedNet, tail, head,
                                true);
                        bestPartialScore = score;
                    }
				}
			}
		}
	}

    /**
     * Method to obtain the <code>InvertLinkEdition</code> with a highest score
     * that can be done to the learnedNet. This <code>InvertLinkEdition</code> 
     * is returned only in case that its associated score is higher than the 
     * score of the bestEdition passed as argument.
     * @return <code>PNEdit</code> <code>InvertLinkEdition</code> with a highest
     * score that can be done to the learnedNet.
     */
	private void findBestArcToReverse() {

		double score;
		
		for (Variable head : learnedNet.getVariables()){
			for (Variable tail : learnedNet.getVariables()){
				score = deltaScoreDel[orderedVariables.get(tail)]
		                      [orderedVariables.get(head)] + 
									deltaScoreAdd[orderedVariables.get(head)]
						                 [orderedVariables.get(tail)];
				/* Check whether the score is the best to the moment and
				 * whether this edition has not been rejected by the algorithm
				 * because it violates the ModelNetworkconstraint.
				 */
				if ((score > bestPartialScore) 
						&& (score <= lastBestPartialScore) 
						&& !lastBestEditions.contains(new InvertLinkEdit(
								learnedNet, tail, head, true))){
					if (reverseArcMakesSense(learnedNet, tail, head)){
						bestEdition = new InvertLinkEdit(learnedNet, tail, head, 
								true);
						bestPartialScore = score;
					}
				}
			}
		}
	}

    /**
     * Method to check if the inversion of the link from tail to head is legal
     * in the given learnedNet.
     * @param learnedNet
     * @param tail origin node of the link to invert.
     * @param head destination node of the link to invert.
     * @return true if the arc can be inverted, false otherwise.
     */
	private boolean reverseArcMakesSense(ProbNet learnedNet, Variable tail, 
			Variable head){
		boolean makesSense = false;
        if (learnedNet.getProbNode(head).getNode().isParent(
			learnedNet.getProbNode(tail).getNode())){
        	try {
				learnedNet.removeLink(tail, head, true);
				
	        	if(!learnedNet.getGraph().existsPath(learnedNet.
	                    getProbNode(tail).getNode(), learnedNet.
	                    getProbNode(head).getNode(), true)){
	                    learnedNet.addLink(head, tail, true);
	                    learnedNet.removeLink(head, tail, true);
	                    makesSense = true;
	        	}
	        } catch (Exception ex) 
	        {
	        }finally
	        {
	        	try
	        	{
	        		learnedNet.addLink(tail, head, true);
	        	}catch(NodeNotFoundException e){}
	        }
        }
        return makesSense;
     }

    /**
     * Method to update the cache after doing an edition to the learnedNet.
     * @param bestEdition <code>PNEdit</code> last edition done to the 
     * learnedNet.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public void updateCache(PNEdit bestEdition) 
            throws NotEnoughMemoryException, Exception{
    	Variable head, head2 = null;
    	PNEdit updatedEdition;
    	
    	/* If we update the cache is because an edition has been done.
    	 * Therefore, the array lastBestEditions should be cleared. 
    	 */
    	lastBestPartialScore = Double.POSITIVE_INFINITY;
    	lastBestEditions.clear();

        /* Obtain the destination node of the given edition*/
    	if(bestEdition.getClass() == AddLinkEdit.class){
//            learnedNet.addLink(((AddLinkEdit)bestEdition).getVariable1(),
//                        ((AddLinkEdit)bestEdition).getVariable2(), true);
    		head = ((AddLinkEdit) bestEdition).getVariable2();
    	}
    	else if(bestEdition.getClass() == RemoveLinkEdit.class){
//            learnedNet.removeLink(((RemoveLinkEdit)bestEdition).getVariable1(),
//                        ((RemoveLinkEdit)bestEdition).getVariable2(), true);
    		head = ((RemoveLinkEdit) bestEdition).getVariable2();
    	}
    	else{
//            learnedNet.invertLink(((InvertLinkEdit)bestEdition).getVariable1(),
//                        ((InvertLinkEdit)bestEdition).getVariable2(), true);
    		head = ((InvertLinkEdit) bestEdition).getVariable2();
            head2 = ((InvertLinkEdit) bestEdition).getVariable1();
        }

        /* Score all the links that have as destination the destination node
         * of the bestEdition */
    	for (Variable tail: learnedNet.getVariables()){
    		if ((!tail.equals(head)) && (!isFixedLink(tail, head))){
    			if (!learnedNet.getProbNode(head).getNode().isParent(
    					learnedNet.getProbNode(tail).getNode())){
    				updatedEdition = new AddLinkEdit(learnedNet, tail, head, 
    						true);
                    put(updatedEdition, metric.score(
    						updatedEdition));
    			}
    			else{
    				updatedEdition = new RemoveLinkEdit(learnedNet, tail, head,
                            true);
    				put(updatedEdition, metric.score(
    						updatedEdition));
    			}
    		}
    	}

        /* If head2 is not null is because we have a link inversion. In this
         * case, we have to update the entries of the cache of both origin
         * and destination node of the original links*/
        if (head2 != null){
            for (Variable tail: learnedNet.getVariables()){
                if ((!tail.equals(head2)) && (!isFixedLink(tail, head2))){
                    if (!learnedNet.getProbNode(head2).getNode().isParent(
                            learnedNet.getProbNode(tail).getNode())){
                        updatedEdition = new AddLinkEdit(learnedNet, tail, 
                        		head2, true);
                        put(updatedEdition, metric.score(
                                updatedEdition));
                    }
                    else{
                        updatedEdition = new RemoveLinkEdit(learnedNet, tail,
                                head2, true);
                        put(updatedEdition, metric.score(
                                updatedEdition));
                    }
                }
            }
        }
    }
    
    public void resetParameters(){
    	lastBestPartialScore = Double.POSITIVE_INFINITY;
    	lastBestEditions.clear();
    }
}
