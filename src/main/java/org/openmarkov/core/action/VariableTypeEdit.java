/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

@SuppressWarnings("serial")
public class VariableTypeEdit extends SimplePNEdit {
	//private ProbNet probNet;
	private ProbNode probNode;
	private VariableType newType;
	private VariableType currentType;
	
	
	public VariableTypeEdit(ProbNode probNode, 
			VariableType newType){
		super(probNode.getProbNet ());
		this.probNode = probNode;
		this.newType = newType;
		this.currentType = probNode.getVariable().getVariableType();
		
	}
	@Override
    public void doEdit ()
        throws DoEditException
    {
        ArrayList<Node> nodes;
        if (currentType != newType && newType.compareTo (VariableType.DISCRETIZED) != 0) {
        	probNode.setUniformPotential2ProbNode();
        	nodes = probNode.getNode ().getChildren ();
            for (Node node : nodes)
            {
            	ProbNode child = (ProbNode) node.getObject ();
            	child.setUniformPotential2ProbNode();
            }
        }
        if (currentType.compareTo (VariableType.NUMERIC) == 0) //if  current type is numeric
        {
            probNode.getVariable ().setStates (probNode.getProbNet ().getDefaultStates ());
            ArrayList<Variable> variables = new ArrayList<Variable> ();
            if (probNode.getNodeType () != NodeType.UTILITY)
            {
                variables.add (probNode.getVariable ());
            }
            for (Node node : probNode.getNode ().getParents ())
            {
                variables.add (((ProbNode) node.getObject ()).getVariable ());
            }
            UniformPotential uniformPotential = new UniformPotential (
                                                                      variables,
                                                                      probNode.getPotentials ().get (0).getPotentialRole ());
            ArrayList<Potential> potentials = new ArrayList<Potential> (1);
            potentials.add (uniformPotential);
            probNode.setPotentials (potentials);
            probNode.setUniformPotential ();
            nodes = probNode.getNode ().getChildren ();
            for (Node node : nodes)
            {
                ProbNode child = (ProbNode) node.getObject ();
                child.setUniformPotential ();
            }
        }
        
       
        resetLink(probNode.getNode());
        probNode.getVariable ().setVariableType (newType);
    }
	@Override
	public void undo(){
		super.undo();
		probNode.getVariable().setVariableType(currentType);
	}
	public VariableType getNewVariableType(){
		return newType;
	}
	
	
	public ProbNode getProbNode()
	{
		
		return this.probNode;
	}
	
	
	/****
	 * This method resets the link restriction of the links of the node
	 * 
	 * @param node
	 */
	private void resetLink(Node node) {
		for (Link link : node.getLinks()) {
			if (link.hasRestrictions()) {
				try {	
					link.resetRestrictionsPotential();
				} catch (NotEnoughMemoryException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
