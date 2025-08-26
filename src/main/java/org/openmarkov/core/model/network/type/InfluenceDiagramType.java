/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.type.plugin.ProbNetType;

@ProbNetType(name = "InfluenceDiagram") public class InfluenceDiagramType extends NetworkType {
    private static final InfluenceDiagramType INSTANCE = new InfluenceDiagramType();

	// Constructor
	private InfluenceDiagramType() {
		super();
	}

	// Methods
	public static InfluenceDiagramType getUniqueInstance() {
        return INSTANCE;
	}
	
}
