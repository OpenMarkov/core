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

import org.apache.log4j.Logger;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UniformPotential;

@SuppressWarnings("serial")
public class RemoveLinkEdit extends BaseLinkEdit {
	
	private Logger logger;

	/**
	 * The first node in the link, if the network is directed, probNode1 is the 
	 * parent
	 */
	protected String nodeName1;

	/**
	 * The second node in the link, if the network is directed, probNode2 is the 
	 * child
	 */
	protected String nodeName2;
	/**
	 * Resulting link of addition or removal.
	 */
	protected Link link;
	/**
	 * The last <code>Potential</code> of the second node before the edition
	 * /**
	 * parent node
	 */
	protected ProbNode node1;
	/**
	 * child node
	 */
	protected ProbNode node2;

	/**
	 * Last potential of the second node before edition
	 */
	protected Potential previousPotential;
	/**
	 * The new <code>Potential</code> of the second node
	 */
	protected ArrayList<Potential> newPotentials = new ArrayList<Potential>() ;
	
	protected ArrayList<Potential> lastPotential;
	// Constructor
	/** @param probNet <code>ProbNet</code>
	 * @param variable1 <code>Variable</code>
	 * @param variable2 <code>Variable</code>
	 * @param isDirected <code>boolean</code> */
	public RemoveLinkEdit(ProbNet probNet, Variable variable1, 
			Variable variable2,	boolean isDirected) {
		super(probNet, variable1, variable2, isDirected);
		
		this.nodeName1 = variable1.getName();
		this.nodeName2 = variable2.getName();
		
		this.link = null;
		
		try {
			this.lastPotential = probNet.getProbNode(nodeName2).getPotentials();
		} catch (ProbNodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.logger = Logger.getLogger(RemoveLinkEdit.class);
	}

	@Override
	public void doEdit() throws NotEnoughMemoryException{
		try{
		node1 = probNet.getProbNode (nodeName1);
		node2 = probNet.getProbNode (nodeName2);
		probNet.removeLink (node1, node2, isDirected);
		this.link = probNet.getGraph ().getLink (node1.getNode (), node2.getNode (), isDirected);
		if (node2.getNodeType () != NodeType.DECISION)
        {
			// Update potential
            ArrayList<Variable> variables = lastPotential.get (0).getVariables ();
            variables.add (probNet.getVariable (nodeName1)); 
            previousPotential = node2.getPotentials().get(0);
            Potential newPotential = previousPotential.removeVariable(probNet.getVariable (nodeName1));
            if ( newPotential == null) {// It has not been implemented yet for this type of potential
            	UniformPotential newUniformPotential = new UniformPotential (
                        variables,
                        lastPotential.get (0).getPotentialRole ());
            	newUniformPotential.setUtilityVariable (lastPotential.get (0).getUtilityVariable ());
				newPotentials.add (newUniformPotential);
				node2.setPotentials (newPotentials);
            } else { 
            	
            	newPotential.setUtilityVariable (lastPotential.get (0).getUtilityVariable ());
            	newPotentials.add (newPotential);
            	node2.setPotentials (newPotentials);
            	
            }
        }
		
	} catch (ProbNodeNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		probNet.removeLink(variable1, variable2, isDirected);
	}

	
	@Override
	public void undo() {
		super.undo();
		try {
			node2 = probNet.getProbNode (nodeName2);
		} catch (ProbNodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		node2.setPotentials (lastPotential);
		try {
			probNet.addLink(variable1, variable2, isDirected);
		} catch (Exception e) {
			logger.fatal (e);
		}
	}

    /** Method to compare two RemoveLinkEdits comparing the names of
     * the source and destination variable alphabetically.
     * @param obj
     * @return
     */
    public int compareTo(RemoveLinkEdit obj){
        int result;

        if (( result = variable1.getName().compareTo(obj.getVariable1().
                getName())) != 0)
            return result;
        if (( result = variable2.getName().compareTo(obj.getVariable2().
                getName())) != 0)
            return result;
        else
            return 0;
    }

	@Override
	public String getOperationName() {
		return "Remove";
	}

	public String toString() {
		if (isDirected) {
			return new String(
					"RemoveLinkEdit: " + variable1 + " --> " + variable2);
		} else {
			return new String(
					"RemoveLinkEdit: " + variable1 + " --- " + variable2);
		}
	}	

}
