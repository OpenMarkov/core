package org.openmarkov.core.learning.algorithm;

import java.util.ArrayList;

import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.learning.util.ModelNetUse;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

/**
 * Abstract learning algorithm.
 */
public abstract class LearningAlgorithm {
	
    /** A copy of the received <code>ProbNet</code>. */
    protected ProbNet learnedNet;
    
    /** This net is used as the initial net from which to start the algorithm
     * with the possibility to fix its links or not.
     */
    protected ProbNet modelNet;
    
    /** Database cases. */
    protected int[][] cases;

    /** For undo/redo operations. */
    protected PNESupport pNESupport;
    
    /** Parameter for the parametric learning. */
    protected double alpha;
  
    /** Method invoked to run the algorithm.
     * 
     * @return <code>ProbNet</code> learned.
     * @throws NotEnoughMemoryException
     * @throws NormalizeNullVectorException
     */
    public abstract ProbNet run() throws NotEnoughMemoryException, 
		NormalizeNullVectorException;
    
    /** Takes a step in the algorithm.
     * 
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws java.lang.Exception
     */
    public abstract ProbNet step(ProbNet learnedNet, PNEdit bestEdition, 
    		boolean doParametricLearning) throws NotEnoughMemoryException, 
		NormalizeNullVectorException;
    
    public abstract void init(ModelNetUse modelNetUse, ProbNet modelNet) 
    		throws ProbNodeNotFoundException, NodeNotFoundException;
    		
    /**
     * Set the listeners of the learned probnet.
     */
    public abstract void setListeners();

	/**
     * This function creates the Potentials associated to each node,
     * normalizing the absolute frequencies of the configurations of 
     * the parents.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws NormalizeNullVectorException 
     */
    public void parametricLearning() 
    		throws NotEnoughMemoryException, NormalizeNullVectorException{
        TablePotential absoluteFrequencies;
        
        for (ProbNode node : learnedNet.getProbNodes()) { 
            absoluteFrequencies = calculateAbsoluteFrequencies(node);
            for (int j = 0; j < absoluteFrequencies.getTableSize(); j++)
                absoluteFrequencies.values[j] += alpha;
            node.addPotential(DiscretePotentialOperations.normalize(absoluteFrequencies));
        }
    }
    
    /**
     * Calculate the absolute frequencies in the database of each of the
     * configurations of the given node and its parents and a given extra
     * parent.
     * @param node <code>ProbNode</code> whose frequencies we want to calculate.
     * @return <code>TablePotential</code> with the absolute frequencies in
     * the database of each of the configurations of the given node and its
     * parents and a given extra parent.
     * @throws NotEnoughMemoryException
     */
    public TablePotential calculateAbsoluteFrequencies(ProbNode node) 
    	throws NotEnoughMemoryException {
        int parentsConfigurations = 1;
        int indexOfParent = 0;
        int numParents = node.getNode().getNumParents();

        int[] indexesOfParents = new int[numParents];
        
        ArrayList<Variable> variables = new ArrayList<Variable>(); 
        variables.add((Variable) node.getVariable());
        
        if (numParents == 0) {
            parentsConfigurations = 1; 
        } else {   
            for (ProbNode parent : ProbNet.getProbNodesOfNodes(node.
            		getNode().getParents())) { 
                variables.add((Variable) parent.getVariable());  
                indexesOfParents[indexOfParent] = learnedNet.getProbNodes().
                	indexOf(learnedNet.getProbNode(parent.getVariable()));
                parentsConfigurations *= ((Variable) 
                        parent.getVariable()).getNumStates();
                indexOfParent++;
            }
        }
        
        return calculateAbsoluteFreqPotential(node, parentsConfigurations, 
                variables, indexesOfParents, node.getVariable().getNumStates());
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
    public TablePotential calculateAbsoluteFreqPotential(ProbNode probNode, 
            int parentsConfigurations, ArrayList<Variable> variables,
            int[] indexesOfParents, int numValues) 
    		throws NotEnoughMemoryException {
        TablePotential absoluteFreqPotential = new TablePotential(
        		variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double[] absoluteFreqs = absoluteFreqPotential.getValues();
        double iCPT;
        int iNode = learnedNet.getProbNodes().indexOf(
                learnedNet.getProbNode(probNode.getVariable())); 

        // Initialize the table
        for (int i = 0; i < parentsConfigurations * numValues; i++) {
            absoluteFreqs[i] = 0;
        }
        
        variables.remove(0);
        // Compute the absolute frequencies
        for (int i = 0; i < cases.length; i++) {
            iCPT = 0;
            int j = 0;
            for (ProbNode parent : learnedNet.getProbNodes(variables)) {
                iCPT = iCPT * parent.getVariable().getNumStates() + cases[i][indexesOfParents[j]];
                j++;
            }
            absoluteFreqs[numValues * ((int) iCPT) + (int) cases[i][iNode]]++;
        }
        return absoluteFreqPotential;
    }
}
