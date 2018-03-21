/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Potential associated to supervalue node to indicate that the utility is a
 * product of the utilities of its parents.
 *
 * @author marias
 * @author mkpalacio
 * @version 1.0
 */
@PotentialType(name = "Product", family = "Utility") public class ProductPotential extends Potential {

	// Constructor

	/**
	 * @param variables variables
	 * @param role      potential role
	 */
	public ProductPotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);
	}

	//	public ProductPotential(Variable utilityVariable, List<Variable> variables) {
	//		super(utilityVariable, variables);
	//	}

	public ProductPotential(ProductPotential potential) {
		super(potential);
	}

	// Methods

	/**
	 * Returns if an instance of a certain Potential type makes sense given
	 * the variables and the potential role.
	 *
	 * @param node      <code>Node</code>
	 * @param variables <code>ArrayList</code> of <code>Variable</code>.
	 * @param role      <code>PotentialRole</code>.
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		boolean suitable = (
				role == PotentialRole.CONDITIONAL_PROBABILITY || role == PotentialRole.POLICY
		) && variables.get(0).getVariableType() == VariableType.NUMERIC;

		return suitable || (role == PotentialRole.UNSPECIFIED && node.isSuperValueNode());
	}

	// Methods
	@Override
	/** @return If none of the potential variables are included in the 
	 * <code>evidenceCase</code> variables returns itself, in other case, 
	 * returns a uniform potential with the potential variables minus the 
	 * <code>evidenceCase</code> variables.
	 * @param evidenceCase. <code>evidenceCase</code> */ public List<TablePotential> tableProject(
			EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials)
			throws NonProjectablePotentialException, WrongCriterionException {
		List<Variable> parentVariables = new ArrayList<>(variables);
		parentVariables.remove(getConditionedVariable());
		List<TablePotential> parentPotentials = new ArrayList<>();
		for (Variable parentVariable : parentVariables) {
			parentPotentials.add(findPotentialByVariable(parentVariable, projectedPotentials));
		}
		TablePotential productPotential = DiscretePotentialOperations.multiply(parentPotentials);
		return Arrays.asList(productPotential);
	}

	@Override public Potential copy() {
		return new ProductPotential(this);
	}

	@Override public boolean isUncertain() {
		return false;
	}

	@Override public void scalePotential(double scale) {

	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		return (ProductPotential) super.deepCopy(copyNet);
	}

}

