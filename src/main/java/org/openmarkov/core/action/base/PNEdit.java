/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreacheableException;
import org.openmarkov.core.model.network.ProbNet;

/**
 * Abstract class that defines the basic attribute (a {@code ProbNet})
 * and operations of editions.
 */
@SuppressWarnings("serial") public abstract class PNEdit {
    
    //Start interface
    
    /**
     * This method acts as a contract saying no constraint will be violated after the edit is done.
     * <p>
     * If this method returns a {@link ConstraintViolatedException}, then it means
     * this edit should not be applied, as it will violate that constraint.
     */
    public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
    }
    
    /**
     * Abstract method to be defined in derived classes
     *
     * @throws DoEditException DoEditException
     */
    public abstract void doEdit() throws DoEditException;
    
    public void executeEdit() throws DoEditException {
        PNESupport pneSupport = getProbNet().getPNESupport();
        ConstraintChecker constraintChecker = new ConstraintChecker(probNet);
        this.checkConstraintsWillBeMet(constraintChecker);
        try {
            constraintChecker.buildAndThrow();
        } catch (ConstraintViolatedException ex) {
            for (PNUndoableEditListener listener : pneSupport.getListeners()) {
                listener.onEditViolatesConstraints(new PNUndoableEditEvent(pneSupport, this, probNet), ex);
            }
            throw ex;
        }
        PNUndoableEditEvent event = new PNUndoableEditEvent(pneSupport, this, probNet);
        for (PNUndoableEditListener listener : pneSupport.getListeners()) {
            listener.beforeEditHappens(event);
        }
        try {
            this.doEdit();
        } catch (DoEditException e) {
            for (PNUndoableEditListener listener : pneSupport.getListeners()) {
                listener.onEditFailed(event, e);
            }
            throw e;
        }
        if (pneSupport.isWithUndo() && !belongsToACompoundEdit) {
            pneSupport.getUndoManager().addEdit(this);
        }
        Class<? extends PNEdit> editClass = getClass();
        boolean isParenthesis = editClass == OpenParenthesisEdit.class || editClass == CloseParenthesisEdit.class;
        if (!isParenthesis) {
            for (PNUndoableEditListener listener : pneSupport.getListeners()) {
                listener.afterEditHappens(event);
            }
        }
    }
    
    //End interface
    
    
    // Attributes
    /**
     * {@code ProbNet} over witch the operations are defined.
     */
    protected ProbNet probNet;
    
    private boolean typicalRedo = true;
    
    //All simple edits are significant
    private boolean significant = true;
    
    private Logger logger;
    
    // Constructor
    
    /**
     * @param probNet {@code ProbNet}
     */
    public PNEdit(ProbNet probNet) {
        this.probNet = probNet;
        this.logger = LogManager.getLogger(PNEdit.class);
    }
    
    // Methods
    
    /**
     * @return probNet. {@code ProbNet}
     */
    public ProbNet getProbNet() {
        return probNet;
    }
    
    public void setProbNet(ProbNet probNet) {
        this.probNet = probNet;
    }
    
    protected void setTypicalRedo(boolean redo) {
        typicalRedo = redo;
    }
    
    public void redo() {
        if (typicalRedo) {
            try {
                doEdit();
            } catch (DoEditException e) {
                throw new UnreacheableException(e);
            }
        } else {
            typicalRedo = true;
        }
    }
    
    public void undo() {
    
    }
    
    public boolean canUndo() {
        return true;
    }
    
    public boolean isSignificant() {
        return significant;
    }
    
    private boolean belongsToACompoundEdit = false;
    
    public void markItBelongsToACompoundEdit() {
        this.belongsToACompoundEdit = true;
    }
    
}
