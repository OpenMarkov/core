package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.Collection;

public abstract sealed class InvalidNetworkTypeException extends BundledOpenMarkovException {
    
    public static final class UnmetConstraints extends InvalidNetworkTypeException {
        public final ProbNet probNet;
        public final NetworkType newNetworkType;
        public final PNConstraint unsatisfiedConstraint;
        
        // Constructor
        public UnmetConstraints(ProbNet probNet, NetworkType newNetworkType, PNConstraint unsatisfiedConstraint) {
            this.probNet = probNet;
            this.newNetworkType = newNetworkType;
            this.unsatisfiedConstraint = unsatisfiedConstraint;
        }
    }
    
    public static final class NotAllowedType extends InvalidNetworkTypeException {
    
        public NotAllowedType(ProbNet probNet, Collection<NetworkType> allowedTypes) {
            this.probNet = probNet;
            this.allowedTypes = allowedTypes;
        }
        
        public final ProbNet probNet;
        public final Collection<NetworkType> allowedTypes;
    }
    
}
