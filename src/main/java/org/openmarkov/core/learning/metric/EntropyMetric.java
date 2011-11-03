package org.openmarkov.core.learning.metric;

import java.util.HashMap;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

/** This class implements the Entropy metric.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class EntropyMetric extends Metric {
	
    /** HashMap with the entropy of each node. (We do not want to
     * recalculate the entropy of all the nodes of the net every time
     * we make an edition)*/
    protected HashMap<String, Double> nodesEntropies; 
    
    /** HashMap with the dimension of each node. (We do not want to
     * recalculate the dimension of all the nodes of the net every time
     * we make an edition)*/
    protected HashMap<String, Double> nodesDimensions;
    
    //Constructor
    /**
     * After constructing the metric, we evaluate the given net.
     * @param probNet <code>ProbNet</code> to evaluate.
     * @param cases <code>double[][]</code> database cases.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public EntropyMetric(ProbNet probNet, int[][] cases) 
            throws NotEnoughMemoryException {
        super(probNet, cases);
        nodesEntropies = new HashMap<String, Double>();
        nodesDimensions = new HashMap<String, Double>();
        score=score();
    }

    /**
     * Scores the associated network.
     * @return <code>double</code> score of the net 
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public double score() throws NotEnoughMemoryException {     
        return calculateEntropy();
    }
    
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
        	newScore = score((InvertLinkEdit) edition, false);
        }
        return newScore;
    }
    
    /**
     * Scores the associated network with the link given in the received 
     * edition added. We only have to recalculate the entropy and dimension 
     * of the destination node. If an undoable edit happened (that is, if
     * parameter change is true) we update the entropy and dimension of the
     * destination node and the net.
     * @param edition <code>AddLinkEdit</code> 
     * @param change <code>boolean</code> indicates wheter the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return <code>double</code> score of the net with the given edition
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    protected double score(AddLinkEdit edition, boolean change) 
            throws NotEnoughMemoryException {
        
        ProbNode destinationNode = probNet.getProbNode(edition.getVariable2());
        ProbNode originNode = probNet.getProbNode(edition.getVariable1());
        double lastNodeEntropy;
        double newNodeEntropy;
        
        /* To calculate the new entropy, we subtract the last entropy of
         * the destination node and sum the new entropy of this node*/
        lastNodeEntropy = nodesEntropies.get(
                ((ProbNode) destinationNode).getName());
        newNodeEntropy = nodeEntropy(destinationNode, originNode,false); 
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change == true){
            nodesEntropies.put(destinationNode.getName(), 
                    new Double(newNodeEntropy));
        }
        
        return newNodeEntropy - lastNodeEntropy;
    }
    
    /**
     * Scores the associated network with the link given in the received 
     * edition removed. We only have to recalculate the entropy and dimension 
     * of the destination node. If an undoable edit happened (that is, if
     * parameter change is true) we update the entropy and dimension of the
     * destination node and the net.
     * @param edition <code>AddLinkEdit</code> 
     * @param change <code>boolean</code> indicates whether the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return <code>double</code> score of the net with the given edition
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    protected double score(RemoveLinkEdit edition, boolean change) 
            throws NotEnoughMemoryException {
        
        ProbNode destinationNode = probNet.getProbNode(edition.getVariable2());
        ProbNode originNode = probNet.getProbNode(edition.getVariable1());
        double newNodeEntropy;
        double lastNodeEntropy;
        
        /* To calculate the new entropy, we subtract the last entropy of
         * the destination node and sum the new entropy of this node*/
        lastNodeEntropy = nodesEntropies.get(
                ((ProbNode) destinationNode).getName());
        newNodeEntropy = nodeEntropyRemovedLink(destinationNode, originNode,
                false);
        
        /*If change is true it's because we have to update the probNet values*/
        if (change) {
            nodesEntropies.put(destinationNode.getName(), 
                    new Double(newNodeEntropy));
        }
        
        return newNodeEntropy - lastNodeEntropy;
    }
    
    /**
     * Scores the associated network with the link given in the received 
     * edition inverted. We have to recalculate the entropy and dimension 
     * of the destination nodes before and after the inversion. If an 
     * undoable edit happened (that is, if parameter change is true) we 
     * update the entropy and dimension of the destination node and the net.
     * @param edition <code>AddLinkEdit</code> 
     * @param change <code>boolean</code> indicates wheter the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return <code>double</code> score of the net with the given edition
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    protected double score(InvertLinkEdit edition, boolean change) 
            throws NotEnoughMemoryException {
        
        ProbNode initialDestinationNode = probNet.getProbNode(
                edition.getVariable2());
        ProbNode initialOriginNode = probNet.getProbNode(
                edition.getVariable1());
        double lastNodeEntropy, newNodeEntropy, result;
        
        /* We first compute the new score of the original destination node*/
        lastNodeEntropy = nodesEntropies.get(
                initialDestinationNode.getName());
        newNodeEntropy = nodeEntropyRemovedLink(initialDestinationNode, 
        		initialOriginNode, false);
        
        /*If change is true it's because we have to update the probNet values*/
        if (change == true){
            nodesEntropies.put(initialDestinationNode.getName(), 
                    new Double(newNodeEntropy));
        }
        
        result = newNodeEntropy - lastNodeEntropy;
        
        /* We compute the final destination node */
        lastNodeEntropy = nodesEntropies.get(initialOriginNode.getName());
        newNodeEntropy = nodeEntropy(initialOriginNode, initialDestinationNode, 
        		false); 
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change == true){
            nodesEntropies.put(initialOriginNode.getName(), 
                    new Double(newNodeEntropy));
        }
        
        result += (newNodeEntropy - lastNodeEntropy);
        return result;
    }

    /**
     * Calculates the entropy of the net as the sum of entropies of each
     * node. It is only used the first time we score the net, so we make 
     * all the calculations.
     * @throws NotEnoughMemoryException
     */
    protected double calculateEntropy() throws NotEnoughMemoryException {
        
        double newEntropy = 0;

        for (ProbNode node : probNet.getProbNodes()){
            newEntropy += nodeEntropy(node,null,true);
        }
        return newEntropy;
    }
    
    /**
     * Calculates the entropy of the given node. If param extraParent is not
     * null, the calculation is done as this node was a parent of the given 
     * node in the probNet. 
     * @param node <code>ProbNode</code> whose entropy we want to calculate
     * @param extraParent <code>ProbNode</code> 
     * @param change <code>boolean</code> indicates wheter the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return double entropy of the given node
     */
    protected double nodeEntropy(ProbNode node, ProbNode extraParent, 
            boolean change) 
            throws NotEnoughMemoryException {
        
        int numStates = ((Variable) node.getVariable()).getNumStates(); 
        int parentsConfigurations = 1; 
        double nodeEntropy = 0;
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

        //j-th configuration of the parents
        for (int j = 0; j < parentsConfigurations; j++){
                n_ij = 0;
                for (int k = 0; k < numStates; k++)
                    n_ij += freq[position+k];
                
                for (int k = 0; k < numStates; k++){
                    n_ijk = freq[position];
                    if (n_ijk > 0){
                        nodeEntropy += n_ijk * Math.log(n_ijk/n_ij);
                    }
                    position++;
                }
        }

        /* Store the entropy of the node to avoid repeating the calcularions */
        if (change == true)
            nodesEntropies.put(node.getName(), new Double(nodeEntropy));
        
        return nodeEntropy;
    }
    
    /**
     * Calculates the entropy of the given node. If param removedParent is not
     * null, the calculation is done as this node was not parent of the given 
     * node in the probNet. 
     * @param node <code>ProbNode</code> whose entropy we want to calculate
     * @param removedParent <code>ProbNode</code> 
     * @param change <code>boolean</code> indicates wheter the edition is 
     * definitive (UndoableEditHappend called this method) or not.
     * @return double entropy of the given node
     */
    protected double nodeEntropyRemovedLink(ProbNode node, 
            ProbNode removedParent, boolean change) 
            throws NotEnoughMemoryException {
        int numStates = ((Variable) node.getVariable()).getNumStates(); //r_i
        int parentsConfigurations = 1;  //q_i
        double nodeEntropy = 0;
        double n_ij;
        double n_ijk;
        int position =0;	
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
                n_ij=0;
                for (int k = 0; k < numStates; k++)
                    n_ij += freq[position+k];
                
                for (int k = 0; k < numStates; k++){
                    n_ijk = freq[position];
                    if (n_ijk > 0){
                        nodeEntropy += n_ijk * Math.log(n_ijk/n_ij);
                    }
                    position++;
                }
        }

        /* Store the entropy of the node to avoid repeating the calcularions */
        if (change == true)
            nodesEntropies.put(node.getName(), new Double(nodeEntropy));
        
        return nodeEntropy;
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
