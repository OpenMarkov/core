package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNode;

/**
 * <code>NodeNameEdit</code> is a simple edit that allow modify the node
 * name.
 *   
 * @version 1.0 21/12/10
 * @author Miguel Palacios
 */
public class NodeNameEdit extends SimplePNEdit {
	/**
	 * Current node name
	 */
	private String previousName;
	/**
	 * New node name
	 */
	private String newName;
	/**
	 * The node edited
	 */
	private ProbNode probNode = null;
	/**
	 * Creates a new <code>NodeNameEdit</code> with the node and new name 
	 * specified.
	 * @param probNode the node that will be modified
	 * @param newName the new name of the node
	 */
	public NodeNameEdit (ProbNode probNode, String newName){
		super(probNode.getProbNet());
		this.probNode = probNode;
		this.previousName = probNode.getName();
		this.newName = newName;
	}	
	
	@Override
	public void doEdit() throws DoEditException {
		probNode.getVariable().setName(newName);
	}
	@Override
	public void undo() {
		super.undo();
		probNode.getVariable().setName(previousName);
	}
	/**
	 * Gets the new name of the node
	 * 
	 * @return
	 * 		the new name of the node
	 */
	public String getNewName(){
		return newName;
	}
	/**
	 * Gets the previous name of the node
	 * 
	 * @return
	 * 		the previous name of the node
	 */
	public String getPreviousName(){
		return previousName;
	}
}
