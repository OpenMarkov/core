/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.oon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.oon.exception.InstanceAlreadyExistsException;

public class OOBNet extends ProbNet
{
    private HashMap<String, Instance> instances     = new HashMap<String, Instance> ();
    private ArrayList<InstanceLink>   instanceLinks = new ArrayList<InstanceLink> ();

    
    /**
     * Constructor for OOBNet.
     */
    public OOBNet ()
    {
        super ();
    }

    /**
     * Constructor for OOBNet.
     * @param networkType
     */
    public OOBNet (NetworkType networkType)
    {
        super (networkType);
    }
    
    /**
     * Constructor for OOBNet.
     * @param networkType
     */
    public OOBNet (ProbNet probNet)
    {
        super();
        try
        {
            setNetworkType (probNet.getNetworkType ());
        }
        catch (ConstraintViolationException e1)
        {
            e1.printStackTrace();
        } 
        // copy constraints
        int numConstraints = probNet.getConstraints ().size(); 
        for (int i = 1; i < numConstraints; i++)
        {
            try
            {
                addConstraint (probNet.getConstraints().get (i), false);
            }
            catch (ConstraintViolationException e)
            {
                // Unreachable code because constraints are not tested in copy
            }
        }
        ArrayList<ProbNode> probNodes = probNet.getProbNodes();
        // Adds variables and create corresponding nodes. Also add potentials
        for (ProbNode probNode : probNodes) {
            // Add variables and create corresponding nodes
            Variable variable = probNode.getVariable();
            ProbNode newProbNode = null;
            newProbNode = addVariable (variable, probNode.getNodeType ());
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

        // Add links
        ArrayList<ProbNode> nodes = probNet.getProbNodes();
        for (ProbNode probNode1 : nodes) {
            Variable variable1 = probNode1.getVariable();
            ProbNode newNode1 = this.getProbNode(variable1);
            ArrayList<ProbNode> neighbors = getProbNodesOfNodes(probNode1
                    .getNode().getNeighbors());
            for (ProbNode probNode2 : neighbors) {
                Variable variable2 = probNode2.getVariable();
                ProbNode newNode2 = this.getProbNode(variable2);
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
        getPNESupport().setListeners(probNet.getPNESupport ().getListeners());
        
        // Copy additionalProperties
        Set<String> keys = probNet.additionalProperties.keySet();
        HashMap<String, String> copyProperties = new HashMap<String, String>();
        for (String key : keys) {
            copyProperties.put(key, probNet.additionalProperties.get(key));
        }
        additionalProperties = copyProperties;

        }
    

    /**
     * @param classNet
     * @param instanceName
     * @param instanceNodes
     * @throws InstanceAlreadyExistsException
     */
    public void addInstance (Instance instance)
        throws InstanceAlreadyExistsException
    {
        if (instances.containsKey (instance.getName ()))
        {
            throw new InstanceAlreadyExistsException ();
        }
        else
        {
            instances.put (instance.getName (), instance);
        }
    }

    /**
     * @return instance list
     */
    public HashMap<String, Instance> getInstances ()
    {
        return instances;
    }

    /**
     * Add an instance link
     * @param link
     */
    public void addInstanceLink (InstanceLink link)
    {
        instanceLinks.add (link);
        link.getDestInstance ().addInputParameter (link.getDestSubInstance (),
                                                   link.getSourceInstance ());
    }

    /**
     * @return the instanceLinks
     */
    public ArrayList<InstanceLink> getInstanceLinks ()
    {
        return instanceLinks;
    }

    /**
     * Removes an instance Link
     * @param instanceLink
     */
    public void removeInstanceLink (InstanceLink instanceLink)
    {
        instanceLinks.remove (instanceLink);
        instanceLink.getDestInstance ().removeInputParameter (instanceLink.getDestSubInstance ());
    }

    /**
     * Returns the equivalent node in <code>sourceInstance</code> to the
     * <code>probNode</code> in <code>destinationInstance</code>
     * @param sourceInstance
     * @param destInstance
     * @param probNode
     * @return
     */
    private ProbNode getEquivalentNode (Instance sourceInstance,
                                        Instance destInstance,
                                        ProbNode probNode)
    {
        ProbNode equivalentNode = null;
        int i = 0;
        String nodeName = probNode.getName ();
        nodeName = nodeName.replace (destInstance.getName () + ".", "");
        while (equivalentNode == null && i < sourceInstance.getNodes ().size ())
        {
            String equivalentNodeName = sourceInstance.getNodes ().get (i).getName ();
            equivalentNodeName = equivalentNodeName.replace (sourceInstance.getName () + ".", "");
            if (equivalentNodeName.equals (nodeName))
            {
                equivalentNode = sourceInstance.getNodes ().get (i);
            }
            ++i;
        }
        return equivalentNode;
    }

    /**
     * Unrolls instances and returns a plain probabilistic network
     * @return
     */
    public ProbNet getPlainProbNet ()
    {
        ProbNet probNet = copy ();
        probNet.getGraph ().makeLinksExplicit (false);
        for (InstanceLink instanceLink : getInstanceLinks ())
        {
            for (ProbNode node : instanceLink.getDestSubInstance ().getNodes ())
            {
                ProbNode paramNode = probNet.getProbNode (getEquivalentNode (
                                                                             instanceLink.getSourceInstance (),
                                                                             instanceLink.getDestSubInstance (),
                                                                             node).getVariable ());
                if (paramNode != null)
                {
                    ProbNode formalNode = probNet.getProbNode (node.getVariable ());
                    // Update potentials
                    HashMap<Potential, Potential> potentialsToReplace = new HashMap<Potential, Potential> ();
                    for (Potential potential : probNet.getPotentials (node.getVariable ()))
                    {
                        Potential potentialCopy;
                        try
                        {
                            potentialCopy = potential.copy ();
                            potentialCopy.replaceVariable (formalNode.getVariable (),
                                                           paramNode.getVariable ());
                            potentialsToReplace.put (potential, potentialCopy);
                        }
                        catch (NotEnoughMemoryException e)
                        {
                        }
                    }
                    for (ProbNode probNode : probNet.getProbNodes ())
                    {
                        for (Potential potentialToReplace : potentialsToReplace.keySet ())
                        {
                            if (probNode.getPotentials ().contains (potentialToReplace))
                            {
                                ArrayList<Potential> potentials = probNode.getPotentials ();
                                potentials.remove (potentialToReplace);
                                potentials.add (potentialsToReplace.get (potentialToReplace));
                                probNode.setPotentials (potentials);
                            }
                        }
                    }
                    // Update Links
                    // Replace links to children
                    if (formalNode == null || formalNode.getNode () == null)
                    {
                        System.out.println ();
                    }
                    for (Node child : formalNode.getNode ().getChildren ())
                    {
                        probNet.removeLink (formalNode, (ProbNode) child.getObject (), true);
                        if (!child.isParent (paramNode.getNode ()))
                        {
                            probNet.addLink (paramNode, (ProbNode) child.getObject (), true);
                        }
                    }
                    // Replace links from parents
                    for (Node parent : formalNode.getNode ().getParents ())
                    {
                        probNet.removeLink ((ProbNode) parent.getObject (), formalNode, true);
                        if (!paramNode.getNode ().isParent (parent))
                        {
                            probNet.addLink ((ProbNode) parent.getObject (), paramNode, true);
                        }
                    }
                    // Replace links between siblings
                    for (Node sibling : formalNode.getNode ().getSiblings ())
                    {
                        probNet.removeLink ((ProbNode) sibling.getObject (), formalNode, false);
                        probNet.addLink ((ProbNode) sibling.getObject (), paramNode, false);
                    }
                }
            }
            // Remove formal parameter nodes
            for (ProbNode node : instanceLink.getDestSubInstance ().getNodes ())
            {
                probNet.removeProbNode (probNet.getProbNode (node.getVariable ()));
            }
        }
        try
        {
            probNet.setNetworkType (BayesianNetworkType.getUniqueInstance ());
        }
        catch (ConstraintViolationException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace ();
        }
        return probNet;
    }
}
