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

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UniformPotential;


/**
 * <code>LinkEdit</code> is an simple edit that allow to add/delete a network link 
 * between two nodes. Edit for directed links.
 * 
 *  
 * @version 1.0 21/12/10
 * @author Miguel Palacios
 * TODO: This class should not be here. It is referenced from NoCycles and that's why we've
 * moved it here but ideally it should depend on BaseLinkEdit. 
 */
@SuppressWarnings("serial")
public class LinkEdit extends SimplePNEdit {

	// Attributes
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
	 * Network is directed or not  (if true then the network is directed)
	 */
	protected boolean isDirected;
	
	/**
	 * Action is add or delete a link (if true then the action is add)
	 */
	protected boolean add;
	
	/**
	 * The last <code>Potential</code> of the second node before the edition
	 */
	protected ArrayList<Potential> lastPotential;
	
	/**
	 * The new <code>Potential</code> of the second node
	 */
	protected ArrayList<Potential> newPotentials = new ArrayList<Potential>() ;
	
	/**
     * Creates a new <code>LinkEdit</code> with two <code>ProbNode</code> 
     * objects,the type of the network graph and the action specified. 
     * The probNode must to has a valid potential
     * 
     * @param nodeName1  the first <code>ProbNode</code> object in the link
     * @param nodeName2  the second <code>ProbNode</code> object in the link
     * @param isDirected  a boolean, true for directed networks, false in other 
     * cases.
     * @param add a boolean, true if the action is add, false for delete action.
     */
	public LinkEdit(ProbNet probNet, String nodeName1, String nodeName2, 
			boolean isDirected, boolean add) {
		super(probNet);
		this.nodeName1 = nodeName1;
		this.nodeName2 = nodeName2;
		this.isDirected = isDirected;
		this.add = add;
		
		try {
			this.lastPotential = probNet.getProbNode(nodeName2).getPotentials();
		} catch (ProbNodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Methods
	@Override
	/** @throws exception <code>Exception</code> */
	public void doEdit() throws DoEditException {
        try
        {
            ProbNode node1 = probNet.getProbNode (nodeName1);
            ProbNode node2 = probNet.getProbNode (nodeName2);

            if (add)
            {
                probNet.addLink (node1, node2, isDirected);
            }
            else
            {
                probNet.removeLink (node1, node2, isDirected);
            }
            
            // TODO revisar si la actualización de potencial debe de hacerse con
            // otro edit
            
            // Update potential
            if (!(node2.getNodeType () == NodeType.DECISION && !node1.hasPolicy ()))
            {
                ArrayList<Variable> variables = lastPotential.get (0).getVariables ();
                if (add)
                {
                    variables.add (probNet.getVariable (nodeName1));
                }
                else
                {
                    variables.remove (probNet.getVariable (nodeName1));
                }
                UniformPotential newPotential = new UniformPotential (
                                                                      variables,
                                                                      lastPotential.get (0).getPotentialRole ());
                newPotential.setUtilityVariable (lastPotential.get (0).getUtilityVariable ());
                newPotentials.add (newPotential);
                node2.setPotentials (newPotentials);
            }            
        }
        catch (ProbNodeNotFoundException e)
        {
            throw new DoEditException (e.getMessage () + e.getStackTrace ());
        }
	}

	public void undo() {
		super.undo();
        try {
            ProbNode node1 = probNet.getProbNode (nodeName1);
            ProbNode node2 = probNet.getProbNode (nodeName2);
		
            if (add)
            {
                probNet.removeLink (node1, node2, isDirected);
            }
            else
            {
                probNet.addLink (node1, node2, isDirected);
            }
            // TODO revisar si la actualización de potencial debe de hacerse con
            // otro edit
			node2.setPotentials(lastPotential);
		} catch (ProbNodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
     * Returns a <code>boolean</code> value. True if the network graph is 
     * directed. 
     * @return a a <code>boolean</code> value that specify if the network is 
     * directed or not.
     */
	public boolean isDirected() {
		return isDirected;
	}

	/**
     * Returns a <code>boolean</code> value. True if the edit is an addition. 
     * @return a a <code>boolean</code> value that specify if if the edit is 
     * an addition or not.
     */
	public boolean isAdd() {
		return add;
	}
	
	/**
     * Gets the first <code>ProbNode</code> object in the link. 
     * 
     * @return the first <code>ProbNode</code> object in the link. 
     */
	public ProbNode getProbNode1() {
		try {
			return probNet.getProbNode(nodeName1);
		} catch (ProbNodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
     * Gets the second <code>ProbNode</code> object in the link. 
     * 
     * @return the second <code>ProbNode</code> object in the link. 
     */
	public ProbNode getProbNode2() {
		try {
			return probNet.getProbNode(nodeName2);
		} catch (ProbNodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/** @return A <code>String</code> with the type of link and the names of
	 *  <code>variable1</code> and <code>variable2</code>. */
	public String toString() {
		StringBuffer buffer = 
			new StringBuffer("LinkEdit" );
        buffer.append("(" + ((isAdd())? "add" : "remove") + "): " );
        buffer.append(nodeName1);
		if (isDirected) {
			buffer.append(" --> ");
		} else {			
			buffer.append(" --- ");
		}
		buffer.append(nodeName2);
		return buffer.toString();
	}
	   
	
}
