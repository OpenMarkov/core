/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ParenthesisAnidationTest {
    
    private int numberOfParenthesis;
    
    private ProbNet probNet;
    
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
        
        nodeB.getVariable().setPartitionedInterval(new PartitionedInterval(nodeB.getVariable().getDefaultInterval(4),
                                                                           Variable.getDefaultBelongs(4)));
        
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
    
    @BeforeEach public void setUp() {
        numberOfParenthesis = 0;
        this.probNet = getProbNet4Test();
    }
    
    @Test public void addParenthesisTest() {
        
        probNet.getPNESupport().setWithUndo(true);
        probNet.getPNESupport().openParenthesis();
        probNet.getPNESupport().openParenthesis();
        probNet.getPNESupport().openParenthesis();
        probNet.getPNESupport().openParenthesis();
        numberOfParenthesis = 4;
        
        assertEquals(probNet.getPNESupport().getOpenParenthesisStack().size(), numberOfParenthesis);
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void closeParenthesisTest() {
        
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
        
        assertEquals(probNet.getPNESupport().getOpenParenthesisStack().size(), numberOfParenthesis);
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void undoManagerTest1() throws DoEditException {
        
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
        
        assertEquals(2, probNet.getPNESupport().getOpenParenthesisStack().size());
        
        assertSame(NullEdit.class, probNet.getPNESupport().getUndoManager().editToBeUndone().getClass());
        
        numNullEdit = doNullEdit(numNullEdit);
        
        probNet.getPNESupport().closeParenthesis();
        
        probNet.getPNESupport().closeParenthesis();
        
        numNullEdit = doNullEdit(numNullEdit);
        
        // NE ( NE NE ( NE NE ) ) NE
        
        assertEquals(7, numNullEdit);
        
        probNet.getPNESupport().undo();
        
        // NE ( NE NE ( NE NE ) )
        
        assertTrue(probNet.getPNESupport().getOpenParenthesisStack().isEmpty());
        
        assertSame(CloseParenthesisEdit.class, probNet.getPNESupport().getUndoManager().editToBeUndone().getClass());
        
        probNet.getPNESupport().undo();
        
        // NE
        assertTrue(probNet.getPNESupport().getOpenParenthesisStack().isEmpty());
        
        assertEquals(0, ((NullEdit) probNet.getPNESupport().getUndoManager().editToBeUndone()).getNumEdit());
    }
    
    @Test
    public void undoManagerTestEmptyParenthesis() throws DoEditException {
        probNet.getPNESupport().setWithUndo(true);
        int numEdit = 0;
        numEdit = doNullEdit(numEdit);
        probNet.getPNESupport().openParenthesis();
        numEdit++;
        probNet.getPNESupport().closeParenthesis();
        numEdit++;
        probNet.getPNESupport().undoAndDelete();
        assertEquals(0, ((NullEdit) probNet.getPNESupport().getUndoManager().editToBeUndone()).getNumEdit());
        assertTrue(probNet.getPNESupport().getOpenParenthesisStack().isEmpty());
    }
    
    @Test
    public void undoManagerWithUndoAndDelete() throws DoEditException {
        
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
        
        assertEquals(1, probNet.getPNESupport().getOpenParenthesisStack().size());
        
        assertSame(NullEdit.class, probNet.getPNESupport().getUndoManager().editToBeUndone().getClass());
        
        // ( NE NE
        probNet.getPNESupport().undo();
        // ( NE
        
        assertEquals(1, probNet.getPNESupport().getOpenParenthesisStack().size());
        
        assertSame(NullEdit.class, probNet.getPNESupport().getUndoManager().editToBeUndone().getClass());
        
        numNullEdit = doNullEdit(numNullEdit);
        
        probNet.getPNESupport().openParenthesis();
        numNullEdit = doNullEdit(numNullEdit);
        probNet.getPNESupport().closeParenthesis();
        // ( NE NE ( NE )
        
        assertEquals(1, probNet.getPNESupport().getOpenParenthesisStack().size());
        assertSame(CloseParenthesisEdit.class, probNet.getPNESupport().getUndoManager().editToBeUndone().getClass());
        
        numNullEdit = doNullEdit(numNullEdit);
        // ( NE NE ( NE ) NE
        
        assertEquals(1, probNet.getPNESupport().getOpenParenthesisStack().size());
        assertSame(NullEdit.class, probNet.getPNESupport().getUndoManager().editToBeUndone().getClass());
        probNet.getPNESupport().closeParenthesis();
        
        // ( NE NE ( NE ) NE )
        assertTrue(probNet.getPNESupport().getOpenParenthesisStack().isEmpty());
        assertSame(CloseParenthesisEdit.class, probNet.getPNESupport().getUndoManager().editToBeUndone().getClass());
        
        probNet.getPNESupport().undoAndDelete();
        
        // empty
        assertTrue(probNet.getPNESupport().getOpenParenthesisStack().isEmpty());
        assertNull(probNet.getPNESupport().getUndoManager().editToBeUndone());
        
    }
    
    private int doNullEdit(int numNullEdit) throws DoEditException {
        NullEdit edit = new NullEdit(probNet, numNullEdit);
        probNet.getPNESupport().doEdit(edit);
        numNullEdit++;
        return numNullEdit;
    }
    
}
