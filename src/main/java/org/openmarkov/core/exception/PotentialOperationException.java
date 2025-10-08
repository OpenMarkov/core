/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.Collection;

//TODO: Catchs of this exception just show it and ignore it, leading to further bugs.
public abstract sealed class PotentialOperationException extends Exception implements IBundledOpenMarkovException {
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    public static final class DifferentSizesInPotentialsAndStates extends PotentialOperationException {
        public DifferentSizesInPotentialsAndStates(Variable variable, Collection<TablePotential> potentials) {
            this.variable = variable;
            this.potentials = potentials;
        }
        
        public final Variable variable;
		public final Collection<TablePotential> potentials;
    }
}
