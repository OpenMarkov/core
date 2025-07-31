/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.oopn.action;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.oopn.Instance;
import org.openmarkov.core.oopn.OOPNet;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings("serial") public class AddInstanceEdit extends AbstractUndoableEdit implements PNEdit {
    private String instanceName;
    private OOPNet oopNet;
    private ProbNet classNet;
    private java.awt.geom.Point2D.Double cursorPositon;
    private List<PNEdit> edits = null;
    private int doneEditCounter;
    
    public AddInstanceEdit(OOPNet probNet, ProbNet classNet, String instanceName,
                           java.awt.geom.Point2D.Double cursorPosition) {
        this.oopNet = probNet;
        this.classNet = classNet;
        this.instanceName = instanceName;
        this.cursorPositon = cursorPosition;
        edits = new ArrayList<>();
    }
    
    @Override public void doEdit() throws DoEditException {
        doneEditCounter = 0;
        
        if (oopNet.getInstances().containsKey(instanceName)) {
            throw new DoEditException.InstanceAlreadyExists(instanceName);
        }
        // Calculate top left corner of net
        double topCorner = Double.POSITIVE_INFINITY;
        double leftCorner = Double.POSITIVE_INFINITY;
        for (Node node : classNet.getNodes()) {
            if (node.getCoordinateX() < leftCorner) {
                leftCorner = node.getCoordinateX();
            }
            if (node.getCoordinateY() < topCorner) {
                topCorner = node.getCoordinateY();
            }
        }
        
        // Add nodes to the probNet class
        for (Node node : classNet.getNodes()) {
            Variable variable = new Variable(node.getVariable());
            variable.setName(instanceName + "." + variable.getName());
            Point2D.Double position = new Point2D.Double(node.getCoordinateX() - leftCorner + cursorPositon.getX(),
                                                         node.getCoordinateY() - topCorner + cursorPositon.getY());
            
            edits.add(new AddNodeEdit(oopNet, variable, node.getNodeType(), position));
        }
        // Apply node generation edits
        for (PNEdit edit : edits) {
            edit.doEdit(oopNet);
            ++doneEditCounter;
        }
        
        // Add links to the probNet class
        // Gather link creation edits
        for (Link<Node> link : classNet.getLinks()) {
            String originalSourceNodeName = link.getNode1().getName();
            String originalDestinationNodeName = link.getNode2().getName();
            
            edits.add(new AddLinkEdit(oopNet, oopNet.getVariable(instanceName + "." + originalSourceNodeName),
                                      oopNet.getVariable(instanceName + "." + originalDestinationNodeName), link.isDirected()));
        }
        
        //Apply link creation edits
        List<Link<Node>> pastedLinks = new ArrayList<>();
        for (PNEdit edit : edits) {
            if (edit instanceof AddLinkEdit) {
                AddLinkEdit linkEdit = ((AddLinkEdit) edit);
                linkEdit.doEdit(this.oopNet);
                ++doneEditCounter;
                pastedLinks.add(linkEdit.getLink());
            }
        }
        
        List<Node> instanceNodes = new ArrayList<>();
        //Replace potentials to already created nodes with copies of copied nodes
        for (Node originalNode : classNet.getNodes()) {
            List<Potential> newPotentials = new ArrayList<>();
            Node newNode = oopNet.getNode(instanceName + "." + originalNode.getName());
            for (Potential originalPotential : originalNode.getPotentials()) {
                Potential potential = originalPotential.copy();
                for (int i = 0; i < potential.getNumVariables(); ++i) {
                    String variableName = potential.getVariable(i).getName();
                    Variable variable = oopNet.getVariable(instanceName + "." + variableName);
                    potential.replaceVariable(i, variable);
                }
                newPotentials.add(potential);
            }
            newNode.setPotentials(newPotentials);
            // Copy comment too!
            newNode.setComment(originalNode.getComment());
            newNode.setRelevance(originalNode.getRelevance());
            newNode.setPurpose(originalNode.getPurpose());
            newNode.additionalProperties = new LinkedHashMap<>(originalNode.additionalProperties);
            newNode.setInput(originalNode.isInput());
            instanceNodes.add(newNode);
        }
        Instance instance = new Instance(instanceName, classNet, instanceNodes);
        oopNet.addInstance(instance);
    }
    
    @Override public void doEdit(ProbNet probNet) throws DoEditException {
        PNEdit.startEdit(this, probNet);
        this.doEdit();
        PNEdit.endEdit(this);
    }
    
    @Override public void setSignificant(boolean significant) {
    }
    
    @Override public ProbNet getProbNet() {
        return this.oopNet;
    }
    
    @Override public void setProbNet(ProbNet probNet) {
    }
    
    @Override public void undo() throws CannotUndoException {
        for (int i = 0; i < doneEditCounter; ++i) {
            oopNet.getPNESupport().undo();
        }
        doneEditCounter = 0;
    }
}
