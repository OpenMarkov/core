package org.openmarkov.core.action;

import javax.swing.undo.CannotUndoException;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.TemporalUnit;

public class TemporalUnitEdit extends SimplePNEdit{

	/**
	 * Default serial UID
	 */
	private static final long serialVersionUID = 1L;
	
	private TemporalUnit oldTemporalUnit;
	private TemporalUnit newTemporalUnit;
	
	public TemporalUnitEdit(ProbNet probNet, TemporalUnit newTemporalUnit) {
		super(probNet);
		this.oldTemporalUnit = probNet.getTemporalUnit().clone();
		this.newTemporalUnit = newTemporalUnit;
	}

	@Override
	public void doEdit() throws DoEditException {
		probNet.setTemporalUnit(this.newTemporalUnit);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		probNet.setTemporalUnit(this.oldTemporalUnit);
	}

	@Override
	public void redo() {
		super.redo();
		try {
			doEdit();
		} catch (DoEditException e) {
			e.printStackTrace();
		}
	}

}
