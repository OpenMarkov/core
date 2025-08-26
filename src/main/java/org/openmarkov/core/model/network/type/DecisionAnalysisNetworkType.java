/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoLinkRestriction;
import org.openmarkov.core.model.network.constraint.NoRevelationArc;
import org.openmarkov.core.model.network.type.plugin.ProbNetType;

@ProbNetType(name = "DAN") public class DecisionAnalysisNetworkType extends NetworkType {
    private static final DecisionAnalysisNetworkType INSTANCE = new DecisionAnalysisNetworkType();

	// Constructor
	private DecisionAnalysisNetworkType() {
		super();
		overrideConstraintBehavior(NoRevelationArc.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(NoLinkRestriction.class, ConstraintBehavior.NO);
	}

	// Methods
	public static DecisionAnalysisNetworkType getUniqueInstance() {
        return INSTANCE;
	}
	
}
