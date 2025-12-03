/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.List;

public class RemovePolicyEdit extends PNEdit {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Potential lastPotential;
	private Variable variable;
	private PolicyType lastPolicyType;
	private Node node;

	/**
	 * @param node Node
	 */
	public RemovePolicyEdit(Node node) {
		super(node.getProbNet());
		this.variable = node.getVariable();
		if (node.getNodeType() == NodeType.DECISION && node.getPolicyType() != PolicyType.OPTIMAL) {
			lastPotential = node.getPotentials().get(0);
			lastPolicyType = node.getPolicyType();
		}

	}

	
	@Override protected void doEdit() {
		/*ArrayList<Potential> potentials = new ArrayList<>();
		if (probNet.getNode(variable).getNodeType() == NodeType.DECISION && lastPolicyType != PolicyType.OPTIMAL) {
			probNet.getNode(variable).setPolicyType(PolicyType.OPTIMAL);
			probNet.getNode(variable).setPotentials(potentials);
		}*/

		List<Potential> noPolicy = new ArrayList<>();
		node.setPotentials(noPolicy);
	}
    
    @Override public void undo() {
		super.undo();
		/*ArrayList<Potential> potentials = new ArrayList<>();
		if (probNet.getNode(variable).getNodeType() == NodeType.DECISION && lastPolicyType != PolicyType.OPTIMAL) {
			potentials.add(lastPotential);
			probNet.getNode(variable).setPotentials(potentials);
			probNet.getNode(variable).setPolicyType(lastPolicyType);
		}*/
		List<Potential> potentials = new ArrayList<>();
		potentials.add(lastPotential);
		node.setPotentials(potentials);
	}
}
