/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;

import org.openmarkov.core.action.CompoundPNEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;

/** Utility methods for constraint package. */
public class UtilConstraints {

    /**
     * @param event <code>UndoableEditEvent</code>
     * @param typeEdit <code>Class</code>
     * @return An <code>ArrayList</code> of <code>PNEdit</code>s of type
     *         <code>typeEdit</code> that are contained in the
     *         <code>event</code> received (if there is any)
     * @throws NotEnoughMemoryException
     * @throws WrongCriterionException
     * @throws NonProjectablePotentialException
     */
    public static ArrayList<PNEdit> getEditsType (UndoableEditEvent event,
                                                  Class<?> typeEdit)
        throws NotEnoughMemoryException,
        NonProjectablePotentialException,
        WrongCriterionException
    {
        ArrayList<PNEdit> edits = new ArrayList<PNEdit> ();
        PNEdit eventEdit = (PNEdit) event.getEdit ();
        if (eventEdit.getClass () == typeEdit)
        {
            edits.add ((PNEdit) eventEdit);
        }
        else
        { // Check compound edits
            if (CompoundPNEdit.class.isInstance (eventEdit))
            {
                Vector<UndoableEdit> simpleEdits = ((CompoundPNEdit) eventEdit).getEdits ();
                for (UndoableEdit edit : simpleEdits)
                {
                    if (typeEdit.isInstance (edit))
                    {
                        edits.add ((PNEdit) edit);
                    }
                }
            }
        }
        return edits;
    }
	
}
