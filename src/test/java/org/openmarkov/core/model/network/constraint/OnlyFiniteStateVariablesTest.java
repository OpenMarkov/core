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
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyFiniteStateVariablesTest {
    
    // Attributes
    private ProbNet influenceDiagram;
    private ProbNet mixedVariableInfluenceDiagram;
    
    // Methods
    @BeforeEach public void setUp() throws NodeNotFoundException {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
        mixedVariableInfluenceDiagram = getMixedVariableInfluenceDiagram();
    }
    
    private ProbNet getMixedVariableInfluenceDiagram() throws NodeNotFoundException {
        ProbNet net = ConstraintsTests.getInfuenceDiagram();
        Variable vC = new Variable("C");
        Variable vU = net.getVariable("U");
        net.addNode(vC, NodeType.CHANCE);
        net.addLink(vU, vC, true);
        return net;
    }
    
    @Test public void testCheckProbNet() {
        OnlyFiniteStatesVariables testedConstraint = new OnlyFiniteStatesVariables();
        influenceDiagram.addConstraint(testedConstraint);
        mixedVariableInfluenceDiagram.addConstraint(testedConstraint);
        
        assertTrue(testedConstraint.checkProbNet(influenceDiagram));
        assertFalse(testedConstraint.checkProbNet(mixedVariableInfluenceDiagram));
    }
    
    @Test public void testUndoableEditWillHappen() throws Exception {
        
        PNESupport pNESupport = new PNESupport(false);
        PNConstraint constraint = new OnlyFiniteStatesVariables();
        influenceDiagram.addConstraint(constraint);
        
        pNESupport.addUndoableEditListener(constraint);
        
        Variable vc1 = new Variable("D1", 0);
        
        // test no exception in legal edit
        AddNodeEdit legalAdd = new AddNodeEdit(influenceDiagram, vc1, NodeType.DECISION);
        //add the node D1 (decision + finite state)
        pNESupport.announceEdit(legalAdd);
        legalAdd.doEdit();
        
        Variable vc2 = new Variable("C1", 0);
        // test no exception in legal edit
        legalAdd = new AddNodeEdit(influenceDiagram, vc2, NodeType.CHANCE);
        //add the node C1 (chance + finite state)
        pNESupport.announceEdit(legalAdd);
        legalAdd.doEdit();
        
        Variable vc3 = new Variable("U1");
        // test no exception in legal edit (utility + numeric)
        legalAdd = new AddNodeEdit(influenceDiagram, vc3, NodeType.UTILITY);
        //add the node U1
        pNESupport.announceEdit(legalAdd);
        legalAdd.doEdit();
        
        Variable vc4 = new Variable("D2");
        // test exception in illegal edit (decision + numeric)
        AddNodeEdit illegalAdd = new AddNodeEdit(influenceDiagram, vc4, NodeType.DECISION);
        //add the node D2
        try {
            pNESupport.announceEdit(illegalAdd);
            fail();
        } catch (Exception cve) {
            //An exception should have been thrown
        }
        
        
        Variable vc5 = new Variable("C2");
        // test  exception in illegal edit (chance + numeric)
        illegalAdd = new AddNodeEdit(influenceDiagram, vc5, NodeType.CHANCE);
        //add the node D2
        try {
            pNESupport.announceEdit(illegalAdd);
            fail();
        } catch (Exception cve) {
            //An exception should have been thrown
        }
        
        VariableTypeEdit legalEdit = new VariableTypeEdit(influenceDiagram.getNode("D1"), VariableType.DISCRETIZED);
        //edit variable type of D1
        // test  exception in legal edit (decision + discrete)
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        
        legalEdit = new VariableTypeEdit(influenceDiagram.getNode("C1"), VariableType.DISCRETIZED);
        //edit variable type of C1
        // test  exception in legal edit (chance + discrete)
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        
        VariableTypeEdit illegalEdit = new VariableTypeEdit(influenceDiagram.getNode("C1"), VariableType.NUMERIC);
        //edit variable type of C1
        // test  exception in illegal edit (chance + numeric)
        try {
            pNESupport.announceEdit(illegalEdit);
            fail();
        } catch (Exception cve) {
            //An exception should have been thrown
        }
    }
    
}
