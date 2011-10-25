package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;

import openmarkov.exceptions.NonProjectablePotentialException;
import openmarkov.exceptions.NotEnoughMemoryException;
import openmarkov.exceptions.WrongCriterionException;
import openmarkov.undo.edit.CompoundPNEdit;
import openmarkov.undo.edit.PNEdit;

/** Utility methods for constraint package. */
public class UtilConstraints {

	/** @param event <code>UndoableEditEvent</code>
	 * @param typeEdit <code>Class</code>
	 * @return An <code>ArrayList</code> of <code>PNEdit</code>s of type 
	 *   <code>typeEdit</code> that are contained in the <code>event</code> 
	 *   received (if there is any) 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
	public static ArrayList<PNEdit> getEditsType(
			UndoableEditEvent event, Class typeEdit) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<PNEdit> edits = new ArrayList<PNEdit>();
		PNEdit eventEdit = (PNEdit)event.getEdit();
		if (eventEdit.getClass() == typeEdit) {
			edits.add((PNEdit)eventEdit);
		} else {  // Check compound edits
			if (CompoundPNEdit.class.isInstance(eventEdit)) {
				Vector<UndoableEdit> simpleEdits = 
					((CompoundPNEdit)eventEdit).getEdits();
				for (UndoableEdit edit : simpleEdits) {
					if (typeEdit.isInstance(edit)) {
						edits.add((PNEdit)edit);
					}
				}
			}
		}
		return edits;
	}
	
}
