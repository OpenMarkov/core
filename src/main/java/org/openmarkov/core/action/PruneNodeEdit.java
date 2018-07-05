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

@SuppressWarnings("serial") public class PruneNodeEdit extends SimplePNEdit{

    private Variable prunedVariable;

    private Node prunedNode;

    private List<Link<Node>> prunedLinks;

    private List<Link<Node>> newParentLinks;

    private List<Potential> oldUtilityPotentials;

    private List<Potential> newPotentials;

    private boolean isParentOfUtility;

    // Constructor

    /**
     * @param probNet    <code>ProbNet</code>
     * @param prunedVariable  <code>Variable</code>
     */
    public PruneNodeEdit(ProbNet probNet, Variable prunedVariable) {
        super(probNet);
        this.prunedVariable = prunedVariable;
        this.prunedNode = probNet.getNode(prunedVariable);
        this.prunedLinks = new ArrayList<>();
        this.newParentLinks = new ArrayList<>();
    }

    @Override
    public void doEdit() throws DoEditException {


        // If there is a child, it is an utility child.
        isParentOfUtility = (prunedNode.getChildren().size() == 1);

        if (isParentOfUtility) {
            // Update the utility potential
            Node child = prunedNode.getChildren().get(0);
            oldUtilityPotentials = child.getPotentials();
            newPotentials = new ArrayList<>();

            /* Chance parent */
            if (prunedNode.getNodeType() == NodeType.CHANCE) {
                for (Potential potential : oldUtilityPotentials) {

                    // Potentials to multiply
                    List<TablePotential> utilityAndChance = new ArrayList<>();
                    try {
                        utilityAndChance.add(potential.getCPT()); //Utility
                        utilityAndChance.add(prunedNode.getPotentials().get(0).getCPT()); //Chance

                    } catch (NonProjectablePotentialException | WrongCriterionException e) {
                        throw new DoEditException("Potential not convertible to table or wrong criterion");
                        // TODO Make compatible with the new Exception frame
                    }

                    /* Obtain parameters to invoke multiplyAndMarginalize */
                    // All variables from chance parent and utility child potentials
                    List<Variable> unionVariables = AuxiliaryOperations.getUnionVariables(utilityAndChance);

                    List<Variable> variablesToKeep = new ArrayList<>(unionVariables);
                    variablesToKeep.remove(prunedVariable);

                    List<Variable> variablesToEliminate = new ArrayList<>();
                    variablesToEliminate.add(prunedVariable);

                    // Discrete operation is valid because all parents are discrete
                    TablePotential marginalizedPotential = DiscretePotentialOperations.
                            multiplyAndMarginalize(utilityAndChance, variablesToKeep, variablesToEliminate);

                    // Convert to utility potential
                    ExactDistrPotential exactDistrPotential = new ExactDistrPotential(variablesToKeep);
                    exactDistrPotential.setValues(marginalizedPotential.values);

                    newPotentials.add(exactDistrPotential);
                }

                // Parents of chance node are now parents of utility node
                for (Node parent : prunedNode.getParents() ) {
                    Link<Node> link = probNet.getLink(parent, child, true);
                    if (link == null) {
                        // creating the Link saves it in the graph
                        newParentLinks.add(probNet.addLink(parent, child, true));

                    }
                }

            /* Decision parent */
            } else if (prunedNode.getNodeType() == NodeType.DECISION) {
                for (Potential potential : oldUtilityPotentials) {
                    TablePotential utilityPotential;

                    try {
                        utilityPotential = potential.getCPT();
                    } catch (NonProjectablePotentialException | WrongCriterionException e) {
                        throw new DoEditException("Potential not convertible to table or wrong criterion");
                    }

                    // Discrete operation is valid because all parents are discrete
                    TablePotential maximizedPotential = (TablePotential) DiscretePotentialOperations.
                            maximize(utilityPotential, prunedVariable)[0];

                    List<Variable> newVariables = new ArrayList<>(potential.getVariables());
                    newVariables.remove(prunedVariable);

                    ExactDistrPotential exactDistrPotential = new ExactDistrPotential(newVariables);
                    exactDistrPotential.setValues(maximizedPotential.values);

                    newPotentials.add(exactDistrPotential);
                }
                // Parents of decision node don't turn into parents of utility node
            }
            child.setPotentials(newPotentials);
        }

        // Links saved for the undo()
        prunedLinks = getLinksWithNode(prunedNode);
        probNet.removeNode(prunedNode);
    }

    public void undo() {
        super.undo();
        probNet.addNode(prunedNode);
        // Restore pruned links
        if (prunedLinks.size() != 0) {
            for (Link<Node> link : prunedLinks) {
                probNet.addLink(link.getNode1(), link.getNode2(), true);
            }
        }
        if (isParentOfUtility) {
            prunedNode.getChildren().get(0).setPotentials(oldUtilityPotentials);
            // Destroy created utility links
            if (newParentLinks.size() != 0) {
                for (Link<Node> link : newParentLinks) {
                    probNet.removeLink(link.getNode1(), link.getNode2(), true);
                }
            }
        }

    }

    public void redo() {
        super.redo();
        isParentOfUtility = (prunedNode.getChildren().size() == 1);
        if (isParentOfUtility) {
            // Re-create utility links
            if (newParentLinks.size() != 0) {
                for (Link<Node> link : newParentLinks) {
                    probNet.addLink(link.getNode1(), link.getNode2(), true);
                }
            }
            prunedNode.getChildren().get(0).setPotentials(newPotentials);
        }

        probNet.removeNode(prunedNode);


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
