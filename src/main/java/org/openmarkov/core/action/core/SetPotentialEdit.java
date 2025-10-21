/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.PotentialManager;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.VisualDecisionNodePolicyChangeListener;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial") public class SetPotentialEdit extends PNEdit {
	// unused - private PotentialType lastPotentialType;
	private Potential lastPotential;
	private String newPotentialType;
	// private ICIModelType newICIModelType;
	private Variable variable;
	private Potential newPotential = null;
	private Node node;
	private VisualDecisionNodePolicyChangeListener listener;
	private Boolean hasPolicy;

	/**
	 * Creates a new SetPotentialEdit object that sets the a new potential with
	 * the type specified for the node object.
	 *
	 * @param node             The node that contains the potential to modify
	 * @param newPotentialType The potential type of the new potential to be created
	 */
	public SetPotentialEdit(Node node, String newPotentialType) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		//if (!(node.getNodeType() == NodeType.DECISION && node
		//	.getPolicyType() == PolicyType.OPTIMAL)) {
		lastPotential = node.getPotentials().get(0);
		//	}

		this.newPotentialType = newPotentialType;

	}

	/**
	 * SetPotentialEdit object that changes the last Potential with the
	 * potential specified for the node object.
	 *
	 * @param node      The node that contains the potential to set.
	 * @param potential The new potential object
	 */
	public SetPotentialEdit(Node node, Potential potential) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
        if (!node.getPotentials().isEmpty()) {// if node is a decision node it could not have a potential assigned yet
			lastPotential = node.getPotentials().get(0);
		}

		newPotential = potential;
		this.newPotentialType = newPotential.getClass().getAnnotation(PotentialType.class).name();
	}

	public SetPotentialEdit(Node node, Potential lastPotential, Potential newPotential){
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		this.lastPotential = lastPotential;


		this.newPotential = newPotential;
		this.newPotentialType = newPotential.getClass().getAnnotation(PotentialType.class).name();
	}

	public SetPotentialEdit(Node node, String newPotentialType, Potential lastPotential,Boolean hasPolicy, VisualDecisionNodePolicyChangeListener listener) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		this.lastPotential = lastPotential;
		this.listener = listener;
		this.hasPolicy = hasPolicy;

		this.newPotentialType = newPotentialType;

	}
	public SetPotentialEdit(Node node, Potential lastPotential, Potential newPotential,Boolean hasPolicy, VisualDecisionNodePolicyChangeListener listener) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		this.lastPotential = lastPotential;
		this.listener = listener;
		this.hasPolicy = hasPolicy;

		this.newPotential = newPotential;

	}

	// TODO al asignar un potencial tener en cuenta a los padres y a los
	// predecesores informativos que me los va a dar Manolo invocando a una
	// funcion
	@Override public void doEdit() {
		setPotential();
		if (node.getNodeType() == NodeType.DECISION && listener != null){

			listener.onNodeValueChanged();

		}

	}
    
    public void setPotential(){
        List<Variable> variables = lastPotential.getVariables();
        PotentialRole role = lastPotential.getPotentialRole();

		List<Potential> potentials = new ArrayList<>();
		if (newPotential == null) {
			PotentialManager relationTypeManager = new PotentialManager();

			if (newPotentialType.equals(PotentialManager.getPotentialName(CycleLengthShift.class))) {
				newPotential = relationTypeManager
						.getByName(newPotentialType, variables, role, probNet.getCycleLength());
			} else {
				newPotential = relationTypeManager.getByName(newPotentialType, variables, role);
			}
		}

		if (!(node.getNodeType() == NodeType.DECISION && node.getPolicyType() == PolicyType.OPTIMAL)) {
			//	probNet.getNode(variable).setPolicyType(PolicyType.PROBABILISTIC);
			node.setPolicyType(PolicyType.PROBABILISTIC);
		}


		potentials.add(newPotential);
		potentials.add(lastPotential);
		//probNet.getNode(variable).setPotentials(potentials);
		node.setPotentials(potentials);
		// update potential with link restriction
		if (newPotential instanceof TablePotential && node.getNodeType() != NodeType.DECISION) {
			newPotential = LinkRestrictionPotentialOperations.updatePotentialByLinkRestrictions(node);
			potentials = new ArrayList<>();
			potentials.add(lastPotential);
			potentials.add(newPotential);
			node.setPotentials(potentials);
			//probNet.getNode(variable).setPotentials(potentials);
		}
	}
	public void setInitialChange(){
        List<Variable> variables = lastPotential.getVariables();
        PotentialRole role = lastPotential.getPotentialRole();

		List<Potential> potentials = new ArrayList<>();
		if (newPotential == null) {
			PotentialManager relationTypeManager = new PotentialManager();

			if (newPotentialType.equals(PotentialManager.getPotentialName(CycleLengthShift.class))) {
				newPotential = relationTypeManager
						.getByName(newPotentialType, variables, role, probNet.getCycleLength());
			} else {
				newPotential = relationTypeManager.getByName(newPotentialType, variables, role);
			}
		}

		if (!(node.getNodeType() == NodeType.DECISION && node.getPolicyType() == PolicyType.OPTIMAL)) {
			node.setPolicyType(PolicyType.PROBABILISTIC);
		}


		potentials.add(newPotential);
		node.setPotentials(potentials);

	}

	@Override public void undo() {
		super.undo();
		Node node = probNet.getNode(variable);
		List<Potential> potentials = new ArrayList<>();
		if (lastPotential != null) {
			if (node.getNodeType() == NodeType.DECISION){
				if (hasPolicy) {
					potentials.add(lastPotential);
					listener.onNodeValueChanged();
				}else {
					listener.removePolicy();
				}
			}else{
				potentials.add(lastPotential);
			}

		} else if (node.getNodeType() == NodeType.DECISION) {
			node.setPolicyType(PolicyType.OPTIMAL);

		}
		node.setPotentials(potentials);
	}

	@Override
	public void redo() {
		super.redo();
		Node node = probNet.getNode(variable);
		List<Potential> potentials = new ArrayList<>();
		potentials.add(newPotential);
		node.setPotentials(potentials);
	}

	public Potential getNewPotential() {
		return newPotential;
	}

	public String getNewPotentialType() {
		return newPotentialType;
	}

	public Node getNode() {
		return node;
	}

}
