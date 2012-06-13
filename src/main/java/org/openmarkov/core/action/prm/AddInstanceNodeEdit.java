/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.prm;

import java.awt.geom.Point2D;

import org.openmarkov.core.action.AddProbNodeEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PolicyType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;
import org.openmarkov.core.model.network.prm.InstanceNode;

/**
 * <code>AddProbNodeEdit</code> is a edit that allow add a node to
 * <code>ProbNet</code> object.
 * @version 1 02/05/10
 * @author ibermejo
 */
@SuppressWarnings("serial")
public class AddInstanceNodeEdit extends AddProbNodeEdit
{
    // Attributes

	/**
	 * Class of the instance node
	 */
	protected ProbNet classNet;
	/**
	 * Instance Name
	 */
	protected String instanceName;

    /**
     * Creates a new <code>AddInstanceNodeEdit</code> with the network where the new
     * new node will be added and basic information about it.
     * @param probNet the <code>ProbNet</code> where the new node will be added.
     * @param variable the variable contained in the new node
     * @param nodeType The new node type.
     * @param cursorposition the position (coordinates X,Y) of the node.
     * @param classNet class of the instance.
     * @param instanceName name of the instance.
     */
    public AddInstanceNodeEdit (ProbNet probNet,
                            Variable variable,
                            NodeType nodeType,
                            Point2D.Double cursorPosition,
                            ProbNet classNet,
                            String instanceName)
    {
        super (probNet, variable, nodeType, cursorPosition);
        this.classNet = classNet;
        this.instanceName = instanceName;
    }
    
    /**
     * Creates a new <code>AddInstanceNodeEdit</code> with the network where the new
     * new node will be added and basic information about it.
     * @param probNet the <code>ProbNet</code> where the new node will be added.
     * @param variable the variable contained in the new node
     * @param nodeType The new node type.
     */
    public AddInstanceNodeEdit (ProbNet probNet,
                            Variable variable,
                            NodeType nodeType, 
                            ProbNet classNet,
                            String instanceName)
    {
        this (probNet, variable, nodeType, new Point2D.Double(), classNet, instanceName);
    }    
    

    @Override
    public void doEdit ()
    {
        // Adds the new variable to network ( creates a probNode instance )
        newNode = new InstanceNode(probNet, variable, nodeType, instanceName);
        probNet.addProbNode(newNode);
        
        // TODO revisar si es conveniente utilizar una constraint
        // Sets a uniformPotential for the new probNode
        // Decision node has no potential when is created
        if (nodeType != NodeType.DECISION)
        {
           newNode = probNet.addPotential (PotentialOperations.getUniformPotential (probNet, variable,
                                                                           nodeType));
        }
        else
        {
            newNode.setPolicyType (PolicyType.OPTIMAL);
        }
        // Sets the visual node position
        newNode.getNode ().setCoordinateX ((int) cursorPosition.getX ());
        newNode.getNode ().setCoordinateY ((int) cursorPosition.getY ());
    }

    public void undo ()
    {
        super.undo ();
        probNet.removeProbNode (newNode);
    }

    /** @return newNode the new <code>ProbNode</code> added */
    public ProbNode getProbNode ()
    {
        return newNode;
    }
    
    public Variable getVariable ()
    {
        return variable;
    }
    
    public NodeType getNodeType ()
    {
        return nodeType;
    }        

    public String getPresentationName ()
    {
        return "Edit.AddProbNodeEdit";
    }

    public String getUndoPresentationName ()
    {
        return "Edit.AddProbNodeEdit.Undo";
    }

    public String getRedoPresentationName ()
    {
        return "Edit.AddProbNodeEdit.Redo";
    }

    public void redo ()
    {
        setTypicalRedo (false);
        super.redo ();
        probNet.addProbNode (newNode);
    }
}
