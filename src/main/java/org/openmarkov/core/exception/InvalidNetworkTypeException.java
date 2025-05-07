/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

/**
 * Thrown when trying to convert a Network of one specific {@link NetworkType} to another.
 */
@SuppressWarnings("serial") public class InvalidNetworkTypeException extends DoEditException {
    private final ProbNet probNet;
    private final NetworkType oldNetworkType;
    private final NetworkType newNetworkType;
    private final PNConstraint newConstraint;
    
    // Constructor
	
	public InvalidNetworkTypeException(ProbNet probNet, NetworkType oldNetworkType, NetworkType newNetworkType, PNConstraint newConstraint) {
		//TODO: Specify this exception
		super("The constraint "+ newConstraint+ " is not valid for the network type "+newNetworkType+System.lineSeparator()
					  +"This happened when converting net "+ probNet+" of type "+oldNetworkType+" to "+newNetworkType);
        this.probNet = probNet;
        this.oldNetworkType = oldNetworkType;
        this.newNetworkType = newNetworkType;
        this.newConstraint = newConstraint;
    }
	
	
}
