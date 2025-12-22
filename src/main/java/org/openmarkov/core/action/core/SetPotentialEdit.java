/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.action.base.PNEdit;


public class SetPotentialEdit extends PNEdit {
    
    private final Potential lastPotential;
    
    private final @Nullable Potential newPotential;
    
    private final Node node;
    
    
    public SetPotentialEdit(Node node) {
        super(node.getProbNet());
        this.node = node;
        lastPotential = node.getPotentials().get(0);
        newPotential = null;
    }
    
    
    public SetPotentialEdit(Node node, Potential potential) {
        super(node.getProbNet());
        this.node = node;
        // If node is a decision node it may have no potential assigned yet.
        if (!node.getPotentials().isEmpty()) {
            lastPotential = node.getPotentials().get(0);
        } else {
            lastPotential = null;
        }
        newPotential = potential;
    }
    
    
    public SetPotentialEdit(Node node,
                            Potential lastPotential,
                            Potential newPotential) {
        super(node.getProbNet());
        this.node = node;
        this.lastPotential = lastPotential;
        this.newPotential = newPotential;
    }
    
    
    // TODO al asignar un potencial tener en cuenta a los padres y a los
    // predecesores informativos que me los va a dar Manolo invocando a una
    // funcion
    
    @Override protected void doEdit() {
        node.setPotentialConsistently(newPotential);
    }
    
    @Override public void undo() {
        super.undo();
        node.setPotentialConsistently(lastPotential);
    }
    
    
    @Override public void redo() {
        super.redo();
        node.setPotentialConsistently(newPotential);
    }
    
}
