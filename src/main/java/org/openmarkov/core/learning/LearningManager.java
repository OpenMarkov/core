
package org.openmarkov.core.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.learning.algorithm.LearningAlgorithm;
import org.openmarkov.core.learning.algorithm.annotation.LearningAlgorithmManager;
import org.openmarkov.core.learning.editionsgenerator.EditAndScorePair;
import org.openmarkov.core.learning.exception.EmptyModelNetException;
import org.openmarkov.core.learning.util.ModelNetUse;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.constraint.ModelNetworkConstraint;

/** This class launches the learning algorithm and receives the results of
 * the learning.
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class LearningManager {
    
    /**  Learning algorithm */
    private LearningAlgorithm learningAlgorithm = null;    
    
    /** Implemented metrics. */
    public static final String[] metrics = {"Bayesiana", "K2", "BD", "Entropía",
            "MDL", "AIC"};
    
    /** Implemented independence tester. */
    public static final String[] independenceTesters = {"Entropía cruzada"};

    /** Implemented algorithms. */
    public static final String[] algorithms = {"Gradiente", "PC"};
    
    /** ProbNet to learn. */
    private ProbNet learnedNet = null;
    
    /** Case database */
    private int[][] cases = null;

    
    /**
     * Constructor
     * @param preprocessedNet <code>ProbNet</code> Net with the variables of
     * interest after preprocessing.
     * @param algorithm <code>LearningAlgorithm</code> indicating the algorithm
     *            selected by the user.
     * @param modelNet <code>ProbNet</code> Net from which take the
     *            information of the nodes and links
     * @param modelNetUse <code>boolean[]</code> use the positions of the nodes,
     *            use also the initial links or use them fixed
     * @throws NormalizeNullVectorException
     * @throws EmptyModelNetException
     * @throws ProbNodeNotFoundException
     * @throws NodeNotFoundException
     * @throws NotEnoughMemoryException
     */
    public LearningManager (ProbNet preprocessedNet,
                            int[][] cases,
                            String algorithmName,
                            List<Object> parameters, 
                            ProbNet modelNet,
                            ModelNetUse modelNetUse
                            )
        throws NormalizeNullVectorException,
        EmptyModelNetException,
        NodeNotFoundException,
        ProbNodeNotFoundException,
        NotEnoughMemoryException
    {
        LearningAlgorithmManager learningAlgorithmManager = new LearningAlgorithmManager ();
        this.cases = cases;
        /* Maybe there's no modelNet to work with */
        if ((modelNetUse.isUseModelNet ()))
        {
            if (modelNet != null)
            {
                this.learnedNet = applyModelNet (preprocessedNet, modelNet,
                                                 modelNetUse);
            }
            else
            {
                throw new EmptyModelNetException ();
            }
        }
        else
        {
            this.learnedNet = preprocessedNet;
        }     
        parameters.add (0, learnedNet);
        parameters.add (1, cases);
        this.learningAlgorithm = learningAlgorithmManager.getByName (algorithmName, parameters);
        this.addElviraProperties (learnedNet);
    }  
    
    /**
     * Main method to launch the learning process.
     * @return <code>ProbNet</code> learned net.
     * @throws NotEnoughMemoryException
     * @throws NodeNotFoundException 
     * @throws NormalizeNullVectorException 
     * @throws ProbNodeNotFoundException 
     */
    public ProbNet learn() 
            throws NotEnoughMemoryException, NodeNotFoundException, 
            NormalizeNullVectorException, ProbNodeNotFoundException {
                
        /* Get current time */
        long start = System.currentTimeMillis();

        learningAlgorithm.run();
        
        /* Get elapsed time in milliseconds */
        long elapsedTimeMillis = System.currentTimeMillis() - start;
                
        System.out.print("\n * Aprendizaje terminado.\n\t Tiempo transcurrido: " 
               + calculateTime(elapsedTimeMillis) + "\n");
        return learnedNet;
    }
    
	public ProbNet getLearnedNet() {
		return this.learnedNet;
	}
	
    /**
     * Score of the associated network. 
     * @return <code>double</code> score of the net 
     */
    public double getScore()  {
			return learningAlgorithm.getScore(this.learnedNet, this.cases);
    }
    
    /**
     * Scores the associated network with the given edition.
     * @param edit <code>PNEdit</code> 
     * @return <code>double</code> score of the net with the given edition
     */
    public double getScore(PNEdit edit)  {
        return learningAlgorithm.getScore (this.learnedNet, this.cases, edit);
    }

    /**
     * Retrieves the best editions suggested by the learning algorithm
     * @param numEdits
     * @param onlyAllowedEdits
     * @param onlyPositiveEdits
     * @param reset
     */
    public ArrayList<EditAndScorePair> getBestEditions (int numEdits,
                                 boolean onlyAllowedEdits,
                                 boolean onlyPositiveEdits,
                                 boolean reset)
    {
        
        return this.learningAlgorithm.getBestEditions (numEdits,
                                                       onlyAllowedEdits,
                                                       onlyPositiveEdits, reset);        
    }
    
    /**
     * Adds elvira properties to the learned net.
     * @param learnedNet <code>ProbNet</code> which receives the elvira
     * properties.
     */
    private void addElviraProperties(ProbNet learnedNet) 
    {
                                
        HashMap<String, String> newIO = learnedNet.additionalProperties;
        State[] defaultNodeStates = {new State("present"), new State("absent")};
        learnedNet.setDefaultStates(defaultNodeStates);
        newIO.put("hasElviraProperties", new String("yes"));
        learnedNet.additionalProperties = newIO;
    }
    
    /**
     * Adds links and constraints depending on the structure of the model net
     * and the option selected by the user.
     * @param modelNetUse use of the model net selected by the user.
     * @param modelNet structure of the net to add the constraints
     * @throws ProbNodeNotFoundException
     * @throws NodeNotFoundException
     */
    private ProbNet applyModelNet (ProbNet learnedNet,
                                   ProbNet modelNet,
                                   ModelNetUse modelNetUse)
        throws ProbNodeNotFoundException,
        NodeNotFoundException
    {
        /*
         * If the option "Use only nodes" is not selected, we add the links of
         * the model net to the learnedNet we are going to learn.
         */
        if (!modelNetUse.isAddLinksAllowed () && (modelNet != null))
        {
            for (Link link : modelNet.getGraph ().getLinks ())
            {
                learnedNet.addLink (learnedNet.getVariable (((ProbNode) link.getNode1 ().getObject ()).getVariable ().getName ()),
                                    learnedNet.getVariable (((ProbNode) link.getNode2 ().getObject ()).getVariable ().getName ()),
                                    link.isDirected ());
            }
        }
        // ModelNetworkConstraint
        try
        {
            learnedNet.addConstraint (new ModelNetworkConstraint (modelNetUse,
                                                                  modelNet),
                                      false);
        }
        catch (ConstraintViolationException e)
        {
        }
        return learnedNet;
    }
    
    /**This function returns a <code>String</code> that represents the given 
     * elapsed time in the format: minutes' seconds'' milliseconds ms.
     * 
     * @param elapsedTimeMillis long with the elapsed time.
     * @return <code>String</code> that represents the given time.
     */
    private static String calculateTime(long elapsedTimeMillis){
        
        StringBuffer timeString = new StringBuffer();
        int minutes, seconds;
        
        minutes = (int) (elapsedTimeMillis / 60000);
        elapsedTimeMillis -= minutes * 60000;
        seconds = (int) (elapsedTimeMillis / 1000);
        elapsedTimeMillis -= seconds * 1000;
        
        timeString.append(minutes + "' " + seconds + "\" " + elapsedTimeMillis 
                + " ms.");
        
        return timeString.toString();
    }

    public static Set<String> getAlgorithmNames ()
    {
        LearningAlgorithmManager learningAlgorithmManager = new LearningAlgorithmManager ();
        
        return learningAlgorithmManager.getLearningAlgorithmNames ();
    }    
	
}
