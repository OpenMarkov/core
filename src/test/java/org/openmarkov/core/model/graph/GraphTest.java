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
		// Part 1: test that there are not explicit links before making links explicit
		for (Node node : nodes) {
			ArrayList<Link> linksNode = node.getLinks();
			assertEquals(0, linksNode.size());
		}
		
		// Test that the number of links does not change after making them explicit
		int numLinksBefore = countLinks(graph);
		graph.makeLinksExplicit(false);
		int numLinksAfter = countLinks(graph);
		assertEquals(numLinksBefore, numLinksAfter);

		// Part 2: examine the explicit links and compare them with the implicit ones
		// Check that the explicit link A->B
		ArrayList<Link> linksOfA = graph.getLinks();
		assertEquals(3, linksOfA.size());
		linksOfA = nodeA.getLinks();
		assertEquals(1, linksOfA.size());
		Link linkABinA = linksOfA.get(0);
		assertTrue(linkABinA.isDirected());
		assertEquals(linkABinA.getNode1(), nodeA);
		assertEquals(linkABinA.getNode2(), nodeB);
		
		// test all the links involving node B
		ArrayList<Link> linksOfB = nodeB.getLinks();
		assertEquals(3, linksOfB.size());
		boolean existsLinkABinB = false;
		boolean existsLinkBCinB = false;
		boolean existsLinkBDinB = false;
		for (Link link : linksOfB) {
			if (link.contains(nodeA)) {
				existsLinkABinB = true;
				// test link A->B is also in nodeB
				assertEquals(linkABinA, link);
			}
			if (link.contains(nodeC)) {
				existsLinkBCinB = true;
				assertTrue(link.getNode1().equals(nodeB));
				assertTrue(link.getNode2().equals(nodeC));
				assertTrue(link.isDirected());
			}
			if (link.contains(nodeD)) {
				existsLinkBDinB = true;
				assertTrue(link.contains(nodeB));
				assertTrue(link.contains(nodeD));
				assertTrue(!link.isDirected());				
			}
		}
		assertTrue(existsLinkABinB);
		assertTrue(existsLinkBCinB);
		assertTrue(existsLinkBDinB);
		
		// Part 3: test that links created after Graph.makeLinksExplicit()  
		// are created properly.
		// Creates an undirected link between A and C
		graph.addLink(nodeA, nodeC, false);
		// checks that the explict link is in both A and C
		linksOfA = nodeA.getLinks();
		assertEquals(2, linksOfA.size());
		ArrayList<Link> linksOfC = nodeC.getLinks();
		assertEquals(2, linksOfC.size());
		for (Link link : linksOfA) {
			if (link.contains(nodeC)) {
				Link linkACinA = link;
				assertTrue(linkACinA.contains(nodeA));
				for (Link link2 : linksOfC) {
					if (link2.contains(nodeA)) {
						assertEquals(link, link2);
					}
				}
			}
		}
		
		// test the implicit links
		assertTrue(nodeA.isSibling(nodeC));
		assertTrue(nodeC.isSibling(nodeA));
	}

	@Test
	public void testRemoveExplicitLink() {
		graph.makeLinksExplicit(false);
		
		Link linkABinA = nodeA.getLinks().get(0);
		graph.removeLink(linkABinA);
		
		// check that there is no implicit link between A and B
		assertEquals(0, nodeA.getChildren().size());
		assertEquals(0, nodeB.getParents().size());
		
		// test that there is no explicit link between A and B
		assertEquals(0, nodeA.getLinks().size());
		assertEquals(2, nodeB.getLinks().size());
	}

	@Test
	public void testRemoveImplicitLink() {
		graph.removeLink(nodeB, nodeC, true);
		// check that there is no implicit link between B and C
		ArrayList<Node> childrenOfB = nodeB.getChildren();
		assertEquals(0, childrenOfB.size());
		ArrayList<Node> parentsOfC = nodeC.getParents();
		assertEquals(0, parentsOfC.size());
	}

	@Test
	public void testRemoveNode() {
		// check the number of nodes and links after removing node A
		graph.removeNode(nodeA);
		assertEquals(2, countLinks(graph));
		assertEquals(3, graph.getNumNodes());
		// check the number of nodes and links after removing node B
		graph.removeNode(nodeB);
		assertEquals(0, countLinks(graph));
		assertEquals(2, graph.getNumNodes());
	}

	@Test
	public void testGetLink() {
		graph.makeLinksExplicit(false);
		// check  that link A->B is directed
		assertNull(graph.getLink(nodeA, nodeB, false));
		Link link = graph.getLink(nodeA, nodeB, true);
		assertEquals(nodeA, link.getNode1());
		assertEquals(nodeB, link.getNode2());
		assertTrue(link.isDirected());
		// check  that link D-B is undirected
		assertNull(graph.getLink(nodeB, nodeD, true));
		link = graph.getLink(nodeD, nodeB, false);
		assertTrue(link.contains(nodeD));
		assertTrue(link.contains(nodeB));
	}

	@Test
	public void testCopy() {
		Graph graphCopy = graph.copy();
		// check that the copied graph has the same nodes and implicit links
		// than the original one
		assertEquals(graph.getNumNodes(), graphCopy.getNumNodes());
		ArrayList<Node> nodesCopy = graphCopy.getNodes();
		for (Node node : nodes) {
			Object objectNode = node.getObject();
			boolean isEqual = false;
			for (Node nodeCopy : nodesCopy) {
				Object objectNodeCopy = nodeCopy.getObject();
				isEqual = isEqual || objectNode.equals(objectNodeCopy);
				if (isEqual) { // test implicit links
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
		// check all the links in the graph directed and undirected ones invoking getLinks()
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
		// check if node B has more neighbors than ones in the list given
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

	/** @return Number of links of the graph */
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
