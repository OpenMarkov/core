package org.openmarkov.core.action;

import org.openmarkov.core.model.network.ProbNet;

/**
 * <code>NetworkCommentEdit</code> is a simple edit that allow modify a network 
 * comment.
 *   
 * @version 1.0 21/12/10
 * @author Miguel Palacios
 */
public class NetworkCommentEdit extends SimplePNEdit {
	/**
	 * The current network comment
	 */
	private String currentComment;
	/**
	 * The new network comment
	 */
	private String newComment;
	
	/**
	 * Creates a new <code>NetworkCommentEdit</code> with the network and new 
	 * comment specified.
	 * 
	 * @param probNet
	 *            the network that will be edited.
	 * @param newComment 
	 * 			the new comment
	 */
	public NetworkCommentEdit(ProbNet probNet,
			String newComment) {
		super(probNet);
		this.newComment = newComment;
		this.currentComment = probNet.getComment();
	}
	
	
	@Override
	public void doEdit() {
		probNet.setComment(newComment);
	}
	public void undo() {
		super.undo();
		probNet.setComment(currentComment);
		
	}
}


