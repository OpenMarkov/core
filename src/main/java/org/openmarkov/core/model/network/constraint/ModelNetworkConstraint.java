/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.*;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;

/**
 * This constraint ensures that the editions done during the learning of a
 * network respect the structure of the model net and the constraints
 * selected by the user.
 */
@Constraint(name = "ModelNetworkConstraint", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class ModelNetworkConstraint
        extends PNConstraint {
    
    private ProbNet modelNet;
    private boolean linkAdditionAllowed;
    private boolean linkRemovalAllowed;
    private boolean linkInversionAllowed;
    
    // Constructors
    public ModelNetworkConstraint(ProbNet modelNet, boolean linkAdditionAllowed, boolean linkRemovalAllowed, boolean linkInversionAllowed) {
        this.linkAdditionAllowed = linkAdditionAllowed;
        this.linkRemovalAllowed = linkRemovalAllowed;
        this.linkInversionAllowed = linkInversionAllowed;
        this.modelNet = modelNet.copy();
    }
    
    @Override public boolean checkProbNet(ProbNet probNet) {
        return true;
    }
    
    public boolean canEditBeDone(BaseLinkEdit simpleEdit) {
        Node source = modelNet.getNode(simpleEdit.getVariable1().getName());
        Node destination = modelNet.getNode(simpleEdit.getVariable2().getName());
        return modelNet.getLink(destination, source, true) == null;
    }
    
    public boolean isLinkAdditionAllowed() {
        return this.linkAdditionAllowed;
    }
    
    public boolean isLinkRemovalAllowed() {
        return this.linkRemovalAllowed;
    }
    
    public boolean isLinkInversionAllowed() {
        return this.linkInversionAllowed;
    }
}
