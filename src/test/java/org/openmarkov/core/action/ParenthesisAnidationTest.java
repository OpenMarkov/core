package org.openmarkov.core.action;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

public class ParenthesisAnidationTest {

	private int numberOfParenthesis;
	
	private ProbNet probNet;
	
	@Before
	public void setUp() throws Exception {
		numberOfParenthesis = 0;
		this.probNet = getProbNet4Test();
	}

	@Test
	public void addParenthesisTest() {

		probNet.getPNESupport().setWithUndo(true);
		probNet.getPNESupport().openParenthesis();
		probNet.getPNESupport().openParenthesis();
		probNet.getPNESupport().openParenthesis();
		probNet.getPNESupport().openParenthesis();
		numberOfParenthesis = 4;
		
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == numberOfParenthesis);
	}
	
	@Test
	public void closeParenthesisTest(){

		probNet.getPNESupport().setWithUndo(true);
		probNet.getPNESupport().openParenthesis();
		probNet.getPNESupport().openParenthesis();
		probNet.getPNESupport().openParenthesis();
		probNet.getPNESupport().openParenthesis();
		numberOfParenthesis = 4;
		
		probNet.getPNESupport().closeParenthesis();
		probNet.getPNESupport().closeParenthesis();
		probNet.getPNESupport().closeParenthesis();
		numberOfParenthesis -= 3;
		
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == numberOfParenthesis);
	}
	
	@Test
	public void undoManagerTest1(){

		probNet.getPNESupport().setWithUndo(true);
		int numNullEdit = 0;
		/*
		 *  Edit list Scheme with NE = NullEdit; ( = OpenParenthesisEdit ; ) = Close ParenthesisEdit
		 *  
		 *  NE ( NE NE ( NE [( NE )] NE ) ) NE
		 */
		
		numNullEdit = doNullEdit(numNullEdit);
		
		probNet.getPNESupport().openParenthesis();
		
		numNullEdit = doNullEdit(numNullEdit);
		
		numNullEdit = doNullEdit(numNullEdit);
		
		probNet.getPNESupport().openParenthesis();
		
		numNullEdit = doNullEdit(numNullEdit);
		
		probNet.getPNESupport().openParenthesis();
		
		numNullEdit = doNullEdit(numNullEdit);
		
		probNet.getPNESupport().closeParenthesis();
		
		// NE ( NE NE ( NE ( NE )
		probNet.getPNESupport().undo();
		// NE ( NE NE ( NE 
		
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 2);
		
		assertTrue(probNet.getPNESupport().getUndoManager().editToBeUndone().getClass() == NullEdit.class);
		
		numNullEdit = doNullEdit(numNullEdit);
		
		probNet.getPNESupport().closeParenthesis();
		
		probNet.getPNESupport().closeParenthesis();
		
		numNullEdit = doNullEdit(numNullEdit);
		
		// NE ( NE NE ( NE NE ) ) NE
		
		assertTrue(numNullEdit == 7);
		
		probNet.getPNESupport().undo();
		
		// NE ( NE NE ( NE NE ) )
		
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 0);
		
		assertTrue(probNet.getPNESupport().getUndoManager().editToBeUndone().getClass() == CloseParenthesisEdit.class);
		
		probNet.getPNESupport().undo();
		
		// NE
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 0);
		
		assertTrue(((NullEdit) probNet.getPNESupport().getUndoManager().editToBeUndone()).getNumEdit() == 0);
	}
	
	@Test
	public void undoManagerTestEmptyParenthesis(){

		probNet.getPNESupport().setWithUndo(true);
		int numEdit = 0;
		numEdit = doNullEdit(numEdit);
		probNet.getPNESupport().openParenthesis();
		numEdit++;
		probNet.getPNESupport().closeParenthesis();
		numEdit++;
		probNet.getPNESupport().undoAndDelete();
		assertTrue(((NullEdit) probNet.getPNESupport().getUndoManager().editToBeUndone()).getNumEdit() == 0);
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 0);
	}
	
	@Test
	public void undoManagerWithUndoAndDelete(){

		probNet.getPNESupport().setWithUndo(true);
		int numNullEdit = 0;
		/*
		 *  Edit list Scheme with NE = NullEdit; ( = OpenParenthesisEdit ; ) = Close ParenthesisEdit; [] = Deleted
		 *  
		 *  ( NE [NE] [( NE )] NE ( NE ) NE )
 		 */
		
		probNet.getPNESupport().openParenthesis();
		
		numNullEdit = doNullEdit(numNullEdit);
		numNullEdit = doNullEdit(numNullEdit);
		
		probNet.getPNESupport().openParenthesis();
		
		numNullEdit = doNullEdit(numNullEdit);
		
		probNet.getPNESupport().closeParenthesis();

		// ( NE NE ( NE )		
		probNet.getPNESupport().undoAndDelete();
		// ( NE NE 
		
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 1);
		
		assertTrue(probNet.getPNESupport().getUndoManager().editToBeUndone().getClass() == NullEdit.class);
		
		// ( NE NE 
		probNet.getPNESupport().undo();
		// ( NE 
		
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 1);
		
		assertTrue(probNet.getPNESupport().getUndoManager().editToBeUndone().getClass() == NullEdit.class);
		
		numNullEdit = doNullEdit(numNullEdit);
		
		probNet.getPNESupport().openParenthesis();
		numNullEdit = doNullEdit(numNullEdit);
		probNet.getPNESupport().closeParenthesis();
		// ( NE NE ( NE )

		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 1);
		assertTrue(probNet.getPNESupport().getUndoManager().editToBeUndone().getClass() == CloseParenthesisEdit.class);
		
		numNullEdit = doNullEdit(numNullEdit);
		// ( NE NE ( NE ) NE
		
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 1);
		assertTrue(probNet.getPNESupport().getUndoManager().editToBeUndone().getClass() == NullEdit.class);
		probNet.getPNESupport().closeParenthesis();
		
		// ( NE NE ( NE ) NE )
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 0);
		assertTrue(probNet.getPNESupport().getUndoManager().editToBeUndone().getClass() == CloseParenthesisEdit.class);
		
		probNet.getPNESupport().undoAndDelete();
		
		// empty
		assertTrue(probNet.getPNESupport().getOpenParenthesisStack().size() == 0);
		assertTrue(probNet.getPNESupport().getUndoManager().editToBeUndone() == null);

	}
	
	private int doNullEdit(int numNullEdit){
		NullEdit edit = new NullEdit(probNet, numNullEdit);
		
		try {
			probNet.getPNESupport().doEdit(edit);
			numNullEdit++;
		} catch (DoEditException | NonProjectablePotentialException
				| WrongCriterionException e) {
			e.printStackTrace();
		}
		return numNullEdit;
	}

	private static ProbNet getProbNet4Test() {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		// Variables
		Variable varA = new Variable("A", "absent", "mild", "moderate", "severe");
		Variable varB = new Variable("B", "yes", "its possible", "maybe not", "no");
		Variable varC = new Variable("C");

		// Nodes
		Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
		Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
		Node nodeC = probNet.addNode(varC, NodeType.CHANCE);

		nodeB.getVariable().setPartitionedInterval(new PartitionedInterval(nodeB.getVariable().getDefaultInterval(4), nodeB.getVariable().getDefaultBelongs(4)));

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeA, nodeB, true);
		probNet.addLink(nodeA, nodeC, true);

		// Potentials
		UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeA.setPotential(potA);

		UniformPotential potB = new UniformPotential(Arrays.asList(varB, varA), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeB.setPotential(potB);

		UniformPotential potC = new UniformPotential(Arrays.asList(varC, varA), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeC.setPotential(potC);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

}
