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

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;


/** A compound edit is a complex edition composed 
 * of several editions. This is an abstract class. */
@SuppressWarnings("serial")
public abstract class CompoundPNEdit extends CompoundEdit implements PNEdit {

	// Attribute
	protected ProbNet probNet;
	
	private boolean generatedEdits;
	
	//All simple edits are significant  
	private boolean significant = true;

	private boolean typicalRedo = true;
	
	private Logger logger;


	
	// Constructor
	/** @param probNet <tt>ProbNet</tt> */
	public CompoundPNEdit(ProbNet probNet) {
		
		this.probNet = probNet;
		generatedEdits = false;
		this.logger = Logger.getLogger(CompoundPNEdit.class);
	}
	
	// Methods
	/** Generate edits and does them
	 * @throws DoEditException 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
	public void doEdit() throws DoEditException, NotEnoughMemoryException, NonProjectablePotentialException, WrongCriterionException {
		if (!generatedEdits) {
			generateEdits();
			generatedEdits = true;
		}
		for (UndoableEdit edit : edits) {
			((PNEdit)edit).doEdit();
		}
		super.end();
	}
	
	public abstract void generateEdits() throws NotEnoughMemoryException, 
	NonProjectablePotentialException, WrongCriterionException;

	protected void setTypicalRedo(boolean redo){
		typicalRedo = redo;
	}
	
	public void redo() {
		super.redo();
		if (typicalRedo){ 
			try {
				doEdit();
			} catch(Exception e) {
				logger.fatal (e);
			}
		} else{
			typicalRedo = true;
		}	
	}
	
	/** @return <code>Vector</code> of <code>UndoableEdit</code>s 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
	public Vector<UndoableEdit> getEdits() 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		if (!generatedEdits) {
			generateEdits();
			generatedEdits = true;
		}
		return edits;
	}
	
	public void setSignificant(boolean significant){
		this.significant  = significant;
	}
	
	public boolean isSignificant(){
		return significant;
	}

}
