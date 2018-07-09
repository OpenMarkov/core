/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author artasom
 * <p>
 * Inverts the arc between two nodes.
 * <p>
 * Being X -> Y the link that is going to be inverted and being:
 * A: the group of nodes that are parents of X and are not parents of Y,
 * C: the group of nodes that are parents of Y (except X) and are not parents of X, and
 * B the group of parents that X and Y share,
 * <p>
 * The process takes five steps:
 * <p>
 * 1. Invert the arc.
 * <p>
 * 2. Share parents between the nodes.
 * <p>
 * 3. 	Calculate P(x, y|a, b, c) through P(x, y|a, b, c) = P(x|a, b) · P(y|x, b, c)
 * Meaning: P(x, y|a, b, c) = pot(x) · pot(y)
 * <p>
 * 4. Calculate P(y|a, b, c) through P(y|a, b, c) = Σ(x) P(x, y|a, b, c) and assign to node Y this probability.
 * <p>
 * 5. Calculate P(x|a, b, c, y) through P(x|a, b, c, y) = P(x, y|a, b, c) / P(y|a, b, c) and assign to node X this probability.
 */
@SuppressWarnings("serial") public class ArcRevertEdit extends BaseLinkEdit {

	// x (parent) node
	private final Node x;
	// y (child) node
	private final Node y;
	// In case of undo, this list will keep the links created so they can be deleted
	private final List<Link> undoLinks = new ArrayList<>();
	// Parent node's old potentials
	private List<Potential> parentsOldPotentials;
	// Child node's old potentials
	private List<Potential> childsOldPotentials;
    // Parent node's new potentials
    private TablePotential xNewPotential;
    // Child node's new potentials
    private TablePotential yNewPotential;

	// Constructor

	/**
	 * @param probNet   <code>ProbNet</code>
	 * @param variable1 <code>Variable</code>
	 * @param variable2 <code>Variable</code>
	 */
	//* @param isDirected <code>boolean</code>
	public ArcRevertEdit(ProbNet probNet, Variable variable1, Variable variable2)
	//boolean isDirected)
	{
		// It will always be directed
		super(probNet, variable1, variable2, true);
		x = probNet.getNode(variable1);
		y = probNet.getNode(variable2);

	}

	// Methods
	@Override
	/** @throws exception <code>Exception</code> */ public void doEdit() throws DoEditException {

		// The parents of x are retrieved
		List<Node> xParents = x.getParents();
		// The parents of y are retrieved
		List<Node> yParents = y.getParents();
		// The nodes will share their parents
		List<Node> newParents;

		// 1. Invert the arc.
		// The link between i and j can be removed
		probNet.removeLink(x, y, true);
		// and the link between j and i can be created
		probNet.addLink(y, x, true);

		// 2. Share parents between the nodes.
		// The list of created links is emptied
		undoLinks.clear();
		// {C(x) \ C(y)} must be parents of y
		// The new parents of y will be those nodes that are parents of x,
		newParents = xParents;
		// and weren't already parents of y
		newParents.removeAll(yParents);

		// The new links are created
		for (Node newParent : newParents) {
			// creating the Link is creating a Link in the node and thus in the graph
			undoLinks.add(new Link(newParent, y, true));
			//probNet.addLink(probNet.getNode(newParent), y, true);
		}

		// {C(y) \ C(x) \ x}
		// The new parents of x will be those nodes that are parents of y,
		newParents = yParents;
		// and weren't already parents of i
		newParents.removeAll(xParents);
		// excluding also the i node itself
		newParents.remove(x);

		// The new links are created
		for (Node newParent : newParents) {
			// creating the Link is creating a Link in the node and thus in the graph
			undoLinks.add(new Link(newParent, x, true));
			//probNet.addLink(probNet.getNode(newParent), x, true);
		}

		List<TablePotential> xyPotentials = new ArrayList<>();

		parentsOldPotentials = x.getPotentials();
		childsOldPotentials = y.getPotentials();

		// TODO Potentials should be converted if necessary
		// potential.tableProject(new EvidenceCase(), new InferenceOptions());

		// 3. 	Calculate P(x, y|a, b, c) through P(x, y|a, b, c) = P(x|a, b) · P(y|x, b, c)
		// Meaning: P(x, y|a, b, c) = pot(x) · pot(y)

		// pot(x) are added to xyPotentials
		for (Potential parentsOldPotential : parentsOldPotentials) {

			try {
				xyPotentials.add(parentsOldPotential.getCPT());
			} catch (NonProjectablePotentialException | WrongCriterionException e) {
				e.printStackTrace();
				throw new DoEditException("Parent");
			}
		}

		// pot(y) are added to xyPotentials
		for (Potential childsOldPotential : childsOldPotentials) {
			try {
				xyPotentials.add(childsOldPotential.getCPT());
			} catch (NonProjectablePotentialException | WrongCriterionException e) {
				e.printStackTrace();
				throw new DoEditException("Child");
			}
		}

		// Correct order of variables
		Set<Variable> variables = new LinkedHashSet<>();
		// The first variable from each factor should go before the others
		for (Potential potential: xyPotentials) {
			if (potential.getNumVariables() > 0) {
				variables.add(potential.getVariable(0));
			}
		}
		for (Potential potential : xyPotentials) {
			variables.addAll(potential.getVariables());
		}
		List<Variable> orderedVariables = new ArrayList<>(variables);

		// xyPotentials are multiplied
		TablePotential xyPotentialMultiplied = DiscretePotentialOperations.multiply(xyPotentials);

		// Apply the correction of the order of the variables: Σ(x) P(x, y|a, b, c) = P(a|b, y, c) to Σ(x) P(x, y|a, b, c) = P(y|a, b, c)
		xyPotentialMultiplied = DiscretePotentialOperations.reorder(xyPotentialMultiplied,
				new ArrayList<>(orderedVariables));
		System.out.println(orderedVariables);
		System.out.println((xyPotentialMultiplied));

		// 4. Calculate P(y|a, b, c) through P(y|a, b, c) = Σ(x) P(x, y|a, b, c) and assign to node Y this probability.
		yNewPotential = DiscretePotentialOperations.marginalize(xyPotentialMultiplied, x.getVariable());
		y.setPotential(yNewPotential);

		// 5. Calculate P(x|a, b, c, y) through P(x|a, b, c, y) = P(x, y|a, b, c) / P(y|a, b, c) and assign to node X this probability.
		xNewPotential = DiscretePotentialOperations.divide(xyPotentialMultiplied, yNewPotential);
		x.setPotential(xNewPotential);

		for (Link link : undoLinks) {
			probNet.addLink((Node) link.getNode1(), (Node) link.getNode2(), true);
		}
	}

	public void undo() {
		super.undo();
		try {
			// Delete link Y -> X
			probNet.removeLink(variable2, variable1, isDirected);
			// Re-create link X -> Y
			probNet.addLink(variable1, variable2, isDirected);
			// Delete the links created when the nodes shared their fathers
			for (Link<Node> undoLink : undoLinks) {
				probNet.removeLink(undoLink.getNode1(), undoLink.getNode2(), true);
			}
			// The potentials of X are restored to the original ones
			x.setPotentials(parentsOldPotentials);
			// The potentials of Y are restored to the original ones
			y.setPotentials(childsOldPotentials);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}


    public void redo() {
        super.redo();
        // TODO See if redos are really necessary
        /*
        try {
            // Re-remove link X -> Y
            probNet.addLink(variable1, variable2, isDirected);
            // Recreate link Y -> X
            probNet.addLink(variable2, variable1, isDirected);
            // Re-created the links of shared fathers
            for (Link<Node> undoLink : undoLinks) {
                probNet.addLink(undoLink.getNode1(), undoLink.getNode2(), true);
            }
            // The potentials of X are restored to the original ones
            x.setPotential(xNewPotential);
            // The potentials of Y are restored to the original ones
            y.setPotential(yNewPotential);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        */
    }

	/**
	 * Method to compare two InvertLinkEdits comparing the names of
	 * the source and destination variable alphabetically.
	 *
	 * @param obj
	 * @return
	 */
	public int compareTo(ArcRevertEdit obj) {
		int result;

		if ((
				result = variable1.getName().compareTo(obj.getVariable1().
						getName())
		) != 0)
			return result;
		if ((
				result = variable2.getName().compareTo(obj.getVariable2().
						getName())
		) != 0)
			return result;
		else
			return 0;
	}

	@Override public String getOperationName() {
		return "Revert arc";
	}

	/**
	 * This method assumes that the link is directed, otherwise has no sense.
	 *
	 * @return <code>String</code>
	 */
	public String toString() {
		return "Reverse arc: " + variable1 + "-->" + variable2 + " ==> " + variable1 + "<--" + variable2;
	}

	@Override public BaseLinkEdit getUndoEdit() {
		return new ArcRevertEdit(getProbNet(), getVariable2(),
				getVariable1()); //, isDirected () is always true in this case
	}

}