package org.openmarkov.core.oopn;

import org.openmarkov.core.model.network.Node;

public class NodeReferenceLink extends ReferenceLink{

	private Node sourceNode;
	private Node destinationNode;

	public NodeReferenceLink(Node sourceNode, Node destinationNode) {
		this.sourceNode = sourceNode;
		this.destinationNode = destinationNode;
	}

	public Node getSourceNode() {
		return sourceNode;
	}

	public Node getDestinationNode() {
		return destinationNode;
	}

}
