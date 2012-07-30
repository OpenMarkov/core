/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.oon;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

/**
 * @author ibermejo
 *
 */
public class InstanceNode extends ProbNode {
	
	private String instanceName;

	/**
	 * Constructor for InstanceNode
	 * @param probNet
	 * @param variable
	 * @param nodeType
	 */
	public InstanceNode(ProbNet probNet, Variable variable, NodeType nodeType, String instanceName) {
		super(probNet, variable, nodeType);
		this.instanceName = instanceName;
	}
	
	/**
	 * Constructor for InstanceNode
	 * @param probNet
	 * @param variable
	 * @param nodeType
	 */
	public InstanceNode(InstanceNode node) {
		super(node);
		this.instanceName = node.getInstanceName();
	}	
	
	/**
	 * @return the instanceName
	 */
	public String getInstanceName() {
		return instanceName;
	}
	

}
