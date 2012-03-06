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

import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UniformPotential;

/**
 *  <code>NodeReplaceStatesEdit</code> is a simple edit that allows modify 
 *  the states of probNode
 *  
 *  @version 1.0 10/05/2011
 *  @author Miguel Palacios
 */

@SuppressWarnings("serial")
public class NodeReplaceStatesEdit extends SimplePNEdit {
	
	//Default increment between discretized intervals
	private final int increment = 2;
	
	 	/**
		 * The current default states of the network
		 */
		private State [] lastStates;
		
		/**
		 * The new default states of the network
		 */
		private State [] newStates;
		
		private ProbNode probNode;

		private ArrayList<Potential> lastPotential;
		
		private ArrayList<Potential> childrenLastPotential = 
			new ArrayList<Potential> () ;

		private PartitionedInterval currentPartitionedInterval;

		
		/**
		 * Creates a <code>NodeReplaceStatesEdit</code> with the node and new
		 * states specified for replace.
		 * 
		 * @param probNet
		 *            the network that will be modified.
		 * @param newDefaulStates 
		 * 			the new default states.
		 */
		public NodeReplaceStatesEdit(ProbNode probNode, State[] newStates) {
			super(probNode.getProbNet());
			this.probNode = probNode;
			
			this.lastStates = probNode.getVariable().getStates();
			this.lastPotential = probNode.getPotentials();
			
			this.currentPartitionedInterval = probNode.getVariable().
				getPartitionedInterval();
			
			
			
			
			this.newStates = newStates;
		}

		// Methods
		@Override
		public void doEdit() {
			if (newStates != null) {
				ArrayList<Node> nodes;
				 probNode.getVariable().setStates(newStates);
				 ArrayList<Potential> newPotentials = new ArrayList<Potential>() ;
				//set uniform potential for the edited node and children if the 
				 //new number of states is different that the last states
				if (newStates.length != lastStates.length){
					UniformPotential newPotential = new UniformPotential(
						lastPotential.get(0).getVariables(),
							lastPotential.get(0).getPotentialRole());
						newPotential.setUtilityVariable(
							lastPotential.get(0).getUtilityVariable());
						newPotentials.add(newPotential);
						probNode.setPotentials(newPotentials);
						
						UniformPotential childLastPotential;
						nodes = probNode.getNode().getChildren();
						
						for (Node node:nodes){
							ArrayList<Potential> container = new ArrayList<Potential> ();
							ProbNode child = (ProbNode)node.getObject();
							childrenLastPotential.add(child.getPotentials().get(0));
							childLastPotential = new UniformPotential(
									child.getPotentials().get(0).getVariables(),
									child.getPotentials().get(0).getPotentialRole());
							//child.setUniformPotential();
							container.add(childLastPotential);
							child.setPotentials(container);
						}
				}
				//TODO revisar el funcionamiento del edit con variables 
				//discretizadas
				//if the node is dicretized add a new row in partitionedInterval 
				//field of the node
				if (probNode.getVariable().getVariableType() == VariableType.
						DISCRETIZED){
					PartitionedInterval newPartitionedInterval = 
						getNewPartitionedInterval();
					probNode.getVariable().setPartitionedInterval(
							newPartitionedInterval);
				}
			}
		}

		public void undo() {
			ArrayList<Node> nodes;
			super.undo();
			if (lastStates != null) {
				probNode.getVariable().setStates(lastStates);
				if ( lastStates.length != newStates.length ){
					
					probNode.setPotentials(lastPotential);
						nodes = probNode.getNode().getChildren();
					int index=0;
					for (Node node:nodes){
						ArrayList<Potential> container = new ArrayList<Potential> ();
						ProbNode child = (ProbNode)node.getObject();
						container.add( childrenLastPotential.get(index));
						child.setPotentials(container);
					}
				}
			}
		}
		private PartitionedInterval getNewPartitionedInterval(){
			double limits [] = currentPartitionedInterval.getLimits();
			double newLimits [] = new double [limits.length + 1];
			boolean belongsToLeftSide [] = currentPartitionedInterval.
				getBelongsToLeftSide();
			boolean newBelongsToLeftSide [] = new boolean [limits.length + 1];
			for (int i = 0; i < limits.length; i++){
				newLimits[ i ] = limits[ i ];
				newBelongsToLeftSide [ i ] = belongsToLeftSide [ i ];
			}
			newLimits[ limits.length ] = currentPartitionedInterval.getMax() + 
				increment; 
			newBelongsToLeftSide [ limits.length ] = false;
			return 	new PartitionedInterval(newLimits, newBelongsToLeftSide);
		}
	}



