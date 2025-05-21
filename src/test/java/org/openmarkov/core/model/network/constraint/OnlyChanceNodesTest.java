/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyChanceNodesTest {
    
    // Attributes
    private ProbNet influenceDiagram;
    
    private ProbNet probNetDirected;
    
    // Methods
    @BeforeEach public void setUp() throws NodeNotFoundException {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
        probNetDirected = ConstraintsTests.getTestProbNetDirected();
    }
    
    /**
     * Checks or not all the <code>probNet</code> in different situations in
     * <code>OnlyDirectedLinks</code> constructor.
     */
    @Test public void testCheckProbNet() {
        // test only directed links insertions without checking.
        assertFalse(new OnlyChanceNodes().checkProbNet(influenceDiagram));
    }
    
    /**
     * Checks veto
     */
    @Test
    public void testUndoableEditWillHappen() throws NonProjectablePotentialException, ConstraintViolationException, WrongCriterionException {
        
        // Add constraints as listeners.
        PNESupport pNESupport = new PNESupport(false);
        probNetDirected.addConstraint(new OnlyChanceNodes());
        List<PNConstraint> constraints = probNetDirected.getConstraints();
        for (PNConstraint constraint : constraints) { // sets listeners
            pNESupport.addUndoableEditListener(constraint);
        }
        // Create edits
        Variable vd = new Variable("D", 0);
        Variable ve = new Variable("E", 0);
        
        // test no exception in legal edit
        AddNodeEdit legalEdit = new AddNodeEdit(probNetDirected, ve, NodeType.CHANCE);
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        // test exception in no legal edit
        AddNodeEdit ilegalEdit = new AddNodeEdit(probNetDirected, vd, NodeType.DECISION);
        try {
            pNESupport.announceEdit(ilegalEdit);
            fail();
        } catch (Exception cve) {
            // An exception should have been thrown.
        }
    }
    
}
