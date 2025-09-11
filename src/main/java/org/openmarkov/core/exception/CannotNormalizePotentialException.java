/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.List;

public class CannotNormalizePotentialException extends Exception implements IBundledOpenMarkovException {
    
    private final Potential potential;
    
    //TODO: Does this really happen in the GUI? It might be a RuntimeException.
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    public CannotNormalizePotentialException(Potential potential) {
        this.potential = potential;
    }
}
