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
import org.openmarkov.core.action.VariableTypeEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyNumericVariablesTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() throws NodeNotFoundException {
        influenceDiagram = ConstraintsTests.getNumericInfluenceDiagram();
    }
    
    @Test public void testCheckProbNet() {
        OnlyNumericVariables testedConstraint = new OnlyNumericVariables();
        influenceDiagram.addConstraint(testedConstraint);
        assertTrue(testedConstraint.checkProbNet(influenceDiagram));
        
        influenceDiagram.addNode(new Variable("E", 2), NodeType.CHANCE);
        assertFalse(testedConstraint.checkProbNet(influenceDiagram));
    }
    
    @Test
    public void testUndoableEditWillHappen() throws NonProjectablePotentialException, WrongCriterionException, DoEditException, ConstraintViolationException {
        
        PNESupport pNESupport = new PNESupport(false);
        PNConstraint constraint = new OnlyNumericVariables();
        influenceDiagram.addConstraint(constraint);
        pNESupport.addUndoableEditListener(constraint);
        
        Variable vc1 = new Variable("E");
        // test no exception in legal edit
        AddNodeEdit legalAdd = new AddNodeEdit(influenceDiagram, vc1, NodeType.DECISION);
        // add the node E (decision + numeric)
        pNESupport.announceEdit(legalAdd);
        legalAdd.doEdit();
        
        
        Variable vc2 = new Variable("F", 3);
        // test exception in ilegal edit
        AddNodeEdit ilegalAdd = new AddNodeEdit(influenceDiagram, vc2, NodeType.DECISION);
        // add the node F (decision + finite state)
        try {
            pNESupport.announceEdit(ilegalAdd);
            ilegalAdd.doEdit();
            fail();
        } catch (ConstraintViolationException e) {
            // An exception should have been thrown
        }
        
        Node node = influenceDiagram.getNode(vc1);
        VariableTypeEdit ilegalEdit = new VariableTypeEdit(node, VariableType.DISCRETIZED);
        // add the node F (decision + finite state)
        try {
            pNESupport.announceEdit(ilegalEdit);
            ilegalEdit.doEdit();
            fail();
        } catch (ConstraintViolationException e) {
            // An exception should have been thrown
        }
        
    }
    
}
