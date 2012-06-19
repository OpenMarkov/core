/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.graph;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	NodeTest.class,
	GraphTest.class,
	LinkTest.class
})

/** Auxiliary class that create a graph for test. */
public class GraphsTests {
	
	/** Create a <code>Graph</code> for test in package 
	 * <code>tests.openmarkov.graphs</code>.<p>
	 * Graph description:<ul>
	 * <li> Nodes are created in this order: nodeA, nodeB, nodeC, nodeD
	 * <li> The object in each node is an <code>String</code>:
	 * nodeA("A"), nodeB("B"), nodeC("C"), nodeD("D")
	 * <li> Directed links: A->B, B->C
	 * <li> Undirected links: B--D
	 * </ul>
	 * @return <code>Graph</code> */
	public static Graph createTestGraph() {
		Graph graph = new Graph();
		Node nodeA = new Node(graph, "A");
		Node nodeB = new Node(graph, "B");
		Node nodeC = new Node(graph, "C");
		Node nodeD = new Node(graph, "D");
		
		graph.addLink(nodeA, nodeB, true);
		graph.addLink(nodeB, nodeC, true);
		graph.addLink(nodeB, nodeD, false);

		return graph;
	}
	
}
