package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.AuxiliaryOperations;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") public class AbsorbNodeEdit extends SimplePNEdit{

    // Both node and variable attributes are created for convenience but one could be extracted from the other
    private Variable absorbedVariable;

    private Node absorbedNode;

    /* Undo attributes */
    private List<Link<Node>> linksDeleted;

    private List<Link<Node>> newParentLinks;

    private List<Potential> oldUtilityPotentials;

    /* Redo attributes */
    private List<Potential> newPotentials;

    // Constructor

    /**
     * @param probNet    <code>ProbNet</code>
     * @param absorbedVariable  <code>Variable</code>
     */
    public AbsorbNodeEdit(ProbNet probNet, Variable absorbedVariable) {
        super(probNet);
        this.absorbedVariable = absorbedVariable;
        this.absorbedNode = probNet.getNode(absorbedVariable);
        this.linksDeleted = new ArrayList<>();
        this.newParentLinks = new ArrayList<>();
    }

    @Override
    public void doEdit() throws DoEditException {


            // Update the utility potential
            Node child = absorbedNode.getChildren().get(0);
            oldUtilityPotentials = child.getPotentials();
            newPotentials = new ArrayList<>();

            /* Chance parent */
            if (absorbedNode.getNodeType() == NodeType.CHANCE) {
                for (Potential potential : oldUtilityPotentials) {

                    // Potentials to multiply
                    List<TablePotential> utilityAndChance = new ArrayList<>();
                    try {
                        utilityAndChance.add(potential.getCPT()); //Utility
                        utilityAndChance.add(absorbedNode.getPotentials().get(0).getCPT()); //Chance

                    } catch (NonProjectablePotentialException | WrongCriterionException e) {
                        throw new DoEditException("Potential not convertible to table or wrong criterion");
                        // TODO Make compatible with the new Exception frame
                    }

                    /* Obtain parameters to invoke multiplyAndMarginalize */
                    // All variables from chance parent and utility child potentials
                    List<Variable> unionVariables = AuxiliaryOperations.getUnionVariables(utilityAndChance);

                    List<Variable> variablesToKeep = new ArrayList<>(unionVariables);
                    variablesToKeep.remove(absorbedVariable);

                    List<Variable> variablesToEliminate = new ArrayList<>();
                    variablesToEliminate.add(absorbedVariable);

                    // Discrete operation is valid because all parents are discrete
                    TablePotential marginalizedPotential = DiscretePotentialOperations.
                            multiplyAndMarginalize(utilityAndChance, variablesToKeep, variablesToEliminate);

                    // Convert to utility potential
                    ExactDistrPotential exactDistrPotential = new ExactDistrPotential(variablesToKeep);
                    exactDistrPotential.setValues(marginalizedPotential.values);

                    newPotentials.add(exactDistrPotential);
                }

                // Parents of chance node are now parents of utility node
                for (Node parent : absorbedNode.getParents() ) {
                    Link<Node> link = probNet.getLink(parent, child, true);
                    if (link == null) {
                        // creating the Link saves it in the graph
                        newParentLinks.add(probNet.addLink(parent, child, true));

                    }
                }

            /* Decision parent */
            } else if (absorbedNode.getNodeType() == NodeType.DECISION) {
                for (Potential potential : oldUtilityPotentials) {
                    TablePotential utilityPotential;

                    try {
                        utilityPotential = potential.getCPT();
                    } catch (NonProjectablePotentialException | WrongCriterionException e) {
                        throw new DoEditException("Potential not convertible to table or wrong criterion");
                        // TODO Make compatible with the new Exception frame
                    }

                    // Discrete operation is valid because all parents are discrete
                    TablePotential maximizedPotential = (TablePotential) DiscretePotentialOperations.
                            maximize(utilityPotential, absorbedVariable)[0];

                    List<Variable> newVariables = new ArrayList<>(potential.getVariables());
                    newVariables.remove(absorbedVariable);

                    // Convert to utility potential
                    ExactDistrPotential exactDistrPotential = new ExactDistrPotential(newVariables);
                    exactDistrPotential.setValues(maximizedPotential.values);

                    newPotentials.add(exactDistrPotential);
                }
                // Parents of decision node don't turn into parents of utility node
            }
            child.setPotentials(newPotentials);


        // Links saved for the undo()
        linksDeleted = getLinksWithNode(absorbedNode);
        probNet.removeNode(absorbedNode);
        System.out.println("I'm messing with you");
    }

    public void undo() {
        super.undo();
        probNet.addNode(absorbedNode);
        // Restore deleted links
        if (linksDeleted.size() != 0) {
            for (Link<Node> link : linksDeleted) {
                probNet.addLink(link.getNode1(), link.getNode2(), true);
            }
        }

            absorbedNode.getChildren().get(0).setPotentials(oldUtilityPotentials);
            // Destroy created utility links
            if (newParentLinks.size() != 0) {
                for (Link<Node> link : newParentLinks) {
                    probNet.removeLink(link.getNode1(), link.getNode2(), true);
                }
            }


    }

    // TODO
    public void redo() {
        super.redo();
        /* The super already does the doEdit
        Node child = absorbedNode.getChildren().get(0);
        // Re-create utility links
            if (newParentLinks.size() != 0) {
                for (Link<Node> link : newParentLinks) {
                    probNet.addLink(link.getNode1(), link.getNode2(), true);
                }
            }
            absorbedNode.getChildren().get(0).setPotentials(newPotentials);


        probNet.removeNode(absorbedNode);
        */
    }

    /*
     * Returns all the incoming and outcoming links of a given node.
     */
    private ArrayList<Link<Node>> getLinksWithNode(Node node) {
        ArrayList<Link<Node>> links = new ArrayList<>();
        for (Link<Node> link : probNet.getLinks() ) {
            if (link.contains(node)) {
                links.add(link);
            }
        }
        return links;
    }

}
