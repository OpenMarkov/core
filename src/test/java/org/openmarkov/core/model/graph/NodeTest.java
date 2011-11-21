package org.openmarkov.core.model.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;


/** Uses <code>GraphsTests</code> to create a simple <code>Graph</code>. 
 * Test elementary methods in <code>Node</code> class. */
public class NodeTest {

	private Graph graph;
	
	private ArrayList<Node> nodes;
	
	private Node nodeA, nodeB, nodeC, nodeD;
	
	@Before
	public void setUp() throws Exception {
		graph = GraphsTests.createTestGraph();
		nodes = graph.getNodes();
		nodeA = nodes.get(0);
		nodeB = nodes.get(1);
		nodeC = nodes.get(2);
		nodeD = nodes.get(3);
	}

	@Test
	public void testNode() {
        // Test created nodes in GraphsTest.createTestGraph()
		assertEquals(4, nodes.size());
		assertTrue(((String)nodeA.getObject()).contains("A"));
		assertTrue(((String)nodeB.getObject()).contains("B"));
		assertTrue(((String)nodeC.getObject()).contains("C"));
		assertTrue(((String)nodeD.getObject()).contains("D"));
	}
	
	@Test
	public void testIsChild() {
		assertTrue(nodeA.isChild(nodeB));
		assertTrue(nodeB.isChild(nodeC));
	}

	@Test
	public void testIsParent() {
		assertTrue(nodeB.isParent(nodeA));
		assertTrue(nodeC.isParent(nodeB));
	}
	
	@Test
	public void testIsSibling() {
		assertTrue(nodeB.isSibling(nodeD));
		assertTrue(nodeD.isSibling(nodeB));
	}
	
	@Test
	public void testGetNeighbors() {
		ArrayList<Node> neighborsB = nodeB.getNeighbors();
		assertEquals(3, neighborsB.size());
		assertTrue(neighborsB.contains(nodeA));
		assertTrue(neighborsB.contains(nodeC));
		assertTrue(neighborsB.contains(nodeD));
	}
	
	@Test
	public void testGetLinks() {
		graph.makeLinksExplicit(false);
		ArrayList<Link> links = nodeB.getLinks();
		assertEquals(3, links.size());
		int directed = 0;
		int undirected = 0;
		for (Link link : links) {
			assertTrue(link.contains(nodeB));
			if (link.isDirected()) {
				directed++;
			} else {
				undirected++;
				assertTrue(link.contains(nodeD));
			}
		}
		assertEquals(2, directed);
		assertEquals(1, undirected);
	}
	
}
