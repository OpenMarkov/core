package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;

import javax.swing.undo.CannotUndoException;

public class RemoveFindingEdit extends SimplePNEdit{

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

    @Override
    public void doEdit() throws DoEditException {
        try {
            finding = evidenceCase.getFinding(variable);
            evidenceCase.removeFinding(variable);
            listener.removeFinding();
        } catch (NoFindingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        try {
            evidenceCase.addFinding(finding);
            listener.onNodeValueChanged();
        } catch (InvalidStateException | IncompatibleEvidenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void redo() {
        super.redo();

    }

}
