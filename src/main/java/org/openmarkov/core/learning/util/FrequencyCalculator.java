package org.openmarkov.core.learning.util;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * Calculates frequencies
 * @author Iñigo
 *
 */
public class FrequencyCalculator
{
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
    public static TablePotential absolute (ProbNet probNet,
                                           int[][] cases,
                                           ProbNode probNode,
                                           ArrayList<Variable> variables,
                                           int numValues)
    {
        
        int parentsConfigurations = 1;
        int[] indexesOfParents = new int[variables.size ()];
        
        // We miss the first one as it is the node itself
        for (int i= 1; i < variables.size (); ++i)
        {
            indexesOfParents[i] = probNet.getProbNodes ().indexOf (probNet.getProbNode (variables.get (i)));
            parentsConfigurations *= variables.get (i).getNumStates ();
        }        
        
        TablePotential absoluteFreqPotential = null;
        try
        {
            absoluteFreqPotential = new TablePotential (
                                                        variables,
                                                        PotentialRole.CONDITIONAL_PROBABILITY);
        }
        catch (NotEnoughMemoryException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        double[] absoluteFreqs = absoluteFreqPotential.getValues ();
        double iCPT;
        int iParent, iNode = probNet.getProbNodes ().indexOf (probNet.getProbNode (probNode.getVariable ()));
        // Initialize the table
        for (int i = 0; i < parentsConfigurations * numValues; i++)
        {
            absoluteFreqs[i] = 0;
        }
        variables.remove (0);
        // Compute the absolute frequencies
        for (int i = 0; i < cases.length; i++)
        {
            iCPT = 0;
            int j = 0;
            for (ProbNode parent : probNet.getProbNodes (variables))
            {
                iParent = indexesOfParents[j];
                iCPT = iCPT * parent.getVariable ().getNumStates ()
                       + cases[i][iParent];
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
     * @return <code>TablePotential</code> with the absolute frequencies in the
     *         database of each of the configurations of the given node and its
     *         parents and a given extra parent.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     */
    public static TablePotential absoluteExtraParent (ProbNet probNet,
                                                                          int[][] cases,
                                                                          ProbNode node,
                                                                          ProbNode extraParent)
    {
        ArrayList<Variable> variables = new ArrayList<Variable> ();
        variables.add ((Variable) node.getVariable ());
        if (extraParent != null)
        {
            variables.add ((Variable) extraParent.getVariable ());
        }
        for (ProbNode parent : ProbNet.getProbNodesOfNodes (node.getNode ().getParents ()))
        {
            variables.add ((Variable) parent.getVariable ());
        }
        return absolute (probNet, cases, node, variables,
                         node.getVariable ().getNumStates ());
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
    public static TablePotential absoluteRemovedParent (ProbNet probNet,
                                                                            int[][] cases,
                                                                            ProbNode node,
                                                                            ProbNode removedParent)
    {
        ArrayList<Variable> variables = new ArrayList<Variable> ();
        variables.add (node.getVariable ());
        
        ArrayList<ProbNode> parents = ProbNet.getProbNodesOfNodes (node.getNode ().getParents ());
        for (ProbNode parent : parents)
        {
            if (parent.getVariable () != removedParent.getVariable ())
            {
                variables.add (parent.getVariable ());
            }
        }
        return absolute (probNet, cases, node, variables,
                         node.getVariable ().getNumStates ());
    }
    
    
}
