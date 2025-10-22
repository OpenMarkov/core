package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.action.base.VisualChanceNodeFindingChangeListener;

public class RemoveFindingEdit extends PNEdit {
    
    private EvidenceCase evidenceCase;
    private VisualChanceNodeFindingChangeListener listener;
    private Variable variable;
    private Finding finding;
    
    
    /**
     * @param node {@code ProbNet}
     */
    public RemoveFindingEdit(Node node, EvidenceCase evidenceCase, VisualChanceNodeFindingChangeListener listener, Variable variable) {
        super(node.getProbNet());
        this.evidenceCase = evidenceCase;
        this.listener = listener;
        this.variable = variable;
    }
    
    @Override public void doEdit() {
        finding = evidenceCase.getFinding(variable);
        evidenceCase.removeFinding(variable);
        listener.removeFinding();
    }
    
    @Override public void undo() {
        super.undo();
        try {
            evidenceCase.addFinding(finding);
            listener.onNodeValueChanged();
        } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
            throw new UnreacheableException(e);
        }
    }
    
    @Override
    public void redo() {
        super.redo();
        
    }
    
}
