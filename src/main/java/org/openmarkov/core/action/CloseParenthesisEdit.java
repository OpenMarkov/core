package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;

@SuppressWarnings("serial")
public class CloseParenthesisEdit extends SimplePNEdit {

	// Constant
	public static final String description = ")";
	
	// Constructor
	public CloseParenthesisEdit() {
	}

	// Methods
	@Override
	public void doEdit() throws DoEditException {
		//super.addEdit(this);
	}

	public void undo() {
		super.undo();
	}

	public String getUndoPresentationName() {
		return description + " " + getPresentationName();
	}
	
	public String getRedoPresentationName() {
		return description + " " + getPresentationName();
	}
	
	public String toString() {
		return description;
	}
	

}
