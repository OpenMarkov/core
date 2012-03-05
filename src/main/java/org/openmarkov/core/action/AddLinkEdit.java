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
    
    private boolean updatePotentials;
    /**
     * Resulting link of addition or removal.
     */
    protected Link link;
    /**
     * The last <code>Potential</code> of the second node before the edition
     */
    protected ArrayList<Potential> oldPotentials;
    /**
     * The new <code>Potential</code> of the second node
     */
    protected ArrayList<Potential> newPotentials = new ArrayList<Potential>() ;
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
            boolean isDirected, boolean updatePotentials) {
        super(probNet, variable1, variable2, isDirected);
        
        try
        {
            node1 = probNet.getProbNode (variable1.getName());
            node2 = probNet.getProbNode (variable2.getName());
        }
        catch (ProbNodeNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.updatePotentials = updatePotentials;
        this.link = null;
    }
    
    public AddLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, 
                       boolean isDirected)
    {
        this(probNet, variable1, variable2, isDirected, true);
    }

    // Methods
    @Override
    /** @throws exception <code>Exception</code> */
    public void doEdit() throws DoEditException, NotEnoughMemoryException {
        probNet.addLink (node1, node2, isDirected);
        this.link = probNet.getGraph ().getLink (node1.getNode (), node2.getNode (), isDirected);
        if (updatePotentials && node2.getNodeType () != NodeType.DECISION)
        {
            this.oldPotentials = node2.getPotentials ();
            for (Potential oldPotential : oldPotentials)
            {
                // Update potential
                Potential newPotential = oldPotential.addVariable (node1.getVariable ());
                if (newPotential == null)
                {// It has not been implemented yet for this type of potential
                    ArrayList<Variable> variables = oldPotential.getVariables ();
                    variables.add (node1.getVariable ());
                    newPotential = new UniformPotential (variables,
                                                         oldPotential.getPotentialRole ());
                }
                newPotential.setUtilityVariable (oldPotential.getUtilityVariable ());
                newPotentials.add (newPotential);
            }
            node2.setPotentials (newPotentials);
        }
    }

    public void undo() {
        super.undo();
        
        try {
            node2 = probNet.getProbNode (variable2.getName ());
        } catch (ProbNodeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(updatePotentials)
        {
            node2.setPotentials (oldPotentials);
        }
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
    public ProbNode getProbNode1 ()
    {
        return node1;
    }

    /**
    * Gets the second <code>ProbNode</code> object in the link. 
    * 
    * @return the second <code>ProbNode</code> object in the link. 
    */
    public ProbNode getProbNode2 ()
    {
        return node2;
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
