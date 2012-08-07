package org.openmarkov.core.oon;

import org.openmarkov.core.model.network.ProbNode;

public class NodeParameterLink extends ParameterLink{

	private ProbNode sourceNode;
	private ProbNode destinationNode;

	public NodeParameterLink(ProbNode sourceNode, ProbNode destinationNode) {
		this.sourceNode = sourceNode;
		this.destinationNode = destinationNode;
	}

	public ProbNode getSourceNode() {
		return sourceNode;
	}

	public ProbNode getDestinationNode() {
		return destinationNode;
	}

}
