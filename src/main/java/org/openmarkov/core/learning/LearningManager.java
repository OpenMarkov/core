
package org.openmarkov.core.learning;

import java.util.HashMap;

import javax.swing.JOptionPane;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.learning.algorithm.HillClimberAlgorithm;
import org.openmarkov.core.learning.algorithm.LearningAlgorithm;
import org.openmarkov.core.learning.algorithm.PCAlgorithm;
import org.openmarkov.core.learning.editionsgenerator.EditionsGenerator;
import org.openmarkov.core.learning.editionsgenerator.HillClimberEditionsGenerator;
import org.openmarkov.core.learning.editionsgenerator.PCEditionsGenerator;
import org.openmarkov.core.learning.independencetester.CrossEntropyIndependenceTester;
import org.openmarkov.core.learning.independencetester.IndependenceTester;
import org.openmarkov.core.learning.metrics.AICMetric;
import org.openmarkov.core.learning.metrics.BDMetric;
import org.openmarkov.core.learning.metrics.BayesianMetric;
import org.openmarkov.core.learning.metrics.EntropyMetric;
import org.openmarkov.core.learning.metrics.K2Metric;
import org.openmarkov.core.learning.metrics.MDLMetric;
import org.openmarkov.core.learning.metrics.Metric;
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
    
    /** Implemented metrics */
    public static final String[] metrics = {"Bayesiana", "K2", "BD", "Entropía",
            "MDL", "AIC"};
    
    /** Implemented independence tester */
    public static final String[] independenceTesters = {"Entropía cruzada"};

    /** Implemented algorithms */
    public static final String[] algorithms = {"Gradiente", "PC"};

    /* Model net posible uses */
    static final int NODES_ONLY = 0;
    static final int INITIAL_LINKS = 1;
    static final int FIXED_LINKS = 2;
    
    /** ProbNet to learn */
    ProbNet learnedNet = null;

    /** Database cases */
    int[][] cases = null;

    LearningAlgorithm learn = null;
    EditionsGenerator editionsGenerator = null;
    Metric metric = null;
    
    /**
     * Constructor
     * @param preprocessedNet <code>ProbNet</code> Net with the variables of
     * interest after preprocessing.
     * @param structureNet <code>ProbNet</code> Net from which take the
     * information of the nodes and links
     * @param modelNetUse <code>int</code> use the positions of the nodes, use
     * also the initial links or use them fixed
     * @param databaseCases <code>int[][]</code> examples in the database 
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws NodeNotFoundException 
     * @throws NormalizeNullVectorException 
     * @throws ProbNodeNotFoundException 
     */
    public LearningManager(ProbNet preprocessedNet, ProbNet structureNet, 
    		boolean[] modelNetUse, int[][] databaseCases) 
            throws NotEnoughMemoryException, NodeNotFoundException, 
            NormalizeNullVectorException, ProbNodeNotFoundException {
        
        cases = databaseCases;
        learnedNet = preprocessedNet;
        
        /* Maybe there's no modelNet to work with */
        if ((modelNetUse[0]) && (structureNet == null)){
        	JOptionPane.showMessageDialog(
					null, "No se ha podido abrir la red modelo.",
					stringResource.getString("ErrorWindow.Title.Label"),
					JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Initializes the Hill climber algorithm.
     * @param algorithm <code>String</code> indicating the algorithm selected
     * by the user.
     * @param metricString <code>String</code> indicating the metric selected
     * by the user.
     * @param structureNet <code>ProbNet</code> Net from which take the
     * information of the nodes and links
     * @param modelNetUse <code>int</code> use the positions of the nodes, use
     * also the initial links or use them fixed
     * @param alphaParameter parameter alpha for the Laplace's
     * correction
     * @throws NodeNotFoundException
     * @throws ProbNodeNotFoundException
     * @throws NotEnoughMemoryException
     */
    public void initHillClimberAlgorithm(String algorithm, String metricString,
			ProbNet structureNet, boolean[] modelNetUse, String alphaParameter) 
			throws NodeNotFoundException, ProbNodeNotFoundException, 
			NotEnoughMemoryException {
    	
    	double alpha = Double.parseDouble(alphaParameter);
    	
        metric = null;
        /* Take the metric selected by the user */
        if (metricString.equals(metrics[0]))
            metric = new BayesianMetric(learnedNet, cases, alpha);
        else if (metricString.equals(metrics[1]))
            metric = new K2Metric(learnedNet, cases);
        else if (metricString.equals(metrics[2]))
            metric = new BDMetric(learnedNet, cases, alpha);
        else if (metricString.equals(metrics[3]))
            metric = new EntropyMetric(learnedNet, cases);
        else if (metricString.equals(metrics[4]))
            metric = new MDLMetric(learnedNet, cases);
        else if (metricString.equals(metrics[5]))
            metric = new AICMetric(learnedNet, cases);

        if (structureNet != null)
        	addModelNetconstraints(modelNetUse, structureNet);
        
    	editionsGenerator = new HillClimberEditionsGenerator(learnedNet, 
    			structureNet, metric);
    
        learn = new HillClimberAlgorithm(learnedNet, structureNet, alpha, 
        		editionsGenerator, cases);
    	learn.init(modelNetUse, structureNet);
	    learn.setListeners();
        
        try
        {
        	learn.parametricLearning();
        }catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        learnedNet = addElviraProperties(learnedNet);
	}
    
    /**
     * Initializes the PC algorithm.
     * @param algorithm <code>String</code> indicating the algorithm selected
     * by the user.
     * @param independenceTesterString <code>String</code> indicating the 
     * independence test selected by the user.
     * @param degreeOfAccuracyString <code>String</code> indicating the 
     * degree of accuracy selected by the user.
     * @param structureNet <code>ProbNet</code> Net from which take the
     * information of the nodes and links
     * @param modelNetUse <code>int</code> use the positions of the nodes, use
     * also the initial links or use them fixed
     * @param alphaParameter parameter alpha for the Laplace's correction
     * @throws NodeNotFoundException
     * @throws ProbNodeNotFoundException
     * @throws NotEnoughMemoryException
     */
    public void initPCAlgorithm(String algorithm, 
    		String independenceTesterString, String degreeOfAccuracyString,
			ProbNet structureNet, boolean[] modelNetUse, String alphaString) 
			throws NodeNotFoundException, ProbNodeNotFoundException, 
			NotEnoughMemoryException {
    	IndependenceTester independenceTester = null;
    	
    	double alpha = Double.parseDouble(alphaString);
    	double degreeOfAccuracy = Double.parseDouble(degreeOfAccuracyString);
	         
        /* Take the independence tester selected by the user */
        if (independenceTesterString.equals(independenceTesters[0]))
            independenceTester = new CrossEntropyIndependenceTester(learnedNet,
	        		cases);
        
        editionsGenerator = new PCEditionsGenerator(learnedNet, 
    			structureNet, independenceTester, degreeOfAccuracy);
        
        if (structureNet != null)
        	addModelNetconstraints(modelNetUse, structureNet);

        learn = new PCAlgorithm(learnedNet, structureNet, 
        		editionsGenerator, alpha, cases);
        learn.init(modelNetUse, structureNet); 
	    learn.setListeners();
        
        try
        {
        	learn.parametricLearning();
        }catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        learnedNet = addElviraProperties(learnedNet);
	}

	/**
     * Main method to launch the learning process.
     * @return <code>ProbNet</code> learned net.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws NodeNotFoundException 
     * @throws NormalizeNullVectorException 
     * @throws ProbNodeNotFoundException 
     */
    public ProbNet learn() 
            throws NotEnoughMemoryException, NodeNotFoundException, 
            NormalizeNullVectorException, ProbNodeNotFoundException {
                
        /* Get current time */
        long start = System.currentTimeMillis();

        learnedNet = learn.run();
        
        /* Get elapsed time in milliseconds */
        long elapsedTimeMillis = System.currentTimeMillis()-start;
                
        System.out.print("\n * Aprendizaje terminado.\n\t Tiempo transcurrido: " 
               + calculateTime(elapsedTimeMillis) + "\n");
        return learnedNet;
    }
    
    /**
     * Take a step in the learning process.
     * @param edition <code>PNEdit</code> to do.
     * @return <code>ProbNet</code> learned net.
     * @throws openmarkov.exceptions.NotEnoughMemoryException
     * @throws NodeNotFoundException 
     * @throws NormalizeNullVectorException 
     */
    public ProbNet step(PNEdit edition) 
            throws NotEnoughMemoryException, NodeNotFoundException, 
            NormalizeNullVectorException {
        return learn.step(learnedNet, edition, true);
                
    }
    
    /**
     * Adds elvira properties to the learned net.
     * @param learnedNet <code>ProbNet</code> which receives the elvira
     * properties.
     * @return <code>ProbNet</code> learned net.
     */
    public ProbNet addElviraProperties(ProbNet learnedNet) 
    {
                                
        HashMap<String, String> newIO = learnedNet.additionalProperties;
        //newIO.put("ProbNet", learnedNet);
        State[] defaultNodeStates = {new State("Presente"), 
        		new State("Ausente")};
        learnedNet.setDefaultStates(defaultNodeStates);
        newIO.put("hasElviraProperties", new String("yes"));
        learnedNet.additionalProperties = newIO;
        
        return learnedNet;
    }
    
    /**
     * Adds the constraints depending on the structure of the model net and 
     * the option selected by the user.
     * @param modelNetUse use of the model net selected by the user.
     * @param modelNet structure of the net to add the constraints
     * @throws ProbNodeNotFoundException
     * @throws NodeNotFoundException
     */
    private void addModelNetconstraints(boolean[] modelNetUse,
    		ProbNet modelNet) throws ProbNodeNotFoundException, 
    		NodeNotFoundException{
    	
    	/* If the option "Use only nodes" is not selected, we add
    	 * the links of the model net to the learnedNet we are going to 
    	 * learn.
    	 */
    	if(!modelNetUse[1] && (modelNet != null)){
    		for (Link link : modelNet.getGraph().getLinks()){
                learnedNet.addLink(learnedNet.getVariable(((ProbNode)link.getNode1().
                		getObject()).getVariable().getName()), 
                		learnedNet.getVariable(((ProbNode)link.getNode2().
                				getObject()).getVariable().getName()), 
                				link.isDirected()); 
            }
    	}
    	
    	//ModelNetworkConstraint
    	try {
			learnedNet.addConstraint(new ModelNetworkConstraint(modelNetUse, 
					modelNet), false);
		} catch (ConstraintViolationException e) {
		}
    }
    
    /**This function returns a <code>String</code> that represents the given 
     * elapsed time in the format: minutes' seconds'' miliseconds ms.
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
    
    
    public EditionsGenerator getEditionsGenerator()
    {
    	return editionsGenerator;
    }
    
	public ProbNet getLearnedNet() {
		return this.learnedNet;
	}
	
    /**
     * Score of the associated network. 
     * @return <code>double</code> score of the net 
     */
    public double getScore()  {
    	
        try {
			return metric.score();
		} catch (NotEnoughMemoryException e) {
			return Double.NaN;
		} catch (NullPointerException e) {
			return Double.NaN;
		}
    }
    
    /**
     * Scores the associated network with the given edition.
     * @param edition <code>PNEdit</code> 
     * @return <code>double</code> score of the net with the given edition
     */
    public double getScore(PNEdit edit)  {
    	
        try {
			return metric.score(edit);
		} catch (NotEnoughMemoryException e) {
			// TODO Auto-generated catch block
			return Double.NaN;
		}
    }
	
}
