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
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoClosedPathTest {
    
    private ProbNet directedNet;
    private ProbNet undirectedNet;
    
    @BeforeEach public void setUp() throws NodeNotFoundException {
        directedNet = ConstraintsTests.getTestProbNetDirected();
        undirectedNet = ConstraintsTests.getTestProbNetUndirected();
    }
    
    @Test public void testCheckProbNet() throws NodeNotFoundException {
        NoClosedPath testedConstraint = new NoClosedPath();
        directedNet.addConstraint(testedConstraint);
        assertTrue(testedConstraint.checkProbNet(directedNet));
        
        Variable varA = directedNet.getVariable("A");
        Variable varC = directedNet.getVariable("C");
        directedNet.addLink(varA, varC, true);
        assertFalse(testedConstraint.checkProbNet(directedNet));
        
        
        undirectedNet.addConstraint(testedConstraint);
        assertTrue(testedConstraint.checkProbNet(undirectedNet));
        
        varA = undirectedNet.getVariable("A");
        varC = undirectedNet.getVariable("C");
        undirectedNet.addLink(varA, varC, false);
        assertFalse(testedConstraint.checkProbNet(undirectedNet));
        
    }
    
    @Test public void testUndoableEditWillHappen() throws Exception {
        
        PNESupport pNESupport = new PNESupport(false);
        PNConstraint constraint = new NoClosedPath();
        
        undirectedNet.addConstraint(constraint);
        pNESupport.addUndoableEditListener(constraint);
        
        // do legal add
        Variable vC = undirectedNet.getVariable("C");
        new AddNodeEdit(undirectedNet, new Variable("D"), NodeType.UTILITY, new Point2D.Double()).doEdit();
        Variable vD = undirectedNet.getVariable("D");
        
        // creates a link from C - D
        AddLinkEdit legalEdit = new AddLinkEdit(undirectedNet, vC, vD, false);
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        Variable vA = undirectedNet.getVariable("C");
        // creates a link from A-C
        AddLinkEdit ilegalEdit = new AddLinkEdit(undirectedNet, vA, vC, false);
        try {
            pNESupport.announceEdit(ilegalEdit);
            ilegalEdit.doEdit();
            fail();
        } catch (ConstraintViolationException e) {
            // The constraint should have failed
        }
        
        // creates a link from A->C
        ilegalEdit = new AddLinkEdit(undirectedNet, vA, vC, true);
        try {
            pNESupport.announceEdit(ilegalEdit);
            ilegalEdit.doEdit();
        } catch (ConstraintViolationException e) {
            // The constraint should have failed
        }
    }
    
}
