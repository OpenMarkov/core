package org.openmarkov.core.learning.algorithm;

import java.util.ArrayList;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.learning.editionsgenerator.EditAndScorePair;
import org.openmarkov.core.learning.editionsgenerator.EditionsGenerator;
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
	
    /** Edition generator */
    protected EditionsGenerator editionsGenerator;
    /** Parameter for the parametric learning. */
    private double alpha;    
    
    
    // Constructor
    /**
     * @param editionsGenerator <code>EditionsGenerator</code> The object that
     * gives the best operation in each iteration of the algorithm.
     **/
    public LearningAlgorithm (EditionsGenerator editionsGenerator, double alpha)
    {
        this.editionsGenerator = editionsGenerator;
        this.alpha = alpha;
    }
    
    /** Method invoked to run the algorithm.
     * 
     * @return <code>ProbNet</code> learned.
     * @throws NotEnoughMemoryException
     * @throws NormalizeNullVectorException
     */
    public void run (int[][] cases, ProbNet probNet, ProbNet modelNet, ModelNetUse modelNetUse)
        throws NotEnoughMemoryException,
        NormalizeNullVectorException
    {
        probNet.getPNESupport ().addUndoableEditListener (editionsGenerator);
        init(cases, probNet, modelNet, modelNetUse);
        /* Main loop */
       ArrayList<EditAndScorePair> bestEditions = editionsGenerator.getBestEditions(probNet, cases, 1,true,true,false);
        while (!bestEditions.isEmpty ())
        {
            step (probNet, bestEditions.get (0).getEdition ());
            bestEditions = editionsGenerator.getBestEditions (probNet, cases, 1, true, true,
                                                              false);
        }
       /* Parametric Learning */
       parametricLearning(probNet, cases);
    }
    
    /**
     * 
     * @param cases
     * @param probNet
     * @param modelNet
     */
    protected void init (int[][] cases, ProbNet probNet, ProbNet modelNet, ModelNetUse modelNetUse)
    {
        
    }
    
    /** Takes a step in the algorithm
     * 
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws java.lang.Exception
     */
    public ProbNet step(ProbNet learnedNet, PNEdit bestEdition) throws NotEnoughMemoryException, 
            NormalizeNullVectorException {

    /* If there have been any improvements on the score, we update
     * the learnedNet. */
        try{
            learnedNet.doEdit(bestEdition);
        } catch (ConstraintViolationException ex){
            /* If the edition was not allowed (ModelNetworkconstraint)
             * the algorithm just goes through the next iteration of the
             * loop, asking the cache for the next best edition.
             */
        }
        catch (Exception exception){
            exception.printStackTrace();
        }
        return learnedNet;
    }
    		
	/**
     * This function creates the Potentials associated to each node,
     * normalizing the absolute frequencies of the configurations of 
     * the parents.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws NormalizeNullVectorException 
     */
    public ProbNet parametricLearning(ProbNet learnedNet, int[][] cases) 
    		throws NotEnoughMemoryException, NormalizeNullVectorException{
        TablePotential absoluteFrequencies;
        
        for (ProbNode node : learnedNet.getProbNodes()) { 
            absoluteFrequencies = calculateAbsoluteFrequencies(learnedNet, cases, node);
            for (int j = 0; j < absoluteFrequencies.getTableSize(); j++)
                absoluteFrequencies.values[j] += alpha;
            node.addPotential(DiscretePotentialOperations.normalize(absoluteFrequencies));
        }
        
        return learnedNet;
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
    private TablePotential calculateAbsoluteFrequencies (ProbNet probNet,
                                                         int[][] cases,
                                                         ProbNode node)
        throws NotEnoughMemoryException
    {
        int parentsConfigurations = 1;
        int indexOfParent = 0;
        int numParents = node.getNode ().getNumParents ();
        int[] indexesOfParents = new int[numParents];
        ArrayList<Variable> variables = new ArrayList<Variable> ();
        variables.add ((Variable) node.getVariable ());
        if (numParents == 0)
        {
            parentsConfigurations = 1;
        }
        else
        {
            for (ProbNode parent : ProbNet.getProbNodesOfNodes (node.getNode ().getParents ()))
            {
                variables.add ((Variable) parent.getVariable ());
                indexesOfParents[indexOfParent] = probNet.getProbNodes ().indexOf (probNet.getProbNode (parent.getVariable ()));
                parentsConfigurations *= ((Variable) parent.getVariable ()).getNumStates ();
                indexOfParent++;
            }
        }
        return calculateAbsoluteFreqPotential (probNet,
                                               cases,
                                               node,
                                               parentsConfigurations,
                                               variables,
                                               indexesOfParents,
                                               node.getVariable ().getNumStates ());
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
    private TablePotential calculateAbsoluteFreqPotential (ProbNet probNet,
                                                          int[][] cases,
                                                          ProbNode probNode,
                                                          int parentsConfigurations,
                                                          ArrayList<Variable> variables,
                                                          int[] indexesOfParents,
                                                          int numValues
                                                          )
    		throws NotEnoughMemoryException {
        TablePotential absoluteFreqPotential = new TablePotential(
        		variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double[] absoluteFreqs = absoluteFreqPotential.getValues();
        double iCPT;
        int iNode = probNet.getProbNodes().indexOf(
                probNet.getProbNode(probNode.getVariable())); 

        // Initialize the table
        for (int i = 0; i < parentsConfigurations * numValues; i++) {
            absoluteFreqs[i] = 0;
        }
        
        variables.remove(0);
        // Compute the absolute frequencies
        for (int i = 0; i < cases.length; i++) {
            iCPT = 0;
            int j = 0;
            for (ProbNode parent : probNet.getProbNodes(variables)) {
                iCPT = iCPT * parent.getVariable().getNumStates() + cases[i][indexesOfParents[j]];
                j++;
            }
            absoluteFreqs[numValues * ((int) iCPT) + (int) cases[i][iNode]]++;
        }
        return absoluteFreqPotential;
    }

    /**
     * Score of the associated network. 
     * @return <code>double</code> score of the net 
     */    
    public abstract double getScore ();

    /**
     * Scores the associated network with the given edition.
     * @param edit <code>PNEdit</code> 
     * @return <code>double</code> score of the net with the given edition
     */    
    public abstract double getScore (PNEdit edit);

    /**
     * Returns best editions
     * @param probNet
     * @param cases
     * @param numEdits
     * @param onlyAllowedEdits
     * @param onlyPositiveEdits
     * @param reset
     * @return
     */
    public ArrayList<EditAndScorePair> getBestEditions (ProbNet probNet, int [][] cases, int numEdits,
                                                        boolean onlyAllowedEdits,
                                                        boolean onlyPositiveEdits,
                                                        boolean reset)
    {
        return this.editionsGenerator.getBestEditions (probNet, cases, numEdits,
                                                       onlyAllowedEdits,
                                                       onlyPositiveEdits, reset);        
    }
}
