package org.openmarkov.core.action;

import org.openmarkov.core.model.network.ProbNet;

public class NullEdit extends SimplePNEdit{

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 1L;
	private int numEdit;
	
	public NullEdit(ProbNet probNet, int numEdit) {
		super(probNet);
		this.numEdit = numEdit;
	}

	@Override
	public void doEdit() {
		// TODO Auto-generated method stub
		System.out.println("Doing edit #" + numEdit);
		
	}

	@Override
	public void undo() {
		// TODO Auto-generated method stub
		super.undo();
		System.out.println("Undoing edit #" + numEdit);
	}

	public int getNumEdit() {
		return numEdit;
	}
	
	

}
