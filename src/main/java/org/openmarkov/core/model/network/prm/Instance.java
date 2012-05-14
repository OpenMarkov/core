/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.prm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;

public class Instance {
	
	private String name;
	private ProbNet classNet;
	private boolean isInput;
	private ArrayList<ProbNode> instanceNodes;
	private HashMap<String, Instance> subInstances;
	private HashMap<Instance, Instance> inputParameters;
	
	/**
	 * Constructor
	 * @param name
	 * @param classNet
	 * @param instanceNodes
	 * @param isInput
	 */
	public Instance(String name, ProbNet classNet, ArrayList<ProbNode> instanceNodes, boolean isInput) {
		super();
		this.name = name;
		this.classNet = classNet;
		this.instanceNodes = instanceNodes;
		this.subInstances = new HashMap<String, Instance>();
		this.inputParameters = new HashMap<Instance, Instance>();
		this.isInput = isInput;
		
		for(String subInstanceName : classNet.getInstances().keySet())
		{
			Instance originalSubinstance = classNet.getInstances().get(subInstanceName);

			ArrayList<ProbNode> subInstanceNodes = new ArrayList<ProbNode>();
			for(ProbNode originalSubinstanceNode : originalSubinstance.getNodes())
			{
				String subinstanceNodeName = name + "." + originalSubinstanceNode.getName();
				int i = 0;
				boolean found = false;
				while(!found && i < instanceNodes.size())
				{
					found = instanceNodes.get(i).getName().equals(subinstanceNodeName);
					if(!found)
					{
						++i;
					}
				}
				subInstanceNodes.add(instanceNodes.get(i));
			}
			this.subInstances.put(subInstanceName, new Instance(subInstanceName,
					originalSubinstance.getClassNet(), subInstanceNodes,
					originalSubinstance.isInput));
		}
	}
	
	/**
	 * Constructor
	 * @param name
	 * @param classNet
	 * @param instanceNodes
	 */
	public Instance(String name, ProbNet classNet, ArrayList<ProbNode> instanceNodes) {
		this(name, classNet, instanceNodes, false);
	}	

	/**
	 * @return the isInput
	 */
	public boolean isInput() {
		return isInput;
	}

	/**
	 * @param isInput the isInput to set
	 */
	public void setInput(boolean isInput) {
		this.isInput = isInput;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the classNet
	 */
	public ProbNet getClassNet() {
		return classNet;
	}

	/**
	 * @return the instanceNodes
	 */
	public ArrayList<ProbNode> getNodes() {
		return instanceNodes;
	}

	/**
	 * 
	 * @return sub instances
	 */
	public HashMap<String, Instance> getSubInstances() {
		return subInstances;
	}

	/**
	 * Returns if this instance has an input parameter of the type of the
	 * inputInstance parameter
	 * 
	 * @param inputInstance
	 * @return
	 */
	public boolean acceptsAsInput(Instance inputInstance) {
		boolean acceptAsInput = false;
		
		Iterator<Instance> iterator = subInstances.values().iterator();
		
		while(!acceptAsInput && iterator.hasNext())
		{
			acceptAsInput = inputInstance.getClassNet().equals(iterator.next().getClassNet());
		}
		return acceptAsInput;
	}

	/**
	 * Adds reference to input parameter 
	 * @param destSubInstance
	 * @param sourceInstance
	 */
	public void addInputParameter(Instance destSubInstance, Instance sourceInstance) {
		inputParameters.put(destSubInstance, sourceInstance);
	}
	
	/**
	 * Removes reference of input parameter 
	 * @param destSubInstance
	 */
	public void removeInputParameter(Instance destSubInstance) {
		inputParameters.remove(destSubInstance);
	}	

	
}
