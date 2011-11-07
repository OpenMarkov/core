package org.openmarkov.core.model.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;


/** Uses <code>GraphsTests</code> to create a simple <code>Graph</code>. 
 * Test elementary methods in <code>Graph</code> class. */
public class GraphTest {

	private Graph graph;
	
	private Node nodeA, nodeB, nodeC, nodeD;
	
	private ArrayList<Node> nodes;
	
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
	public void testMakeLinksExplicit() {
		// Part 1: test that not exists explicit links before use
		//  Graph.makeLinksExplicit()
		for (Node node : nodes) {
			ArrayList<Link> linksNode = node.getLinks();
			assertEquals(0, linksNode.size());
		}
		
		// Test that number of links does not change after and before use of
		// Graph.makeLinksExplicit()
		int numLinksBefore = countLinks(graph);
		graph.makeLinksExplicit(false);
		int numLinksAfter = countLinks(graph);
		assertEquals(numLinksBefore, numLinksAfter);

		// Part 2: test that all the links that already exist in graph are 
		//  properly translated
		// test directed link between nodeA and nodeB
		ArrayList<Link> linksA = graph.getLinks();
		assertEquals(3, linksA.size());
		linksA = nodeA.getLinks();
		assertEquals(1, linksA.size());
		Link linkA = linksA.get(0);
		assertTrue(linkA.isDirected());
		// test link A->B is in nodeA
		assertEquals(linkA.getNode1(), nodeA);
		assertEquals(linkA.getNode2(), nodeB);
		
		// test all the nodeB links
		ArrayList<Link> linksB = nodeB.getLinks();
		assertEquals(3, linksB.size());
		boolean linkAB = false;
		boolean linkBC = false;
		boolean linkBD = false;
		for (Link linkB : linksB) {
			if (linkB.contains(nodeA)) {
				linkAB = true;
				// test link A->B is also in nodeB
				assertEquals(linkA, linkB);// The same link object in both nodes
			}
			if (linkB.contains(nodeC)) {
				linkBC = true;
				assertTrue(linkB.getNode1().equals(nodeB));
				assertTrue(linkB.getNode2().equals(nodeC));
				assertTrue(linkB.isDirected());
			}
			if (linkB.contains(nodeD)) {
				linkBD = true;
				assertTrue(linkB.contains(nodeB));
				assertTrue(linkB.contains(nodeD));
				assertTrue(!linkB.isDirected());				
			}
		}
		assertTrue(linkAB);
		assertTrue(linkBC);
		assertTrue(linkBD);
		
		// Part 3: test that new links after Graph.makeLinksExplicit() are 
		//  created in both ways: implicit and explicit.
		graph.addLink(nodeA, nodeC, false);
		// test explicit links
		linksA = nodeA.getLinks();
		assertEquals(2, linksA.size());
		for (Link link : linksA) {
			if (link.contains(nodeC)) {
				assertTrue(link.contains(nodeA));
				ArrayList<Link> linksC = nodeC.getLinks();
				assertEquals(2, linksC.size());
				for (Link linkC : linksC) {
					if (linkC.contains(nodeA)) {
						assertEquals(link, linkC);
					}
				}
			}
		}
		// test implicit links
		assertTrue(nodeA.isSibling(nodeC));
		assertTrue(nodeC.isSibling(nodeA));
	}

	@Test
	public void testRemoveLinkLink() {
		graph.makeLinksExplicit(false);
		
		Link linkAB = nodeA.getLinks().get(0);
		graph.removeLink(linkAB);
		
		// test implicit links
		assertEquals(0, nodeA.getChildren().size());
		assertEquals(0, nodeB.getParents().size());
		
		// test explicit links
		assertEquals(0, nodeA.getLinks().size());
		assertEquals(2, nodeB.getLinks().size());
	}

	@Test
	public void testRemoveLinkNodeNodeBoolean() {
		// Test before calling graph.makeLinksExplicit()
		graph.removeLink(nodeB, nodeC, true);
		ArrayList<Node> nodeBChildren = nodeB.getChildren();
		assertEquals(0, nodeBChildren.size());
		ArrayList<Node> nodeCParents = nodeC.getParents();
		assertEquals(0, nodeCParents.size());
		// Test after calling graph.makeLinksExplicit()
		graph.makeLinksExplicit(false);
		graph.removeLink(nodeB, nodeD, true); // this link does not exists
		
	}

	@Test
	public void testRemoveNode() {
		graph.removeNode(nodeA);
		assertEquals(2, countLinks(graph));
		assertEquals(3, graph.getNumNodes());
		graph.removeNode(nodeB);
		assertEquals(0, countLinks(graph));
		assertEquals(2, graph.getNumNodes());
	}

	@Test
	public void testGetLink() {
		graph.makeLinksExplicit(false);
		// test the directed link between nodeA and nodeB
		assertNull(graph.getLink(nodeA, nodeB, false));
		Link link = graph.getLink(nodeA, nodeB, true);
		assertEquals(nodeA, link.getNode1());
		assertEquals(nodeB, link.getNode2());
		assertTrue(link.isDirected());
		// test the undirected link between nodeB and nodeD
		assertNull(graph.getLink(nodeB, nodeD, true));
		link = graph.getLink(nodeD, nodeB, false);
		assertTrue(link.contains(nodeD));
		assertTrue(link.contains(nodeB));
	}

	@Test
	public void testCopy() {
		Graph graphCopy = graph.copy();
		// test nodes
		assertEquals(graph.getNumNodes(), graphCopy.getNumNodes());
		ArrayList<Node> nodesCopy = graphCopy.getNodes();
		for (Node node : nodes) {
			Object objectNode = node.getObject();
			boolean isEqual = false;
			for (Node nodeCopy : nodesCopy) {
				Object objectNodeCopy = nodeCopy.getObject();
				isEqual = isEqual || objectNode.equals(objectNodeCopy);
				if (isEqual) { // test links
					assertEquals(
						nodeCopy.getNumChildren(), node.getNumChildren());
					assertEquals(
						nodeCopy.getNumParents(), node.getNumParents());
					assertEquals(
						nodeCopy.getNumSiblings(), node.getNumSiblings());
					break;
				}
			}
			assertTrue(isEqual);
		}
	}

	@Test
	public void testGetLinks() {
		int numLinks = countLinks(graph);
		ArrayList<Link> links = graph.getLinks();
		assertEquals(links.size(), numLinks);
		for (Link link : links) {
			assertTrue(link.contains(nodeB)); // all links contains nodeB
			if (!link.isDirected()) {
				assertTrue(link.contains(nodeD));
			} else {
				if (!link.contains(nodeA)) {
					assertEquals(link.getNode2(), nodeC);
				} else {
					assertEquals(link.getNode2(), nodeB);
				}
			}
		}
	}

	@Test
	public void testHasNeighborsOutside() {
		ArrayList<Node> neighborsB = new ArrayList<Node>();
		neighborsB.add(nodeA);
		neighborsB.add(nodeC);
		neighborsB.add(nodeD);
		assertFalse(graph.hasNeighborsOutside(nodeB, neighborsB));
		neighborsB.remove(nodeD);
		assertTrue(graph.hasNeighborsOutside(nodeB, neighborsB));
		neighborsB.add(nodeD);
		neighborsB.remove(nodeA);
		assertTrue(graph.hasNeighborsOutside(nodeB, neighborsB));
	}

	@Test
	public void testExistsPath() {
		assertTrue(graph.existsPath(nodeC, nodeA, false));
		assertFalse(graph.existsPath(nodeC, nodeA, true));
		assertFalse(graph.existsPath(nodeA, nodeD, true));
		assertTrue(graph.existsPath(nodeD, nodeA, false));
	}

	@Test
	public void testMarry() {
		// Marry 2 nodes
		ArrayList<Node> nodesToMarry = new ArrayList<Node>();
		nodesToMarry.add(nodeA);
		nodesToMarry.add(nodeB);
		int numLinks = countLinks(graph);
		graph.marry(nodesToMarry);
		assertEquals(numLinks + 1, countLinks(graph));
		assertFalse(graph.useExplicitLinks()); // it does not add explicit links
		assertEquals(1, nodeA.getNumSiblings());
		assertTrue(nodeA.getSiblings().contains(nodeB));
		assertTrue(nodeB.getSiblings().contains(nodeA));
		assertEquals(2, nodeB.getNumSiblings());
		
		// Marry 3 nodes. It exists 2 undirected links and it must create only 1
		nodesToMarry.add(nodeD);
		numLinks = countLinks(graph);
		graph.marry(nodesToMarry);		
		assertEquals(numLinks + 1, countLinks(graph));
		assertEquals(2, nodeA.getNumSiblings());
		assertEquals(2, nodeB.getNumSiblings());
		assertEquals(2, nodeD.getNumSiblings());
		assertTrue(nodeA.getSiblings().contains(nodeD));
	}

	/** @return Number of links of  */
	private int countLinks(Graph graph) {
		ArrayList<Node> nodes = graph.getNodes();
		int numLinks = 0;
		if (graph.useExplicitLinks()) {
			for (Node node : nodes) {
				numLinks += node.getNumLinks();
			}
		} else {
			for (Node node : nodes) {
				numLinks += node.getNumChildren() + node.getNumParents() 
				    + node.getNumSiblings();
			}
		}
		numLinks /= 2;
		return numLinks;
	}

}
