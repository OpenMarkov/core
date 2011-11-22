package org.openmarkov.core.action;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;

import org.apache.log4j.Logger;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;

/**
 * This class is used over a <code>openmarkov.inference.ProbNet</code> where
 * changes can be undone and redone. One edition has two parts:
 * <ol>
 * <li>Inform the listeners and
 * <li>If the action is not vetoed (with a <code>Exception</code>) for a
 * listener it does the edition.
 * </ol>
 * 
 * @author marias
 */
public class PNESupport extends UndoableEditSupport {

	// Attributes
	private ProbNet probNet;

	ArrayList<PNConstraint> constraints;

	/**
	 * If <code>true</code> stores editions in
	 * <code>openmarkov.undo#UndoManager</code> for undo/redo.
	 */
	protected boolean withUndo;

	/**
	 * List of undoable edits.
	 * 
	 * @see javax.swing.undo#UndoManager
	 */
	protected UndoManagerSupport undoManagerSupport;

	/**
	 * When open a parenthesis, we increase this variable and when we close a
	 * parenthesis we decrease this variable.
	 */
	protected int parenthesisDeph = 0;

	private boolean significantEdits = true;

	private boolean openParenthesis = false;

	private boolean editsExecuted = false;

	private int editCount;
	
	private Logger logger;

	// Constructor
	/**
	 * @param probNet
	 *            <code>ProbNet</code>.
	 * @param withUndo
	 *            <code>boolean</code>
	 */
	public PNESupport(ProbNet probNet, boolean withUndo) {
		super(probNet);
		this.probNet = probNet;
		this.withUndo = withUndo;
		undoManagerSupport = new UndoManagerSupport();

		// set probNet constraints as listeners
		constraints = probNet.getConstraints();

		for (PNConstraint constraint : constraints) {
			listeners.add(constraint);
		}
		
		this.logger = Logger.getLogger(PNESupport.class);
	}

	// Methods
	public void setListeners(Vector<UndoableEditListener> listeners) {
		this.listeners = listeners;
	}

	public Vector<UndoableEditListener> getListeners() {
		return listeners;
	}

	/**
	 * In some cases, the operations are done on a network other than the
	 * original, which has different restrictions. This method swaps the
	 * restrictions of both networks in <code>PNESupport</code>
	 * 
	 * @param newProbNet
	 *            . <code>ProbNet</code>
	 * @return oldProbNet. <code>ProbNet</code>
	 */
	public ProbNet swapNetwork(ProbNet newProbNet) {
		// Remove constraints of old network...
		for (PNConstraint constraint : constraints) {
			listeners.remove(constraint);
		}

		// ...and add constraints of new network
		ArrayList<PNConstraint> newConstraints = newProbNet.getConstraints();
		for (PNConstraint constraint : newConstraints) {
			listeners.add(constraint);
		}

		// Swaps old and new ProbNets
		ProbNet oldProbNet = probNet;
		probNet = newProbNet;
		constraints = newConstraints;

		return oldProbNet;
	}

	/**
	 * First part: Announce to the listeners than an edition can happen
	 * 
	 * @param edit
	 *            <code>PNEdit</code>.
	 * @throws NotEnoughMemoryException
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException 
	 * @throws <code>ConstraintViolationException</code> in case of illegal
	 *         <code>probNet</code> modification.
	 * @throws <code>CanNotDoEditException</code> in case of illegal
	 *         modifications in others listeners such as heuristics, GUI, ...
	 */
	public void announceEdit(PNEdit edit) throws ConstraintViolationException,
			CanNotDoEditException, NotEnoughMemoryException, 
			NonProjectablePotentialException, WrongCriterionException {
		PNUndoableEditEvent event = new PNUndoableEditEvent(this, edit,
				(ProbNet) realSource);
		for (UndoableEditListener listener : listeners) {
			((PNUndoableEditListener) listener).undoableEditWillHappen(event);
		}
	}

	/**
	 * Second part: It does the edition and inform to the listeners
	 * 
	 * @param edit
	 *            <code>PNEdit</code>.
	 * @throws DoEditException
	 * @throws NotEnoughMemoryException
	 * @throws WrongCriterionException
	 * @throws NonProjectablePotentialException
	 */
	public void doEdit(PNEdit edit) 
	throws DoEditException,	NotEnoughMemoryException, 
	NonProjectablePotentialException,
			WrongCriterionException {
		// Inform the listeners that an edition will happen
		// May return an exception

		edit.doEdit();
		if (withUndo) {
			edit.setSignificant(significantEdits);
			editCount++;
			if (openParenthesis) {
				significantEdits = false;// from now, only no significant edits
				editsExecuted = true; // at least one edit was executed
			}

			undoManagerSupport.addEdit(edit);
			/*
			 * String undoString = undoManagerSupport.getUndoPresentationName();
			 * if (undoString.startsWith(OpenParenthesisEdit.description)) {
			 * parenthesisDeph++; } else if
			 * (undoString.startsWith(CloseParenthesisEdit.description)){
			 * parenthesisDeph--; }else{ emptyParenthesis = false; }
			 */
		}
		postEdit(edit);// Inform the listeners that an edition has happened
	}

	/**
	 * @see javax.swing.undo.UndoManager#canUndo()
	 * @see javax.swing.undo.UndoManager#undo()
	 */
	public void undo() {
		if (withUndo && undoManagerSupport.canUndo()) {
			/*
			 * String undoString = undoManagerSupport.getUndoPresentationName();
			 * if (undoString.startsWith(CloseParenthesisEdit.description)) { //
			 * undo all the edits contained in the parenthesis int
			 * actualParenthesisDeph = parenthesisDeph;
			 * undoManagerSupport.undo(); parenthesisDeph++; while
			 * ((parenthesisDeph != actualParenthesisDeph) &&
			 * undoManagerSupport.canUndo()) { undoString =
			 * undoManagerSupport.getUndoPresentationName(); if
			 * (undoString.startsWith( CloseParenthesisEdit.description)) {
			 * undoManagerSupport.undo(); parenthesisDeph++; } else if
			 * (undoString.startsWith( OpenParenthesisEdit.description)) {
			 * undoManagerSupport.undo(); parenthesisDeph--; }else if
			 * (actualParenthesisDeph != parenthesisDeph) {
			 * undoManagerSupport.undo(); PNUndoableEditEvent event = new
			 * PNUndoableEditEvent(this, null, (ProbNet)realSource); for
			 * (UndoableEditListener listener : listeners) {
			 * ((PNUndoableEditListener)listener).undoEditHappened(event); } } }
			 * }else {
			 */
			undoManagerSupport.undo();
			PNUndoableEditEvent event = new PNUndoableEditEvent(this, null,
					(ProbNet) realSource);
			for (UndoableEditListener listener : listeners) {
				((PNUndoableEditListener) listener).undoEditHappened(event);
			}

			// }

		}
	}

	/**
	 * @see javax.swing.undo.UndoManager#canRedo()
	 * @see javax.swing.undo.UndoManager#redo()
	 */
	public void redo() {
		if (withUndo && undoManagerSupport.canRedo()) {
			/*
			 * String redoString = undoManagerSupport.getRedoPresentationName();
			 * if (redoString.startsWith(OpenParenthesisEdit.description)) { //
			 * redo all the edits contained in the parenthesis int
			 * actualParenthesisDeph = parenthesisDeph;
			 * undoManagerSupport.redo(); parenthesisDeph++; while
			 * ((parenthesisDeph != actualParenthesisDeph) &&
			 * undoManagerSupport.canRedo()) { redoString =
			 * undoManagerSupport.getRedoPresentationName(); if
			 * (redoString.startsWith( OpenParenthesisEdit.description)) {
			 * parenthesisDeph++; undoManagerSupport.redo(); } else if
			 * (redoString.startsWith( CloseParenthesisEdit.description)) {
			 * parenthesisDeph--; undoManagerSupport.redo(); }else if
			 * (actualParenthesisDeph != parenthesisDeph) {
			 * undoManagerSupport.redo(); PNUndoableEditEvent event = new
			 * PNUndoableEditEvent(this, null, (ProbNet)realSource); for
			 * (UndoableEditListener listener : listeners) {
			 * ((PNUndoableEditListener)listener).undoEditHappened(event); } } }
			 * }else {
			 */
			undoManagerSupport.redo();
			PNUndoableEditEvent event = new PNUndoableEditEvent(this, null,
					(ProbNet) realSource);
			for (UndoableEditListener listener : listeners) {
				((PNUndoableEditListener) listener).undoEditHappened(event);
			}

			// }

		}
	}

	public UndoManagerSupport getUndoManager() {
		return undoManagerSupport;
	}

	public boolean getCanUndo() {
		return undoManagerSupport.canUndo();
	}

	public boolean getCanRedo() {
		return undoManagerSupport.canRedo();
	}

	/**
	 * Add a <code>OpenParenthesisEdit</code> edit instance to
	 * <code>undoManager</code> and increases the parenthesis deph.
	 */
	public void openParenthesis() {
		if (withUndo) {
			parenthesisDeph++; // TODO Eliminar
			openParenthesis = true;
			editCount = 0;
			editsExecuted = false;
		}
	}

	/**
	 * Add a <code>CloseParenthesisEdit</code> edit instance to
	 * <code>undoManager</code> and decreases the parenthesis deph.
	 */
	public void closeParenthesis() {
		if (withUndo) {
			parenthesisDeph--;// TODO Eliminar
			openParenthesis = false;
			significantEdits = true;
		}
	}

	/** @return withUndo <code>boolean</code>. */
	public boolean isWithUndo() {
		return withUndo;
	}

	/**
	 * @param withUndo
	 *            <code>boolean</code>.
	 */
	public void setWithUndo(boolean withUndo) {
		this.withUndo = withUndo;
	}

	/** @return probNet <code>ProbNet</code>. */
	/*
	 * public ProbNet getProbNet() { return (ProbNet)realSource; }
	 */

	public String toString() {
		String out = "PNESupport. probNet: ";
		if (realSource == null) {
			out = out + "not defined.";
		} else {
			try {
				String name = (String) ((ProbNet) realSource).getName();
				if (name != null) {
					out = out + name + '.';
				} else {
					out = out + "no name.";
				}
			} catch (Exception e) {
				logger.fatal (e);
			}
		}
		if (listeners != null) {
			out = out + " Number of listeners: " + listeners.size() + '.';
		} else {
			out = out + " Number of listeners: 0.";
		}
		if (withUndo) {
			out = out + " With undo.";
		} else {
			out = out + " Without undo.";
		}
		return out;
	}

	public void undoAndDelete() {
		if (editsExecuted) {
			this.undo();
			undoManagerSupport.deleteEdits(editCount);
			PNUndoableEditEvent event = new PNUndoableEditEvent(this, null,
					(ProbNet) realSource);
			for (UndoableEditListener listener : listeners) {
				((PNUndoableEditListener) listener).undoEditHappened(event);
			}

		}

	}

}
