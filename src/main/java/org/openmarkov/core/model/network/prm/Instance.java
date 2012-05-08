/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.prm;

import org.openmarkov.core.model.network.ProbNet;

public class Instance {
	
	private String name;
	private ProbNet classNet;
	private boolean isInput;
	
	public Instance(String name, ProbNet classNet) {
		super();
		this.name = name;
		this.classNet = classNet;
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

	
}
