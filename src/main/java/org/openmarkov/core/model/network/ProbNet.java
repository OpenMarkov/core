/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.openmarkov.core.model.network.constraint.ConstraintManager;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.OnlyOneAgent;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;

/**
 * A <code>ProbNet</code> stores <code>Node</code>s in a efficient manner.
 * It has the operations to manage <code>Variables, nodes</code> and <code>
 *  Potentials</code>.
 * 
 * @author marias
 * @author fjdiez
 * @author mpalacios
 * @author mluque
 * @see openmarkov.graphs.Graph
 * @see org.openmarkov.core.model.network.Node
 * @version 1.0
 * @since OpenMarkov 1.0
 */
public class ProbNet  extends Graph<Node> implements Cloneable{
    // Attributes
    /**
     * This object contains all the information that the parser reads from disk
     * that does not have a direct connection with the attributes stored in the
     * <code>ProbNet</code> object.
     */
    public HashMap<String, String>     additionalProperties = new HashMap<String, String>();
    /**
     * Network type of this <code>ProbNet</code>.
     */
    private NetworkType                networkType;
    /**
     * <code>ArrayList</code> of <code>Constraints</code> that defines this
     * <code>ProbNet</code>. This attribute is not frozen to allow conversions
     */
    private List<PNConstraint>         constraints;
    /** Set of agents, defined by a name. Each one may have several properties. */
    // private StringsWithProperties agents;
    private List<StringWithProperties> agents;

    /**
     * Set of criterion for decision, defined by a name. Each one may have
     * several properties.
     */
    private List<Criterion> decisionCriteria;
    
    /**
     * Cycle length value and unit of the probNet
     */
    private CycleLength cycleLength;

    /**
     * Nodes are stored in several HashMaps to accelerate the access. The type
     * of node determines the <code>HashMap</code> in which the node is stored.
     */
    protected NodeTypeDepot            nodeDepot;
    private PNESupport                 pNESupport;
    /** The file where the network has been saved */
    private String                     name;
    /** ProbNet comment */
    private String                     comment              = "";
    /** Indicates whether the comment should be shown when opening the net */
    private boolean showCommentWhenOpening					= false;
    /** Default States of the probNet */
    private State[]                    defaultStates        = { new State("absent"),
            new State("present")                           };
    
    private InferenceOptions inferenceOptions;

    // Constructors
    public ProbNet(NetworkType networkType) {
        this.pNESupport = new PNESupport(false);
        this.decisionCriteria = new ArrayList<Criterion>();
        this.constraints = new ArrayList<PNConstraint>();
        this.nodeDepot = new NodeTypeDepot();
        this.inferenceOptions = new InferenceOptions();
        if(!hasConstraint(OnlyAtemporalVariables.class)){
        	this.cycleLength = new CycleLength();
        }
        
        try {
            this.setNetworkType(networkType);
        } catch (ConstraintViolationException e) {
            // Impossible to reach here as the net is empty
        }
    }

    /**
     * Creates a probabilistic network. NetworkTypeConstraint defines the
     * network type. If NetworkTypeConstraint is null the network type will be
     * Bayesian Network
     */
    public ProbNet() {
        this(BayesianNetworkType.getUniqueInstance());
    }

    // Methods
    /**
     * Applies edit to the probNet
     * 
     * @param edit
     * @throws ConstraintViolationException
     * @throws CanNotDoEditException
     * @throws NonProjectablePotentialException
     * @throws WrongCriterionException
     * @throws DoEditException
     */
    public void doEdit(PNEdit edit)
            throws ConstraintViolationException, CanNotDoEditException,
            NonProjectablePotentialException, WrongCriterionException, DoEditException {
        pNESupport.announceEdit(edit);
        pNESupport.doEdit(edit);
    }

    /**
     * @param constraint
     *            <code>PNConstraint</code>
     * @param check
     *            . when <code>false</code>, constraint is added to the
     *            constraints list without testing. Otherwise,
     *            <code>constraint</code> is added only when it is full-filled.
     *            <code>boolean</code>
     * @throws ConstraintViolationException
     */
    public void addConstraint(PNConstraint constraint, boolean check)
            throws ConstraintViolationException {
        if (!this.networkType.isApplicableConstraint(constraint)) {
            throw new ConstraintViolationException("Can not apply "
                    + constraint.toString()
                    + " to a probNet of type "
                    + this.networkType.getClass());
        } else if (!constraints.contains(constraint)) {
            if (check && !constraint.checkProbNet(this)) {
                throw new ConstraintViolationException("Can not apply "
                        + constraint.toString()
                        + " to this probNet.");
            }
            constraints.add(constraint);
            pNESupport.addUndoableEditListener(constraint);
        }
    }

    public void addConstraint(PNConstraint constraint)
            throws ConstraintViolationException {
        addConstraint(constraint, true);
    }

    /**
     * @param constraints
     *            <code>ArrayList<PNConstraint></code>
     * @param check
     *            . when <code>false</code>, constraint is added to the
     *            constraints list without testing. Otherwise,
     *            <code>constraint</code> is added only when it is full-filled.
     *            <code>boolean</code>
     * @throws ConstraintViolationException
     */
    public void addConstraints(List<PNConstraint> constraints, boolean check)
            throws ConstraintViolationException {
        for (PNConstraint constraint : constraints) {
            addConstraint(constraint, check);
        }
    }

    /**
     * @param constraint
     *            <code>PNConstraint</code>
     */
    public void removeConstraint(PNConstraint constraint) {
        if (constraints.contains(constraint)) {
            constraints.remove(constraint);
            pNESupport.removeUndoableEditListener(constraint);
        }
    }

    /**
     * @param constraints
     *            <code>ArrayList<PNConstraint></code>
     */
    public void removeConstraints(List<PNConstraint> constraints) {
        for (PNConstraint constraint : constraints) {
            removeConstraint(constraint);
        }
    }

    /**
     * @param constraintClass
     *            <code>Class</code>
     */
    public void removeAllConstraints(Class<PNConstraint> constraintClass) {
        List<PNConstraint> constraintsToRemove = new ArrayList<PNConstraint>();
        for (PNConstraint constraint : constraints) {
            if (constraint.getClass().equals(constraintClass)) {
                constraintsToRemove.add(constraint);
            }
        }
        constraints.removeAll(constraintsToRemove);
    }

    /** @return <code>ArrayList</code> of <code>PNConstraint</code>s */
    public List<PNConstraint> getConstraints() {
        return new ArrayList<PNConstraint>(constraints);
    }

    /** @return <code>ArrayList</code> of <code>PNConstraint</code>s */
    public List<PNConstraint> getAdditionalConstraints() {
        List<PNConstraint> additionalConstraints = new ArrayList<PNConstraint>(constraints);
        List<PNConstraint> networkTypeConstraints = ConstraintManager.getUniqueInstance().buildConstraintList(networkType);
        additionalConstraints.removeAll(networkTypeConstraints);
        return additionalConstraints;
    }

    /**
     * Sets Network type
     * 
     * @param networkType
     *            <code>NetworkType</code>
     * @throws ConstraintViolationException
     */
    public void setNetworkType(NetworkType networkType)
            throws ConstraintViolationException {
        NetworkType oldNetworkType = this.networkType;
        this.networkType = networkType;
        List<PNConstraint> constraints = new ArrayList<PNConstraint>();
        try {
            constraints = ConstraintManager.getUniqueInstance().buildConstraintList(networkType);
            // Add new constraints implied by the network type
            addConstraints(constraints, true);
            // Remove those constraints that are no longer applicable to the new
            // network type
            List<PNConstraint> constraintsToRemove = new ArrayList<PNConstraint>();
            for (PNConstraint constraint : this.constraints) {
                if (!networkType.isApplicableConstraint(constraint)) {
                    constraintsToRemove.add(constraint);
                }
            }
            removeConstraints(constraintsToRemove);
        } catch (ConstraintViolationException e) {
            // Revert
            this.networkType = oldNetworkType;
            throw e;
        }
    }

    /**
     * Gets Network type constraint. There is only one and it is stored in first
     * position.
     * 
     * @return constraint. <code>NetworkType
     */
    public NetworkType getNetworkType() {
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
     * Checks whether this <code>probNet</code> is temporal or not.
     * 
     * @return <code>true</code> when this network has not associated
     *         OnlyAtemporalVariables constraint, otherwise <code>false</code>.
     */
    public boolean variablesCouldBeTemporal() {
        for (PNConstraint constraint : constraints) {
            if (constraint instanceof OnlyAtemporalVariables) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether this <code>probNet</code> is multiagent or not.
     * 
     * @return <code>true</code> when this network has not associated
     *         OnlyOneAgent constraint, otherwise <code>false</code>.
     */
    public boolean isMultiagent() {
        for (PNConstraint constraint : constraints) {
            if (constraint instanceof OnlyOneAgent) {
                return false;
            }
        }
        return true;
    }

    public boolean thereAreTemporalNodes() {
        boolean thereAreTemporalNodes = false;
        for (int i = 0; i < getNodes().size(); i++) {
            if (getNodes().get(i).getVariable().isTemporal()) {
                thereAreTemporalNodes = true;
                break;
            }
        }
        return thereAreTemporalNodes;
    }

    /**
     * Checks whether this <code>probNet</code> is temporal or not.
     * 
     * @return <code>true</code> when this network has not associated
     *         OnlyAtemporalVariables constraint, otherwise <code>false</code>.
     */
    public boolean onlyTemporal() {
        for (PNConstraint constraint : constraints) {
            if (constraint instanceof OnlyTemporalVariables) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this <code>probNet</code> has only chance node or not.
     * 
     * @return <code>true</code> when this network has not associated
     *         OnlyChanceNodes constraint, otherwise <code>false</code>.
     */
    public boolean onlyChanceNodes() {
        for (PNConstraint constraint : constraints) {
            if (constraint instanceof OnlyChanceNodes) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a low deep copy of <code>this ProbNet</code>: copy the
     * <code>graph</code> and the <code>nodes</code> but do not copy nor
     * variables nor potentials.
     * 
     * @return <code>this probNet</code> copied.
     * @throws ConstraintViolationException
     */
    public ProbNet copy() {
        ProbNet copyNet = new ProbNet(this.networkType);
        return auxCopy(copyNet);
    }
    
    
    /**
     * Auxiliary metod for copy, which creates a low deep copy of <code>this ProbNet</code>: copy the
     * <code>graph</code> and the <code>nodes</code> but do not copy nor
     * variables nor potentials.
     * 
     * @return <code>this probNet</code> copied.
     * @throws ConstraintViolationException
     */
    protected ProbNet auxCopy(ProbNet copyNet) {
        //ProbNet copyNet = new ProbNet(this.networkType);
        copyNet.setName(name);
        // copy constraints
        int numConstraints = constraints.size();
        for (int i = 1; i < numConstraints; i++) {
            try {
                copyNet.addConstraint(constraints.get(i), false);
            } catch (ConstraintViolationException e) {
                // Unreachable code because constraints are not tested in copy
            }
        }
        List<Node> nodes = getNodes();
        // Adds variables and create corresponding nodes. Also add potentials
        for (Node node : nodes) {
            // Add variables and create corresponding nodes
            Variable variable = node.getVariable();
            Node newNode = copyNet.addNode(variable, node.getNodeType());
            newNode.setCoordinateX(node.getCoordinateX());
            newNode.setCoordinateY(node.getCoordinateY());
            newNode.setPotentials(node.getPotentials());
            // TODO Hacer clon para node y quitar estas lineas
            newNode.setPurpose(node.getPurpose());
            newNode.setRelevance(node.getRelevance());
            newNode.setComment(node.getComment());
            newNode.additionalProperties = additionalProperties;
            newNode.setAlwaysObserved(node.isAlwaysObserved());
        }
        // Adds links
        // Copy explicit links' properties
        if (hasExplicitLinks()) {
        	copyNet.makeLinksExplicit(false);
            for (Link<Node> originalLink : getLinks()) {
                Node copyNode1 = copyNet.getNode(originalLink.getNode1().getVariable());
                Node copyNode2 = copyNet.getNode(originalLink.getNode2().getVariable());
                Link<Node> copyLink = copyNet.addLink(copyNode1, copyNode2, originalLink.isDirected());
                copyLink.setRestrictionsPotential(originalLink.getRestrictionsPotential());
                copyLink.setRevealingIntervals(originalLink.getRevealingIntervals());
                copyLink.setRevealingStates(originalLink.getRevealingStates());
            }
        }else
        {
            for (Node node : nodes) {
                Node copyNode = copyNet.getNode(node.getVariable());
                List<Node> siblings = getSiblings(node);
                for (Node sibling : siblings) {
                    Node copySibling = copyNet.getNode(sibling.getVariable());
                    if (!copyNet.isSibling(copyNode, copySibling)) {
                    	copyNet.addLink(copyNode, copySibling, false);
                    }
                }
                List<Node> children = getChildren(node);
                for (Node child : children) {
                    Node copyChild = copyNet.getNode(child.getVariable());
                    copyNet.addLink(copyNode, copyChild, true);
                }
            }        	
        }
        // copy listeners
        copyNet.getPNESupport().setListeners(pNESupport.getListeners());
        // Copy additionalProperties
        Set<String> keys = additionalProperties.keySet();
        HashMap<String, String> copyProperties = new HashMap<String, String>();
        for (String key : keys) {
            copyProperties.put(key, additionalProperties.get(key));
        }
        copyNet.additionalProperties = copyProperties;
        // Copy decisionCriterion variable
        // copy decision criteria
        if (this.getDecisionCriteria() != null) {
            copyNet.setDecisionCriteria(new ArrayList<Criterion>(this.getDecisionCriteria()));
        }
        
        // Copy temporal units
        if(this.getCycleLength() != null){
        	copyNet.setCycleLength(this.getCycleLength());
        }
        
        //Copy Inference Options
        copyNet.getInferenceOptions().setMultiCriteriaOptions(this.getInferenceOptions().getMultiCriteriaOptions());
        copyNet.getInferenceOptions().setTemporalOptions(this.getInferenceOptions().getTemporalOptions());
        return copyNet;
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
        Node node1 = getNode(variable1);
        Node node2 = getNode(variable2);
        if (node1 == null) {
            throw new NodeNotFoundException(this, variable1);
        }
        if (node2 == null) {
            throw new NodeNotFoundException(this, variable2);
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
    public void invertLink(Variable variable1, Variable variable2, boolean directed)
            throws Exception {
        removeLink(variable1, variable2, true);
        addLink(variable2, variable1, true);
    }

    public String getName() {
        return name;
    }

    /** @return Number of nodes in <code>probNet</code>. <code>int</code> */
    public int getNumNodes() {
        return nodeDepot.getNumNodes();
    }

    /**
     * @param nodeType
     *            - <code>NodeType</code>
     * @return Number of nodes with <code>NodeType = nodeType</code>.
     *         <code>int</code>
     */
    public int getNumNodes(NodeType nodeType) {
        return nodeDepot.getNumNodes(nodeType);
    }

    /**
     * @param evidenceCase
     * @return The potentials of the network projected on the evidence
     * @throws NonProjectablePotentialException
     * @throws WrongCriterionException
     * @throws NoFindingException
     */
    public List<TablePotential> tableProjectPotentials(EvidenceCase evidenceCase)
            throws NonProjectablePotentialException, WrongCriterionException {
        List<Potential> originalPotentials = getSortedPotentials();
        List<TablePotential> projectedPotentials = new ArrayList<TablePotential>();
        // each original potential may yield several projected potentials;
        List<TablePotential> potentials;
        for (Potential potential : originalPotentials) {
            InferenceOptions inferenceOptions = new InferenceOptions(this, null);
            potentials = potential.tableProject(evidenceCase, inferenceOptions, projectedPotentials);
            projectedPotentials.addAll(potentials);
        }
        return projectedPotentials;
    }

    /**
     * @return All the potentials of this network. <code>List</code> of
     *         <code>Potential</code>s.
     * @consultation
     */
    public List<Potential> getPotentials() {
        List<Node> nodes = getNodes();
        List<Potential> potentials = new ArrayList<Potential>();
        for (Node node : nodes) {
            potentials.addAll(node.getPotentials());
        }
        return potentials;
    }
    
    /**
     * @return All the potentials of this network sorted topologically.
     *         <code>List</code> of <code>Potential</code>s.
     * @consultation
     */
    public List<Potential> getSortedPotentials() {
        List<Node> nodes = ProbNetOperations.sortTopologically(this);
        List<Potential> potentials = new ArrayList<Potential>();
        for (Node node : nodes) {
            potentials.addAll(node.getPotentials());
        }
        return potentials;
    }    

    /**
     * @return All the nodes corresponding to variables in same order.
     *         <code>ArrayList</code> of <code>Node</code>
     * @param variables
     *            <code>ArrayList</code> of <code>Variable</code>
     * @consultation
     */
    public List<Node> getNodes(List<Variable> variables) {
        List<Node> nodes = new ArrayList<Node>(variables.size());
        for (Variable variable : variables) {
            nodes.add(getNode(variable));
        }
        return nodes;
    }

    /**
     * @return All the nodes of certain kind
     * @param nodeType
     * @consultation
     */
    public List<Node> getNodes(NodeType nodeType) {
        return nodeDepot.getNodes(nodeType);
    }
    

    /**
     * Gets all utility nodes with the cECriterion
     * @param cECriterion 
     * @return All utility nodes with the cost effectiveness criterion
     */
    public List<Node> getNodes(CECriterion cECriterion){
    	List<Node> utilityNodes = getNodes(NodeType.UTILITY);
    	List<Node> nodes = new ArrayList<Node>();
    	
    	for(Node utilityNode : utilityNodes){
    		if(utilityNode.getVariable().getDecisionCriterion().getCECriterion() == cECriterion){
    			nodes.add(utilityNode);
    		}
    	}
    	return nodes;
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
    public List<Potential> getPotentials(Variable variable) {
        List<Potential> potentials = new ArrayList<Potential>();
        Node node = getNode(variable);
        // potentials associated to this node
        if (node != null) { // Variable exists in this ProbNet
            potentials.addAll(node.getPotentials());
            // potentials in neighbors that contains variable
            List<Node> neighbors = getNeighbors(node);
            for (Node neighbor : neighbors) {
                List<Potential> nodePotentials = neighbor.getPotentials();
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
    public List<Potential> getPotentialsByType(NodeType nodeType) {
        return nodeDepot.getPotentialsByType(nodeType);
    }

    /**
     * @param role
     * @return All the potentials of a role.
     */
    public List<Potential> getPotentialsByRole(PotentialRole role) {
        return nodeDepot.getPotentialsByRole(role);
    }

    /**
     * Gets all the probability potentials that contain the
     * <code>Variable</code> received. The potentials that can contain that
     * variable are in the node associated to the variable and its neighbors.
     * 
     * @param variable
     * @return <code>ArrayList</code> of potentials containing
     *         <code>variable</code>.
     * @argCondition variable belongs to this <code>ProbNet</code>
     */
    public List<Potential> getProbPotentials(Variable variable) {
        Node nodeVariable = getNode(variable);
        List<Node> allNodes = getNeighbors(nodeVariable);
        allNodes.add(nodeVariable);
        List<Potential> potentialsVariable = new ArrayList<Potential>();
        for (Node node : allNodes) {
            List<Potential> potentialsNode = node.getPotentials();
            for (Potential potential : potentialsNode) {
                if ((potential.getVariables().contains(variable)) && !potential.isUtility()) {
                    potentialsVariable.add(potential);
                }
            }
        }
        return potentialsVariable;
    }

    /**
     * Gets all the utility potentials that contain the <code>variable</code>
     * received. Constant utility potentials are also returned by this method.
     * <p>
     * The potentials that can contain that variable are in the node associated
     * to the variable and its neighbors.
     * 
     * @param variable
     *            <code>Variable</code>.
     * @return <code>ArrayList</code> of potentials containing
     *         <code>variable</code>.
     * @argCondition variable belongs to this <code>ProbNet</code>
     */
    public List<Potential> getUtilityPotentials(Variable variable) {
        Node nodeVariable = getNode(variable);
        List<Node> allNodes = getNeighbors(nodeVariable);
        allNodes.add(nodeVariable);
        List<Potential> potentialsVariable = new ArrayList<Potential>();
        for (Node node : allNodes) {
            List<Potential> potentialsNode = node.getPotentials();
            for (Potential potential : potentialsNode) {
                List<Variable> variables = potential.getVariables();
                if ((variables.size() == 0 || variables.contains(variable))
                        && potential.isUtility()) {
                    potentialsVariable.add(potential);
                }
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
    public List<Potential> extractPotentials(Variable variable) {
        // get the nodes that contains potentials associated to the variable
        List<Node> nodes = new ArrayList<Node>();
        // node associated to variable
        Node nodeContainer = getNode(variable);
        nodes.add(nodeContainer);
        // and its siblings
        nodes.addAll(getNeighbors(nodeContainer));
        List<Potential> potentialsVariable = new ArrayList<Potential>();
        // for each node extract its potentials ...
        for (Node node : nodes) {
            List<Potential> potentialsNode = node.getPotentials();
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
    public Node removePotential(Potential potential) {
        List<Variable> variables = potential.getVariables();
        List<Node> candidateNodes = new ArrayList<Node>();
        // gets nodes that could contain the potential
        if (!potential.isUtility()) { // chance potential
                                      // find nodes corresponding to variables
            for (Variable variable : variables) {
                Node node = getNode(variable);
                if (node != null) {
                    candidateNodes.add(getNode(variable));
                }
            }
        } else { // utility potential.
            if (variables.size() == 0) {// Constant potentials can be in any
                                        // node
                candidateNodes = this.getNodes();
            } else {
                List<Node> utilityNodes = getNodes(NodeType.UTILITY);
                candidateNodes.addAll(utilityNodes);
                Node firstNode = getNode(variables.get(0));
                candidateNodes.add(firstNode);
                Variable utilityVariable = potential.getUtilityVariable();
                if (utilityVariable != null) {
                	Node utilityNode = getNode(utilityVariable);
                	if ((utilityNode != null) && (!candidateNodes.contains(utilityNode))) {
                		candidateNodes.add(utilityNode);
                	}
                }
            }
        }
        // find in such nodes the potential to remove
        for (Node node : candidateNodes) {
            if (node != null) {
                List<Potential> potentialsNode = node.getPotentials();
                for (Potential potentialNode : potentialsNode) {
                    if (potentialNode == potential) {
                        if (node.removePotential(potentialNode)) {
                            return node;
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
     * @param node
     *            <code>Node</code>
     */
    public void removePotentials(Node node) {
        // get the nodes that contains potentials associated to the variable
        List<Node> nodes = new ArrayList<Node>();
        Variable variable = node.getVariable();
        nodes.add(node);
        // and its siblings
        nodes.addAll(getSiblings(node));
        // for each node extract its potentials ...
        for (Node otherNode : nodes) {
            List<Potential> potentialsNode = new ArrayList<Potential>(otherNode.getPotentials());
            for (Potential potential : potentialsNode) {
                // ... and removes the potentials that contains the variable
                if (potential.getVariables().contains(variable)) {
                	otherNode.removePotential(potential);
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
    public void removePotentials(List<Potential> toRemovePotentials) {
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
     * @return The <code>node</code> that points to <code>variable</code> in
     *         <code>this</code> network.
     * @argCondition the variable must not be in the ProbNet.
     */
    public Node addNode(Variable variable, NodeType nodeType) {
        Node node = nodeDepot.getNode(nodeType, variable);
        if (node == null) {
            node = new Node(this, variable, nodeType);
            addNode(node);
        }
        return node;
    }

    /**
     * @param node
     *            . <code>Node</code>
     * @argCondition the variable must not be in the ProbNet. This method is
     *               used to redo the <code>AddVariableEdit</code>, i.e., to
     *               reinsert a Node that has been removed.
     */
    @Override
    public void addNode(Node node) {
        super.addNode(node);
        nodeDepot.addNode(node);
    }

    /**
     * @param nameOfVariable
     *            <code>String</code>
     * @return The <code>Node</code> that matches the
     *         <code>nameOfVariable</code>
     * @consultation
     */
    public Node getNode(String nameOfVariable)
            throws NodeNotFoundException {
        Node node = nodeDepot.getNode(nameOfVariable);
        if (node == null) {
            throw new NodeNotFoundException(this, nameOfVariable);
        }
        return node;
    }


    /**
     * @param nameOfVariable
     *            <code>String</code>
     * @param nodeType
     *            <code>NodeType</code>
     * @return The node with <code>nameOfVariable</code> and
     *         <code>kindOfNode</code> if exists otherwhise null
     * @throws NodeNotFoundException
     * @consultation
     */
    public Node getNode(String nameOfVariable, NodeType nodeType)
            throws NodeNotFoundException {
        Node node = nodeDepot.getNode(nameOfVariable, nodeType);
        if (node == null) {
            throw new NodeNotFoundException(this, nameOfVariable);
        }
        return node;
    }

    /**
     * @param variable
     *            <code>Variable</code>
     * @return The <code>Node</code> that matches the <code>Variable</code>
     * @consultation
     */
    public Node getNode(Variable variable) {
        return nodeDepot.getNode(variable);
    }

    /**
     * @param nameOfVariable
     *            . <code>String</code>
     * @return variable that matches <code>variableName</code> if exists,
     *         otherwise <code>null</code>. <code>Variable</code>
     * @consultation
     */
    public Variable getVariable(String variableName)
            throws NodeNotFoundException {
        Node node = getNode(variableName);
        return node.getVariable();
    }

    /**
     * Returns variable on a certain timeSlice
     * 
     * @param variable
     * @param timeSlice
     * @return
     * @throws NodeNotFoundException
     */
    public Variable getVariable(String baseName, int timeSlice)
            throws NodeNotFoundException {
        return getVariable(baseName + " [" + timeSlice + "]");
    }

    /**
     * @param variable
     *            . a <code>Variable</code>
     * @argCondition variable must be in the network and must be temporal
     * @param int timeSlice
     * @return a new variable having the same base name as the first argument
     *         but in the time slice indicated by the second argument
     * @throws NodeNotFoundException
     * @consultation
     */
    public Variable getShiftedVariable(Variable variable, int timeDifference)
            throws NodeNotFoundException {
        return getVariable(variable.getBaseName(), variable.getTimeSlice() + timeDifference);
    }

    // TODO Con este nuevo metodo podemos evitar la chapuza hecha en
    // varios lugares de invocar getVariable para ver si lanzaba una excepcion.
    // Revisar el uso de esa excepcion y evitarla en lo posible.
    public boolean containsVariable(String variableName) {
        Node node = null;
        try {
            node = getNode(variableName);
        } catch (NodeNotFoundException e) {
        }
        return (node != null);
    }

    public boolean containsVariable(Variable variable) {
        return getNode(variable) != null;
    }

    /**
     * Returns true if this probNet contains the shifted variable
     * 
     * @param variableName
     * @param timeDifference
     * @return
     */
    public boolean containsShiftedVariable(Variable variable, int timeDifference) {
        int timeSlice = variable.getTimeSlice() + timeDifference;
        String baseName = variable.getBaseName();
        return containsVariable(baseName + " [" + timeSlice + "]");
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
     * @return The <code>Node</code> in which the <code>potential</code>
     *         received has been added.
     */
    public Node addPotential(Potential potential) {
        List<Variable> potentialVariables = potential.getVariables();
        addPotentialVariables(potential);
        Node node = null; // The potential will be added here
        if (potential.isUtility()) { // Create node without variable
            node = addUtilityPotential(potential, potentialVariables);
        } else {
            if (potentialVariables.size() > 0) {
                node = addProbabilityPotential(potential, potentialVariables);
            } else {// potential does not depend on any variable (is a constant)
                List<Node> chanceNodes = getNodes(NodeType.CHANCE);
                if (chanceNodes.size() > 0) {
                    node = chanceNodes.get(0);
                    node.addPotential(potential);
                    // If there are no chance nodes there is no reason to
                    // add the constant potential
                }
            }
        }
        return node;
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
        List<Variable> potentialVariables = potential.getVariables();
        for (Variable variable : potentialVariables) {
            // add the variables that were not yet in the network
            if (getNode(variable) == null) {
                addNode(variable, NodeType.CHANCE);
            }
        }
        // Only for utility potentials
        if (potential.isUtility()) {
            Variable utilityVariable = potential.getUtilityVariable();
            if (getNode(utilityVariable) == null) {
                addNode(utilityVariable, NodeType.UTILITY);
            }
        }
    }

    /**
     * @param potential
     *            . <code>Potential</code>
     * @param potentialVariables
     *            . <code>ArrayList</code> of <code>Variable</code>s
     * @return The <code>Node</code> in which the <code>potential</code> is
     *         stored
     */
    private Node addUtilityPotential(Potential potential, List<Variable> potentialVariables) {
        Variable utilityVariable = potential.getUtilityVariable();
        Node utilityNode = this.getNode(utilityVariable);
        if (utilityNode == null) { // The variable does not exists yet
            utilityNode = new Node(this, utilityVariable, NodeType.UTILITY);
        }
        utilityNode.addPotential(potential);
        if (!hasConstraint(OnlyUndirectedLinks.class)) {
            for (Variable variable : potentialVariables) {
                Node parent = getNode(variable);
                if (!isParent(parent, utilityNode)) {
                    addLink(parent, utilityNode, true);
                }
            }
        }
        return utilityNode;
    }

    /**
     * @param potential
     *            - <code>Potential</code>
     * @param potentialVariables
     *            - <code>ArrayList</code> of <code>Variable</code>s
     */
    private Node addProbabilityPotential(Potential potential, List<Variable> potentialVariables) {
        Variable conditionedVariable = potentialVariables.get(0);
        Node conditionedNode = getNode(conditionedVariable);
        conditionedNode.addPotential(potential);
        if (hasConstraint(OnlyUndirectedLinks.class)) {
            createClique(potential);
        } else {
            if (hasConstraint(OnlyDirectedLinks.class)) {
                for (int i = 1; i < potentialVariables.size(); i++) {
                    Node conditioningNode = getNode(potentialVariables.get(i));
                    if (!isParent(conditioningNode, conditionedNode)) {
                        addLink(conditioningNode, conditionedNode, true);
                    }
                }
            }
        }
        return conditionedNode;
    }

    /**
     * @param constraint
     *            <code>Class</code>
     * @return <code>true</code> if this probabilistic network contains the
     *         received constraint type.
     */
    public boolean hasConstraint(Class<?> constraint) {
        for (PNConstraint constraintProbNet : constraints) {
            if (constraintProbNet.getClass() == constraint) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return All <code>Variable</code>s except utility nodes variables.
     *         <code>ArrayList</code> of <code>Variable</code>.
     */
    public List<Variable> getChanceAndDecisionVariables() {
        List<Variable> variables = new ArrayList<Variable>();
        List<Node> nodes = getNodes();
        for (Node node : nodes) {
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
    public List<Variable> getVariables(NodeType nodeType) {
        List<Variable> variablesType = new ArrayList<Variable>();
        List<Node> nodes = getNodes(nodeType);
        for (Node node : nodes) {
            variablesType.add(node.getVariable());
        }
        return variablesType;
    }

    /**
     * @param nodes
     *            list of <code>Node</code>s
     * @return variables corresponding to the received nodes.
     *         <code>List</code> of <code>Variable</code>
     */
    public static List<Variable> getVariables(List<Node> nodes) {
        List<Variable> variables = null;
        if (nodes != null) {
        	variables = new ArrayList<Variable>(nodes.size());
            for (Node node : nodes) {
                variables.add(node.getVariable());
            }
        }
        return variables != null ? variables : new ArrayList<Variable>();
    }    

    /**
     * @return All the variables. <code>ArrayList</code> of
     *         <code>Variable</code>
     */
    public List<Variable> getVariables() {
        List<Variable> variables = new ArrayList<Variable>();
        for (Node node : getNodes()) {
            variables.add(node.getVariable());
        }
        return variables;
    }

    /**
     * Removes <code>node</code> from <code>this ProbNet</code> and removes
     * also the associated <code>node</code> from the associated
     * <code>Graph</code>.
     * 
     * @param node
     *            <code>Node</code>
     */
    public void removeNode(Node node) {
        super.removeNode(node);
        nodeDepot.removeNode(node);
    }

    /**
     * @param variable1
     *            <code>Variable</code>
     * @param variable2
     *            <code>Variable</code>
     * @param directed
     *            <code>boolean</code>
     */
    public void removeLink(Variable variable1, Variable variable2, boolean directed) {
        Node node1 = getNode(variable1);
        Node node2 = getNode(variable2);
        removeLink(node1, node2, directed);
    }

    /** @return Number of potentials. <code>int</code> */
    public int getNumPotentials() {
        return nodeDepot.getNumPotentials();
    }

    /**
     * Creates a clique by adding undirected links between the nodes that
     * represent the variables of the <code>potential</code>.
     * 
     * @param potential
     *            <code>Potential</code>.
     */
    private void createClique(Potential potential) {
        List<Variable> variablesPotential = potential.getVariables();
        int potentialSize = variablesPotential.size();
        for (int i = 0; i < potentialSize - 1; i++) {
            Node node1 = getNode(variablesPotential.get(i));
            if (node1.getNodeType() != NodeType.CHANCE) {
                continue;
            }
            for (int j = i + 1; j < potentialSize; j++) {
                Node node2 = getNode(variablesPotential.get(j));
                if (node2.getNodeType() != NodeType.CHANCE) {
                    continue;
                }
                if (!isSibling(node1, node2)) {
                    addLink(node1, node2, false);
                }
            }
        }
    }

    public PNESupport getPNESupport() {
        return pNESupport;
    }

    /** @return String */
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Type: " + networkType.toString() + "\n");
        List<Node> nodes = getNodes();
        int numPotentials = getNumPotentials();
        int numNodes = nodes.size();
        if (numNodes == 0) {
            out.append("No nodes.\n");
        } else {
            out.append("Nodes (" + numNodes + "): ");
            for (Node node : nodes) {
                out.append("\n  " + node.toString());
            }
            out.append("\n");
        }
        if (numPotentials == 0) {
            out.append("No potentials.\n");
        } else {
            out.append("Number of potentials: " + numPotentials + "\n");
        }
        if (constraints.size() == 0) {
            out.append("No constraints\n");
        } else {
            out.append("Constraints: ");
            for (int i = 0; i < constraints.size(); i++) {
                String strConstraint = constraints.get(i).toString();
                strConstraint = strConstraint.substring(strConstraint.lastIndexOf('.') + 1,
                        strConstraint.length());
                out.append(strConstraint);
                if (i < constraints.size() - 1) {
                    out.append(", ");
                }
            }
            out.append("\n");
        }
        if (agents != null) {
            out.append("\n");
            out.append("Agents:\n" + agents.toString());
        }
        return out.toString();
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param comment
     *            the comment to set
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
     * @param defaultStates
     *            the defaultStates to set
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

    /** @argCondition oldNode belongs to this probNet */
    public Node addShiftedNode(Node oldNode,
            int timeDifference,
            double coordinateXOffset,
            double coordinateYOffset) {
        Variable oldVariable = oldNode.getVariable();
        Variable newVariable = (Variable) oldVariable.clone();
        newVariable.setTimeSlice(oldVariable.getTimeSlice() + timeDifference);
        Node newNode = addNode(newVariable, oldNode.getNodeType());
        newNode.setCoordinateX(oldNode.getCoordinateX() + coordinateXOffset);
        newNode.setCoordinateY(oldNode.getCoordinateY() + coordinateYOffset);
        // TODO Hacer clon para node y quitar estas lineas
        newNode.setPurpose(oldNode.getPurpose());
        newNode.setRelevance(oldNode.getRelevance());
        newNode.setComment(oldNode.getComment());
        newNode.additionalProperties = additionalProperties;
        return newNode;
    }

    /** @return <code>ArrayList</code> of <code>StringsWithProperties</code> */
    public List<StringWithProperties> getAgents() {
        return agents;
    }

    /** @return <code>StringsWithProperties</code> */
    public List<Criterion> getDecisionCriteria() {
        return decisionCriteria;
    }

    /**
     * @param decisionCriteria
     *            . <code>StringsWithProperties</code>
     */
    public void setDecisionCriteria(List<Criterion> decisionCriteria) {
        this.decisionCriteria = decisionCriteria;
    }

    /**
     * @param agents
     *            . <code>StringsWithProperties</code>
     */
    public void setAgents(List<StringWithProperties> agents) {
        this.agents = agents;
    }

    public boolean getShowCommentWhenOpening() {
		return showCommentWhenOpening;
	}
    
	public void setShowCommentWhenOpening(boolean showCommentWhenOpening) {
		this.showCommentWhenOpening = showCommentWhenOpening;
	}
	
	/**
	 * Node calls this method when its variable instance has beens changed, so
	 * that probNet updates its reference too.
	 * 
	 * @param oldVariable
	 */
	public void updateVariable(Variable oldVariable)
	{
		Node node = nodeDepot.getNode(oldVariable);
		nodeDepot.removeNode(oldVariable);
		nodeDepot.addNode(node);
	}

	public InferenceOptions getInferenceOptions() {
		return inferenceOptions;
	}

    public void setInferenceOptions(InferenceOptions inferenceOptions) {
        this.inferenceOptions = inferenceOptions;
    }

    public CycleLength getCycleLength() {
		return cycleLength;
	}

	public void setCycleLength(CycleLength temporalUnit) {
		this.cycleLength = temporalUnit;
	}
	


    public ProbNet deepCopy() {
        ProbNet copyNet = new ProbNet(this.networkType);
        copyNet.constraints = new ArrayList<>();

        // copy decision criteria
        if (this.getDecisionCriteria() != null) {
            List<Criterion> newDecisionCriteria = new ArrayList<Criterion>();
            for(Criterion criterion : this.getDecisionCriteria()){
                Criterion newCriterion = new Criterion(criterion);
                newDecisionCriteria.add(newCriterion);
            }
            copyNet.setDecisionCriteria(newDecisionCriteria);
        }
        
        // copy net name
        copyNet.setName(new String(name));
        
        // Copy temporal units
        if(this.getCycleLength() != null){
        	copyNet.setCycleLength(new CycleLength(this.getCycleLength()));
        }
        
        //Copy Inference Options
        copyNet.setInferenceOptions(new InferenceOptions(this.getInferenceOptions()));
        
        // copy constraints
        int numConstraints = constraints.size();
        for (int i = 1; i < numConstraints; i++) {
            try {
                copyNet.addConstraint(constraints.get(i), false);
            } catch (ConstraintViolationException e) {
                // Unreachable code because constraints are not tested in copy
            }
        }
        
        List<Node> nodes = getNodes();
        // Adds variables and create corresponding nodes. Also add potentials
        for (Node node : nodes) {
            Node newNode = node.clone(copyNet);            
            copyNet.addNode(newNode);
        }

        // Add new potentials and update list of neighbours
        for(Node node : nodes){
            List<Node> neighbours = this.getNeighbors(node);
            for(Node neighbour : neighbours){
                try {
                    neighbour = copyNet.getNode(neighbour.getName());
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            }

            ArrayList<Potential> newPotentials = new ArrayList<Potential>();
            for(Potential potential : node.getPotentials()){
                newPotentials.add(potential.deepCopy(copyNet));
            }
            
            try {
                copyNet.getNode(node.getName()).setPotentials(newPotentials);
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
        }



        // Adds links
        // Copy explicit links' properties
        // TODO - Check this code
        if (hasExplicitLinks()) {
            copyNet.makeLinksExplicit(false);
            for (Link<Node> originalLink : getLinks()) {
                try {

                    Node copyNode1 = copyNet.getNode(originalLink.getNode1().getVariable().getName());
                    Node copyNode2 = copyNet.getNode(originalLink.getNode2().getVariable().getName());

                    Link<Node> copyLink = copyNet.addLink(copyNode1, copyNode2, originalLink.isDirected());
                    if(originalLink.getRestrictionsPotential() != null){
                    	copyLink.setRestrictionsPotential(originalLink.getRestrictionsPotential().deepCopy(copyNet));
                    }
                    
                    List<PartitionedInterval> newRevealingIntervals = new ArrayList<PartitionedInterval>();
                    for(PartitionedInterval interval : originalLink.getRevealingIntervals()){
                    	PartitionedInterval newInterval = new PartitionedInterval(interval.limits.clone(), interval.belongsToLeftSide.clone());
                    	newRevealingIntervals.add(newInterval);
                    }
                    
                    copyLink.setRevealingIntervals(newRevealingIntervals);
                    copyLink.setRevealingStates(new ArrayList<State>(originalLink.getRevealingStates()));
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }else
        {
            for (Node node : nodes) {
                try {
                    Node copyNode = copyNet.getNode(node.getVariable().getName());
                    List<Node> siblings = getSiblings(node);
                    for (Node sibling : siblings) {
                        Node copySibling = copyNet.getNode(sibling.getVariable().getName());
                        if (!copyNet.isSibling(copyNode, copySibling)) {
                            copyNet.addLink(copyNode, copySibling, false);
                        }
                    }
                    List<Node> children = getChildren(node);
                    for (Node child : children) {
                        Node copyChild = copyNet.getNode(child.getVariable().getName());
                        copyNet.addLink(copyNode, copyChild, true);
                    }
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }
        // copy listeners
        copyNet.getPNESupport().setListeners(pNESupport.getListeners());
        // Copy additionalProperties
        Set<String> keys = additionalProperties.keySet();
        HashMap<String, String> copyProperties = new HashMap<String, String>();
        for (String key : keys) {
            copyProperties.put(key, additionalProperties.get(key));
        }
        copyNet.additionalProperties = copyProperties;

        return copyNet;
    }

	
	
}
