/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import java.util.List;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint(name = "DistinctLinks", defaultBehavior = ConstraintBehavior.YES)
/****
 * This class implements the DistinctLinks constraint, which establishes that the network 
 * can not have two equal links.
 * @author ckonig
 * @author manuel arias
 *
 */
public class DistinctLinks extends PNConstraint
{

    @Override
    public boolean checkProbNet (ProbNet probNet)
    {
        List<Node> nodes = probNet.getNodes ();
        for (Node node : nodes)
        {
            if (probNet.getNumLinks (node) > (probNet.getNumChildren (node) + probNet.getNumParents (node) + probNet.getNumSiblings (node)))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkEdit (ProbNet probNet, PNEdit edit)
        throws NonProjectablePotentialException,
        WrongCriterionException
    {
        List<PNEdit> edits = UtilConstraints.getSimpleEditsByType (edit, AddLinkEdit.class);
        for (PNEdit simpleEdit : edits)
        {
        	AddLinkEdit addLinkEdit = (AddLinkEdit) simpleEdit;
            Variable variable1 = addLinkEdit.getVariable1 ();
            Node node1 = probNet.getNode (variable1);
            Variable variable2 = ((AddLinkEdit) simpleEdit).getVariable2 ();
            Node node2 = probNet.getNode (variable2);
            boolean directed = ((AddLinkEdit) simpleEdit).isDirected ();
            if (!checkLink (probNet, node1, node2, directed))
            {
                return false;
            }
        }
        /**List<PNEdit> edits2 = UtilConstraints.getEditsType (edit, LinkEdit.class);
        for (PNEdit simpleEdit : edits2)
        {
            LinkEdit linkEdit = (LinkEdit) simpleEdit;
            Node node1 = linkEdit.getNode1 ().getNode ();
            Node node2 = linkEdit.getNode2 ().getNode ();
            boolean directed = linkEdit.isDirected ();
            if (linkEdit.isAdd () && !checkLink (graph, node1, node2, directed))
            {
                return false;
            }
        }**/
        List<PNEdit> edits3 = UtilConstraints.getSimpleEditsByType (edit, InvertLinkEdit.class);
        for (PNEdit simpleEdit : edits3)
        {
            Variable variable1 = ((InvertLinkEdit) simpleEdit).getVariable1 ();
            Node node1 = probNet.getNode (variable1);
            Variable variable2 = ((InvertLinkEdit) simpleEdit).getVariable2 ();
            Node node2 = probNet.getNode (variable2);
            boolean directed = ((InvertLinkEdit) simpleEdit).isDirected ();
            if (!checkLink (probNet, node2, node1, directed))
            {
                return false;
            }
        }
        return true;
    }

    /*******
     * Checks if a link between <code>node1</code> and <code>node2</code>
     * satisfies the restriction of distinctLinks
     * @param graph
     * @param node1
     * @param node2
     * @param directed
     * @return code>true</code> if the link between <code>node1</code> and
     *         <code>node2</code>has distinctLinks
     */
    private boolean checkLink (ProbNet graph, Node node1, Node node2, boolean directed)
    {
        return  !((graph.getLink (node1, node2, directed) != null) || 
        		(!directed && graph.getLink (node2, node1, directed) != null));
    }

    @Override
    protected String getMessage ()
    {
        return "No equal links allowed.";
    }
}
