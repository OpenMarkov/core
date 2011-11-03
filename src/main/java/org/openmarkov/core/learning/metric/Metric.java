package org.openmarkov.core.learning.metric;

import java.util.ArrayList;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/** This abstract class defines the basic elements of a metric.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public abstract class Metric implements PNUndoableEditListener {
	
    // Attributes
    /** A copy of the received <code>ProbNet</code>. */
    protected ProbNet probNet;
    
    /** Score of the associated net */
    protected double score;

    /** Database cases. */
    protected int[][] cases;

    // Constructor
    /** @param inputProbNet <code>ProbNet</code> The graph to which the 
     * algorithm will be applied.
     * @param cases <code>double[][]</code> database cases.  */
    public Metric(ProbNet inputProbNet, int[][] cases) {
            this.probNet = inputProbNet.copy();
            this.cases = cases;
    }
    
    //Methods
    /**
     * Scores the associated network with the given edition.
     * @param edition <code>PNEdit</code> 
     * @return <code>double</code> score of the net with the given edition
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public abstract double score(PNEdit edition) 
            throws NotEnoughMemoryException;

    /**
     * Scores the associated network
     * @return <code>double</code> score of the net
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public abstract double score() 
            throws NotEnoughMemoryException;
    
    /**
     * @return <code>double</code> score of the associated net.
     */
    public double getScore(){
       return score;
    }

    /**
     * Calculate the absolute frequencies in the database of each of the
     * configurations of the given node and its parents.
     * @param probNode <code>ProbNode</code> whose frequencies we want to 
     * calculate.
     * @param parentsConfigurations product of the number of states of the
     * parents.
     * @param variables <code>ArrayList</code> formed by the variable associated
     * to the given node and the variables associated to its parents.
     * @param indexesOfParents <code>int[]</code> indexes of the parents in the
     * probNet list of nodes.
     * @param numValues number of states of the given node
     * @return <code>TablePotential</code> with the absolute frequencies in
     * the database of each of the configurations of the given node and its
     * parents.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public TablePotential absoluteFrequencies(ProbNode probNode, 
            int parentsConfigurations, ArrayList<Variable> variables,
            int[] indexesOfParents, int numValues) 
    		throws NotEnoughMemoryException {
        TablePotential absoluteFreqPotential = new TablePotential(
        		variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double[] absoluteFreqs = absoluteFreqPotential.getValues();
        double iCPT;
        int iParent, iNode = probNet.getProbNodes().indexOf(
                probNet.getProbNode(probNode.getVariable())); 

        // Initialize the table
        for (int i = 0; i < parentsConfigurations * numValues; i++){
            absoluteFreqs[i] = 0;
        }
        
        variables.remove(0);
        // Compute the absolute frequencies
        for (int i = 0; i < cases.length; i++){
            iCPT = 0;
            int j = 0;
            for (ProbNode parent : probNet.getProbNodes(variables)){
                iParent = indexesOfParents[j];
                iCPT = iCPT * parent.getVariable().getNumStates() + 
                        cases[i][iParent];
                j++;
            }
            absoluteFreqs[numValues * ((int) iCPT) + (int) cases[i][iNode]]++;
        }
        return absoluteFreqPotential;
    }

    /**
     * Calculate the absolute frequencies in the database of each of the
     * configurations of the given node and its parents and a given extra
     * parent.
     * @param node <code>ProbNode</code> whose frequencies we want to calculate.
     * @param extraParent <code>ProbNode</code>
     * @return <code>TablePotential</code> with the absolute frequencies in
     * the database of each of the configurations of the given node and its
     * parents and a given extra parent.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public TablePotential absoluteFrequenciesExtraParent(ProbNode node, 
            ProbNode extraParent) throws NotEnoughMemoryException{
        int parentsConfigurations = 1;
        int indexOfParent = 0;
        int numParents = node.getNode().getNumParents();
        
        if (extraParent != null)
            numParents++;
        int[] indexesOfParents = new int[numParents];
        
        ArrayList<Variable> variables = new ArrayList<Variable>(); 
        variables.add((Variable) node.getVariable());
        
        if ((numParents == 0) && (extraParent == null)){
            parentsConfigurations = 1;
        }
        else{   
            if (extraParent != null){
                indexesOfParents[0] = probNet.getProbNodes().
                	indexOf(extraParent);
                variables.add((Variable) extraParent.getVariable());
                parentsConfigurations *= ((Variable) 
                        extraParent.getVariable()).getNumStates();
                indexOfParent = 1;
            }
 
            for (ProbNode parent : ProbNet.getProbNodesOfNodes(node.getNode().
                    getParents())){ 
                variables.add((Variable) parent.getVariable());  
                indexesOfParents[indexOfParent] = probNet.getProbNodes().
                	indexOf(probNet.getProbNode(parent.getVariable()));
                parentsConfigurations *= ((Variable) 
                        parent.getVariable()).getNumStates();
                indexOfParent++;
            }
        }
        
        return absoluteFrequencies(node, parentsConfigurations, 
                variables, indexesOfParents, node.getVariable().getNumStates());
    }

    /**
     * Calculate the absolute frequencies in the database of each of the
     * configurations of the given node and its parents except one.
     * @param node <code>ProbNode</code> whose frequencies we want to calculate.
     * @param removedParent <code>ProbNode</code> that we do not want to include
     * in the calculations
     * @return <code>TablePotential</code> with the absolute frequencies in
     * the database of each of the configurations of the given node and its
     * parents except one.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public TablePotential absoluteFrequenciesRemovedParent(ProbNode node, 
            ProbNode removedParent) throws NotEnoughMemoryException {
        
        int parentsConfigurations = 1;  
        int numParents = node.getNode().getNumParents();
        int[] indexesOfParents = new int[numParents-1];
        
        ArrayList<Variable> variables = new ArrayList<Variable>(); 
        variables.add(node.getVariable());
            
        if (numParents == 1) {
            parentsConfigurations = 1;
        }
        else {
            int indexParent = 0;
            ArrayList<ProbNode> parents = 
            	ProbNet.getProbNodesOfNodes(node.getNode().getParents()); 
            for (ProbNode parent : parents) {
                if (parent.getVariable() != removedParent.getVariable()) {
                    indexesOfParents[indexParent] = probNet.getProbNodes().
                    	indexOf(probNet.getProbNode(parent.getVariable()));
                    variables.add(parent.getVariable());
                    parentsConfigurations *= parent.getVariable().
                    	getNumStates();
                    indexParent++;
                }
            }
        }
        
        return absoluteFrequencies(node, parentsConfigurations, 
                variables, indexesOfParents, node.getVariable().getNumStates());
    }
    
    /** @return <code>String</code> with the class name */
    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
