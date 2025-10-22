/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * A compound edit is a complex edition composed of several editions. This is an
 * abstract class.
 */
@SuppressWarnings("serial") public abstract class CompoundPNEdit extends PNEdit {

    // Attribute
    private boolean generatedEdits;
    
    // Constructor
    private ArrayList<PNEdit> edits;
    

    public CompoundPNEdit(ProbNet probNet) {
        super(probNet);
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
            edit.executeEdit();
        }
    }
    
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        for (PNEdit pnEdit : getEdits()) {
            pnEdit.checkConstraintsWillBeMet(constraintChecker);
        }
    }
    
    protected abstract ArrayList<PNEdit> generateEdits();
    
    public ArrayList<PNEdit> getEdits() {
        initializeEdits();
        return edits;
    }
    
    private void initializeEdits() {
        if (!this.generatedEdits) {
            this.edits = generateEdits();
            edits.forEach(edit -> edit.markItBelongsToACompoundEdit());
            this.generatedEdits = true;
        }
    }
    
    @Override public ProbNet getProbNet() {
        return probNet;
    }
    
    @Override public void setProbNet(ProbNet probNet) {
        this.probNet = probNet;
    }
    
    @Override public void redo() {
        edits.forEach(PNEdit::redo);
        setTypicalRedo(false);
        super.redo();
    }
    
    @Override public void undo() {
        IntStream.range(0, edits.size())
                 .mapToObj(i -> {
                     int realIndex = edits.size() - 1 - i;
                     return edits.get(realIndex);
                 })
                 .forEach(PNEdit::undo);
        super.undo();
    }
    
}
