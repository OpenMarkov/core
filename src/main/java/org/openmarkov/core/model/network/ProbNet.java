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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet.ProbNetNodesHashMapsType.NodesHashMapType;
import org.openmarkov.core.model.network.constraint.ConstraintManager;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;

/** A <code>ProbNet</code> stores <code>ProbNode</code>s in a efficient manner.
 * It has the operations to manage <code>Variables, ProbNodes</code> and <code>
 *  Potentials</code>.
 * 
 * @author marias
 * @author fjdiez
 * @author mpalacios
 * @author mluque
 * @see openmarkov.graphs.Graph
 * @see org.openmarkov.core.model.network.ProbNode
 * @version 1.0
 * @since OpenMarkov 1.0 */
public class ProbNet implements Cloneable {

	// Attributes
	/**
	 * This object contains all the information that the parser reads from disk
	 * that does not have a direct connection with the attributes stored in the
	 * <code>ProbNet</code> object.
	 */
	public HashMap<String, String> additionalProperties = 
		new HashMap<String, String>();
	
    /**
     * Network type of this <code>ProbNet</code>.
     */
    private NetworkType networkType;

	/**
	 * <code>ArrayList</code> of <code>Constraints</code> that defines this
	 * <code>ProbNet</code>. This attribute is not frozen to allow conversions
	 */
    private ArrayList<PNConstraint> constraints;
	

	/** Associated graph */
    protected Graph graph;
    
    /**
     * @author mluque
     * It is the type of 'nodesHashMaps'. It contains a <code>LinkedHashMap</code>
     * from <code>NodeType</code> to <code>NodesHashMapType</code>.
     */
    public class ProbNetNodesHashMapsType {
    	
    	  /**
         * @author mluque
         * Contains a <code>LinkedHashMap</code> from <code>Variable</code>
         * to <code>ProbNode</code>.
         */
    	public class NodesHashMapType {
    		LinkedHashMap<Variable, ProbNode> nodesHashMap;
    		
    		NodesHashMapType(){
    			nodesHashMap = new LinkedHashMap<Variable, ProbNode>();
    		}
    		
    		public ProbNode get(Variable variable){
    			return nodesHashMap.get(variable);
    		}
    		
    		public void put(Variable variable, ProbNode probNode){
    			nodesHashMap.put(variable, probNode);
    		}

			public int size() {
				return nodesHashMap.size();
			}

			public Collection<ProbNode> values() {
				return nodesHashMap.values();
			}

			public void remove(Variable variable) {
				nodesHashMap.remove(variable);
			}
    	}
    	
    	
    	LinkedHashMap<NodeType, NodesHashMapType> nodesHashMaps;

    	ProbNetNodesHashMapsType(){
    		nodesHashMaps = new LinkedHashMap<NodeType, NodesHashMapType>();
    		 // create a linkedHashMap for each type of nodes
    		for (NodeType type:NodeType.values())
            {
                nodesHashMaps.put(type, new NodesHashMapType());
            }
    	}

		public void put(NodeType type, NodesHashMapType nodesHashMap) {
			nodesHashMaps.put(type, nodesHashMap);
			
		}
		
		public Collection<NodesHashMapType> values(){
			return nodesHashMaps.values();
		}

		public NodesHashMapType get(NodeType nodeType) {
			return nodesHashMaps.get(nodeType);
		}
    }
    
	/**
	 * Nodes are stored in several HashMaps to accelerate the access. The type of node
	 * determines the <code>HashMap</code> in
	 * which the node is stored.
	 */
	protected ProbNetNodesHashMapsType nodesHashMaps;

	/** Each value of the decision criteria variable represents one criterion,
	 * used in multicriteria decision analysis */
	public Variable decisionCriteria;

	private PNESupport pNESupport;

	/** The file where the network has been saved */
	private String name;

	/** ProbNet comment */
	private String comment = "";

	/** Default States of the probNet */
	private State[] defaultStates = { new State("absent"), new State("present")};

	// Constructors
    public ProbNet (NetworkType networkType)
    {
        this.graph = new Graph();
        this.pNESupport = new PNESupport (this, false);        
        this.constraints = new ArrayList<PNConstraint> ();
        this.nodesHashMaps = new ProbNetNodesHashMapsType();
        
        try
        {
            this.setNetworkType(networkType);
        }
        catch (ConstraintViolationException e)
        {
            // Impossible to reach here as the net is empty
        }        
    }

    /**
     * Creates a probabilistic network. NetworkTypeConstraint defines the
     * network type. If NetworkTypeConstraint is null the network type will be 
     * Bayesian Network
     */
    public ProbNet() {
        this(BayesianNetworkType.getUniqueInstance ());
    }    
	
    

	// Methods
    /**
     * Applies edit to the probNet
     * @param edit
     * @throws NotEnoughMemoryException
     * @throws ConstraintViolationException
     * @throws CanNotDoEditException
     * @throws NonProjectablePotentialException
     * @throws WrongCriterionException
     * @throws DoEditException
     */
    public void doEdit (PNEdit edit)
        throws NotEnoughMemoryException,
        ConstraintViolationException,
        CanNotDoEditException,
        NonProjectablePotentialException,
        WrongCriterionException,
        DoEditException
    {
        pNESupport.announceEdit (edit);
        pNESupport.doEdit (edit);
    }

    /**
     * @param constraint <code>PNConstraint</code>
     * @param check . when <code>false</code>, constraint is added to the
     *            constraints list without testing. Otherwise,
     *            <code>constraint</code> is added only when it is full-filled.
     *            <code>boolean</code>
     * @throws ConstraintViolationException
     */
    public void addConstraint (PNConstraint constraint, boolean check)
        throws ConstraintViolationException
    {
        if (!this.networkType.isApplicableConstraint (constraint))
        {
            throw new ConstraintViolationException (
                                                    "Can not apply "
                                                            + constraint.toString ()
                                                            + " to a probNet of type "
                                                            + this.networkType.getClass ());
        }
        else if (!constraints.contains (constraint))
        {
            if (check && !constraint.checkProbNet (this))
            {
                throw new ConstraintViolationException (
                                                        "Can not apply "
                                                                + constraint.toString ()
                                                                + " to this probNet.");
            }
            constraints.add (constraint);
            pNESupport.addUndoableEditListener (constraint);
        }
    }
    
    public void addConstraint (PNConstraint constraint) throws ConstraintViolationException
    {
        addConstraint(constraint, true);
    }    
    
    /**
     * @param constraints <code>ArrayList<PNConstraint></code>
     * @param check . when <code>false</code>, constraint is added to the
     *            constraints list without testing. Otherwise,
     *            <code>constraint</code> is added only when it is full-filled.
     *            <code>boolean</code>
     * @throws ConstraintViolationException
     */
    public void addConstraints (ArrayList<PNConstraint> constraints, boolean check)
        throws ConstraintViolationException
    {
        for(PNConstraint constraint : constraints)
        {
            addConstraint(constraint, check);
        }
    }
    

	/**
	 * @param constraint
	 *            <code>PNConstraint</code>
	 */
    public void removeConstraint (PNConstraint constraint)
    {
        if (constraints.contains (constraint))
        {
            constraints.remove (constraint);
            pNESupport.removeUndoableEditListener (constraint);
        }
    }
    
    /**
     * @param constraints
     *            <code>ArrayList<PNConstraint></code>
     */
    public void removeConstraints (ArrayList<PNConstraint> constraints)
    {
        for(PNConstraint constraint : constraints)
        {
            removeConstraint(constraint);
        }
    }    
    
    /**
     * @param constraintClass
     *            <code>Class</code>
     */
    public void removeAllConstraints (Class<PNConstraint> constraintClass)
    {
        ArrayList<PNConstraint> constraintsToRemove = new ArrayList<PNConstraint>();
        
        for(PNConstraint constraint : constraints)
        {
            if(constraint.getClass ().equals (constraintClass))
            {
                constraintsToRemove.add (constraint);
            }
        }
        
        constraints.removeAll (constraintsToRemove);
    }    

	/** @return <code>ArrayList</code> of <code>PNConstraint</code>s */
	@SuppressWarnings("unchecked")
	public ArrayList<PNConstraint> getConstraints() {
		return (ArrayList<PNConstraint>) constraints.clone();
	}
	
	/** @return <code>ArrayList</code> of <code>PNConstraint</code>s */
	public ArrayList<PNConstraint> getAdditionalConstraints() {
		@SuppressWarnings("unchecked")
		ArrayList<PNConstraint> additionalConstraints = (ArrayList<PNConstraint>)constraints.clone();
		ArrayList<PNConstraint> networkTypeConstraints = ConstraintManager.getUniqueInstance().
				buildConstraintList(networkType);
		additionalConstraints.removeAll(networkTypeConstraints);
		return additionalConstraints;
	}

    /**
     * Sets Network type
     * @param networkType <code>NetworkType</code>
     * @throws ConstraintViolationException 
     */
    public void setNetworkType (NetworkType networkType) throws ConstraintViolationException
    {
        ArrayList<PNConstraint> constraints = ConstraintManager.getUniqueInstance ().buildConstraintList (networkType);
        NetworkType oldNetworkType = this.networkType;
        this.networkType = networkType;        

        try
        {
            // Add new constraints implied by the network type 
            addConstraints(constraints, true);
            
            // Remove those constraints that are no longer applicable to the new network type
            ArrayList<PNConstraint> constraintsToRemove = new ArrayList<PNConstraint> ();
            for(PNConstraint constraint : this.constraints)
            {
                if(!networkType.isApplicableConstraint (constraint))
                {
                    constraintsToRemove.add (constraint);
                }
            }
            removeConstraints (constraintsToRemove);
            
        }catch(ConstraintViolationException e)
        {
            // Revert
            this.networkType = oldNetworkType;
            throw e;
        }
        
    }

    /**
     * Gets Network type constraint. There is only one and it is stored in first
     * position.
     * @return constraint. <code>NetworkType
     */
    public NetworkType getNetworkType ()
    {
        return networkType;
    }

	/**
	 * Checks all the constraints applied to this <code>probNet</code>.
	 * 
	 * @return <code>true</code> when all the constraints are full filled,
	 *         otherwise <code>false</code>.
	 */
	public boolean checkProbNet() {
		for (PNConstraint constraint : constraints) {
			if ((constraint != null) && (!constraint.checkProbNet(this))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates a low deep copy of <code>this ProbNet</code>: copy the
	 * <code>graph</code> and the <code>probNodes</code> but do not copy nor
	 * variables nor potentials.
	 * 
	 * @return <code>this probNet</code> copied.
	 * @throws ConstraintViolationException 
	 */
	public ProbNet copy() {
	    
		ProbNet probNetCopy = new ProbNet(this.networkType);
        // copy constraints
        int numConstraints = constraints.size(); 
        for (int i = 1; i < numConstraints; i++)
        {
            try
            {
                probNetCopy.addConstraint (constraints.get (i), false);
            }
            catch (ConstraintViolationException e)
            {
                // Unreachable code because constraints are not tested in copy
            }
        }
		ArrayList<ProbNode> probNodes = getProbNodes();
		// Adds variables and create corresponding nodes. Also add potentials
		for (ProbNode probNode : probNodes) {
			// Add variables and create corresponding nodes
			Variable variable = probNode.getVariable();
			ProbNode newProbNode = null;
            newProbNode = probNetCopy.addVariable (variable,
                                                   probNode.getNodeType ());
			Node newNode = newProbNode.getNode();
			Node node = probNode.getNode();
			newNode.setCoordinateX(node.getCoordinateX());
			newNode.setCoordinateY(node.getCoordinateY());
			newProbNode.setPotentials(probNode.getPotentials());

			// TODO Hacer clon para probNode y quitar estas lineas
			newProbNode.setPurpose(probNode.getPurpose());
			newProbNode.setRelevance(probNode.getRelevance());
			newProbNode.setComment(probNode.getComment());
			newProbNode.setCanonicalParameters(probNode.isCanonicalParameters());
			newProbNode.additionalProperties = additionalProperties;
		}

		// Adds links
		ArrayList<ProbNode> nodes = this.getProbNodes();
		for (ProbNode probNode1 : nodes) {
			Variable variable1 = probNode1.getVariable();
			ProbNode newNode1 = probNetCopy.getProbNode(variable1);
			ArrayList<ProbNode> neighbors = getProbNodesOfNodes(probNode1
					.getNode().getNeighbors());
			for (ProbNode probNode2 : neighbors) {
				Variable variable2 = probNode2.getVariable();
				ProbNode newNode2 = probNetCopy.getProbNode(variable2);
				if (probNode1.getNode().isSibling(probNode2.getNode())) {
					if (!newNode1.getNode().isSibling(newNode2.getNode())) {
						graph.addLink(newNode1.getNode(), newNode2.getNode(),
								false);
					}
				}
				if (probNode1.getNode().isChild(probNode2.getNode())) {
					graph.addLink(newNode1.getNode(), newNode2.getNode(), true);
				}
			}
		}

		// copy listeners
		probNetCopy.getPNESupport().setListeners(pNESupport.getListeners());
		
		// Copy additionalProperties
		Set<String> keys = additionalProperties.keySet();
		HashMap<String, String> copyProperties = new HashMap<String, String>();
		for (String key : keys) {
			copyProperties.put(key, additionalProperties.get(key));
		}
		probNetCopy.additionalProperties = copyProperties;

		return probNetCopy;
	}

	
    /**
     * Inserts a link (<code>directed = true</code> or <code>false</code>)
     * between the nodes <code>node1</code> and <code>node2</code> in
     * <code>this</code> graph.
     * @param node1 <code>ProbNode</code>
     * @param node2 <code>ProbNode</code>
     * @param directed <code>boolean</code>
     * @throws NodeNotFoundException
     */
    public void addLink (ProbNode node1, ProbNode node2, boolean directed)
    {
        // Add link between nodes. This can throw an exception
        graph.addLink (node1.getNode (), node2.getNode (), directed);
    }   
    
	/**
	 * Inserts a link (<code>directed = true</code> or <code>false</code>)
	 * between the nodes associated to <code>variable1</code> and
	 * <code>variable2</code> in <code>this</code> graph.
	 * 
	 * @param variable1
	 *            <code>Variable</code>
	 * @param variable2
	 *            <code>Variable</code>
	 * @param directed
	 *            <code>boolean</code>
	 * @throws NodeNotFoundException
	 * @throws An
	 *             exception when the addition of this link is not consistent
	 *             with the restrictions applied to the graph or when one or
	 *             both variables does not belong to <code>this</code> graph.
	 */
	public void addLink(Variable variable1, Variable variable2, boolean directed)
			throws NodeNotFoundException {
		// Get nodes
		ProbNode node1 = getProbNode(variable1);
		ProbNode node2 = getProbNode(variable2);
		
        // Throw NotExistsNodeException if one or both nodes does not exists
        if ((node1 == null) || (node2 == null)) {
            String msg = "ProbNet.addLink(" + variable1.getName() + ", "
                    + variable2.getName() + "). It does not exist: ";
            if (node1 == null) {
                msg = msg + variable2.getName();
                if (node2 == null) {
                    msg = msg + " and " + variable2.getName();
                }
            } else {
                msg = msg + node2.getName();
            }
            throw new NodeNotFoundException(msg);
        }		

		addLink(node1, node2, directed);
	}

	/**
	 * Inverts the link (<code>directed = true</code> or <code>false</code>)
	 * that goes from the nodes associated to <code>variable1</code> and
	 * <code>variable2</code> in <code>this</code> graph.
	 * 
	 * @param variable1
	 *            <code>Variable</code>
	 * @param variable2
	 *            <code>Variable</code>
	 * @param directed
	 *            <code>boolean</code>
	 * @throws An
	 *             exception when the inversion of this link is not consistent
	 *             with the restrictions applied to the graph or when one or
	 *             both variables does not belong to <code>this</code> graph.
	 */
	public void invertLink(Variable variable1, Variable variable2,
			boolean directed) throws Exception {
		removeLink(variable1, variable2, true);
		addLink(variable2, variable1, true);
	}

	/** @return Number of nodes in <code>probNet</code>. <code>int</code> */
	public int getNumNodes() {
		int numNodes = 0;
		for (NodesHashMapType hashMap : nodesHashMaps.values()) {
			numNodes = numNodes + hashMap.size();
		}
		return numNodes;
	}

	/**
	 * @param nodeType
	 *            - <code>NodeType</code>
	 * @return Number of nodes with <code>NodeType = nodeType</code>.
	 *         <code>int</code>
	 */
	public int getNumNodes(NodeType nodeType) {
		return (nodesHashMaps.get(nodeType).size());
	}

	/**
	 * @param evidenceCase
	 * @return The potentials of the network projected on the evidence
	 * @throws NotEnoughMemoryException
	 * @throws NonProjectablePotentialException 
	 * @throws WrongCriterionException 
	 * @throws NoFindingException
	 */
	public ArrayList<Potential> getProjectedPotentials(EvidenceCase evidenceCase)
			throws NotEnoughMemoryException, NonProjectablePotentialException, WrongCriterionException {
		ArrayList<Potential> originalPotentials = getPotentials();
		ArrayList<Potential> projectedPotentials = new ArrayList<Potential>();

		// each original potential may yield several projected potentials;
		for (Potential potential : originalPotentials) {
			//auxPotentials = potential.project(evidenceCase);
			for (TablePotential auxPotential : 
					potential.tableProject(evidenceCase, null)) {
				projectedPotentials.add(auxPotential);
			}
		}
		return projectedPotentials;
	}

	/**
	 * @param evidenceCase
	 * @return The potentials of the network projected on the evidence
	 * @throws NotEnoughMemoryException
	 * @throws NonProjectablePotentialException 
	 * @throws WrongCriterionException 
	 * @throws NoFindingException
	 */
    public ArrayList<TablePotential> tableProjectPotentials (EvidenceCase evidenceCase)
	throws NotEnoughMemoryException, NonProjectablePotentialException, WrongCriterionException {
		ArrayList<Potential> originalPotentials = getPotentials();
		ArrayList<TablePotential> projectedPotentials = new ArrayList<TablePotential>();

		// each original potential may yield several projected potentials;
		ArrayList<TablePotential> auxPotentials;
		for (Potential potential : originalPotentials) {
			auxPotentials = potential.tableProject(evidenceCase, null);
			for (TablePotential auxPotential : auxPotentials) {
				projectedPotentials.add(auxPotential);
			}
		}
		return projectedPotentials;
	}

	/**
	 * @return All the potentials of this network. <code>ArrayList</code> of
	 *         <code>Potential</code>
	 * @consultation
	 */
	public ArrayList<Potential> getPotentials() {
		ArrayList<ProbNode> nodes = getProbNodes();
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		for (ProbNode node : nodes) {
			potentials.addAll(node.getPotentials());
		}
		return potentials;
	}

	/**
	 * @return All the potentials of this network except those assigned to 
	 * utility nodes that are parents of super-value utility nodes. 
	 * <code>ArrayList</code> of <code>Potential</code>
	 * @consultation
	 */
	// TODO find a better name for this method
	public ArrayList<Potential> getPotentials2() {
		ArrayList<ProbNode> nodes = getProbNodes();
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		for (ProbNode node : nodes) {
			if ((node.getNodeType() != NodeType.UTILITY 
					|| node.getNode().getNumChildren() == 0)) {
				potentials.addAll(node.getPotentials());
			}
		}
		return potentials;
	}

	/**
	 * @return <code>ArrayList</code> with all <code>ProbNode</code>s
	 * @consultation
	 */
	public ArrayList<ProbNode> getProbNodes() {
		ArrayList<ProbNode> nodes = new ArrayList<ProbNode>(getNumNodes());
		for (NodesHashMapType hashMap : nodesHashMaps.values()) {
			nodes.addAll(hashMap.values());
		}
		return nodes;
	}

	/**
	 * @return All the nodes corresponding to variables in same order.
	 *         <code>ArrayList</code> of <code>ProbNode</code>
	 * @param variables
	 *            <code>ArrayList</code> of <code>Variable</code>
	 * @consultation
	 */
	public ArrayList<ProbNode> getProbNodes(ArrayList<Variable> variables) {
		ArrayList<ProbNode> probNodes = new ArrayList<ProbNode>(
				variables.size());
		for (Variable variable : variables) {
			probNodes.add(getProbNode(variable));
		}
		return probNodes;
	}

	/**
	 * @param nodes
	 *            <code>ArrayList</code> of <code>Node</code>
	 * @return <code>ArrayList</code> of <code>ProbNode</code>
	 */
	public static ArrayList<ProbNode> getProbNodesOfNodes(ArrayList<Node> nodes) {
		ArrayList<ProbNode> probNodes = new ArrayList<ProbNode>(nodes.size());
		for (Node node : nodes) {
			probNodes.add((ProbNode) node.getObject());
		}
		return probNodes;
	}

	/**
	 * @param probNodes
	 *            <code>ArrayList</code> of <code>ProbNode</code>
	 * @return <code>ArrayList</code> of <code>ProbNode</code>
	 */
	public static ArrayList<Node> getNodesOfProbNodes(
			ArrayList<ProbNode> probNodes) {
		ArrayList<Node> nodes = new ArrayList<Node>(probNodes.size());
		for (ProbNode probNode : probNodes) {
			nodes.add(probNode.getNode());
		}
		return nodes;
	}

	/**
	 * @return All the nodes of certain kind
	 * @param nodeType
	 * @consultation
	 */
	public ArrayList<ProbNode> getProbNodes(NodeType nodeType) {
		return new ArrayList<ProbNode>(nodesHashMaps.get(nodeType).values());
	}

	/**
	 * The potentials that contain <code>variable</code> are stored in the node
	 * associated to the <code>variable</code> or in the neighbors of that node.
	 * This method returns as well the constant potentials (i.e., the potentials
	 * that do not depend on any variable) stored in the node associated to
	 * <code>variable</code>.
	 * 
	 * @param variable
	 *            <code>Variable</code>.
	 * @return <code>ArrayList</code> of all the <code>Potential</code>s in this
	 *         network that contains <code>variable</code>
	 */
	public ArrayList<Potential> getPotentials(Variable variable) {
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		ProbNode probNode = getProbNode(variable);
		// potentials associated to this node
		if (probNode != null) { // Variable exists in this ProbNet
			potentials.addAll(probNode.getPotentials());
			// potentials in neighbors that contains variable
			ArrayList<ProbNode> neighbors = getProbNodesOfNodes(probNode.getNode()
					.getNeighbors());
			for (ProbNode node : neighbors) {
				ArrayList<Potential> nodePotentials = node.getPotentials();
				for (Potential potential : nodePotentials) {
					if (potential.contains(variable)) {
						potentials.add(potential);
					}
				}
			}
		}
		return potentials;
	}

	/**
	 * @param nodeType
	 * @return All the utility potentials when <code>isUtility</code> param =
	 *         <code>true</code> otherwise returns all chance potentials.
	 * @consultation
	 */
	public ArrayList<Potential> getPotentialsType(NodeType nodeType) {
		NodesHashMapType nodesType = nodesHashMaps.get(nodeType);
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		for (ProbNode node : nodesType.values()) {
			potentials.addAll(node.getPotentials());
		}
		return potentials;
	}
	
	
	/**
	 * @param role
	 * @return All the potentials of a role.
	 */
	public ArrayList<Potential> getPotentialsRole(PotentialRole role) {
		
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		for (NodesHashMapType nodesHashMap:nodesHashMaps.values()){
			for (ProbNode auxProbNode:nodesHashMap.values()){
				for (Potential auxPot:auxProbNode.getPotentials()){
					if (auxPot.getPotentialRole()==role){
						potentials.add(auxPot);
					}
				}
			}
		}
		
		return potentials;
	}

	/**
	 * Gets all the probability potentials that contain the
	 * <code>Variable</code> received. The potentials that can contains that
	 * variable are in the node associated to the variable and its neighbors.
	 * 
	 * @param variable
	 * @return <code>ArrayList</code> of potentials containing
	 *         <code>variable</code>.
	 * @argCondition variable belongs to this <code>ProbNet</code>
	 */
	public ArrayList<Potential> getProbPotentials(Variable variable) {
		ProbNode nodeVariable = getProbNode(variable);
		ArrayList<ProbNode> allNodes = getProbNodesOfNodes(nodeVariable
				.getNode().getNeighbors());
		allNodes.add(nodeVariable);
		ArrayList<Potential> potentialsVariable = new ArrayList<Potential>();
		for (ProbNode node : allNodes) {
			ArrayList<Potential> potentialsNode = ((ProbNode) node)
					.getPotentials();
			for (Potential potential : potentialsNode) {
				if ((potential.getVariables().contains(variable))
						&& !potential.isUtility()) {
					potentialsVariable.add(potential);
				}
			}
		}
		return potentialsVariable;
	}

	/**
	 * Gets all the utility potentials that contains the <code>variable</code>
	 * received.
	 * <p>
	 * The potentials that can contains that variable are in the node associated
	 * to the variable and its neighbors.
	 * 
	 * @param variable
	 *            <code>Variable</code>.
	 * @return <code>ArrayList</code> of potentials containing
	 *         <code>variable</code>.
	 * @argCondition variable belongs to this <code>ProbNet</code>
	 */
	public ArrayList<Potential> getUtilityPotentials(Variable variable) {
		ProbNode nodeVariable = getProbNode(variable);
		ArrayList<ProbNode> allNodes = getProbNodesOfNodes(nodeVariable
				.getNode().getNeighbors());
		allNodes.add(nodeVariable);
		ArrayList<Potential> potentialsVariable = new ArrayList<Potential>();
		for (ProbNode node : allNodes) {
			ArrayList<Potential> potentialsNode = node.getPotentials();
			for (Potential potential : potentialsNode) {
				if ((potential.getVariables().contains(variable))
						&& potential.isUtility()) {
					potentialsVariable.add(potential);
				}
			}
		}
		return potentialsVariable;
	}

	public ArrayList<Potential> getUtilityPotentials2(Variable variable) {
		ArrayList<Potential> allPotentials = getPotentials(); 
		ArrayList<Potential> potentialsVariable = new ArrayList<Potential>();
		for (Potential potential : allPotentials) {
			if (potential.contains(variable) && potential.isUtility()) {
				potentialsVariable.add(potential);
			}
		}
		return potentialsVariable;
	}

	/**
	 * @param variable
	 *            <code>Variable</code>
	 * @return <code>ArrayList</code> of <code>Potentials</code> that contains
	 *         the variable received.
	 * @argCondition variable belongs to this <code>ProbNet</code>
	 */
	public ArrayList<Potential> extractPotentials(Variable variable) {
		// get the nodes that contains potentials associated to the variable
		ArrayList<ProbNode> nodes = new ArrayList<ProbNode>();
		// node associated to variable
		ProbNode nodeContainer = getProbNode(variable);
		nodes.add(nodeContainer);
		// and its siblings
		nodes.addAll(getProbNodesOfNodes(nodeContainer.getNode().getNeighbors()));
		ArrayList<Potential> potentialsVariable = new ArrayList<Potential>();
		// for each node extract its potentials ...
		for (ProbNode node : nodes) {
			ArrayList<Potential> potentialsNode = node.getPotentials();
			for (Potential potential : potentialsNode) {
				// ... and selects the potentials that contains the variable
				if (potential.getVariables().contains(variable)) {
					potentialsVariable.add(potential);
				}
			}
		}
		return potentialsVariable;
	}

	/**
	 * Removes <code>potential</code> from this <code>ProbNet</code>
	 * 
	 * @return The node where the potential was located or <code>null</code> if
	 *         it did not exists
	 */
	public ProbNode removePotential(Potential potential) {
		ArrayList<Variable> variables = potential.getVariables();
		ArrayList<ProbNode> candidateNodes = new ArrayList<ProbNode>();

		// gets probNodes that could contain the potential
		if (!potential.isUtility()) { // chance potential
			// find nodes corresponding to variables
			for (Variable variable : variables) {
				ProbNode probNode = getProbNode(variable);
				if (probNode != null) {
					candidateNodes.add(getProbNode(variable));
				}
			}
		} else { // utility potential.
			ArrayList<ProbNode> utilityNodes = getProbNodes(NodeType.UTILITY);
			candidateNodes.addAll(utilityNodes);

			ProbNode firstProbNode = getProbNode(variables.get(0));
			candidateNodes.add(firstProbNode);
		}

		// find in such nodes the potential to remove
		for (ProbNode probNode : candidateNodes) {
			if (probNode != null) {
				ArrayList<Potential> potentialsNode = probNode.getPotentials();
				for (Potential potentialNode : potentialsNode) {
					if (potentialNode == potential) {
						if (probNode.removePotential(potentialNode)) {
							return probNode;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Removes all the potentials that contains the <code>variable</code>
	 * associated to <code>node</code>
	 * 
	 * @param probNode
	 *            <code>ProbNode</code>
	 */
	public void removePotentials(ProbNode probNode) {
		// get the nodes that contains potentials associated to the variable
		ArrayList<ProbNode> nodes = new ArrayList<ProbNode>();
		Variable variable = probNode.getVariable();
		nodes.add(probNode);
		// and its siblings
		nodes.addAll(getProbNodesOfNodes(probNode.getNode().getSiblings()));
		// for each node extract its potentials ...
		for (ProbNode node : nodes) {
			ArrayList<Potential> potentialsNode = new ArrayList<Potential>(
					node.getPotentials());
			for (Potential potential : potentialsNode) {
				// ... and removes the potentials that contains the variable
				if (potential.getVariables().contains(variable)) {
					node.removePotential(potential);
				}
			}
		}
	}

	/**
	 * Removes all the potentials in the array of potentials received.
	 * 
	 * @param toRemovePotentials
	 *            <code>ArrayList</code> of <code>Potential</code>
	 */
	public void removePotentials(ArrayList<Potential> toRemovePotentials) {
		if (toRemovePotentials != null) {
			for (Potential potential : toRemovePotentials) {
				removePotential(potential);
			}
		}
	}

	/**
	 * @param variable
	 *            . <code>Variable</code>
	 * @param nodeType
	 *            . <code>NodeType</code>
	 * @return The <code>probNode</code> that points to <code>variable</code> in
	 *         <code>this</code> network.
	 * @argCondition the variable must not be in the ProbNet.
	 */
	public ProbNode addVariable(Variable variable, NodeType nodeType) {
		ProbNode probNode = nodesHashMaps.get(nodeType).get(variable);
		if (probNode == null) {
			probNode = new ProbNode(this, variable, nodeType);
		}
		nodesHashMaps.get(nodeType).put(variable, probNode);
		return probNode;
	}

	/**
	 * @param probNode
	 *            . <code>ProbNode</code>
	 * @argCondition the variable must not be in the ProbNet.
	 * This method is used to redo the <code>AddVariableEdit</code>, i.e., to
	 * reinsert a ProbNode that has been removed. 
	 */
	public void addProbNode(ProbNode probNode) {
		Variable variable = probNode.getVariable();
		NodeType nodeType = probNode.getNodeType();
		nodesHashMaps.get(nodeType).put(variable, probNode);
		this.getGraph().uf_addNode( probNode.getNode() );
	}

	/**
	 * @param nameOfVariable
	 *            <code>String</code>
	 * @return The <code>ProbNode</code> that matches the
	 *         <code>nameOfVariable</code>
	 * @consultation
	 */
	public ProbNode getProbNode(String nameOfVariable)
			throws ProbNodeNotFoundException {
		for (NodeType nodeType : NodeType.values()) {
			Collection<ProbNode> probNodes = nodesHashMaps.get(nodeType).values();
			for (ProbNode probNode : probNodes) {
						
				if (probNode.getVariable().getName().contentEquals(
						nameOfVariable)) {
					return probNode;
				}
			}
		}
		throw new ProbNodeNotFoundException(getName(), nameOfVariable);
	}

	/**
	 * @param nameOfVariable
	 *            <code>String</code>
	 * @return The <code>ProbNode</code> that matches the
	 *         <code>nameOfVariable</code>
	 * @consultation
	 */
	public ProbNode getProbNode(Node node) throws ProbNodeNotFoundException {
		for (NodeType nodeType : NodeType.values()) {
			Collection<ProbNode> probNodes = nodesHashMaps.get(nodeType).values();
			for (ProbNode probNode : probNodes) {
				if (probNode.getNode().equals(node)) {
					return probNode;
				}
			}
		}
		throw new ProbNodeNotFoundException(getName(), node.toString());
	}

	/**
	 * @param nameOfVariable
	 *            <code>String</code>
	 * @param nodeType
	 *            <code>NodeType</code>
	 * @return The node with <code>nameOfVariable</code> and
	 *         <code>kindOfNode</code> if exists otherwhise null
	 * @throws ProbNodeNotFoundException
	 * @consultation
	 */
	public ProbNode getProbNode(String nameOfVariable, NodeType nodeType)
			throws ProbNodeNotFoundException {
		for (ProbNode node : nodesHashMaps.get(nodeType).values()) {
			if (node.getVariable().getName().contentEquals(nameOfVariable)) {
				return node;
			}
		}
		throw new ProbNodeNotFoundException(getName(), nameOfVariable);
	}

	public String getName() {
		return name;
	}

	/**
	 * @param variable
	 *            <code>Variable</code>
	 * @return The <code>ProbNode</code> that matches the <code>Variable</code>
	 * @throws ProbNodeNotFoundException 
	 * @consultation
	 */
	public ProbNode getProbNode(Variable variable) {
		ProbNode probNode = null;
		for (NodesHashMapType nodes : nodesHashMaps.values()) {
			if ((probNode = nodes.get(variable)) != null) {
				break;
			}
		}
		return probNode;
	}

	/**
	 * @param nameOfVariable
	 *            . <code>String</code>
	 * @return variable that matches <code>variableName</code> if exists,
	 *         otherwise <code>null</code>. <code>Variable</code>
	 * @consultation
	 */
	public Variable getVariable(String variableName)
			throws ProbNodeNotFoundException {
		ProbNode probNode = getProbNode(variableName);
		return probNode.getVariable();
	}

	// TODO Con este nuevo metodo podemos evitar la chapuza hecha en
	// varios lugares de invocar getVariable para ver si lanzaba una excepcion.
	// Revisar el uso de esa excepcion y evitarla en lo posible.
	public boolean containsVariable(String variableName) {
		ProbNode probNode = null;
		try {
			probNode = getProbNode(variableName);
		} catch (ProbNodeNotFoundException e) {
		}
		return (probNode != null);
	}

	/**
	 * @param variable
	 *            . a <code>Variable</code>
	 * @argCondition variable must be in the network and must be temporal
	 * @param int timeSlice
	 * @return a new variable having the same base name as the first argument but 
	 * in the time slice indicated by the second argument
	 * @consultation
	 */
	public Variable getShiftedVariable(Variable variable, int timeDifference) 
	 {
		int timeSlice = variable.getTimeSlice() + timeDifference;
		String baseName = variable.getBaseName();
		try {
			return getVariable(baseName+" ["+timeSlice+"]");
		} catch (ProbNodeNotFoundException e) {
			// TODO Unreachable code
			throw new Error("Error: " + baseName+" ["+timeSlice+"]");
		}
	}

	/**
	 * Adds the received potential to the list of potentials of the conditioned
	 * variable (the first one).
	 * 
	 * @preCondition network contains at least one chance variable
	 * @argCondition potential type must correspond with the roles (discrete or
	 *               continuous) of the variables in the network
	 * @argCondition If A is the first variable in the potential and
	 *               B<sub>0</sub> ... B<sub>n</sub> the remainders, there must
	 *               be a directed link B<sub>i</sub> -> A for every variable
	 *               B<sub>i</sub> in the potential (other than A)
	 * @param potential
	 *            . <code>Potential</code>
	 * @return The <code>ProbNode</code> in which the <code>potential</code>
	 *         received has been added.
	 */
	public ProbNode addPotential(Potential potential) {
		ArrayList<Variable> potentialVariables = potential.getVariables();
		addPotentialVariables(potential);
		ProbNode probNode = null; // The potential will be added here
		if (potential.isUtility()) { // Create probNode without variable
			probNode = addUtilityPotential(potential, potentialVariables);
		} else {
			if (potentialVariables.size() > 0) {
				probNode = addProbabilityPotential(potential,
						potentialVariables);
			} else {// potential does not depend on any variable (is a constant)
				ArrayList<ProbNode> chanceProbNodes = getProbNodes(NodeType.CHANCE);
				if (chanceProbNodes.size() > 0) {
					probNode = chanceProbNodes.get(0);
					probNode.addPotential(potential);
					// If there are no chance nodes there is no reason to 
					// add the constant potential
				} 
			}
		}
		return probNode;
	}

	/**
	 * If there are missing variables (variables that exists in the
	 * <code>potential</code> but not in the <code>probNet</code>), the method
	 * adds all those variables to the <code>probNet</code>.
	 * 
	 * @param potential
	 *            . <code>Potential</code>
	 */
	private void addPotentialVariables(Potential potential) {
		// Common part
		ArrayList<Variable> potentialVariables = potential.getVariables();
		for (Variable variable : potentialVariables) {
			// add the variables that were not yet in the network
			if (getProbNode(variable) == null) {
				addVariable(variable, NodeType.CHANCE);
			}
		}
		// Only for utility potentials
		if (potential.isUtility()) {
			Variable utilityVariable = potential.getUtilityVariable();
			if (getProbNode(utilityVariable) == null) {
				addVariable(utilityVariable, NodeType.UTILITY);
			}
		}
	}

	/**
	 * @param potential
	 *            . <code>Potential</code>
	 * @param potentialVariables
	 *            . <code>ArrayList</code> of <code>Variable</code>s
	 * @return The <code>ProbNode</code> in which the <code>potential</code> is
	 *         stored
	 */
	private ProbNode addUtilityPotential(Potential potential,
			ArrayList<Variable> potentialVariables) {
		Variable utilityVariable = potential.getUtilityVariable();
		ProbNode utilityProbNode = this.getProbNode(utilityVariable);
		if (utilityProbNode == null) { // The variable does not exists yet
			utilityProbNode = 
				new ProbNode(this, utilityVariable, NodeType.UTILITY);
		}
		utilityProbNode.addPotential(potential);
		if (!hasConstraint(OnlyUndirectedLinks.class)) {
			Node utilityNode = utilityProbNode.getNode();
			for (Variable variable : potentialVariables) {
				Node parent = getProbNode(variable).getNode();
				if (!parent.isParent(utilityNode)) {
					graph.addLink(parent, utilityNode, true);
				}
			}
		}
		return utilityProbNode;
	}

	/**
	 * @param potential
	 *            - <code>Potential</code>
	 * @param potentialVariables
	 *            - <code>ArrayList</code> of <code>Variable</code>s
	 */
	private ProbNode addProbabilityPotential(Potential potential,
			ArrayList<Variable> potentialVariables) {
		Variable conditionedVariable = potentialVariables.get(0);
		ProbNode conditionedProbNode = getProbNode(conditionedVariable);
		conditionedProbNode.addPotential(potential);
		if (hasConstraint(OnlyUndirectedLinks.class)) {
			createClique(potential);
		} else {
			if (hasConstraint(OnlyDirectedLinks.class)) {
				Node conditionedNode = conditionedProbNode.getNode();
				for (int i = 1; i < potentialVariables.size(); i++) {
					Node conditioningNode = getProbNode(
							potentialVariables.get(i)).getNode();
					if (!conditioningNode.isParent(conditionedNode)) {
						graph.addLink(conditioningNode, conditionedNode, true);
					}
				}
			}
		}
		return conditionedProbNode;
	}

    /**
     * @param constraint <code>Class</code>
     * @return <code>true</code> if this probabilistic network contains the
     *         received constraint type.
     */
    @SuppressWarnings("rawtypes")
    public boolean hasConstraint (Class constraint)
    {
        for (PNConstraint constraintProbNet : constraints)
        {
            if (constraintProbNet.getClass () == constraint)
            {
                return true;
            }
        }
        return false;
    }

	/**
	 * @return All <code>Variable</code>s except utility nodes variables.
	 *         <code>ArrayList</code> of <code>Variable</code>.
	 */
	public ArrayList<Variable> getChanceAndDecisionVariables() {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ArrayList<ProbNode> nodes = getProbNodes();
		for (ProbNode node : nodes) {
			if (node.getNodeType() != NodeType.UTILITY) {
				variables.add(node.getVariable());
			}
		}
		return variables;
	}

	/**
	 * @return Variables corresponding to the node type received.
	 *         <code>ArrayList</code> of <code>Variable</code>
	 * @param nodeType
	 *            <code>NodeType</code>
	 */
	public ArrayList<Variable> getVariables(NodeType nodeType) {
		ArrayList<Variable> variablesType = new ArrayList<Variable>();
		ArrayList<ProbNode> probNodesType = getProbNodes(nodeType);
		for (ProbNode probNode : probNodesType) {
			variablesType.add(probNode.getVariable());
		}
		return variablesType;
	}

	/**
	 * @param nodes
	 *            list of nodes of this <code>ProbNet</code>.
	 *            <code>ArrayList</code> of <code>extends Node</code>
	 * @return variables corresponding to the received nodes.
	 *         <code>ArrayList</code> of <code>Variable</code>
	 */
	public static ArrayList<Variable> getVariables(List<?> objects) {
		ArrayList<Variable> variables = new ArrayList<Variable>(objects.size());
		for (Object object : objects) {
			if (object.getClass() == Node.class) {
				ProbNode probNode = (ProbNode)((Node)object).getObject();
				variables.add(probNode.getVariable());
			} else if (object.getClass() == ProbNode.class) {
				ProbNode probNode = (ProbNode)object;
				variables.add(probNode.getVariable());
			}
		}
		return variables;
	}
	
	/**
	 * @return All the variables. <code>ArrayList</code> of <code>Variable</code>
	 */
	public ArrayList<Variable> getVariables() {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		for (NodesHashMapType probNodesMap : nodesHashMaps.values()) {
			for (ProbNode probNode : probNodesMap.values()) {
				variables.add(probNode.getVariable());
			}
		}
		return variables;
	}

	/**
	 * Removes <code>probNode</code> from <code>this ProbNet</code> and removes
	 * also the associated <code>node</code> from the associated
	 * <code>Graph</code>.
	 * 
	 * @param probNode
	 *            <code>Node</code>
	 */
	public void removeProbNode(ProbNode probNode) {
		if (probNode != null) {
			NodeType nodeKindValue = probNode.getNodeType();
			Variable variable = probNode.getVariable();
			NodesHashMapType nodesMap = nodesHashMaps.get(nodeKindValue);
			nodesMap.remove(variable);
			graph.removeNode(probNode.getNode());
		}
	}

	/**
	 * @param node1
	 *            <code>ProbNode</code>
	 * @param node2
	 *            <code>ProbNode</code>
	 * @param directed
	 *            <code>boolean</code>
	 */
    public void removeLink (ProbNode node1, ProbNode node2, boolean directed)
    {
		graph.removeLink(node1.getNode(), node2.getNode(), directed);
	}
	
    /**
     * @param variable1
     *            <code>Variable</code>
     * @param variable2
     *            <code>Variable</code>
     * @param directed
     *            <code>boolean</code>
     */
    public void removeLink (Variable variable1, Variable variable2, boolean directed)
    {
        ProbNode node1 = getProbNode(variable1);
        ProbNode node2 = getProbNode(variable2);
        removeLink(node1, node2, directed);
    }	

	/** @return <code>graph</code> associated to this <code>probNet</code>. */
	public Graph getGraph() {
		return graph;
	}

	/** @return Number of potentials. <code>int</code> */
	public int getNumPotentials() {
		int numPotentials = 0;
		for (NodesHashMapType linkedHasMap : nodesHashMaps.values()) {
			for (ProbNode probNode : linkedHasMap.values()) {
				numPotentials += probNode.getNumPotentials();
			}
		}
		return numPotentials;
	}
	
	/** Puts a property A that is an array as a set of additionalProperties with names
	 * A[0], A[1],...
	 * @param key. <code>String</code>
	 * @param values. <code>ArrayList</code> of <code>String</code> */
	public static void putArrayProperty(HashMap<String, String> properties, 
			String key, ArrayList<String> values) {
		if (values != null) {
			int numProperties = values.size();
			for (int i = 0; i < numProperties; i++) {
				properties.put(key + "[" + i + "]", values.get(i));
			}
		}
	}
	
	/** Gets a multi-valued property as an ArrayList.
	 * @param key. <code>String</code>
	 * @return <code>ArrayList</code> of <code>String</code> */
	public static ArrayList<String> getArrayProperty(
			HashMap<String, String> properties, String key) {
		ArrayList<String> values = new ArrayList<String>();
		int i = 0;
		String value = null;
		do {
			String extendedKey = key + "[" + i++ + "]";
			value = properties.get(extendedKey);
			if (value != null) {
				values.add(value);
			}
		} while (value != null);
		if (values.size() == 0) { // property does not exists
			values = null;
		}
		return values;
	}

	/**
	 * Creates a clique by adding undirected links between the nodes that
	 * represent the variables of the <code>potential</code>.
	 * 
	 * @param potential
	 *            <code>Potential</code>.
	 */
	private void createClique(Potential potential) {
		ArrayList<Variable> variablesPotential = potential.getVariables();
		Node node1, node2;
		int potentialSize = variablesPotential.size();
		for (int i = 0; i < potentialSize - 1; i++) {
			ProbNode probNode1 = getProbNode(variablesPotential.get(i));
			if (probNode1.getNodeType() != NodeType.CHANCE) {
				continue;
			}
			node1 = probNode1.getNode();
			for (int j = i + 1; j < potentialSize; j++) {
				ProbNode probNode2 = getProbNode(variablesPotential.get(j));
				if (probNode2.getNodeType() != NodeType.CHANCE) {
					continue;
				}
				node2 = probNode2.getNode();
				if (!node1.isSibling(node2)) {
					new Link(node1, node2, false);
				}
			}
		}
	}

	public PNESupport getPNESupport() {
		return pNESupport;
	}

	/** @return String */
	public String toString() {
		String out = new String();
		ArrayList<ProbNode> nodes = getProbNodes();
		int numPotentials = getNumPotentials();
		int numNodes = nodes.size();
		if (numNodes == 0) {
			out = out + "No nodes.\n";
		} else {
			out = out
					+ new String("Number of probabilistic nodes: " + numNodes
							+ "\n");
		}
		if (numPotentials == 0) {
			out = out + "No potentials.\n";
		} else {
			out = out + "Number of potentials: " + numPotentials + "\n";
		}
		if (constraints.size() == 0) {
			out = out + "No constraints\n";
		} else {
			out = out + "Constraints: ";
			for (int i = 0; i < constraints.size(); i++) {
				String strConstraint = constraints.get(i).toString();
				strConstraint = strConstraint.substring(
						strConstraint.lastIndexOf('.') + 1,
						strConstraint.length());
				out = out + strConstraint;
				if (i < constraints.size() - 1) {
					out = out + ", ";
				}
			}
			out = out + "\n";
		}
		out = out + "\n";
		for (ProbNode probNode : nodes) {
			out = out + probNode.toString() + "\n";
		}
		return out;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param defaultStates the defaultStates to set
	 */
	public void setDefaultStates(State[] defaultStates) {
		this.defaultStates = defaultStates;
	}

	/**
	 * @return the defaultStates
	 */
	public State[] getDefaultStates() {
		return defaultStates;
	}

	/** @argCondition oldProbNode belongs to this probNet */
	public ProbNode addShiftedProbNode(ProbNode oldProbNode, int timeDifference,
			double coordinateXOffset, double coordinateYOffset) {
		Variable oldVariable = oldProbNode.getVariable();
		Variable newVariable = (Variable) oldVariable.clone();
		newVariable.setTimeSlice(oldVariable.getTimeSlice() + timeDifference);
		ProbNode newProbNode = addVariable(newVariable, 
				oldProbNode.getNodeType());
		Node oldNode = oldProbNode.getNode();
		Node newNode = newProbNode.getNode();
		newNode.setCoordinateX(oldNode.getCoordinateX()+coordinateXOffset);
		newNode.setCoordinateY(oldNode.getCoordinateY()+coordinateYOffset);
		// TODO Hacer clon para probNode y quitar estas lineas
		newProbNode.setPurpose(oldProbNode.getPurpose());
		newProbNode.setRelevance(oldProbNode.getRelevance());
		newProbNode.setComment(oldProbNode.getComment());
		newProbNode.setCanonicalParameters(oldProbNode.isCanonicalParameters());
		newProbNode.additionalProperties = additionalProperties;

		
		return newProbNode;
	}
	
	/** Given a modelNet, applies the node positions of the modelNet to the
	 * nodes of the current probNet
	 * @param modelNet - the modelNet to copy the node positions from
	 */
    public void copyNodePositionsFromModelNet(ProbNet modelNet)
    {
        ProbNode positionNode = null;
    	
	    /* Take the positions of the nodes */
	    if(modelNet != null){
	        for (ProbNode node : modelNet.getProbNodes()){
	            try {
					positionNode = this.getProbNode(node.getVariable().
							getName());
					if (positionNode != null){
						positionNode.getNode().setCoordinateX(node.getNode().
								getCoordinateX());
						positionNode.getNode().setCoordinateY(node.getNode().
								getCoordinateY());
					}
	            } catch (ProbNodeNotFoundException e) {}
	        }
	    }        
    }
	

	public void setDecisionCriteria(String[] criteriaNames) {
		int numCriteria = criteriaNames.length;
		State[] states = new State[numCriteria];
		for (int i = 0; i < numCriteria; i++) {
			states[i] = new State(criteriaNames[i]);
		}
		decisionCriteria = new Variable("### Decision Criteria ###", states);
	}
	
	/**
	 * Returns true if and only if there is a path between nodes a and b
	 * @param a
	 * @param b
	 * @param directed
	 * @return
	 */
	public boolean existsPath(ProbNode a, ProbNode b, boolean directed)
	{
	    return graph.existsPath (a.getNode (),b.getNode (), directed);
	}

}
