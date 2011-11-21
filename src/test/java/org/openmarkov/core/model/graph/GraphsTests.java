package org.openmarkov.core.model.graph;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	NodeTest.class,
	GraphTest.class
})

/** Auxiliary class that create a graph for test. */
public class GraphsTests {
	
	/** Create a <code>Graph</code> for test in package 
	 * <code>tests.openmarkov.graphs</code>.<p>
	 * Graph description:<ul>
	 * <li> Nodes created in this order: nodeA, nodeB, nodeC, nodeD
	 * <li> The object in each node is an <code>String</code>:
	 * nodeA("A"), nodeB("B"), nodeC("C"), nodeD("D")
	 * <li> Directed links: A->B, B->C
	 * <li> Undirected links: B--D
	 * </ul>
	 * @return <code>Graph</code> */
	public static Graph createTestGraph() {
		Graph graph = new Graph();
		String a = new String("A");
		String b = new String("B");
		String c = new String("C");
		String d = new String("D");
		Node nodeA = new Node(graph, a);
		Node nodeB = new Node(graph, b);
		Node nodeC = new Node(graph, c);
		Node nodeD = new Node(graph, d);
		
		graph.addLink(nodeA, nodeB, true);
		graph.addLink(nodeB, nodeC, true);
		graph.addLink(nodeB, nodeD, false);

		return graph;
	}
	
}
