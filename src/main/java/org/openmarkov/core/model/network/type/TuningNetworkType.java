/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoLinkRestriction;
import org.openmarkov.core.model.network.type.plugin.ProbNetType;

@ProbNetType(name = "TuningNetwork") public class TuningNetworkType extends NetworkType {
    private static final TuningNetworkType INSTANCE = new TuningNetworkType();

	// Constructor
	protected TuningNetworkType() {
		super();
		overrideConstraintBehavior(NoLinkRestriction.class, ConstraintBehavior.NO);
	}

	// Methods
	public static TuningNetworkType getUniqueInstance() {
        return INSTANCE;
	}
	
}
