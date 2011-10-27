package org.openmarkov.core.learning.metrics;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

/** This class implements the BD metric.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class BDMetric extends BayesianMetric {
    
    //Constructor
    /**
     * After constructing the metric, we evaluate the given net.
     * @param probNet <code>ProbNet</code> to evaluate.
     * @param cases <code>double[][]</code> database cases.
     * @param double alpha. alpha parameter
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public BDMetric(ProbNet probNet, int[][] cases, double alpha) 
            throws NotEnoughMemoryException {
        super(probNet, cases, alpha);
    }
    
    @Override
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
        int numParents = node.getNode().getNumParents();
        int parentsConfigurations = 1;  
        double sumStates = 0;
        double nodeScore = 0;
        double n_ij;
        double n_ijk;
        int position = 0;	
        double[] freq = null;
        int indexParent = 0;
        
        if (extraParent != null)
            numParents++;
        int[] indexParents = new int[numParents];
        
        ArrayList<Variable> variables = new ArrayList<Variable>(); 
        variables.add((Variable) node.getVariable());
        
        if ((numParents == 0) && (extraParent == null)){
            parentsConfigurations = 1;
        }
        else{   
            if (extraParent != null){
                indexParents[0] = probNet.getProbNodes().indexOf(extraParent);
                variables.add((Variable) extraParent.getVariable());
                parentsConfigurations *= ((Variable) 
                        extraParent.getVariable()).getNumStates();
                indexParent = 1;
            }
 
            for (ProbNode parent : ProbNet.getProbNodesOfNodes(node.getNode().
                    getParents())){
                variables.add((Variable) parent.getVariable());  
                indexParents[indexParent] = probNet.getProbNodes().indexOf(
                        parent);
                parentsConfigurations *= ((Variable) 
                        parent.getVariable()).getNumStates();
                indexParent++;
            }
        }
        
        freq = absoluteFrequencies(node, parentsConfigurations, 
                variables, indexParents, node.getVariable().getNumStates()).
                getValues();
        //j-th configuration of the parents
        for (int j = 0; j < parentsConfigurations; j++){
            n_ij = 0;
            sumStates = 0;
            //k-th state of the node
            for (int k = 0; k < numStates; k++){
                n_ijk = freq[position];
                n_ij += n_ijk;
                sumStates += (MathUtils.lnGamma((1.0/(numStates *
                        parentsConfigurations)) + n_ijk));
                position++;
            }
            nodeScore += (MathUtils.lnGamma(1.0/parentsConfigurations)) -
                MathUtils.lnGamma(n_ij + (1.0/parentsConfigurations)) -
                numStates * MathUtils.lnGamma(1.0/(numStates * 
                		parentsConfigurations)) + sumStates; 
        }
        
        /* Store the entropy of the node to avoid repeating the calcularions */
        if (change == true)
            nodesScores.put(node.getName(), new Double(nodeScore));
        
        return nodeScore;
    }
    
    @Override
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
        int numParents = node.getNode().getNumParents();
        int[] indexParents = new int[numParents-1];
        int parentsConfigurations = 1;  
        double sumStates = 0;
        double nodeScore = 0;
        double n_ij;
        double n_ijk;
        int position = 0;	
        double[] freq = null;
        
        ArrayList<Variable> variables = new ArrayList<Variable>(); 
        variables.add(node.getVariable());
            
        if (numParents == 1){
            parentsConfigurations = 1;
        }
        else{
            int i = 0;
            for (ProbNode parent : ProbNet.getProbNodesOfNodes(node.getNode().
                    getParents())){
                if (!removedParent.getName().equals(parent.getName())){
                    indexParents[i] = probNet.getProbNodes().indexOf(parent);
                    variables.add((Variable) parent.getVariable());
                    parentsConfigurations *= ((Variable) 
                            parent.getVariable()).getNumStates();
                    i++;
                }
            }
        }
        
        freq = absoluteFrequencies(node, parentsConfigurations, 
                variables, indexParents, node.getVariable().getNumStates()).
                getValues();
        
        //j-th configuration of the parents
        for (int j = 0; j < parentsConfigurations; j++){
            n_ij = 0;
            sumStates = 0;
            //k-th state of the node
            for (int k = 0; k < numStates; k++){
                n_ijk = freq[position];
                n_ij += n_ijk;
                sumStates += (MathUtils.lnGamma((1.0/(numStates *
                        parentsConfigurations)) + n_ijk));
                position++;
            }
            nodeScore += (MathUtils.lnGamma(1.0/parentsConfigurations)) -
                MathUtils.lnGamma(n_ij + (1.0/parentsConfigurations)) -
                numStates * MathUtils.lnGamma(1.0/(numStates * 
                		parentsConfigurations)) + sumStates;
        }
        
        /* Store the entropy of the node to avoid repeating the calcularions */
        if (change == true)
            nodesScores.put(node.getName(), new Double(nodeScore));
        
        return nodeScore;
    }
}
