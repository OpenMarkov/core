/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network;

import java.util.ArrayList;

import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.constraint.OnlyDiscreteVariables;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;
import org.openmarkov.core.model.network.potential.Potential;


/** Util class of Markov networks.
 * Constraints:
 * <ol>
 * <li>OnlyUndirectedLinks
 * </ol>
 * @author marias */
public class UtilMarkovNet {

	// TODO eliminar este metodo
	/** @param probNet <code>ProbNet</code> possibly with directed links
	 * @return A Markov Network in witch directed links are converted to 
	 *  undirected links. 
	 * @throws NotEnoughMemoryException */
	public static ProbNet getMarkovNet(ProbNet probNet) 
			throws NotEnoughMemoryException {
		return buildMarkovNet(probNet.getPotentials());
	}
	
	/** Adds the variables in the received <code>Potential</code> to this 
	 *   <code>MarkovNet</code>, creates links between those variables creating
	 *   cliques and assigns the <code>potential</code> to the conditioned
	 *   variable (the first one).
	 * @argCondition At least one potential depends on at least one variable
	 * (otherwise the network would have no node, and it would be impossible
	 * to assign constant potentials)
	 * @param projectedTablePotentials <code>ArrayList</code> of 
	 *   <code>Potential</code>s
	 * @return A Markov Network in witch potentials are used to create cliques.
	 *   (<code>ProbNet</code>). 
	 * @throws NotEnoughMemoryException */
	public static ProbNet buildMarkovNet(
			ArrayList<? extends Potential> projectedTablePotentials) 
			throws NotEnoughMemoryException {
		ProbNet markovNet = getMarkovNet();
		try {
            markovNet.addConstraint (new OnlyDiscreteVariables (), false);
		} catch (ConstraintViolationException e) {
			// Unreachable code
			e.printStackTrace();
		}
    	for (Potential potential : projectedTablePotentials) {
    		addPotential(markovNet, potential);
    	}
		return markovNet;
	}

	/**
	 * Adds the received potential to the list of potentials of the first 
	 * variable.
	 * 
	 * @preCondition network contains at least one variable
	 * @argCondition If A is the first variable in the potential and
	 *               B<sub>0</sub> ... B<sub>n</sub> are the others, there must
	 *               be a directed link B<sub>i</sub> -> A for every variable
	 *               B<sub>i</sub> in the potential (other than A)
	 * @param potential
	 *            . <code>Potential</code>
	 * @return The <code>ProbNode</code> in which the <code>potential</code>
	 *         received has been added.
	 */
	public static void addPotential(ProbNet markovNet, Potential potential) {
		ArrayList<Variable> potentialVariables = potential.getVariables();
		// the probNode where the potential will be stored
		// TODO hacerlo con edits
		if (potential.getVariables().size() == 0) {
			// it is a constant potential;
			// adds it to any variable of the network
			markovNet.getProbNodes().get(0).addPotential(potential);
		} else {
			// the potential depends on several variables
			for (Variable variable : potentialVariables) {
				if (markovNet.getProbNode(variable) == null) {
					// in a Markov net all nodes are treated as if they were
					// CHANCE
					markovNet.addVariable(variable, NodeType.CHANCE);
				}
			}
			markovNet.getProbNode(potentialVariables.get(0)).
				addPotential(potential);
			int numVariables = potentialVariables.size();
			for (int i = 0; i < numVariables - 1; i++) {
				Variable variable1 = potentialVariables.get(i);
				for (int j = i + 1; j < numVariables; j++) {
					Variable variable2 = potentialVariables.get(j);
					try {
						markovNet.addLink(variable1, variable2, false);
					} catch (NodeNotFoundException e) {
						// Unreachable code because the variables are in the net
					}
				}
			}
		}
	}
	

	/** @return An empty Markov Network. <code>ProbNet</code> */
	public static ProbNet getMarkovNet() {
		ProbNet probNet = new ProbNet();
		try {
            probNet.addConstraint (new OnlyUndirectedLinks (), true);
		} catch (ConstraintViolationException e) {
			System.err.println(e.getStackTrace()); // Unreachable code
		}
		return probNet;		
	}

	// TODO eliminar 
	/** @return <code>true</code> if the received <code>probNet</code> has all
	 *   the restrictions applied to a Markov network.
	 * @param probNet <code>ProbNet</code> */
	public static boolean isMarkovNet(ProbNet probNet) {
        return probNet.getConstraints ().contains (new OnlyUndirectedLinks ());
	}
	
}
