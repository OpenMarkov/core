/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;

/**
 * @author mluque It is the type of 'nodesHashMaps'. It contains a
 *         <code>LinkedHashMap</code> from <code>NodeType</code> to
 *         <code>NodesHashMapType</code>.
 */
public class ProbNodeDepot {
    /**
     * @author mluque Contains a <code>LinkedHashMap</code> from
     *         <code>Variable</code> to <code>ProbNode</code>.
     */
    private class NodesHashMap {
        LinkedHashMap<Variable, Node> nodesHashMap;

        NodesHashMap() {
            nodesHashMap = new LinkedHashMap<Variable, Node>();
        }

        public Node get(Variable variable) {
            return nodesHashMap.get(variable);
        }

        public void put(Variable variable, Node probNode) {
            nodesHashMap.put(variable, probNode);
        }

        public int size() {
            return nodesHashMap.size();
        }

        public Collection<Node> values() {
            return nodesHashMap.values();
        }

        public void remove(Variable variable) {
            nodesHashMap.remove(variable);
        }
    }

    private LinkedHashMap<NodeType, NodesHashMap> nodesHashMaps;

    public ProbNodeDepot() {
        nodesHashMaps = new LinkedHashMap<NodeType, NodesHashMap>();
        // create a linkedHashMap for each type of nodes
        for (NodeType type : NodeType.values()) {
            nodesHashMaps.put(type, new NodesHashMap());
        }
    }

    public int getNumNodes() {
        int numNodes = 0;
        for (NodesHashMap hashMap : nodesHashMaps.values()) {
            numNodes = numNodes + hashMap.size();
        }
        return numNodes;
    }

    public int getNumNodes(NodeType nodeType) {
        return nodesHashMaps.get(nodeType).size();
    }

    public List<Node> getProbNodes() {
        List<Node> nodes = new ArrayList<Node>(getNumNodes());
        for (NodesHashMap hashMap : nodesHashMaps.values()) {
            nodes.addAll(hashMap.values());
        }
        return nodes;
    }

    public List<Potential> getPotentialsByType(NodeType nodeType) {
        NodesHashMap nodesType = nodesHashMaps.get(nodeType);
        List<Potential> potentials = new ArrayList<Potential>();
        for (Node node : nodesType.values()) {
            potentials.addAll(node.getPotentials());
        }
        return potentials;
    }

    /**
     * @return All the nodes of certain kind
     * @param nodeType
     * @consultation
     */
    public List<Node> getProbNodes(NodeType nodeType) {
        return new ArrayList<Node>(nodesHashMaps.get(nodeType).values());
    }

    public List<Potential> getPotentialsByRole(PotentialRole role) {
        List<Potential> potentials = new ArrayList<Potential>();
        for (NodesHashMap nodesHashMap : nodesHashMaps.values()) {
            for (Node auxProbNode : nodesHashMap.values()) {
                for (Potential auxPot : auxProbNode.getPotentials()) {
                    if (auxPot.getPotentialRole() == role) {
                        potentials.add(auxPot);
                    }
                }
            }
        }
        return potentials;
    }

    public Node getProbNode(NodeType nodeType, Variable variable) {
        return nodesHashMaps.get(nodeType).get(variable);
    }

    public Node getProbNode(String nameOfVariable) {
        for (NodeType nodeType : NodeType.values()) {
            Collection<Node> probNodes = nodesHashMaps.get(nodeType).values();
            for (Node probNode : probNodes) {
                if (probNode.getVariable().getName().contentEquals(nameOfVariable)) {
                    return probNode;
                }
            }
        }
        return null;
    }

    public Node getProbNode (Variable variable)
    {
        Node probNode = null;
        for (NodesHashMap nodes : nodesHashMaps.values ())
        {
            if ((probNode = nodes.get (variable)) != null)
            {
                break;
            }
        }
        return probNode;
    }    
    
    /**
     * @param nameOfVariable <code>String</code>
     * @param nodeType <code>NodeType</code>
     * @return The node with <code>nameOfVariable</code> and
     *         <code>kindOfNode</code> if exists otherwhise null
     * @throws ProbNodeNotFoundException
     */
    public Node getProbNode (String nameOfVariable, NodeType nodeType)
    {
        for (Node node : nodesHashMaps.get (nodeType).values ())
        {
            if (node.getVariable ().getName ().contentEquals (nameOfVariable))
            {
                return node;
            }
        }
        return null;
    }    

    public void addProbNode(Node probNode) {
        nodesHashMaps.get(probNode.getNodeType()).put(probNode.getVariable(), probNode);
    }

    public void removeProbNode(Node probNode) {
        NodeType nodeKindValue = probNode.getNodeType ();
        Variable variable = probNode.getVariable ();
        NodesHashMap nodesMap = nodesHashMaps.get (nodeKindValue);
        nodesMap.remove (variable);
     }

    public int getNumPotentials() {
        int numPotentials = 0;
        for (NodesHashMap linkedHasMap : nodesHashMaps.values ())
        {
            for (Node probNode : linkedHasMap.values ())
            {
                numPotentials += probNode.getNumPotentials ();
            }
        }
        return numPotentials;
    }
    
    
}
