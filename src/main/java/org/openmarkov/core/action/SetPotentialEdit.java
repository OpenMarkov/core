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

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PolicyType;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.RelationPotentialType;
import org.openmarkov.core.model.network.potential.plugin.RelationPotentialTypeManager;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

@SuppressWarnings("serial")
public class SetPotentialEdit extends SimplePNEdit {
	// unused - private PotentialType lastPotentialType;
	private Potential lastPotential;
	private String newPotentialType;
	// private ICIModelType newICIModelType;
	private Variable variable;
	private Potential newPotential = null;

	/**
	 * Creates a new SetPotentialEdit object that sets the a new potential with
	 * the type specified for the probNode object.
	 * 
	 * @param probNode
	 *            The probNode that contains the potential to modify
	 * @param newPotentialType
	 *            The potential type of the new potential to be created
	 */
	public SetPotentialEdit(ProbNode probNode, String newPotentialType) {
		super(probNode.getProbNet());

		this.variable = probNode.getVariable();
		if (!(probNode.getNodeType() == NodeType.DECISION && probNode
				.getPolicyType() == PolicyType.OPTIMAL)) {
			lastPotential = probNode.getPotentials().get(0);
		}

		this.newPotentialType = newPotentialType;

	}

	/**
	 * SetPotentialEdit object that changes the last Potential with the
	 * potential specified for the probNode object.
	 * 
	 * @param probNode
	 *            The probNode that contains the potential to set.
	 * @param newPotentialType
	 *            The new potential object
	 */
	public SetPotentialEdit(ProbNode probNode, Potential potential) {
		super(probNode.getProbNet());
		this.variable = probNode.getVariable();
		lastPotential = probNode.getPotentials().get(0);
		newPotential = potential;
	}

	// TODO al asignar un potencial tener en cuenta a los padres y a los
	// predecesores informativos que me los va a dar Manolo invocando a una
	// funcion
	@Override
	public void doEdit() throws DoEditException {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ProbNode probNode = probNet.getProbNode(variable);
		PotentialRole role;
		// si es un nodo de decision y la politica es optima se asume un cambio
		// de politica optima a probabilista (de momento no se tiene en cuenta
		// la politica determinista)
		if ((probNode.getNodeType() == NodeType.DECISION && probNode
				.getPolicyType() == PolicyType.OPTIMAL)) {// no tiene potencial
															// hay que crear uno
															// uniforme en
															// funcion de los
															// predecesores
															// informativos
			role = PotentialRole.POLICY;
			variables.add(variable);
			for (Node node : probNode.getNode().getParents()) {// cambiando el
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
				variables.add(((ProbNode) node.getObject()).getVariable());
			}
		} else {
			variables = lastPotential.getVariables();
			role = lastPotential.getPotentialRole();
		}
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		if (newPotential == null) {
			RelationPotentialTypeManager relationTypeManager = new RelationPotentialTypeManager();
			if (lastPotential.isUtility()) {
				newPotential = relationTypeManager.getByName(newPotentialType,
						variables, role, lastPotential
						.getUtilityVariable());
			} else {
			newPotential = relationTypeManager.getByName(newPotentialType,
					variables, role);
			}

			// TODO Potential: SameAsPrevious without ProbNet
			// newPotential = new SameAsPrevious (probNet, variable);
		}

		if (!(probNode.getNodeType() == NodeType.DECISION && probNode
				.getPolicyType() == PolicyType.OPTIMAL)) {
		} else {
			probNet.getProbNode(variable).setPolicyType(
					PolicyType.PROBABILISTIC);
		}

		potentials.add(newPotential);
		probNet.getProbNode(variable).setPotentials(potentials);
		// update potential with link restriction
		if (newPotentialType == TablePotential.class.getAnnotation(
				RelationPotentialType.class).name()) {
			newPotential = (TablePotential) LinkRestrictionPotentialOperations
					.updatePotentialByLinkRestrictions(probNode.getNode());
			potentials = new ArrayList<Potential>();
			potentials.add(newPotential);
			probNet.getProbNode(variable).setPotentials(potentials);
		}
	}

	public void undo() {
		super.undo();
		ProbNode probNode = probNet.getProbNode(variable);
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		if (lastPotential != null) {
			potentials.add(lastPotential);
		} else if (probNode.getNodeType() == NodeType.DECISION) {
			probNode.setPolicyType(PolicyType.OPTIMAL);

		}
		probNode.setPotentials(potentials);
	}

	public Potential getNewPotential() {
		return newPotential;
	}

}
