package org.openmarkov.core.learning.independencetester;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NormalizeNullVectorException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.learning.editionsgenerator.EditAndScorePair;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * This class inplements an independence tester based on the conditional
 * entropy criterium.
 * @author joliva
 */
public class CrossEntropyIndependenceTester implements IndependenceTester {

	/**
     * A copy of the learnedNet that is being learned.
     */
    protected ProbNet learnedNet;
    
    /** Database cases. */
    protected int[][] cases;
    
    
    public CrossEntropyIndependenceTester(ProbNet learnedNet, int[][] cases){
    	this.learnedNet = learnedNet;
    	this.cases = cases;
    }
    
    /**
	 * Tests whether two variables are independent or not.
	 * @param node1 <code>Node</code> first variable.
	 * @param node2 <code>Node</code> second variable.
	 * @param adjacencySubset <code>ArrayList</code> of <code>Node</code> 
	 * representing the separation set (i.e. the conditional set).
	 * @return <code>EditAndScorePair</code> representing the edition
	 * (RemoveLinkEdit) between the two variables and the score obtained
	 * in the independence test.
	 * @throws ProbNodeNotFoundException
	 */
	public EditAndScorePair independents(Node node1, Node node2, 
			ArrayList<Node> adjacencySubset)
					throws ProbNodeNotFoundException{
        double test = -1;
        
        try{
        	test = testValue(node1, node2, adjacencySubset);
        }catch (Exception e){
        	System.err.println("Error while computing the independence test.");
        	e.printStackTrace();
        }
        // System.out.println("Grados de Libertad: "+degreesOfFreedom);
        // System.out.println("Tabla x2 = "+test);
        // System.out.println("Estadistico = "+chiS);
        // System.out.println("Level of conf = "+degreeOfAccuracy);
        
        return (new EditAndScorePair(new RemoveLinkEdit(learnedNet, 
        		learnedNet.getProbNode(node1).getVariable(),
        		learnedNet.getProbNode(node2).getVariable(), false), test));  
    }
	
	/**
	 * This method computes the value of the independence test for
	 * two nodes given a separation set.
	 */
	public double testValue(Node nodeX, Node nodeY, 
			ArrayList<Node> adjacencySubset) throws ProbNodeNotFoundException, 
			NotEnoughMemoryException, NormalizeNullVectorException {
	    long degreesOfFreedom, numStatesAdjacency = 1, potentialSize = 1;
	    double crossEntropy, chiS, test;
	    ArrayList<ProbNode> nodesYZ = new ArrayList<ProbNode>();
		ArrayList<ProbNode> nodesZ = new ArrayList<ProbNode>();
		
		nodesYZ.add(learnedNet.getProbNode(nodeY));
		for (Node adjacent : adjacencySubset){
			nodesYZ.add(learnedNet.getProbNode(adjacent));
			nodesZ.add(learnedNet.getProbNode(adjacent));
			numStatesAdjacency *= learnedNet.getProbNode(adjacent).
					getVariable().getNumStates();
		}
		potentialSize = numStatesAdjacency * learnedNet.getProbNode(nodeX).
				getVariable().getNumStates() * learnedNet.getProbNode(nodeY).
				getVariable().getNumStates();
		
	    
	    crossEntropy = crossEntropy(nodeX, nodeY, nodesYZ, nodesZ);
	    chiS = ((double) 2.0 * cases.length) * crossEntropy;
	    
	    if (Math.abs(chiS) < 1e-10){
	    	chiS = 0.0;
	    }

	    if (adjacencySubset.size() != 0) {
	        degreesOfFreedom = numStatesAdjacency
	        * (learnedNet.getProbNode(nodeX).getVariable().getNumStates() - 1) 
	        * (learnedNet.getProbNode(nodeY).getVariable().getNumStates() - 1);
	    } else {
	        degreesOfFreedom = (learnedNet.getProbNode(nodeX).getVariable().
	        		getNumStates() - 1) * (learnedNet.getProbNode(nodeY).
	        		getVariable().getNumStates() - 1);
	    }
	
	    if (potentialSize < degreesOfFreedom)
	        degreesOfFreedom = potentialSize;
	    if (degreesOfFreedom <= 0)
	        degreesOfFreedom = 1;
	    double aux1 = (double) degreesOfFreedom;
	    test = StatisticalUtilities.chiSquare(chiS, aux1);
	    
	    return(test); 
	}
	
	/**
	 * Method that calculates the cross entropy between two nodes given a 
	 * conditional set. We use the formula: CE(X,Y|Z) = H(X|Z) - H(X|Y,Z) (where
	 * CE means 'cross entropy' and H means 'entropy'.
	 */
	public double crossEntropy(Node nodeX, Node nodeY, 
			ArrayList<ProbNode> nodesYZ, ArrayList<ProbNode> nodesZ) 
					throws ProbNodeNotFoundException, NotEnoughMemoryException, 
					NormalizeNullVectorException{
		ProbNode probNodeX = learnedNet.getProbNode(nodeX);
		
		return (conditionedEntropy(probNodeX, nodesZ) - 
				conditionedEntropy(probNodeX,nodesYZ));
	}
	
	/**
	 * Method that calculates the conditioned entropy of a node given a 
	 * conditional set. 
	 */
	public double conditionedEntropy(ProbNode nodeX, 
			ArrayList<ProbNode> adjacencySubset) throws 
			NotEnoughMemoryException, NormalizeNullVectorException, 
			ProbNodeNotFoundException{
		int numStates = ((Variable) nodeX.getVariable()).getNumStates(); 
	    int adjacentConfigurations = 1; 
	    double nodeEntropy = 0;
	    double n_ij;
	    double n_ijk;
	    int position = 0;
	    double[] freq = null;
	    ArrayList<ProbNode> nodeAndAdjacency = new ArrayList<ProbNode>();
	    	
	    nodeAndAdjacency.add(nodeX);
	    for (ProbNode adjacent : adjacencySubset){
	    	nodeAndAdjacency.add(adjacent);
	        adjacentConfigurations *= adjacent.getVariable().getNumStates();
	    }
	    
	    freq = absoluteNormalization(absoluteFrequencies(nodeAndAdjacency));
	
	    for (int j = 0; j < adjacentConfigurations; j++){
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
	
	    return -nodeEntropy;
	}
	
	/**
     * Calculate the absolute frequencies in the database of each of the
     * configurations of the given nodes.
     * @return <code>TablePotential</code> with the absolute frequencies in
     * the database of each of the configurations of the given nodes
     * @throws openmarkov.exceptions.NotEnoughMemoryException
	 * @throws ProbNodeNotFoundException 
     */
    public TablePotential absoluteFrequencies(ArrayList<ProbNode> nodeList) 
    	throws NotEnoughMemoryException, ProbNodeNotFoundException{
        int configurationsSize = 1;
        int index = 0;
        ArrayList<Variable> variables = new ArrayList<Variable>(); 
        int[] indexes = new int[nodeList.size()];
        
        for (ProbNode probNode : nodeList){
        	
        	variables.add((Variable) probNode.getVariable());
          
            indexes[index] = learnedNet.getProbNodes().
            	indexOf(learnedNet.getProbNode(probNode.getVariable()));
            configurationsSize *= ((Variable) 
                    probNode.getVariable()).getNumStates();
            index++;
        }
        
        return absoluteFreqPotential(nodeList, configurationsSize, 
                variables, indexes);
    }
    
    /**
     * Calculate the absolute frequencies in the database of each of the
     * configurations of the given nodes.
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
     * @throws ProbNodeNotFoundException 
     */
    public TablePotential absoluteFreqPotential(ArrayList<ProbNode> nodeList, 
            int configurationsSize, ArrayList<Variable> variables,
            int[] indexes) 
    		throws NotEnoughMemoryException, ProbNodeNotFoundException {
        TablePotential absoluteFreqPotential = new TablePotential(
        		variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double[] absoluteFreqs = absoluteFreqPotential.getValues();
        double iCPT;
        int numValues = nodeList.get(0).getVariable().
        		getNumStates();
        int iParent, iNode = learnedNet.getProbNodes().indexOf(nodeList.get(0));
        nodeList.remove(0);
        variables.remove(0);
        
        // Initialize the table
        for (int i = 0; i < configurationsSize; i++){
            absoluteFreqs[i] = 0;
        }
        
        // Compute the absolute frequencies
        for (int i = 0; i < cases.length; i++){
            iCPT = 0;
            int j = 1;
            for (ProbNode parent : learnedNet.getProbNodes(variables)){
                iParent = indexes[j];
                iCPT = iCPT * parent.getVariable().getNumStates() + 
                        cases[i][iParent];
                j++;
            }
            absoluteFreqs[numValues * ((int) iCPT) + (int) cases[i][iNode]]++;
        }
        return absoluteFreqPotential;
    }
    
    private double[] absoluteNormalization(TablePotential potential){
    	double[] normalizedPotential = new double[potential.getTableSize()];
    	
    	for (int i = 0 ; i < potential.getTableSize(); i++){
    		normalizedPotential[i] = potential.getValues()[i]/cases.length;
    	}
    	return normalizedPotential;
    }
	
	@Override
	public void undoableEditWillHappen(PNUndoableEditEvent event)
			throws ConstraintViolationException, CanNotDoEditException,
			NotEnoughMemoryException, NonProjectablePotentialException,
			WrongCriterionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void undoEditHappened(PNUndoableEditEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void undoableEditHappened(UndoableEditEvent arg0) {
		// TODO Auto-generated method stub

	}

}
