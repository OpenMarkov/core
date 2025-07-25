/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;

public class NullEdit extends SimplePNEdit {
    
    /**
     * Serial ID
     */
    private static final long serialVersionUID = 1L;
    private final int numEdit;
    
    public NullEdit(ProbNet probNet, int numEdit) {
        super(probNet);
        this.numEdit = numEdit;
    }
    
    @Override public void doEdit() {
    
    }
    
    @Override public void doEdit(ProbNet probNet) throws DoEditException.ConstraintViolated {
        PNEdit.startEdit(this, probNet);
        this.doEdit();
        PNEdit.endEdit(this);
    }
    
    @Override public void undo() {
        super.undo();
        
    }
    
    public int getNumEdit() {
        return numEdit;
    }
    
}
