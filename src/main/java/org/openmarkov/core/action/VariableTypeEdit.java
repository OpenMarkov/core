/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.OnlyDiscreteVariables;
import org.openmarkov.core.model.network.constraint.OnlyFiniteStatesVariables;
import org.openmarkov.core.model.network.constraint.OnlyNumericVariables;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.operation.Util;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") public class VariableTypeEdit extends SimplePNEdit {
	// private ProbNet probNet;
	private Node node;
	private VariableType newType;
	private VariableType currentType;
	private State[] currentStates;

	public VariableTypeEdit(Node node, VariableType newType) {
		super(node.getProbNet());
		this.node = node;
		this.newType = newType;
		this.currentType = node.getVariable().getVariableType();

	}
    
    @Override public void checkConstraintsWillBeMet() throws DoEditException.ConstraintViolated {
        if (probNet.getConstraintOfClass(OnlyDiscreteVariables.class) instanceof OnlyDiscreteVariables constraint) {
            if (this.newType != VariableType.DISCRETIZED) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(OnlyFiniteStatesVariables.class) instanceof OnlyFiniteStatesVariables constraint) {
            if (!OnlyFiniteStatesVariables.nodeIsFinite(this.node.getNodeType(), this.newType)) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
        if (probNet.getConstraintOfClass(OnlyNumericVariables.class) instanceof OnlyNumericVariables constraint) {
            if (this.newType != VariableType.NUMERIC) {
                throw new DoEditException.ConstraintViolated(constraint);
            }
        }
    }
    
    @Override public void doEdit() {
        // Save the current states
        currentStates = node.getVariable().getStates();

        // Set the new variable type
        node.getVariable().setVariableType(newType);

        // Restore the states
        node.getVariable().setStates(currentStates.length == 1
                ? node.getProbNet().getDefaultStates()
                : currentStates);

        if (currentType != newType) {
            boolean needsUniformPotential = false;

            switch (currentType) {
                case FINITE_STATES:
                case DISCRETIZED:
                    if (newType == VariableType.NUMERIC) {
                        setPotentialsNodeAndChildren();
                    }
                    break;

                case NUMERIC:
                    setPotentialsNodeAndChildren();

                    if (newType == VariableType.DISCRETIZED
                            && node.getVariable().getPartitionedInterval().getNumSubintervals() == 1) {

                        // If there is only one interval, set default partitioned interval
                        PartitionedInterval interval = new PartitionedInterval(
                                node.getVariable().getDefaultInterval(node.getVariable().getNumStates()),
                                Variable.getDefaultBelongs(node.getVariable().getNumStates())
                        );
                        node.getVariable().setPartitionedInterval(interval);
                    }

                    // Mark to set uniform potential later
                    needsUniformPotential = true;
                    break;

                default:
                    break;
            }

            if (needsUniformPotential) {
                // Build the list of variables (node and its parents)
                List<Variable> variables = new ArrayList<>();
                if (node.getNodeType() != NodeType.UTILITY) {
                    variables.add(node.getVariable());
                }
                for (Node parent : probNet.getParents(node)) {
                    variables.add(parent.getVariable());
                }

                // Create and assign uniform potential
                UniformPotential uniformPotential = new UniformPotential(
                        variables,
                        node.getPotentials().get(0).getPotentialRole()
                );

                List<Potential> potentials = new ArrayList<>(1);
                potentials.add(uniformPotential);
                node.setPotentials(potentials);
                node.setUniformPotential();
            }
        }


        resetLink(node);

	}
	
	@Override public void doEdit(ProbNet probNet) throws DoEditException.ConstraintViolated {
        this.checkConstraintsWillBeMet();
		PNEdit.startEdit(this, probNet);
		this.doEdit();
		PNEdit.endEdit(this);
	}

	@Override public void undo() {
		node.getVariable().setVariableType(currentType);
		node.getVariable().setStates(currentStates);
	}

	public VariableType getNewVariableType() {
		return newType;
	}

	public Node getNode() {

		return this.node;
	}

	/****
	 * This method resets the link restriction of the links of the node
	 *
	 * @param node Node
	 */
	private void resetLink(Node node) {

		List<Node> children = probNet.getChildren(node);
		for (Node child : children) {
			Link<Node> link = probNet.getLink(node, child, true);
			if (link.hasRevealingConditions()) {
				link.setRevealingIntervals(new ArrayList<PartitionedInterval>());
				link.setRevealingStates(new ArrayList<State>());
			}
		}

		for (Link<Node> link : probNet.getLinks(node)) {
			if (link.hasRestrictions()) {
				link.setRestrictionsPotential(null);
			}

		}
	}
    
    public static void setUniformPotential2Node(Node node) {

		List<Potential> newListPotentials = new ArrayList<>();
		List<Variable> variables = new ArrayList<>();
        List<Potential> potentials = node.getPotentials();
		PotentialRole role = potentials.get(0).getPotentialRole();
		// first, this variable. The potentials is not null
        Variable thisVariable = potentials.get(0).getVariable(0);
		variables.add(thisVariable);

		int numOfCellsInTable = thisVariable.getNumStates();
		double initialValue = Util.round(1 / ((double) numOfCellsInTable), "0.01");
		// add now all the parents

		for (Node parent : node.getParents()) {
			// TODO Revisar, ¿Solo se agrega/elimina un padre a la vez?
			// mpalacios
			// the set of variables could be changed, so , have to be updated.
			variables.add(parent.getVariable());
			numOfCellsInTable *= parent.getVariable().getNumStates();
		}
        
        //TODO: This array is never used. Why is it created then?
		// sets a new table with new columns and with all the same values
		double[] table = new double[numOfCellsInTable];
		for (int i = 0; i < numOfCellsInTable; i++) {
			table[i] = initialValue;
		}
        
        // and finally, create the potential and the list of potentials

		// TODO Comprobar que efectivamente es un CONDITIONAL_PROBABILITY
		UniformPotential uniformPotential = new UniformPotential(variables, role);

		newListPotentials.add(uniformPotential);

		node.setPotentials(newListPotentials);

	}

	private void setPotentialsNodeAndChildren() {
		// from numeric to finite states or discretized or vice versa
		// if child is utility to potential to be set depends on the
		// type of the other parents
		// it is not always uniform
		setUniformPotential2Node(node);
		for (Node child : probNet.getChildren(node)) {
			if (child.getNodeType() == NodeType.UTILITY) {
				List<Potential> newPotentials = new ArrayList<>();
				if (child.onlyNumericalParents()) {// utility and
					// numerical parents
					// sum
					for (Potential oldPotential : child.getPotentials()) {
						// Update potential
						Potential newPotential = new SumPotential(oldPotential.getVariables(),
								oldPotential.getPotentialRole());
						newPotentials.add(newPotential);
					}
				} else if (!child.onlyNumericalParents()) {// mixture of
					// finite
					// states
					// and
					// numerical
					// Uniform
					for (Potential oldPotential : child.getPotentials()) {
						// Update potential
						Potential newPotential = new UniformPotential(oldPotential.getVariables(),
								oldPotential.getPotentialRole());
						newPotentials.add(newPotential);
					}
				}
				child.setPotentials(newPotentials);
			} else {
				// if child is not utility always change potential to Uniform
				// 25/11/2014
				// If there are any potentials in the child
				// Example. In the "ID-decide-test" network, if you change the Domain of Result of test variable,
				// no potential should be set to Therapy
				if (child.getPotentials() != null) {
                    if (!child.getPotentials().isEmpty()) {
						setUniformPotential2Node(child);
					}
				}
			}
		}
	}

}
