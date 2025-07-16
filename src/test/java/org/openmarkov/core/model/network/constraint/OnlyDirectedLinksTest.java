/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyDirectedLinksTest {
    
    private ProbNet probNetMixed;
    
    private ProbNet probNetUndirected;
    
    private ProbNet probNetDirected;
    
    @BeforeEach public void setUp() {
        probNetMixed = ConstraintsTests.getTestProbNetMixed();
        probNetUndirected = ConstraintsTests.getTestProbNetUndirected();
        probNetDirected = ConstraintsTests.getTestProbNetDirected();
    }
    
    /**
     * Checks or not all the <code>probNet</code> in different situations in
     * <code>OnlyDirectedLinks</code> constructor.
     */
    @Disabled
    @Test public void testCheckProbNet() {
        // test only directed links insertions without checking.
        PNConstraint constraint = new OnlyDirectedLinks();
        assertFalse(constraint.checkProbNet(probNetMixed));
        assertFalse(constraint.checkProbNet(probNetUndirected));
    }
    
    /**
     * Checks veto
     */
    @Test public void testUndoableEditWillHappen() throws Exception {
        
        // Add constraints as listeners.
        PNESupport pNESupport = new PNESupport(false);
        probNetDirected.addConstraint(new OnlyDirectedLinks());
        List<PNConstraint> constraints = probNetDirected.getConstraints();
        for (PNConstraint constraint : constraints) { // sets listeners
            pNESupport.addUndoableEditListener(constraint);
        }
        // Create edits
        Variable va = probNetDirected.getVariable("A");
        Variable vc = probNetDirected.getVariable("C");
        
        // test no exception in legal edit
        AddLinkEdit cEdit = new AddLinkEdit(probNetDirected, va, vc, true);
        pNESupport.announceEdit(cEdit);
        cEdit.doEdit();
        
        // test exception in no legal edit
        AddLinkEdit iEdit;
        try {
            iEdit = new AddLinkEdit(probNetDirected, va, vc, false);
            pNESupport.announceEdit(iEdit);
            fail();
        } catch (Exception cve) {
            // It should have thrown an exception.
        }
    }
    
}
