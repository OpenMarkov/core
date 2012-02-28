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
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UniformPotential;

/** Creates a directed or undirected link between two nodes associated to two
 * variables in a <code>ProbNet</code> */
@SuppressWarnings("serial")
public class AddLinkEdit extends BaseLinkEdit {
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
     */
    protected ArrayList<Potential> oldPotential;
    /**
     * The new <code>Potential</code> of the second node
     */
    protected ArrayList<Potential> newPotentials = new ArrayList<Potential>() ;
    /**
     * Last potential of the second node before edition
     */
    protected Potential previousPotential;
    /**
     * parent node
     */
    protected ProbNode node1;
    /**
     * child node
     */
    protected ProbNode node2;

    // Constructor
    /** @param probNet <code>ProbNet</code>
     * @param variable1 <code>Variable</code>
     * @param variable2 <code>Variable</code>
     * @param isDirected <code>boolean</code> */
    public AddLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, 
            boolean isDirected) {
        super(probNet, variable1, variable2, isDirected);
        this.nodeName1 = variable1.getName();
        this.nodeName2 = variable2.getName();
        
        this.link = null;
        
        try {
            this.oldPotential = probNet.getProbNode(nodeName2).getPotentials();
        } catch (ProbNodeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Methods
    @Override
    /** @throws exception <code>Exception</code> */
    public void doEdit() throws DoEditException, NotEnoughMemoryException {
        try
        {
            node1 = probNet.getProbNode (nodeName1);
            node2 = probNet.getProbNode (nodeName2);
            probNet.addLink (node1, node2, isDirected);
            this.link = probNet.getGraph ().getLink (node1.getNode (), node2.getNode (), isDirected);
            if (node2.getNodeType () != NodeType.DECISION && !oldPotential.isEmpty ())
            {
                // Update potential
                ArrayList<Variable> variables = oldPotential.get (0).getVariables ();
                variables.add (probNet.getVariable (nodeName1));
                previousPotential = node2.getPotentials ().get (0);
                Potential newPotential = previousPotential.addVariable (probNet.getVariable (nodeName1));
                if (newPotential == null)
                {// It has not been implemented yet for this type of potential
                    newPotential = new UniformPotential (variables,
                                                         oldPotential.get (0).getPotentialRole ());
                }
                newPotential.setUtilityVariable (oldPotential.get (0).getUtilityVariable ());
                newPotentials.add (newPotential);
                node2.setPotentials (newPotentials);
            }
        }
        catch (ProbNodeNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    public void undo() {
        super.undo();
        
        try {
            node2 = probNet.getProbNode (nodeName2);
        } catch (ProbNodeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        node2.setPotentials (oldPotential);
        probNet.removeLink(variable1, variable2, isDirected);
    }
   
    /** Method to compare two AddLinkEdits comparing the names of
     * the source and destination variable alphabetically.
     * @param obj
     * @return
     */
    public int compareTo(AddLinkEdit obj){
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
        return "Add";
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
    
   /**
    * Returns the link.
    * @return the link.
    */
   public Link getLink ()
   {
       return link;
   }    
    
}
