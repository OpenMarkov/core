package org.openmarkov.core.action.core;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.inference.BasicOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.action.base.CompoundPNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;

@SuppressWarnings("serial") public class AbsorbParentsEdit extends CompoundPNEdit {
    private Node node;
    
    public AbsorbParentsEdit(ProbNet probNet, Node node) {
        super(probNet);
        this.node = node;
    }
    
    @Override
    public ArrayList<PNEdit> generateEdits() {
        ArrayList<PNEdit> edits = new ArrayList<>();
        // gets neighbors of this node
        Variable nodeVariable = node.getVariable();
        List<Node> parents = probNet.getParents(node);
        Potential potential = BasicOperations.buildPotentialByAbsorbingParents(node, null);
        for (Node x : parents) {
            PNEdit newEdit;
            if (x.getChildren().size() > 1) {
                newEdit = new RemoveLinkEdit(probNet, x.getVariable(), nodeVariable, true, false);
            } else {// x.getChildren().size() == 1
                newEdit = new CRemoveNodeEdit(probNet, x);
            }
            edits.add(newEdit);
        }
        
        for (Variable variable : potential.getVariables()) {
            if (variable != nodeVariable) {
                AddLinkEdit addLinkEdit = new AddLinkEdit(probNet, variable, nodeVariable, true);
                addLinkEdit.setUpdatePotentials(false);
                edits.add(addLinkEdit);
            }
        }
        edits.add(new SetPotentialEdit(node, potential));
        return edits;
    }
}
