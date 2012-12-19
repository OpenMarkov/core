/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.action;

import java.util.Vector;

import javax.swing.undo.UndoableEdit;

import org.openmarkov.core.model.network.ProbNet;

@SuppressWarnings("serial")
public class CompoundDirectLinkEdit extends CompoundPNEdit {
	
	public CompoundDirectLinkEdit(ProbNet probNet, Vector<UndoableEdit> edits) {
		super(probNet);
		this.edits = edits;
	}

	// Methods
	@Override
	public void generateEdits() {
	}	
		
	public String toString() {
		StringBuffer buffer = new StringBuffer("CompoundDirectLinkEdit: ");
		for (UndoableEdit edit : edits){
			buffer.append(edit.toString());
		}
		return buffer.toString();
	}

    @Override
    public boolean equals (Object arg0)
    {
        boolean equals = true;
        
        if(arg0 instanceof CompoundDirectLinkEdit)
        {
            CompoundDirectLinkEdit editToCompare = (CompoundDirectLinkEdit)arg0;
            
            for(UndoableEdit edit : editToCompare.edits)
            {
                equals &= edits.contains (edit);
            }
            
            for(UndoableEdit edit : edits)
            {
                equals &= editToCompare.edits.contains (edit);
            }            
        }else
        {
            equals = false;
        }

        return equals;
    }

	
}
