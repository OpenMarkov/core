package org.openmarkov.core.action.core;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.SimplePNEdit;
import org.openmarkov.core.action.base.VisualChanceNodeFindingChangeListener;


public class AddFindingEdit extends SimplePNEdit {
    
    Node node;
    EvidenceCase evidenceCase;
    Boolean isInferenceMode;
    VisualChanceNodeFindingChangeListener listener;
    Finding finding;
    Finding previousFinding;
    
    /**
     * @param node {@code Node}
     */
    public AddFindingEdit(Node node, EvidenceCase evidenceCase, Finding previousFinding, Finding finding, VisualChanceNodeFindingChangeListener listener) {
        super(node.getProbNet());
        this.node = node;
        this.evidenceCase = evidenceCase;
        this.listener = listener;
        this.finding = finding;
        this.previousFinding = previousFinding;
    }
    
    @Override
    public void doEdit() throws DoEditException.CannotDoEditException {
        try {
            evidenceCase.addFinding(finding);
            listener.onNodeValueChanged();
        } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
            throw new DoEditException.CannotDoEditException(e);
        }
    }
    
    @Override
    public void doEdit(ProbNet probNet) throws ConstraintViolatedException, DoEditException.CannotDoEditException {
        this.checkConstraintsWillBeMet();
        PNEdit.startEdit(this, probNet);
        this.doEdit();
        PNEdit.endEdit(this);
    }
    
    @Override
    public void undo() {
        super.undo();
        if (previousFinding == null) {
            evidenceCase.removeFinding(finding.getVariable());
            listener.removeFinding();
        } else {
            try {
                evidenceCase.removeFinding(finding.getVariable());
                evidenceCase.addFinding(previousFinding);
            } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
                throw new UnreacheableException(e);
            }
        }
        
    }
    
    
    @Override
    public void redo() {
        super.redo();
    }
    
    
}
