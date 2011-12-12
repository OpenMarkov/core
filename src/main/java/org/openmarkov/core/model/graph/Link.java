/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.graph;

/** This class implements explicit links.
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 * @see openmarkov.graphs.Node
 * @see openmarkov.graphs.Graph */
public class Link {

	// Attributes
	/** The first node. If the link is directed, this node is the parent. */
	private Node node1;

	/** The first node. If the link is directed, this node is the parent. */
	private Node node2;

	/** If true, the link is directed. Otherwise, it is an undirected link. */
	private boolean directed;

	// Constructors
	/** Creates an unlabelled link and sets the cross references in 
	 * the nodes. This constructor should be called only from the
	 * <code>addLink</code> function in the class Graph.
	 * @param node1 <code>Node</code>.
	 * @param node2 <code>Node</code>.
	 * @param directed <code>boolean</code>.
	 * @argCondition Both nodes must belong to the same graph. */
	public Link(Node node1, Node node2, boolean directed) {
		Graph graph = node1.getGraph();
		this.node1 = node1;
		this.node2 = node2;
		this.directed = directed;
		graph.uf_addImplicitLink(this);
		node1.uf_addLink(this);
		node2.uf_addLink(this);
	}

	// Methods
	/** @return The parent (if the link is directed) or the first node (if the
	 * link is undirected).
	 * @consultation */
	public Node getNode1() {
		return node1;
	}

	/** @return  The child (if the link is directed) or the second node (if the
	 * link is undirected).
	 * @consultation */
	public Node getNode2() {
		return node2;
	}

	/** @param node <code>Node</code>.
	 * @return <code>true</code> if the link contains <code>node</code>.
	 * @consultation */
	public boolean contains(Node node) {
		return ((node1 == node) || (node2 == node));
	}

	/** @return <code>true</code> if the link is directed, false if it is
	 * undirected
	 * @consultation */
	public boolean isDirected() {
		return directed;
	}

	/** @return String */
	public String toString() {
		StringBuffer buffer = new StringBuffer(node1.toString());
		if (directed) {
			buffer.append(" --- ");
		} else {
			buffer.append(" --> ");
		}
		buffer.append(node2.toString());
		return buffer.toString();
	}

}
