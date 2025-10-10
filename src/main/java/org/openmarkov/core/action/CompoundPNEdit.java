/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.openmarkov.core.annotation.ToCheck;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.ProbNet;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import java.util.Vector;

/**
 * A compound edit is a complex edition composed of several editions. This is an
 * abstract class.
 */
@SuppressWarnings("serial") public abstract class CompoundPNEdit extends CompoundEdit implements PNEdit {
    // Attribute
    protected ProbNet probNet;
    private boolean generatedEdits;
    // All simple edits are significant
    private boolean significant = true;
    
    // Constructor
    
    /**
     * @param probNet <tt>ProbNet</tt>
     */
    public CompoundPNEdit(ProbNet probNet) {
        this.probNet = probNet;
        generatedEdits = false;
    }
    
    // Methods
    
    /**
     * Generate edits and does them
     *
     * @throws DoEditException DoEditException
     */
    @Override public void doEdit() throws DoEditException {
        for (PNEdit edit : getEdits()) {
            edit.doEdit();
        }
        super.end();
    }
    
    @Override public void checkConstraintsWillBeMet() throws DoEditException.ConstraintViolated {
        for (PNEdit pnEdit : getEdits()) {
            pnEdit.checkConstraintsWillBeMet();
        }
    }
    
    @Override public final void doEdit(ProbNet probNet) throws DoEditException {
        this.initializeEdits();
        this.checkConstraintsWillBeMet();
        PNEdit.startEdit(this, probNet);
        this.doEdit();
        PNEdit.endEdit(this);
    }
    
    public abstract Vector<PNEdit> generateEdits();
    
    /**
     * @return {@code Vector} of {@code UndoableEdit}s
     */
    public Vector<PNEdit> getEdits() {
        initializeEdits();
        return (Vector<PNEdit>) (Vector) edits;
    }
    
    private void initializeEdits() {
        if (!this.generatedEdits) {
            this.edits = (Vector<UndoableEdit>) (Vector) generateEdits();
            this.generatedEdits = true;
        }
    }
    
    @Override public boolean isSignificant() {
        return significant;
    }
    
    @Override public void setSignificant(boolean significant) {
        this.significant = significant;
    }
    
    @Override public ProbNet getProbNet() {
        return probNet;
    }
    
    @Override public void setProbNet(ProbNet probNet) {
        this.probNet = probNet;
    }
}
