package org.openmarkov.core.learning.metric;

import java.util.HashMap;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.LinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.learning.metrics.util.MathUtils;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

/** This class implements the Bayesian metric.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class BayesianMetric extends Metric {

    /** HashMap with the score of each node. (We do not want to
     * recalculate the score of all the nodes of the net every time
     * we make an edition)*/
    protected HashMap<String, Double> nodesScores; 

    /** Parameter alpha */
    protected double alpha = 0.5;
    
    //Constructor
    /**
     * After constructing the metric, we evaluate the given net.
     * @param probNet <code>ProbNet</code> to evaluate.
     * @param cases <code>double[][]</code> database cases.
     * @param alpha <code>double</code> alpha parameter.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public BayesianMetric(ProbNet probNet, int[][] cases, double alpha) 
            throws NotEnoughMemoryException {
        super(probNet, cases);
        nodesScores = new HashMap<String, Double>();
        this.alpha = alpha;
        score=score();
    }
    
    /**
     * Scores the associated network. 
     * @return <code>double</code> score of the net 
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public double score() throws NotEnoughMemoryException {
        double newScore = 0;

        for (ProbNode node : probNet.getProbNodes()){
            newScore += nodeScore(node, null, true);
        }
        return newScore;
    }
    
    /**
     * Scores the given node with the new parent given. 
     * @param node <code>ProbNode</code> 
     * @param extraParent <code>ProbNode</code>
     * @param change <code>boolean</code> indicates wheter the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return <code>double</code> score of the node with the given parent
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    protected double nodeScore(ProbNode node, ProbNode extraParent, 
            boolean change) throws NotEnoughMemoryException{
        int numStates = ((Variable) node.getVariable()).getNumStates(); 
        int parentsConfigurations = 1;
        double nodeScore = 0;
        double n_ij;
        double n_ijk;
        int position = 0;
        double[] freq = null;

        if (extraParent != null){
            parentsConfigurations *= ((Variable)
                    extraParent.getVariable()).getNumStates();
        }

        for (ProbNode parent : ProbNet.getProbNodesOfNodes(node.getNode().
                getParents())){
            parentsConfigurations *= ((Variable)
                    parent.getVariable()).getNumStates();
        }

        freq = absoluteFrequenciesExtraParent(node, extraParent).getValues();
        
        for (int j = 0; j < parentsConfigurations; j++){
            n_ij = 0;
            //k-th state of the node
            for (int k = 0; k < numStates; k++){
                if (alpha + freq[position] != 0){
                    n_ijk = freq[position];
                    n_ij += n_ijk + alpha;
                    nodeScore += (MathUtils.lnGamma(alpha + n_ijk));
                }
                position++;
            }
            if (n_ij != 0)
                nodeScore -= MathUtils.lnGamma(n_ij);

            if (alpha != 0){
                nodeScore += MathUtils.lnGamma(numStates * alpha);
                nodeScore -= numStates * MathUtils.lnGamma(alpha);
            }
        }
        
        /* Store the entropy of the node to avoid repeating the calcularions */
        if (change == true)
            nodesScores.put(node.getName(), new Double(nodeScore));
        
        return nodeScore;
    }
    
    /**
     * Scores the given node without the removed parent given. 
     * @param node <code>ProbNode</code> 
     * @param removedParent <code>ProbNode</code>
     * @param change <code>boolean</code> indicates wheter the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return <code>double</code> score of the node without the given parent
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    protected double nodeScoreRemovedLink(ProbNode node, ProbNode removedParent, 
            boolean change) throws NotEnoughMemoryException{
        int numStates = ((Variable) node.getVariable()).getNumStates(); 
        int parentsConfigurations = 1;  
        double nodeScore = 0;
        double n_ij;
        double n_ijk;
        int position = 0;
        double[] freq = null;

        for (ProbNode parent : ProbNet.getProbNodesOfNodes(node.getNode().
                getParents())){
            if (!removedParent.getName().equals(parent.getName())){
                parentsConfigurations *= ((Variable)
                        parent.getVariable()).getNumStates();
            }
        }

        freq = absoluteFrequenciesRemovedParent(node, removedParent).getValues();
        
        //j-th configuration of the parents
        for (int j = 0; j < parentsConfigurations; j++){
            n_ij = 0;
            //k-th state of the node
            for (int k = 0; k < numStates; k++){
                if (alpha + freq[position] != 0){
                    n_ijk = freq[position];
                    n_ij += n_ijk;
                    nodeScore += (MathUtils.lnGamma(alpha + n_ijk));
                }
                position++;
            }
            if (n_ij + (numStates * alpha) != 0)
                nodeScore -= MathUtils.lnGamma(n_ij + (numStates * alpha));

            if (alpha != 0){
                nodeScore += MathUtils.lnGamma(numStates * alpha);
                nodeScore -= numStates * MathUtils.lnGamma(alpha);
            }
        }
        
        /* Store the entropy of the node to avoid repeating the calculations */
        if (change == true)
            nodesScores.put(node.getName(), new Double(nodeScore));
        
        return nodeScore;
    }
    
    @Override
    /**
     * Scores the associated network with the given edition. In fact, this
     * method call another method depending the type of edition it receives.
     * @param edition <code>PNEdit</code> 
     * @return <code>double</code> score of the net with the given edition
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public double score(PNEdit edition) throws NotEnoughMemoryException {
        double newScore = 0;
        
        Class pNEditClass = edition.getClass();
        if (pNEditClass == AddLinkEdit.class) {
            newScore=score((AddLinkEdit) edition, false);
        }
        else if (pNEditClass == RemoveLinkEdit.class){
            newScore=score((RemoveLinkEdit) edition, false);
        }
        else{
        	newScore = score ((InvertLinkEdit) edition, false);
        }
        return newScore;
    }

    /**
     * Scores the associated network with the link given in the received 
     * edition added. We only have to recalculate the score 
     * of the destination node. If an undoable edit happened (that is, if
     * parameter change is true) we update the entropy and dimension of the
     * destination node and the net.
     * @param edition <code>AddLinkEdit</code> 
     * @param change <code>boolean</code> indicates wheter the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return <code>double</code> score of the net with the given edition
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public double score(AddLinkEdit edition, boolean change) 
            throws NotEnoughMemoryException {
        
        ProbNode destinationNode = probNet.getProbNode(edition.getVariable2());
        ProbNode originNode = probNet.getProbNode(edition.getVariable1());
        double lastNodeScore;
        double newNodeScore;
        
        lastNodeScore = nodesScores.get(
                ((ProbNode) destinationNode).getName());
        newNodeScore = nodeScore(destinationNode, originNode, false);
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change == true){
            nodesScores.put(destinationNode.getName(), 
                    new Double(newNodeScore));
        }
        
        return newNodeScore - lastNodeScore;
    }

    /**
     * Scores the associated network with the link given in the received 
     * edition removed. We only have to recalculate the score 
     * of the destination node. If an undoable edit happened (that is, if
     * parameter change is true) we update the entropy and dimension of the
     * destination node and the net.
     * @param edition <code>AddLinkEdit</code> 
     * @param change <code>boolean</code> indicates wheter the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return <code>double</code> score of the net with the given edition
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    protected double score(RemoveLinkEdit edition, boolean change) 
            throws NotEnoughMemoryException {
        ProbNode destinationNode = probNet.getProbNode(
                edition.getVariable2());
        ProbNode originNode = probNet.getProbNode(edition.getVariable1());
        double lastNodeScore;
        double newNodeScore;
        
        lastNodeScore = nodesScores.get(destinationNode.getName());
        newNodeScore = nodeScoreRemovedLink(destinationNode, originNode, false);
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change == true){
            nodesScores.put(destinationNode.getName(), 
                    new Double(newNodeScore));
        }
        
        return newNodeScore - lastNodeScore;
    }
    
    /**
     * Scores the associated network with the link given in the received 
     * edition inverted. We have to recalculate the scores 
     * of the destination nodes before and after the inversion. If an undoable 
     * edit happened (that is, if parameter change is true) we update the 
     * entropy and dimension of the destination node and the net.
     * @param edition <code>AddLinkEdit</code> 
     * @param change <code>boolean</code> indicates whether the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return <code>double</code> score of the net with the given edition
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    protected double score(InvertLinkEdit edition, boolean change) 
            throws NotEnoughMemoryException {
        ProbNode initialDestinationNode = probNet.getProbNode(
                edition.getVariable2());
        ProbNode initialOriginNode = 
        	probNet.getProbNode(edition.getVariable1());
        double lastNodeScore;
        double newNodeScore;
        double result;
        
        lastNodeScore = nodesScores.get(initialDestinationNode.getName());
        newNodeScore = nodeScoreRemovedLink(initialDestinationNode, 
        		initialOriginNode, false);
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change == true){
            nodesScores.put(initialDestinationNode.getName(), 
                    new Double(newNodeScore));
        }
        
        result = newNodeScore - lastNodeScore;
        
        lastNodeScore = nodesScores.get(
                ((ProbNode) initialOriginNode).getName());
        newNodeScore = nodeScore(initialOriginNode, initialDestinationNode,
                false);
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change == true){
            nodesScores.put(initialOriginNode.getName(), 
                    new Double(newNodeScore));
        }
        
        result += (newNodeScore - lastNodeScore);
        return result;
    }
    
    /** An undoable edit will happen.
     * @param event <code>UndoableEditEvent</code> that will happen
     */
    public void undoableEditWillHappen(PNUndoableEditEvent event){
    }
    
    /** 
     * An undoable edit happened. We have to update the copy of the net and
     * score this new net.
     * @param event <code>UndoableEditEvent</code> that happened
     */
    public void undoableEditHappened(UndoableEditEvent event) {
        
        UndoableEdit pNEdit = event.getEdit();
        Class pNEditClass = ((PNEdit)pNEdit).getClass();

        if(pNEditClass == LinkEdit.class)
        {
        	Variable tail = null;
    		for (Variable node : probNet.getVariables()){
    			if(((LinkEdit)pNEdit).getProbNode1().getName().equals(node.getBaseName()))
    				tail = node;
    		}
        	Variable head = null;
    		for (Variable node : probNet.getVariables()){
    			if(((LinkEdit)pNEdit).getProbNode2().getName().equals(node.getBaseName()))
    				head = node;
    		}
    		
    		if(((LinkEdit)pNEdit).isAdd())
    			pNEdit = new AddLinkEdit(probNet, head, tail, ((LinkEdit)pNEdit).isDirected());
    		else
    			pNEdit = new RemoveLinkEdit(probNet, head, tail, ((LinkEdit)pNEdit).isDirected());
    		
    		pNEditClass = ((PNEdit)pNEdit).getClass();
        }
        
        try{
            if (pNEditClass == AddLinkEdit.class) {
                score = score((AddLinkEdit) pNEdit, true);
                probNet.addLink(((AddLinkEdit)pNEdit).getVariable1(), 
                        ((AddLinkEdit)pNEdit).getVariable2(), true);
            }
            else if (pNEditClass == RemoveLinkEdit.class) {
                score = score((RemoveLinkEdit) pNEdit, true);
                probNet.removeLink(((RemoveLinkEdit)pNEdit).getVariable1(), 
                        ((RemoveLinkEdit)pNEdit).getVariable2(), true);
            } 
            else if (pNEditClass == InvertLinkEdit.class){
            	score = score((InvertLinkEdit) pNEdit, true);
            	probNet.invertLink(((InvertLinkEdit)pNEdit).getVariable1(), 
                        ((InvertLinkEdit)pNEdit).getVariable2(), true);
            }
        } catch (Exception e){
            e.printStackTrace();
        } 
    }

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub
		
	}
}
