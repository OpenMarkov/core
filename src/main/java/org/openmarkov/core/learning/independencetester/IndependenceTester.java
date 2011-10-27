package org.openmarkov.core.learning.independencetester;

import java.util.ArrayList;

import org.openmarkov.core.action.PNUndoableEditListener;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.learning.editionsgenerator.EditAndScorePair;
import org.openmarkov.core.model.graph.Node;

/**
 * This interface represents a general independence tester.
 * @author joliva
 *
 */
public interface IndependenceTester extends PNUndoableEditListener{
	
	/**
	 * Tests whether two variables are independent or not.
	 * @param node1 <code>Node</code> first variable.
	 * @param node2 <code>Node</code> second variable.
	 * @param adjacencySubset <code>ArrayList</code> of <code>Node</code> 
	 * representing the separation set (i.e. the conditional set).
	 * @return <code>EditAndScorePair</code> representing the edition
	 * (RemoveLinkEdit) between the two variables and the score obtained
	 * in the independence test.
	 * @throws ProbNodeNotFoundException
	 */
	public EditAndScorePair independents(Node node1, Node node2, 
			ArrayList<Node> adjacencySubset)
					throws ProbNodeNotFoundException;
}
