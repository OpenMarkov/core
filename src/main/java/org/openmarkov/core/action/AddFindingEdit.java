package org.openmarkov.core.action;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;


public class AddFindingEdit extends SimplePNEdit{

    Node node;
    EvidenceCase evidenceCase;
    Boolean isInferenceMode;
    VisualChanceNodeFindingChangeListener listener;
    Finding finding;
    Finding previousFinding;

     /**
     * @param node {@code Node}
     */
    public AddFindingEdit(Node node, EvidenceCase evidenceCase,Finding previousFinding, Finding finding, VisualChanceNodeFindingChangeListener listener) {
        super(node.getProbNet());
        this.node = node;
        this.evidenceCase = evidenceCase;
        this.listener = listener;
        this.finding = finding;
        this.previousFinding = previousFinding;
    }

    @Override
    public void doEdit() throws DoEditException {
        try {
            evidenceCase.addFinding(finding);
            listener.onNodeValueChanged();
        } catch (IncompatibleEvidenceException e) {
            throw new DoEditException(e.getToken());
        }

    }

    @Override
    public void undo(){
        super.undo();
        if(previousFinding == null){
            try {
                evidenceCase.removeFinding(finding.getVariable());
                listener.removeFinding();
            } catch (NoFindingException e) {
                throw new UnreacheableException(e);
            }
        }else {
            try {
                evidenceCase.removeFinding(finding.getVariable());
                evidenceCase.addFinding(previousFinding);
            } catch (IncompatibleEvidenceException | NoFindingException e) {
                throw new UnreacheableException(e);
            }
        }

    }




    @Override
    public void redo() {
        super.redo();


    }



}
