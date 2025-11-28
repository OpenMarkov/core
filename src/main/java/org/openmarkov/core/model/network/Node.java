/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.exception.ThereIsNoPotentialsInNodeException;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.modelUncertainty.Tools;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.operation.AuxiliaryOperations;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.core.model.network.potential.operation.Util;
import org.openmarkov.core.model.network.potential.plugin.PotentialManager;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.java.cloneUtils.CloneUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A probabilistic node has a set of conditional probabilities, one variable,
 * etc. The structural aspect of the underlying  graph is in the node
 * associated.
 *
 * @author marias
 * @author fjdiez
 * @version 1.0
 * @see Node
 * @see ProbNet
 * @since OpenMarkov 1.0
 */
public class Node implements Cloneable, ClassLocalizable {
    
    // Constants
    public final static double DEFAULT_RELEVANCE = 5.0;
    /**
     * This object contains all the information that the parser reads from
     * disk that does not have a direct connection with the attributes stored
     * in the {@code Node} object.
     */
    public LinkedHashMap<String, String> additionalProperties;    // changed by agoni
    // public Map<String, String> additionalProperties;
    
    // Attributes/
    /**
     * Node type
     */
    protected NodeType nodeType;
    
    /**
     * Network
     */
    protected ProbNet probNet;
    
    /**
     * Each {@code Node} has a list of potentials
     */
    @NotNull
    protected List<Potential> potentials;
    
    /**
     * The variable associated
     */
    protected Variable variable;
    private final int hashCode;
    /**
     * Purpose of node
     */
    private String purpose = "";
    /**
     * Relevance of node
     */
    private double relevance = DEFAULT_RELEVANCE;
    /**
     * Comment about node definition
     */
    private String comment = "";
    
    //TODO OOPN start
    private PolicyType policyType = PolicyType.OPTIMAL;
    //TODO OOPN end
    /**
     * Indicates whether this node is an input parameter
     */
    private boolean isInput = false;
    private double coordinateX = 100;
    private double coordinateY = 100;
    private boolean alwaysObserved = false;
    
    // Constructor
    
    /**
     * @param probNet  {@code ProbNet}
     * @param variable {@code Variable}
     * @param nodeType {@code NodeType}
     */
    public Node(ProbNet probNet, Variable variable, NodeType nodeType) {
        this.probNet = probNet;
        this.variable = variable;
        if (nodeType == NodeType.UTILITY) {
            this.variable.setVariableType(VariableType.NUMERIC);
        }
        this.nodeType = nodeType;
        potentials = new ArrayList<>();
        additionalProperties = new LinkedHashMap<>();
        hashCode = 31 * variable.hashCode() + 17 * nodeType.hashCode();
    }
    
    /**
     * Copy Constructor for the GUI
     *
     * @param node Node
     */
    public Node(Node node) {
        this.probNet = node.getProbNet();
        this.variable = node.getVariable();
        this.nodeType = node.getNodeType();
        potentials = new ArrayList<>(node.getPotentials());
        additionalProperties = new LinkedHashMap<>(node.additionalProperties);
        alwaysObserved = node.isAlwaysObserved();
        hashCode = 31 * variable.hashCode() + 17 * nodeType.hashCode();
    }
    
    //Methods
    
    /**
     * @return The {@code Variable} associated to this
     * {@code node}.
     */
    public Variable getVariable() {
        return variable;
    }
    
    /**
     * Sets a new variable and updates the reference in ProbNet
     *
     * @param newVariable New variable
     */
    public void setVariable(Variable newVariable) {
        Variable oldVariable = this.variable;
        this.variable = newVariable;
        this.probNet.updateVariable(oldVariable);
    }
    
    /**
     * Sets the additional properties
     *
     * @param additionalProperties New additional properties
     */
    public void setOtherProperties(LinkedHashMap<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
    
    /**
     * @return Variable name. {@code String}
     */
    public String getName() {
        return getVariable().getName();
    }

    public String getBaseName() {
        return getVariable().getBaseName();
    }
    
    /**
     * @param potential {@code Potential}
     */
    public void setPotential(Potential potential) {
        this.potentials.clear();
        addPotential(potential);
    }
    
    /**
     * @param potential {@code Potential}
     */
    public void addPotential(Potential potential) {
        this.potentials.add(potential);
    }
    
    /**
     * @param potentials {@code Potential}
     */
    public void setPotentials(List<Potential> potentials) {
        this.potentials.clear();
        if (potentials != null) {
            this.potentials.addAll(potentials);
        }
    }
    
    /**
     * @param potential {@code Potential}
     *
     * @return {@code true} if {@code potentialList} contained the
     * specified element; otherwise {@code false}.
     */
    public boolean removePotential(Potential potential) {
        return potentials.remove(potential);
    }
    
    /**
     * @return {@code NodeType}
     */
    public NodeType getNodeType() {
        return nodeType;
    }
    
    public void setNodeType(NodeType nodeType) {
        // Remove node from NodeTypeDepot HashMap
        this.probNet.nodeDepot.removeNode(this);
        // Change of nodeType
        this.nodeType = nodeType;
        // Add node to NodeTypeDepot HashMap
        this.probNet.nodeDepot.addNode(this);
    }
    
    /**
     * @return An {@code ArrayList} cloned with all the potentials
     * associated to this {@code Node}
     */
    public List<Potential> getPotentials() {
        return new ArrayList<>(potentials);
    }
    
    public Potential getFirstPotential() throws ThereIsNoPotentialsInNodeException {
        if (this.potentials.isEmpty()) {
            throw new ThereIsNoPotentialsInNodeException(this);
        }
        return potentials.getFirst();
    }
    
    /**
     * @return Number of potentials. {@code int}
     */
    public int getNumPotentials() {
        return potentials.size();
    }
    
    /**
     * @return additionalProperties. {@code LinkedHashMap<String, String>}
     */
    public LinkedHashMap<String, String> getOtherProperties() {
        return additionalProperties;
    }
    
    /**
     * @return probNet. {@code ProbNet}
     */
    public ProbNet getProbNet() {
        return probNet;
    }
    
    public List<Link<Node>> getLinks() {
        return probNet.getLinks(this);
    }
    
    public List<Node> getChildren() {
        return probNet.getChildren(this);
    }
    
    public List<Node> getParents() {
        return probNet.getParents(this);
    }
    
    public List<Node> getSiblings() {
        return probNet.getSiblings(this);
    }
    
    public List<Node> getNeighbors() {
        return probNet.getNeighbors(this);
    }
    
    public int getNumChildren() {
        return probNet.getNumChildren(this);
    }
    
    public int getNumParents() {
        return probNet.getNumParents(this);
    }
    
    public int getNumSiblings() {
        return probNet.getNumSiblings(this);
    }
    
    public int getNumNeighbors() {
        return probNet.getNumNeighbors(this);
    }
    
    /**
     * @param node {@code Node}
     *
     * @return True if {@code node} is parent of {@code this} node
     */
    public boolean isParent(Node node) {
        return probNet.isParent(node, this);
    }
    
    /**
     * @param node {@code Node}
     *
     * @return True if {@code node} is child of {@code this} node
     */
    public boolean isChild(Node node) {
        return probNet.isChild(node, this);
    }
    
    /**
     * @param node {@code Node}
     *
     * @return True if {@code node} and {@code this} are siblings
     */
    public boolean isSibling(Node node) {
        return probNet.isSibling(node, this);
    }
    
    /**
     * @param node {@code Node}
     *
     * @return True if {@code node} and {@code this} are neighbors
     */
    public boolean isNeighbor(Node node) {
        return probNet.isNeighbor(node, this);
    }
    
    //	@Override
    //	public int hashCode() {
    //		return super.hashCode();
    //	}
    @Override public boolean equals(Object obj) {
        boolean equals = obj instanceof Node;
        if (equals) {
            Node otherNode = (Node) obj;
            equals = variable.equals(otherNode.variable) && probNet.equals(otherNode.probNet) && nodeType == otherNode.nodeType;
        }
        return equals;
    }
    
    @Override public int hashCode() {
        return hashCode;
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(variable.getName() + " (");
        switch (nodeType) {
            case CHANCE:
                out.append("Chance");
                break;
            case DECISION:
                out.append("Decision");
                break;
            case UTILITY:
                out.append("Utility");
                break;
            /*
             * case COST: out.append("Utility, Cost node"); break; case
             * EFFECTIVENESS: out.append("Utility, Effectiveness node"); break; case
             * CE: out.append("Utility, Cost-Effectiveness"); break;
             */
            default:
                break;
        }
        out.append("): ");
        List<Node> parents = getParents();
        List<Node> children = getChildren();
        List<Node> siblings = getSiblings();
        List<Node> neighbors = getNeighbors();
        if (neighbors.isEmpty()) {
            out.append("No neighbors - ");
        } else {
            if (!parents.isEmpty()) {
                out.append(((parents.size() == 1) ? "Parent" : "Parents") + ": {");
                for (int i = 0; i < parents.size(); i++) {
                    Node parent = parents.get(i);
                    out.append(parent.getVariable());
                    if (i < parents.size() - 1) {
                        out.append(", ");
                    }
                }
                out.append("} - ");
            }
            if (!children.isEmpty()) {
                out.append(((children.size() == 1) ? "Child" : "Children") + ": {");
                for (int i = 0; i < children.size(); i++) {
                    Node child = children.get(i);
                    out.append(child.getVariable());
                    if (i < children.size() - 1) {
                        out.append(", ");
                    }
                }
                out.append("} - ");
            }
            if (!siblings.isEmpty()) {
                out.append(((siblings.size() == 1) ? "Sibling" : "Siblings") + ": {");
                for (int i = 0; i < siblings.size(); i++) {
                    Node sibling = siblings.get(i);
                    out.append(sibling.getVariable());
                    if (i < siblings.size() - 1) {
                        out.append(", ");
                    }
                }
                out.append("} - ");
            }
        }
        int numPotentials = potentials.size();
        if (numPotentials > 0) {
            out.append((numPotentials == 1) ? "Potential: " : "Potentials (" + numPotentials + "): {");
            for (int i = 0; i < potentials.size(); ++i) {
                out.append(potentials.get(i).toShortString());
                if (i < potentials.size() - 1) {
                    out.append(", ");
                }
            }
            if (numPotentials > 1) {
                out.append("}");
            }
        } else {
            out.append("No potentials");
        }
        return out.toString();
    }
    
    // TODO Comentar
    public void setUniformPotential() {
        
        // first, this variable. The potentials is not null
        Variable thisVariable = potentials.get(0).getVariable(0);
        List<Variable> variables = new ArrayList<>();
        variables.add(thisVariable);
        
        int numOfCellsInTable = thisVariable.getNumStates();
        double initialValue = Util.round(1 / ((double) numOfCellsInTable), "0.01");
        // add now all the parents
        
        for (Node parent : getParents()) {
            //TODO Revisar, ¿Solo se agrega/elimina un padre a la vez?
            //mpalacios
            //the set of variables could be changed, so , have to be updated.
            variables.add(parent.getVariable());
            numOfCellsInTable *= parent.getVariable().
                                       getNumStates();
        }
        // sets a new table with new columns and with all the same values
        double[] table = new double[numOfCellsInTable];
        for (int i = 0; i < numOfCellsInTable; i++) {
            table[i] = initialValue;
        }
        // and finally, create the potential and the list of potentials
        
        // TODO Comprobar que efectivamente es un CONDITIONAL_PROBABILITY
        TablePotential tablePotential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY, table);
        List<Potential> newListPotentials = new ArrayList<>();
        newListPotentials.add(tablePotential);
        
        potentials = newListPotentials;
        
    }
    
    /**
     * @return {@code String}
     */
    public String getPurpose() {
        return purpose;
    }
    
    /**
     * @param purpose {@code String}
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    /**
     * @return {@code double}
     */
    public double getRelevance() {
        return relevance;
    }
    
    /**
     * @param relevance {@code double}
     */
    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }
    
    /**
     * @return the comment. {@code String}
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param comment the comment to set. {@code String}
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * @return the modelType. {@code PolicyType}
     */
    public PolicyType getPolicyType() {
        return policyType;
    }
    
    /**
     * @param policyType the modelType to set. {@code PolicyType}
     */
    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }
    
    /**
     * @return {@code true} if it is a decision node with a non uniform potential.
     * {@code boolean}
     */
    public boolean hasPolicy() {
        return nodeType == NodeType.DECISION && !potentials.isEmpty();
    }
    
    public void samplePotentials() throws NotSupportedOperationException {
        for (int i = 0; i < potentials.size(); i++) {
            Potential originalPotential = potentials.get(i);
            potentials.set(i, originalPotential.sample());
        }
    }
    
    /**
     * @return Approximates the maximum or the minimum of the utility function of the Node. It is computed recursively by using the utility function
     * of parent nodes. If 'computeMax' is true then it computes the maximum; otherwise it computes the minimum.
     * For an exact computation of the maximum or the minimum of the utility function then it is required to use
     * method 'getUtilityFunction' and computes the maximum or the minimum over the resulting potential.
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    private double getApproximateMaxOrMinUtilityFunction(boolean computeMax) throws NonProjectablePotentialException {
        List<Potential> potentials = this.getPotentials();
        if (potentials == null || potentials.isEmpty()) {
            return 0.0;
        }
        Potential firstPotential = potentials.getFirst();
        switch (firstPotential) {
            case SumPotential ignored -> {
            }
            case ProductPotential ignored -> {
            }
            default -> throw new NonProjectablePotentialException.SuperValueMustBeSumOrProduct(firstPotential);
        }
        ;
        if (!this.isSuperValueNode()) {
            List<TablePotential> tableProject = firstPotential.tableProject(null, null);
            double[] values = !tableProject.isEmpty() ? tableProject.getFirst().values : new double[1];
            return computeMax ? Tools.max(values) : Tools.min(values);
        }
        List<Node> parents = this.getParents();
        double[] parentValues = new double[parents.size()];
        for (int i = 0; i < parents.size(); i++) {
            parentValues[i] = parents.get(i).getApproximateMaxOrMinUtilityFunction(computeMax);
        }
        return switch (firstPotential) {
            case SumPotential ignored -> Tools.sum(parentValues);
            case ProductPotential ignored -> Tools.multiply(parentValues);
            //This was checked before already, so this line cannot happen.
            default -> throw new NonProjectablePotentialException.SuperValueMustBeSumOrProduct(firstPotential);
        };
    }
    
    /**
     * @return Approximates the maximum of the utility function of the Node. It is computed recursively by using the utility function
     * of parent nodes. For an exact computation of the maximum of the utility function then it is required to use
     * method 'getUtilityFunction' and computes the maximum over the resulting potential.
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    public double getApproximateMaximumUtilityFunction() throws NonProjectablePotentialException {
        return getApproximateMaxOrMinUtilityFunction(true);
    }
    
    /**
     * @return Approximates the maximum of the utility function of the Node. It is computed recursively by using the utility function
     * of parent nodes. For an exact computation of the maximum of the utility function then it is required to use
     * method 'getUtilityFunction' and computes the maximum over the resulting potential.
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    public double getApproximateMinimumUtilityFunction() throws NonProjectablePotentialException {
        return getApproximateMaxOrMinUtilityFunction(false);
    }
    
    /**
     * @return The utility function of a utility variable. If it is a super-value node
     * then it operates their parent's utility functions recursively.
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    public @Nullable TablePotential getUtilityFunction() throws NonProjectablePotentialException {
        TablePotential result;
        List<Potential> potentials = this.getPotentials();
        if (potentials == null || potentials.isEmpty()) {
            return null;
        }
        Potential firstPotential = potentials.getFirst();
        switch (firstPotential) {
            case SumPotential ignored -> {
            }
            case ProductPotential ignored -> {
            }
            default -> throw new NonProjectablePotentialException.SuperValueMustBeSumOrProduct(firstPotential);
        }
        ;
        if (!this.isSuperValueNode()) {
            return firstPotential.tableProject(null, null).get(0);
        }
        List<TablePotential> utilityFunctionsParents = new ArrayList<>();
        for (Node node : this.getParents()) {
            utilityFunctionsParents.add(node.getUtilityFunction());
        }
        return switch (firstPotential) {
            case SumPotential ignored -> DiscretePotentialOperations.sum(utilityFunctionsParents);
            case ProductPotential ignored -> DiscretePotentialOperations.multiply(utilityFunctionsParents);
            //This was checked before already, so this line cannot happen.
            default -> throw new NonProjectablePotentialException.SuperValueMustBeSumOrProduct(firstPotential);
        };
    }
    
    /**
     * @return true if the variable is a supervalue node. False if does not
     */
    public boolean isSuperValueNode() {
        Node utilityNode = probNet.getNode(variable);
        int numOfUtilityParents = 0;
        for (Node parent : probNet.getParents(utilityNode)) {
            if (parent.getNodeType() == NodeType.UTILITY) {
                //if the node has two or more utility parents then is a super value node
                if ((numOfUtilityParents++) >= 1) {
                    return true;
                }
            }
            
        }
        return false;
    }
    
    /**
     * This method is used to
     *
     * @return a list with utility parents
     */
    public List<Node> getUtilityParents() {
        List<Node> utilityParents = new ArrayList<>();
        for (Node parent : getParents()) {
            if (parent.getNodeType() == NodeType.UTILITY) {
                utilityParents.add(parent);
            }
        }
        return utilityParents;
    }
    
    /**
     * @return true if a node has only utility parents
     */
    public boolean checkOnlyUtilityparents() {
        return getUtilityParents().size() == getParents().size();
    }
    
    /**
     * @return True if the node has only numerical parents
     */
    public boolean onlyNumericalParents() {
        List<Node> numericalParents = new ArrayList<>();
        List<Node> finiteStatesOrDiscretizedParents = new ArrayList<>();
        
        for (Node parent : getParents()) {
            if (parent.getVariable().getVariableType() == VariableType.NUMERIC) {
                numericalParents.add(parent);
            } else if (parent.getVariable().getVariableType() == VariableType.FINITE_STATES
                    || parent.getVariable().getVariableType() == VariableType.DISCRETIZED) {
                finiteStatesOrDiscretizedParents.add(parent);
            }
        }
        return !numericalParents.isEmpty() && finiteStatesOrDiscretizedParents.isEmpty();
    }
    
    /**
     * Returns the isInput.
     *
     * @return the isInput.
     */
    public boolean isInput() {
        return isInput;
    }
    
    /**
     * Sets the isInput.
     *
     * @param isInput the isInput to set.
     */
    public void setInput(boolean isInput) {
        this.isInput = isInput;
    }
    
    /**
     * @return the alwaysObserved
     */
    public boolean isAlwaysObserved() {
        return alwaysObserved;
    }
    
    /**
     * @param alwaysObserved the alwaysObserved to set
     */
    public void setAlwaysObserved(boolean alwaysObserved) {
        this.alwaysObserved = alwaysObserved;
    }
    
    public double getCoordinateX() {
        return coordinateX;
    }
    
    public void setCoordinateX(double coordinateX) {
        this.coordinateX = coordinateX;
    }
    
    public double getCoordinateY() {
        return coordinateY;
    }
    
    public void setCoordinateY(double coordinateY) {
        this.coordinateY = coordinateY;
    }
    
    public Node clone(ProbNet probNet) {
        Variable newVariable = CloneUtils.safeClone(this.variable);
        if (this.getNodeType() == NodeType.UTILITY) {
            for (Criterion criterion : probNet.getDecisionCriteria()) {
                if (criterion.getCriterionName().equals(this.variable.getDecisionCriterion().getCriterionName())) {
                    newVariable.setDecisionCriterion(criterion);
                    break;
                }
            }
        }
        Node newNode = new Node(probNet, newVariable, this.getNodeType());
        newNode.coordinateX = this.coordinateX;
        newNode.setCoordinateX(this.getCoordinateX());
        newNode.setCoordinateY(this.getCoordinateY());
        newNode.setPurpose(this.getPurpose());
        newNode.setRelevance(this.getRelevance());
        newNode.setComment(this.getComment());
        newNode.additionalProperties = CloneUtils.safeClone(this.additionalProperties);
        newNode.setAlwaysObserved(this.isAlwaysObserved());
        return newNode;
    }
    
    public Potential getPotential() {
        return getPotentials().get(0);
    }
    
    public Potential getPreviousPotential() {
        int x = getPotentials().size() - 1;
        return getPotentials().get(x);
    }
    
    public void finalizePotentialEdition() {
        List<Potential> potentials = new ArrayList<>();
        potentials.add(getPotential());
        setPotentials(potentials);
    }

    public void setPotentialConsistently(Potential newPotential){
        Potential lastPotential = getPotential();
        String newPotentialType = newPotential.getClass().getAnnotation(PotentialType.class).name();

        List<Variable> variables = lastPotential.getVariables();
        PotentialRole role = lastPotential.getPotentialRole();

        List<Potential> potentials = new ArrayList<>();
        if (newPotential == null) {
            PotentialManager relationTypeManager = new PotentialManager();

            if (newPotentialType.equals(PotentialManager.getPotentialName(CycleLengthShift.class))) {
                newPotential = relationTypeManager
                        .getByName(newPotentialType, variables, role, probNet.getCycleLength());
            } else {
                newPotential = relationTypeManager.getByName(newPotentialType, variables, role);
            }
        }

        potentials.add(newPotential);
        setPotentials(potentials);
        // update potential with link restriction
        if (newPotential instanceof TablePotential && getNodeType() != NodeType.DECISION) {
            newPotential = LinkRestrictionPotentialOperations.updatePotentialByLinkRestrictions(this);
            potentials = new ArrayList<>();
            potentials.add(newPotential);
            setPotentials(potentials);
        }
    }

    public void absorbNodeConsistently(Variable absorbedVariable) throws DoEditException.CannotDoEditException {
        Node absorbedNode = probNet.getNode(absorbedVariable);
        Node child = absorbedNode.getChildren().get(0);
        List<Link<Node>> newParentLinks = new ArrayList<>();;
        List<Potential> oldUtilityPotentials = child.getPotentials();
        List<Potential> newPotentials = new ArrayList<>();

        /* Chance parent */
        if (absorbedNode.getNodeType() == NodeType.CHANCE) {
            for (Potential potential : oldUtilityPotentials) {

                // Potentials to multiply
                List<TablePotential> utilityAndChance = new ArrayList<>();
                try {
                    utilityAndChance.add(potential.getCPT()); //Utility
                    utilityAndChance.add(absorbedNode.getPotentials().get(0).getCPT()); //Chance
                } catch (NonProjectablePotentialException e) {
                    throw new DoEditException.CannotDoEditException(e);
                }

                /* Obtain parameters to invoke multiplyAndMarginalize */
                // All variables from chance parent and utility child potentials
                List<Variable> unionVariables = AuxiliaryOperations.getUnionVariables(utilityAndChance);

                List<Variable> variablesToKeep = new ArrayList<>(unionVariables);
                variablesToKeep.remove(absorbedVariable);

                List<Variable> variablesToEliminate = new ArrayList<>();
                variablesToEliminate.add(absorbedVariable);

                // Discrete operation is valid because all parents are discrete
                TablePotential marginalizedPotential = DiscretePotentialOperations.
                        multiplyAndMarginalize(utilityAndChance, variablesToKeep, variablesToEliminate);

                // Convert to utility potential
                ExactDistrPotential exactDistrPotential = new ExactDistrPotential(variablesToKeep);
                exactDistrPotential.setValues(marginalizedPotential.values);

                newPotentials.add(exactDistrPotential);
            }

            // Parents of chance node are now parents of utility node
            for (Node parent : absorbedNode.getParents() ) {
                Link<Node> link = probNet.getLink(parent, child, true);
                if (link == null) {
                    // creating the Link saves it in the graph
                    newParentLinks.add(probNet.addLink(parent, child, true));

                }
            }

            /* Decision parent */
        } else if (absorbedNode.getNodeType() == NodeType.DECISION) {
            for (Potential potential : oldUtilityPotentials) {
                TablePotential utilityPotential;

                try {
                    utilityPotential = potential.getCPT();
                } catch (NonProjectablePotentialException e) {
                    throw new DoEditException.CannotDoEditException(e);
                }

                // Discrete operation is valid because all parents are discrete
                TablePotential maximizedPotential = (TablePotential) DiscretePotentialOperations.
                        maximize(utilityPotential, absorbedVariable)[0];
                List<Variable> newVariables = new ArrayList<>(potential.getVariables());
                newVariables.remove(absorbedVariable);


                // Convert to utility potential
                ExactDistrPotential exactDistrPotential = new ExactDistrPotential(newVariables);
                exactDistrPotential.setValues(maximizedPotential.values);

                newPotentials.add(exactDistrPotential);
            }
            // Parents of decision node don't turn into parents of utility node
        }
        child.setPotentials(newPotentials);
    }

}
