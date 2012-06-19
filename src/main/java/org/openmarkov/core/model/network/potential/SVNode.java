/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

public class SVNode extends ProbNode {

	// Attributes
    /** There are two types of <code>SVNode</code>: addition and product */
    private SVNodeType svNodeType;

	// Constructor
	/** @param probNet <code>ProbNet</code>
	 * @param variable <code>Variable</code>
	 * @param nodeType <code>NodeType</code> */
    public SVNode(ProbNet probNet, Variable variable, SVNodeType svNodeType) {
        super(probNet, variable, NodeType.UTILITY);
    }

    // Methods
    /** @return Returns the SVNode type (<code>int</code>). */
    public SVNodeType getSVNodeType() {
        return svNodeType;
    }

    /** @param type (<code>int</code>) The SVNode type. */
    public void setSVNodeType(SVNodeType svNodeType) {
        this.svNodeType = svNodeType;
    }

}
