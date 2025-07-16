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
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.test.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoCyclesTest {
    
    private ProbNet probNetDirected;
    
    @BeforeEach public void setUp() {
        probNetDirected = ConstraintsTests.getTestProbNetDirected();
    }
    
    /**
     * Checks or not all the <code>probNet</code> in different situations in
     * <code>OnlyDirectedLinks</code> constructor.
     *
     */
    @Tag(TestSpeed.SLOW)
    @Test public void testCheckProbNet() {
        NoCycle testedConstraint = new NoCycle();
        probNetDirected.addConstraint(testedConstraint);
        Variable va = probNetDirected.getNode("A", NodeType.CHANCE).getVariable();
        Variable vc = probNetDirected.getNode("C", NodeType.CHANCE).getVariable();
        
        assertTrue(testedConstraint.checkProbNet(probNetDirected));
        new AddLinkEdit(probNetDirected, vc, va, true).doEdit();
        assertFalse(testedConstraint.checkProbNet(probNetDirected));
    }
    
}
