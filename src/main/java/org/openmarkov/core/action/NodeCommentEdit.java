/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import java.util.List;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;

/**
 * <code>NetworkCommentEdit</code> is a simple edit that allow modify a network 
 * comment.
 *   
 * @version 1.0 21/12/10
 * @author Miguel Palacios
 */
@SuppressWarnings("serial")
public class NodeCommentEdit extends SimplePNEdit{
	/**
	 * Current node comment
	 */
	private String currentComment = "";
	/**
	 * New node comment
	 */
	private String newComment;
	
	/**
	 * Comment type, could be "DefinitionComment" or "ProbsTableComment"
	 */
	private String typeComment="";
	/**
	 * The node
	 */
	private Node node;
	
	/**
	 * Creates a <code>NodeCommentEdit</code> with the node, new comment and 
	 * type of comment specified.
	 */
	public NodeCommentEdit(Node node,String newComment,
			String typeComment) {
		super(node.getProbNet());
		this.newComment = newComment;
		this.typeComment = typeComment;
		this.node = node;
		if (typeComment.equals("DefinitionComment")){
			this.currentComment = node.getComment();
		}else{
			this.currentComment = node.getPotentials().get(0).getComment();

		}
	}
	// Methods
	@Override
	public void doEdit() {
		if (typeComment.equals("DefinitionComment")){
			 node.setComment(newComment);
		}else{
		    List<Potential> potential = node.getPotentials();
			potential.get(0).setComment(newComment);
			node.setPotentials(potential); 
			
		}
	}
	
	public void undo() {
		super.undo();
		if (typeComment.equals("DefinitionComment")){
			node.setComment(currentComment);
		}else{
		    List<Potential> potential = node.getPotentials();
			potential.get(0).setComment(currentComment);
			node.setPotentials(potential); 
		}
	}
}
