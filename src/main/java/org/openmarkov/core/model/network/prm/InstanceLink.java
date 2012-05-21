/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.model.network.prm;

public class InstanceLink {
	
	private Instance sourceInstance;
	private Instance destInstance;
	private Instance destSubInstance;
	
	/**
	 * Constructor
	 * @param sourceInstance
	 * @param destInstance
	 */
	public InstanceLink(Instance sourceInstance, Instance destInstance, Instance destSubInstance) {
		super();
		this.sourceInstance = sourceInstance;
		this.destInstance = destInstance;
		this.destSubInstance = destSubInstance;
	}

	/**
	 * @return the sourceInstance
	 */
	public Instance getSourceInstance() {
		return sourceInstance;
	}

	/**
	 * @return the destInstance
	 */
	public Instance getDestInstance() {
		return destInstance;
	}

	/**
	 * @return the destSubInstance
	 */
	public Instance getDestSubInstance() {
		return destSubInstance;
	}


}
