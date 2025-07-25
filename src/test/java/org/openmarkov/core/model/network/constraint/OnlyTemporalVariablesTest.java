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
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.test.TestSpeed;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyTemporalVariablesTest {
    
    private ProbNet network;
    
    @BeforeEach public void setUp() {
        network = ConstraintsTests.getTemporalVarNet();
    }
    
    @Test public void testCheckProbNet() {
        OnlyTemporalVariables testedConstraint = new OnlyTemporalVariables();
        network.addConstraint(testedConstraint);
        assertTrue(testedConstraint.checkProbNet(network));
        
        network.addNode(new Variable("A"), NodeType.CHANCE);
        assertFalse(testedConstraint.checkProbNet(network));
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testUndoableEditWillHappen() throws DoEditException.ConstraintViolated {
        PNESupport pNESupport = new PNESupport(false);
        PNConstraint constraint = new OnlyTemporalVariables();
        network.addConstraint(constraint);
        pNESupport.addUndoableEditListener(constraint);
        
        boolean exceptionLaunched = false;
        Variable var = new Variable(" [11]", "Y", "N");
        AddNodeEdit legalEdit = new AddNodeEdit(network, var, NodeType.CHANCE);
        // add the variable
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        Variable var1 = new Variable("F");
        AddNodeEdit ilegalEdit = new AddNodeEdit(network, var1, NodeType.CHANCE);
        // add the variable
        try {
            pNESupport.announceEdit(ilegalEdit);
            ilegalEdit.doEdit();
            fail();
        } catch (DoEditException.ConstraintViolated cve) {
            // An exception should have been thrown
        }
    }
    
}
