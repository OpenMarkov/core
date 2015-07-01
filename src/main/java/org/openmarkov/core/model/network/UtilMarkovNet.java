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
import java.util.Collection;
import java.util.List;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.type.MarkovNetworkType;


/** Util class of Markov networks.
 * Constraints:
 * <ol>
 * <li>OnlyUndirectedLinks
 * </ol>
 * @author marias */
public class UtilMarkovNet {

	/**
	 * @param probNet. <code>ProbNeta</code>
	 * @return A Markov decision network without the OnlyUndirectedLinks constraint. <code>ProbNeta</code> 
	 */
	public static ProbNet getMarkovDecisionNetworkWithoutConstraints(ProbNet probNet) {
		ProbNet markovDecisionNetwork = new ProbNet(MarkovNetworkType.getUniqueInstance());
		Collection<Potential> potentials = probNet.getPotentials();
		List<Potential> constantPotentials = new ArrayList<Potential>();
		Node lastNodeAdded = null; // This node will contain the last variable added to the markovDecisionNetwork
		
		for (Potential potential : potentials) {
			List<Variable> potentialVariables = potential.getVariables();
			if (potentialVariables.size() == 0) {
				// it is a constant potential, that can be added to any variable, 
				// but we wait until the end because if it is found one of these potentials at the beginning, 
				// the network could be empty of nodes;
				constantPotentials.add(potential);
			} else {
				
				// Add variables when they do not exist in the Markov decision network
				for (Variable variable : potentialVariables) {
					if (!markovDecisionNetwork.containsVariable(variable)) {
						Node probNetNode = probNet.getNode(variable);
						NodeType nodeType = probNetNode.getNodeType();
						lastNodeAdded = markovDecisionNetwork.addNode(variable, nodeType);
					}
				}
				
				// Add the potential to the node that contains the first variable of the potential
				markovDecisionNetwork.getNode(potentialVariables.get(0)).addPotential(potential);

				// Add undirected links when they do not exist in the Markov decision network
				int numVariables = potentialVariables.size();
				for (int i = 0; i < numVariables - 1; i++) {
					Variable variable1 = potentialVariables.get(i);
					for (int j = i + 1; j < numVariables; j++) {
						Variable variable2 = potentialVariables.get(j);
						try {
							markovDecisionNetwork.addLink(variable1, variable2, false);
						} catch (NodeNotFoundException e) {
							// Unreachable code because the variables variable1 and variable2 exists in the network
							System.err.println(e.getMessage());
							System.err.println(e.getStackTrace());
						}
					}
				}
				
			}
		}
		
		// Add constant potentials
		if (lastNodeAdded != null) { // This condition ensures that the original probNet contains at least one node 
			for (Potential constantPotential : constantPotentials) {
				lastNodeAdded.addPotential(constantPotential);
			}
		}

		return markovDecisionNetwork;
	}
	
}
