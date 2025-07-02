/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.Nls;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

/**
 * Thrown when trying to convert a Network of one specific {@link NetworkType} to another.
 */
@SuppressWarnings("serial") public class InvalidNetworkTypeException extends OpenMarkovException2 {
    private final ProbNet probNet;
    private final NetworkType newNetworkType;
    private final PNConstraint newConstraint;
    
    // Constructor
	public InvalidNetworkTypeException(ProbNet probNet, NetworkType newNetworkType, PNConstraint newConstraint) {
        this.probNet = probNet;
        this.newNetworkType = newNetworkType;
        this.newConstraint = newConstraint;
    }
    
    @Override protected @Nullable String getExceptionTitle() {
        return "Error: Invalid Network Type";
        //return Nls.CoreExceptions.InvalidNetworkTypeException.title.stringify();
    }
    
    @Override protected @Nullable String getExceptionMessage() {
        return "The network "+probNet.getName()+", which is of type "+probNet.getNetworkType()+", cannot be converted into " +
                newNetworkType+" because it does not accept the constraint "+newConstraint ;
        //return Nls.CoreExceptions.InvalidNetworkTypeException.message.stringify(probNet, oldNetworkType, newNetworkType, newConstraint);
    }
}
