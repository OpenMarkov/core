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
import java.util.List;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PolicyType;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.potential.plugin.PotentialManager;

@SuppressWarnings("serial")
public class SetPotentialEdit extends SimplePNEdit {
	// unused - private PotentialType lastPotentialType;
	private Potential lastPotential;
	private String newPotentialType;
	// private ICIModelType newICIModelType;
	private Variable variable;
	private Potential newPotential = null;
	private Node node;

	/**
	 * Creates a new SetPotentialEdit object that sets the a new potential with
	 * the type specified for the node object.
	 * 
	 * @param node
	 *            The node that contains the potential to modify
	 * @param newPotentialType
	 *            The potential type of the new potential to be created
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
	 * @param node
	 *            The node that contains the potential to set.
	 * @param newPotentialType
	 *            The new potential object
	 */
	public SetPotentialEdit(Node node, Potential potential) {
		super(node.getProbNet());
		this.node = node;
		this.variable = node.getVariable();
		if (node.getPotentials().size() != 0) {// if node is a decision node it could not have a potential assigned yet
			lastPotential = node.getPotentials().get(0);
		}
		
		newPotential = potential;
	}

	// TODO al asignar un potencial tener en cuenta a los padres y a los
	// predecesores informativos que me los va a dar Manolo invocando a una
	// funcion
	@Override
	public void doEdit() throws DoEditException {
		List<Variable> variables = new ArrayList<Variable>();
		//Node node = probNet.getNode(variable);
		PotentialRole role;
		// si es un nodo de decision y la politica es optima se asume un cambio
		// de politica optima a probabilista (de momento no se tiene en cuenta
		// la politica determinista)
	/*	if ((node.getNodeType() == NodeType.DECISION && node
				.getPolicyType() == PolicyType.OPTIMAL)) {// no tiene potencial
															// hay que crear uno
															// uniforme en
															// funcion de los
															// predecesores
															// informativos
			role = PotentialRole.POLICY;
			variables.add(variable);
			for (Node node : node.getNode().getParents()) {// cambiando el
																// getParents
																// por
																// predecesores
																// informativos,
																// quitar
																// el for y
																// llamar al
																// metodo de
																// Manolo que me
																// devuelve las
																// variables
				variables.add(((Node) node.getObject()).getVariable());
			}
		} else {*/
			variables = lastPotential.getVariables();
			role = lastPotential.getPotentialRole();
	//	}
		List<Potential> potentials = new ArrayList<Potential>();
		if (newPotential == null) {
			PotentialManager relationTypeManager = new PotentialManager();
			if (lastPotential.isUtility()) {
				newPotential = relationTypeManager.getByName(newPotentialType, probNet,
						lastPotential.getUtilityVariable(), variables);
			} else {
			newPotential = relationTypeManager.getByName(newPotentialType, probNet, variables, role);
			}

			// TODO Potential: SameAsPrevious without ProbNet
			// newPotential = new SameAsPrevious (probNet, variable);
		}

		if (!(node.getNodeType() == NodeType.DECISION && node
				.getPolicyType() == PolicyType.OPTIMAL)) {
		//	probNet.getNode(variable).setPolicyType(PolicyType.PROBABILISTIC);
			node.setPolicyType(PolicyType.PROBABILISTIC);
		}

		potentials.add(newPotential);
		//probNet.getNode(variable).setPotentials(potentials);
		node.setPotentials(potentials);
		// update potential with link restriction
		if (newPotentialType.contentEquals(TablePotential.class.getAnnotation(
				PotentialType.class).name()) && node.getNodeType() != NodeType.DECISION ) {
			newPotential = (TablePotential) LinkRestrictionPotentialOperations
					.updatePotentialByLinkRestrictions(node);
			potentials = new ArrayList<Potential>();
			potentials.add(newPotential);
			node.setPotentials(potentials);
			//probNet.getNode(variable).setPotentials(potentials);
		}
	}

	public void undo() {
		super.undo();
		Node node = probNet.getNode(variable);
		List<Potential> potentials = new ArrayList<Potential>();
		if (lastPotential != null) {
			potentials.add(lastPotential);
		} else if (node.getNodeType() == NodeType.DECISION) {
			node.setPolicyType(PolicyType.OPTIMAL);

		}
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
