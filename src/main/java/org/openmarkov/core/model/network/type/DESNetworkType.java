/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.*;
//TODO Link restriction?
import org.openmarkov.core.model.network.type.plugin.ProbNetType;

/**
 * PGM network to implement Discrete Event Simulation (DES) Models
 * @author cyago - 10/01/2019
 */
@ProbNetType(name = "DESNet") public class DESNetworkType extends NetworkType {
	private static DESNetworkType instance = null;

	// Constructor
	private DESNetworkType() {
		super();
		overrideConstraintBehavior(NoLinkRestriction.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(NoCycle.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(NoEventNodes.class, ConstraintBehavior.NO);

	}

	// Methods
	public static DESNetworkType getUniqueInstance() {
		if (instance == null) {
			instance = new DESNetworkType();
		}
		return instance;
	}

	// TODO
	/**
	 * @return String "DecisionAnalysisNetwork".
	 */
	public String toString() {
		return "DESNet";
	}

}
