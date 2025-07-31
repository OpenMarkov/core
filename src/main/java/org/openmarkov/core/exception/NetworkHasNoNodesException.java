package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.ProbNet;

public class NetworkHasNoNodesException extends BundledOpenMarkovException {
    
    public NetworkHasNoNodesException(ProbNet probNet) {
        this.probNet = probNet;
    }
    
    public final ProbNet probNet;
}
