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
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoClosedPathTest {
    
    private ProbNet directedNet;
    private ProbNet undirectedNet;
    
    @BeforeEach public void setUp() {
        directedNet = ConstraintsTests.getTestProbNetDirected();
        undirectedNet = ConstraintsTests.getTestProbNetUndirected();
    }
    
    @Test public void testCheckProbNet() {
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

    
}
