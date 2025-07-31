/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.List;

public abstract sealed class DoEditException extends BundledOpenMarkovException {
	
	public static CannotDoEditException of(OpenMarkovException exception) {
		return new CannotDoEditException(exception);
	}
	
	public static final class CannotDoEditException extends DoEditException {
		public CannotDoEditException(OpenMarkovException originException) {
            this.originException = originException;
        }
        
        public final OpenMarkovException originException;
    }
    
    public static final class ConstraintViolated extends DoEditException {
        public final PNConstraint constraint;
        
        public ConstraintViolated(PNConstraint constraint) {
            this.constraint = constraint;
        }
    }
	
	public static final class InstanceAlreadyExists extends DoEditException {
		public InstanceAlreadyExists(String instanceName) {
            this.instanceName = instanceName;
        }
        
        public final String instanceName;
    }
	
	public static final class NodeIsNull extends DoEditException {
		public NodeIsNull(ProbNet probNet) {
            this.probNet = probNet;
        }
        
        public final ProbNet probNet;
    }
	
	public static final class CannotRemovePotential extends DoEditException {
		public CannotRemovePotential(ProbNet probNet, Potential oldPotential) {
            this.probNet = probNet;
            this.oldPotential = oldPotential;
        }
        
        public final ProbNet probNet;
        public final Potential oldPotential;
    }
	
	public static final class CannotInvertLink extends DoEditException {
		public CannotInvertLink(Node from, Node to, ProbNet probNet, List<PNConstraint> unsatisfiedConstraint) {
            this.from = from;
            this.to = to;
            this.probNet = probNet;
            this.unsatisfiedConstraint = unsatisfiedConstraint;
        }
        
        public final Node from;
        public final Node to;
        public final ProbNet probNet;
        public final List<PNConstraint> unsatisfiedConstraint;
    }
	
}
