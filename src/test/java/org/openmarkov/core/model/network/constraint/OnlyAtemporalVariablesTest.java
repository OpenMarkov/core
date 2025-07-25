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
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyAtemporalVariablesTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
    }
    
    @Test public void testCheckProbNet() {
        OnlyAtemporalVariables testedConstraint = new OnlyAtemporalVariables();
        influenceDiagram.addConstraint(testedConstraint);
        assertTrue(testedConstraint.checkProbNet(influenceDiagram));
        
        Variable variable = new Variable(" [10]", "YES", "NO");
        influenceDiagram.addNode(variable, NodeType.CHANCE);
        assertFalse(testedConstraint.checkProbNet(influenceDiagram));
    }
    
    @Test
    public void testUndoableEditWillHappen() throws DoEditException.ConstraintViolated {
        PNESupport pNESupport = new PNESupport(false);
        PNConstraint constraint = new OnlyAtemporalVariables();
        
        influenceDiagram.addConstraint(constraint);
        pNESupport.addUndoableEditListener(constraint);
        
        Variable var = new Variable("F");
        AddNodeEdit legalEdit = new AddNodeEdit(influenceDiagram, var, NodeType.CHANCE);
        // add the variable
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        Variable var1 = new Variable(" [11]", "Y", "N");
        AddNodeEdit ilegalEdit = new AddNodeEdit(influenceDiagram, var1, NodeType.CHANCE);
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
