package org.openmarkov.core.action;

import javax.swing.undo.AbstractUndoableEdit;

import org.apache.log4j.Logger;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;


/** Abstract class that defines the basic attribute (a <code>ProbNet</code>)
 *  and operations of editions. */
@SuppressWarnings("serial")
public abstract class SimplePNEdit extends AbstractUndoableEdit 
		implements PNEdit {

	// Attributes
	/** <code>ProbNet</code> over witch the operations are defined. */
	protected ProbNet probNet;
	
	private boolean typicalRedo = true;

	//All simple edits are significant  
	private boolean significant = true;
	
	private Logger logger;
	
	// Constructors
	public SimplePNEdit() {
		this.logger = Logger.getLogger(SimplePNEdit.class);
	}
	
	/** @param probNet <code>ProbNet</code> */
	public SimplePNEdit(ProbNet probNet) {
		this.probNet = probNet;
	}
	
	// Methods
	/** Abstract method to be defined in derived classes */
	public abstract void doEdit() throws DoEditException;
	
	/** @return probNet. <code>ProbNet</code> */
	public ProbNet getProbNet() {
		return probNet;
	}
	
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
	public void setSignificant(boolean significant){
		this.significant = significant;
	}
	
	public boolean isSignificant(){
		return significant;
	}

}
