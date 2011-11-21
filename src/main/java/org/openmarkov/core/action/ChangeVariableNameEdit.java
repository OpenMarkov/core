package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

@SuppressWarnings("serial")
public class ChangeVariableNameEdit extends SimplePNEdit {

	// Attributes
	private Variable variable;
	
	private String oldName;
	
	private String newName;
	
	// Constructor
	/** @param probNet. <code>ProbNet</code>
	 * @param variable. <code>Variable</code>
	 * @param newName. <code>String</code> */
	public ChangeVariableNameEdit(
			ProbNet probNet, Variable variable, String newName) {
		super(probNet);
		this.variable = variable;
		this.oldName = variable.getName();
		this.newName = newName;
	}
	
	// Methods
	@Override
	public void doEdit() throws DoEditException {
		variable.setName(newName);
	}

	public void undo() {
		super.undo();
		if (variable != null) {
			probNet.removeProbNode(probNet.getProbNode(variable));
		}
	}
	
	/** @return variable <code>Variable</code> */
	public Variable getVariable() {
		return variable;
	}

	/** @return new variable name. <code>String</code> */
	public String getNewName() {
		return newName;
	}
	
	/** @return old variable name. <code>String</code> */
	public String getOldName() {
		return oldName;
	}
	
	public String toString() {
		return new String("ChangeVariableNameEdit: " +
				oldName + " -> " + newName);
	}

}
