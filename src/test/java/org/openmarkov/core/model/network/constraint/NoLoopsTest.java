/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.test.TestSpeed;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoLoopsTest {
    
    private ProbNet directedNet;
    private ProbNet undirectedNet;
    
    @BeforeEach public void setUp() {
        directedNet = ConstraintsTests.getTestProbNetDirected();
        undirectedNet = ConstraintsTests.getTestProbNetUndirected();
    }
    
    @Tag(TestSpeed.SLOW)
    @Test public void testCheckProbNet() {
        NoLoops testedConstraints = new NoLoops();
        directedNet.addConstraint(testedConstraints);
        assertTrue(testedConstraints.checkProbNet(directedNet));
        
        Variable varA = directedNet.getVariable("A");
        Variable varC = directedNet.getVariable("C");
        directedNet.addLink(varA, varC, true);
        
        assertFalse(testedConstraints.checkProbNet(directedNet));
        
        varA = undirectedNet.getVariable("A");
        Variable varB = undirectedNet.getVariable("B");
        varC = undirectedNet.getVariable("C");
        undirectedNet.addLink(varA, varC, false);
        undirectedNet.removeLink(varA, varC, false);
        undirectedNet.removeLink(varB, varC, false);
        undirectedNet.addLink(varB, varC, true);
        undirectedNet.addLink(varA, varC, true);
        assertFalse(testedConstraints.checkProbNet(directedNet));
        
        undirectedNet.removeLink(varA, varB, false);
        undirectedNet.addLink(varA, varB, true);
        assertFalse(testedConstraints.checkProbNet(directedNet));
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void testUndoableEditWillHappen() throws DoEditException.ConstraintViolated {
        
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
        } catch (DoEditException.ConstraintViolated e) {
            // The constraint should have failed
        }
        
        // creates a link from A->C
        ilegalEdit = new AddLinkEdit(undirectedNet, vA, vC, true);
        try {
            pNESupport.announceEdit(ilegalEdit);
            ilegalEdit.doEdit();
            fail();
        } catch (DoEditException.ConstraintViolated e) {
            // The constraint should have failed
        }
    }
}

