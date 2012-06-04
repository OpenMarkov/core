/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network;

import java.util.ArrayList;
import java.util.HashMap;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.operation.Util;


/** A probabilistic node has a set of conditional probabilities, one variable, 
 * etc. The structural aspect of the underlying  graph is in the node
 * associated.  * @author marias * @author fjdiez
 * @since OpenMarkov 1.0 * @see openmarkov.graphs.Node * @see org.openmarkov.core.model.network.ProbNet * @version 1.0 */
public class ProbNode implements Cloneable, PotentialsContainer {

	// Attributes/
	/** @frozen
	 * <code>node</code> that supplies the structural aspect. */
	protected Node node;

	/** @frozen */
	protected NodeType nodeType;
	
	/** @frozen */
	protected UtilityCombinationFunction utilityCombinationFunction;
	
	/** @frozen */
	protected ProbNet probNet;

    /** Each <code>probNode</code> has a list of potentials */
    protected ArrayList<Potential> potentialsList;
    
    /** The variable associated */
    protected Variable variable;
    
    /** Purpose of node */
    private String purpose = "";
    
    /** Relevance of node */
    private double relevance  = 5.0;
    
    /** Comment about node definition */
    private String comment = "";
    
    /** For ICI Models visualization. */
    private boolean canonicalParameters = false;
    
    /** Indicate how to visualize data: as values or not. */
    private boolean asValues = false;
    
    private PolicyType policyType = PolicyType.OPTIMAL;
    
    /**
     * Agent
     */
    private StringWithProperties agent;
    
    /** This object contains all the information that the parser reads from 
	 *  disk that does not have a direct connection with the attributes stored 
	 *  in the <code>ProbNode</code> object. */
	public HashMap<String, String> additionalProperties;

    // Constructor
    /** @param probNet. <code>ProbNet</code>
      * @param variable. <code>Variable</code> 
      * @param nodeType. <code>NodeType</code> */
	public ProbNode(ProbNet probNet, Variable variable, NodeType nodeType) {
    	this.probNet = probNet;
    	this.variable = variable;
        this.nodeType = nodeType;
        node = new Node(probNet.getGraph(), this);
        potentialsList = new ArrayList<Potential>();
        additionalProperties = new HashMap<String, String>();
        
	}
	/**
	 * Copy Constructor for the GUI
	 * @param probNode
	 */
	public ProbNode(ProbNode probNode) {
    	this.probNet = probNode.getProbNet();
    	this.variable = (Variable)probNode.getVariable();
    			//.clone();
        this.nodeType = probNode.getNodeType();
       // node = new Node(probNet.getGraph(), this);
        node = probNode.getNode();
        potentialsList = new ArrayList<Potential>();
        
       // node = new Node(probNet.getGraph(), this);
        potentialsList = new ArrayList<Potential>(probNode.getPotentials());
        additionalProperties = new HashMap<String, String>();
        
	}	


   //Methods
    /** @return Potentials associated to this <code>ProbNode</code> that 
     *  contains the received variable.
     *  <code>ArrayList</code> of <code>Potential</code>
     * @param variable. <code>Variable</code> */
    public ArrayList<Potential> getPotentials(Variable variable) {
        ArrayList<Potential> clonedPotentials = new ArrayList<Potential>();
        for (Potential potential : clonedPotentials) {
            if (potential.getVariables().contains(variable)) {
                clonedPotentials.add(potential);
            }
        }
        return clonedPotentials;
    }
    
    /** @return The <code>Variable</code> associated to this 
     *   <code>probNode</code>.
     * @consultation */
    public Variable getVariable() {
    	return variable;
    }
    
    
    /** @return Variable name. <code>String</code> */
    public String getName() {
    	return getVariable().getName();
    }

    /** @return Type of function. <code>UtilityCombinationFunction</code> */
    public UtilityCombinationFunction getUtilityCombinationFunction() {
		return utilityCombinationFunction;
	}

    /** @param potential. <code>Potential</code> */
    public void addPotential(Potential potential) {
        this.potentialsList.add(potential);
    }
    /** @param potential. <code>Potential</code> */
    public void setPotentials(ArrayList <Potential> potentials) {
        this.potentialsList = potentials;
    }

    /** @param potential. <code>Potential</code>
     * @return <code>true</code> if <code>potentialList</code> contained the
     *   specified element; otherwise <code>false</code>. */
    public boolean removePotential(Potential potential) {
        return potentialsList.remove(potential);
    }

	/** @consultation
	 * @return <code>NodeType</code> */
	public NodeType getNodeType() {
		return nodeType;
	}

    /** @return An <code>ArrayList</code> cloned with all the potentials 
     *   associated to this <code>ProbNode</code> */
	@SuppressWarnings("unchecked")
	public ArrayList<Potential> getPotentials() {
		if (potentialsList != null) {
			return (ArrayList<Potential>)potentialsList.clone();
		}
		return null;
    }

	/** @return node. <code>Node</code> */
	public Node getNode() {
		return node;
	}
	
	/** @return Number of potentials. <code>int</code> */
	public int getNumPotentials() {
		return potentialsList.size();
	}

	/** @return probNet. <code>ProbNet</code> */
	public ProbNet getProbNet() {
		return probNet;
	}
	
	/** @param utilityCombinationFunction. <code>UtilityCombinationFunction</code> */
 	public void setUtilityCombinationFunction(
			UtilityCombinationFunction utilityCombinationFunction) {
		this.utilityCombinationFunction = utilityCombinationFunction;
	}
	
	public String toString() {
		String out = new String();
		switch(nodeType) {
		case CHANCE:   
			out = out + "Chance node: " + variable.getName(); 
			break;
		case DECISION:
			out = out + "Decision node: " + variable.getName();
			break;
		case UTILITY:
			out = out + "Utility node: " + variable.getName(); 
			break;
		case COST:
			out = out + "Utility, Cost node: " + variable.getName(); 
			break;
		case EFFECTIVENESS:
			out = out + "Utility, Effectiveness node: " + variable.getName(); 
			break;
		case CE:
			out = out + "Utility, Cost-Effectiveness node: " + 
				variable.getName(); 
			break;
		default:
			out = out + "Probabilistic node: "+ variable.getName();
		   	break;
		}
		out = out + ".\n";
        int numParents = node.getNumParents();
        int numChildren = node.getNumChildren();
        int numSiblings = node.getNumSiblings();
        int numNeighbors = numParents + numChildren + numSiblings;
        if (numNeighbors == 0) {
        	out = out + "No neighbors\n";
        } else {
	        out = out + "Neighbors: " + numNeighbors + "\n";
	        if (numParents > 0) {
	        	if (numParents == 1) {
	        		out = out + numParents + " parent: ";        		
	        	} else {
	        		out = out + numParents + " parents: ";
	        	}
	        	ArrayList<Node> parents = node.getParents();
	        	for (int i = 0; i < parents.size(); i++) {
	        		ProbNode probNode =(ProbNode)parents.get(i).getObject(); 
	        		out = out + probNode.getVariable();
	        		if (i < parents.size() - 1) {
	        			out = out + ", ";	        		
	        		}
	        	}
	        	out = out + "\n";
	    	}
	    	if (numChildren > 0) {
	    		if (numChildren == 1) {
	    			out = out + numChildren + " child: ";
	    		} else {
	    			out = out + numChildren + " children: ";
	    		}
	        	ArrayList<Node> children = node.getChildren();
	        	for (int i = 0; i < children.size(); i++) {
	        		ProbNode probNode =(ProbNode)children.get(i).getObject(); 
	        		out = out + probNode.getVariable();
	        		if (i < children.size() - 1) {
	        			out = out + ", ";	        		
	        		}
	        	}
	        	out = out + "\n";
	    	}
	    	if (numSiblings > 0) {
	    		if (numSiblings == 1) {
	        		out = out + numSiblings+" sibling: "; 			
	    		} else {
	    			out = out + numSiblings+" siblings: ";
	    		}
	        	ArrayList<Node> siblings = node.getSiblings();
	        	for (int i = 0; i < siblings.size(); i++) {
	        		ProbNode probNode =(ProbNode)siblings.get(i).getObject(); 
	        		out = out + probNode.getVariable();
	        		if (i < siblings.size() - 1) {
	        			out = out + ", ";	        		
	        		}
	        	}
	        	out = out + "\n";
	    	}
        }
        int numPotentials = potentialsList.size();
		if (numPotentials > 0) {
	        out = out + "Number of potentials: " + numPotentials + "\n";
			for (Potential potential : potentialsList) {
				out = out + potential.getVariables();
				if (potential.isUtility()) {
					out = out + " - Utility Potential";
				}
				out = out + " ";
			}
			out = out + "\n";
		} else {
			out = out + "No potentials\n";
		}
		return out;
	}
	
	// TODO Comentar
	public void setUniformPotential() {
		
		ArrayList<Potential> newListPotentials = new ArrayList<Potential> ();
		ArrayList<Variable> variables = new ArrayList<Variable>();
		Variable thisVariable;
        // first, this variable. The potentials is not null
		if (this.getNodeType() == NodeType.UTILITY)
			thisVariable = potentialsList.get( 0 ).getUtilityVariable();
		else{
			thisVariable = potentialsList.get( 0 ).getVariable( 0 );
			variables.add(thisVariable);
		}
		
		int numOfCellsInTable = thisVariable.getNumStates();
		double initialValue = Util.round( 1 / (new Double(numOfCellsInTable)), 
				"0.01");
		    // add now all the parents 
		
		for (Node node: getNode().getParents()) {
			//TODO Revisar, ¿Solo se agrega/elimina un padre a la vez?
			//mpalacios
			//the set of variables could be changed, so , have to be updated.
			variables.add(((ProbNode)node.getObject()).getVariable());
			numOfCellsInTable *= ((ProbNode)node.getObject()).getVariable().
			getNumStates();
		}
		// sets a new table with new columns and with all the same values
		double[] table = new double[numOfCellsInTable] ;
		for (int i=0; i<numOfCellsInTable; i++) {
			table[i] = initialValue;
		}
		// and finally, create the potential and the list of potentials
		
		// TODO Comprobar que efectivamente es un CONDITIONAL_PROBABILITY
		TablePotential tablePotential =	new TablePotential(
				variables, PotentialRole.CONDITIONAL_PROBABILITY, table);
		newListPotentials.add( tablePotential );
		
		if (this.getNodeType() == NodeType.UTILITY){
			//tablePotential.getVariables().remove(0);
			tablePotential.setUtilityVariable(thisVariable);
		}
		potentialsList = newListPotentials;
		
	}

	public void setUniformPotential2ProbNode() {
		
		ArrayList<Potential> newListPotentials = new ArrayList<Potential> ();
		ArrayList<Variable> variables = new ArrayList<Variable>();
		Variable thisVariable;
		PotentialRole role = potentialsList.get(0).getPotentialRole();
        // first, this variable. The potentials is not null
		if (this.getNodeType() == NodeType.UTILITY)
			thisVariable = potentialsList.get( 0 ).getUtilityVariable();
		else{
			thisVariable = potentialsList.get( 0 ).getVariable( 0 );
			variables.add(thisVariable);
		}
		
		int numOfCellsInTable = thisVariable.getNumStates();
		double initialValue = Util.round( 1 / (new Double(numOfCellsInTable)), 
				"0.01");
		    // add now all the parents 
		
		for (Node node: getNode().getParents()) {
			//TODO Revisar, ¿Solo se agrega/elimina un padre a la vez?
			//mpalacios
			//the set of variables could be changed, so , have to be updated.
			variables.add(((ProbNode)node.getObject()).getVariable());
			numOfCellsInTable *= ((ProbNode)node.getObject()).getVariable().
			getNumStates();
		}
		// sets a new table with new columns and with all the same values
		double[] table = new double[numOfCellsInTable] ;
		for (int i=0; i<numOfCellsInTable; i++) {
			table[i] = initialValue;
		}
		// and finally, create the potential and the list of potentials
		
		// TODO Comprobar que efectivamente es un CONDITIONAL_PROBABILITY
		UniformPotential uniformPotetnial = new UniformPotential(variables, role);
		
		newListPotentials.add( uniformPotetnial );
		
		if (this.getNodeType() == NodeType.UTILITY && role == PotentialRole.UTILITY){
			//tablePotential.getVariables().remove(0);
			uniformPotetnial.setUtilityVariable(thisVariable);
		}
		potentialsList = newListPotentials;
		
	}
	
	/** @param purpose. <code>String</code>	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	/** @return <code>String</code> */
	public String getPurpose() {
		return purpose;
	}

	/** @param relevance. <code>double</code> */
	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}

	/** @return <code>double</code> */
	public double getRelevance() {
		return relevance;
	}

	/** @param comment the comment to set. <code>String</code> */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/** @return the comment. <code>String</code> */
	public String getComment() {
		return comment;
	}

	/** @param <code>canonicalParameters</code>. <code>boolean</code>
	 */
	public void setCanonicalParameters(boolean canonicalParameters) {
		this.canonicalParameters = canonicalParameters;
	}

	/** @return the canonicalParameters. <code>boolean</code> */
	public boolean isCanonicalParameters() {
		return canonicalParameters;
	}

	/** @param asValues the asValues to set. <code>boolean</code> */
	public void setAsValues(boolean asValues) {
		this.asValues = asValues;
	}

	/** @return the asValues. <code>boolean</code> */
	public boolean isAsValues() {
		return asValues;
	}

	/** @param modelType the modelType to set. <code>PolicyType</code> */
	public void setPolicyType(PolicyType policyType) {
		this.policyType = policyType;
	}

	/** @return the modelType. <code>PolicyType</code> */
	public PolicyType getPolicyType() {
		return policyType;
	}

	/** @return <code>true</code> if it is a decision node with a non uniform potential.
	 *  <code>boolean</code> */
	public boolean hasPolicy() {
		return nodeType == NodeType.DECISION &&  
				potentialsList.size() != 0 && 
				potentialsList.get(0).getPotentialType() != 
				PotentialType.UNIFORM;
	}

	/** @param simulationIndexVariable. <code>Variable</code>
	 * @throws NotEnoughMemoryException */
	public void samplePotentials(Variable simulationIndexVariable)
			throws NotEnoughMemoryException {
		for (int i = 0; i < potentialsList.size(); i++) {
			Potential originalPotential = potentialsList.get(i);
			potentialsList.set(i, 
					originalPotential.sample(simulationIndexVariable));
		}
	}
	
	/** @param node. <code>ProbNode</code>
	 * @return True if <code>node</code> is parent of <code>this</code> node */
	public boolean isParent(ProbNode node) {
        return this.getNode ().isParent (node.getNode ());
	}
	
	public StringWithProperties getAgent() {
		return agent;
	}

	public void setAgent (StringWithProperties agent) {
		this.agent = agent;
	}
}
