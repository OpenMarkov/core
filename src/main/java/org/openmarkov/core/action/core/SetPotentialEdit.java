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

/**
 * Edit operation that sets or replaces the {@link Potential} associated with a
 * {@link Node} in a {@link ProbNet}. <br>
 * <p>
 * This edit:
 * <ul>
 *     <li>Creates or replaces the potential of a node using either a potential
 *     type name or an explicit {@link Potential} instance.</li>
 *     <li>Maintains a reference to the previous potential ({@code lastPotential})
 *     in order to support {@link #undo()} and {@link #redo()}.</li>
 *     <li>Updates decision node policy type (e.g. from
 *     {@link PolicyType#OPTIMAL} to {@link PolicyType#PROBABILISTIC}) when
 *     appropriate.</li>
 *     <li>For non-decision nodes, updates the potential according to link
 *     restrictions via {@link LinkRestrictionPotentialOperations} when the new
 *     potential is a {@link TablePotential}.</li>
 *     <li>Optionally notifies a {@link VisualDecisionNodePolicyChangeListener}
 *     when the policy of a decision node changes.</li>
 * </ul>
 */public class SetPotentialEdit extends PNEdit {
	// unused - private PotentialType lastPotentialType;
	 
    /** Previous potential associated with the node, used for undo. */
	private Potential lastPotential;
	
    /**
     * Type name of the new potential to create, as used by
     * {@link PotentialManager#getByName(String, List, PotentialRole, Object...)}.
     */
	private String newPotentialType;
	
	// private ICIModelType newICIModelType;
	
	/** Variable associated with the node whose potential is edited. */
	private Variable variable;
	
	/** New potential to be set in the node (may be created lazily). */
	private Potential newPotential = null;
	
	/** Node whose potential is being edited. */
	private Node node;
	
    /**
     * Listener used to notify visual changes when the policy of a decision node
     * is modified.
     */
	private VisualDecisionNodePolicyChangeListener listener;
	
	 /**
     * Flag indicating whether the decision node had a policy before the edit.
     * Used to restore the previous state on {@link #undo()}.
     */
	private Boolean hasPolicy;

    /**
     * Creates a new {@code SetPotentialEdit} that will replace the current
     * potential of the given node by a new potential of the specified type.
     * The new potential will be created lazily in {@link #setPotential()} using
     * {@link PotentialManager}.
     *
     * @param node             the node whose potential will be modified.
     * @param newPotentialType the type name of the new potential to be created.
     *                         It must match a name known by {@link PotentialManager}.
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
     * Creates a new {@code SetPotentialEdit} that will replace the current
     * potential of the given node by the specified {@link Potential} instance.
     *
     * @param node      the node whose potential will be modified.
     * @param potential the new potential to assign to the node.
     *                  Its {@link PotentialType} annotation is used to
     *                  determine {@link #newPotentialType}.
     */
	public SetPotentialEdit(Node node, Potential potential) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		   // If node is a decision node it may have no potential assigned yet.
        if (!node.getPotentials().isEmpty()) {		
        	lastPotential = node.getPotentials().get(0);
		}

		newPotential = potential;
		this.newPotentialType = newPotential.getClass().getAnnotation(PotentialType.class).name();
	}

    /**
     * Creates a new {@code SetPotentialEdit} specifying explicitly both the last
     * potential and the new potential. This constructor is useful when the
     * previous potential is not necessarily the first element in
     * {@link Node#getPotentials()}.
     *
     * @param node         the node whose potential will be modified.
     * @param lastPotential the previous potential to be restored on {@link #undo()}.
     * @param newPotential  the new potential to assign to the node.
     */	
	public SetPotentialEdit(Node node, Potential lastPotential, Potential newPotential) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		this.lastPotential = lastPotential;
		this.newPotential = newPotential;
		this.newPotentialType = newPotential.getClass().getAnnotation(PotentialType.class).name();
	}
	
    /**
     * Creates a new {@code SetPotentialEdit} that will create a new potential of
     * the given type and replace the last potential. This constructor also
     * includes information about whether the decision node had a policy and a
     * listener to notify visual changes.
     *
     * @param node             the node whose potential will be modified.
     * @param newPotentialType the type name of the new potential to be created.
     * @param lastPotential    the previous potential to be restored on {@link #undo()}.
     * @param hasPolicy        {@code true} if the decision node had a policy
     *                         before the edit; {@code false} otherwise.
     * @param listener         listener to notify changes in the visual policy
     *                         representation; may be {@code null}.
     */
	public SetPotentialEdit(Node node, 
			String newPotentialType, 
			Potential lastPotential,
			Boolean hasPolicy, 
			VisualDecisionNodePolicyChangeListener listener) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		this.lastPotential = lastPotential;
		this.listener = listener;
		this.hasPolicy = hasPolicy;
		this.newPotentialType = newPotentialType;

	}
	
    /**
     * Creates a new {@code SetPotentialEdit} specifying explicitly both the last
     * potential and the new potential, as well as the policy state and a visual
     * listener for decision nodes.
     *
     * @param node          the node whose potential will be modified.
     * @param lastPotential the previous potential to be restored on {@link #undo()}.
     * @param newPotential  the new potential to assign to the node.
     * @param hasPolicy     {@code true} if the decision node had a policy
     *                      before the edit; {@code false} otherwise.
     * @param listener      listener to notify changes in the visual policy
     *                      representation; may be {@code null}.
     */
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

    /**
     * Executes the edit: replaces the node potential by the new potential,
     * updating policy type and link restrictions when necessary. If the node
     * is a decision node and a listener was provided, notifies the policy
     * change through {@link VisualDecisionNodePolicyChangeListener}.
     */
	@Override public void doEdit() {
		setPotential();
		if (node.getNodeType() == NodeType.DECISION && listener != null){

			listener.onNodeValueChanged();

		}

	}
    
    /**
     * Creates (if necessary) and assigns the new potential to the node,
     * preserving the last potential for undo. For non-decision nodes with
     * table potentials, the potential is updated according to link restrictions.
     * <p>
     * This method also adjusts the policy type of decision nodes, changing it
     * to {@link PolicyType#PROBABILISTIC} when appropriate.
     */
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
    

    /**
     * Assigns the new potential as the only potential of the node, without
     * keeping a copy of the previous potential in the node's potentials list.
     * <p>
     * This is typically used for the initial assignment when no prior
     * potential should remain attached to the node.
     */
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

    /**
     * Undoes the edit, restoring the previous potential and policy state.
     * <ul>
     *     <li>If {@link #lastPotential} is not {@code null}, it is restored as
     *     the node's potential.</li>
     *     <li>For decision nodes, the previous policy state is restored:
     *     if {@link #hasPolicy} is {@code true}, the listener is notified,
     *     otherwise {@link VisualDecisionNodePolicyChangeListener#removePolicy()}
     *     is invoked.</li>
     *     <li>If there was no previous potential and the node is a decision node,
     *     its policy type is set back to {@link PolicyType#OPTIMAL}.</li>
     * </ul>
     */
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

    /**
     * Redoes the edit after an {@link #undo()}, assigning the new potential as
     * the only potential of the node.
     */
	@Override public void redo() {
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
