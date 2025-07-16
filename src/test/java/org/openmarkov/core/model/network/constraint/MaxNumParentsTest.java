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
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.test.TestSpeed;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MaxNumParentsTest {
    
    private ProbNet net;
    
    @BeforeEach public void setUp() {
        net = ConstraintsTests.getTestProbNetDirected();
    }
    
    @Tag(TestSpeed.SLOW)
    @Test public void testCheckProbNet() {
        MaxNumParents constraint = new MaxNumParents();
        constraint.setMaxNumParents(1);
        net.removeConstraint(constraint);
        net.addConstraint(constraint);
        
        
        net.removeConstraint(constraint);
        Variable vD = new Variable("D");
        Variable vB = net.getVariable("B");
        net.addNode(vD, NodeType.CHANCE);
        net.addLink(vD, vB, true);
        net.addConstraint(constraint);
        assertFalse(constraint.checkProbNet(net)); //La prueba solo se cumple si no se cumplen las constraints.
    }
    
    @Test public void testUndoableEditWillHappen() throws Exception {
        
        PNESupport pNESupport = new PNESupport(false);
        MaxNumParents constraint = new MaxNumParents();
        constraint.setMaxNumParents(2);
        net.addConstraint(constraint);
        pNESupport.addUndoableEditListener(constraint);
        
        // do legal add
        Variable vB = net.getVariable("B");
        new AddNodeEdit(net, new Variable("D"), NodeType.CHANCE, new Point2D.Double()).doEdit();
        Variable vD = net.getVariable("D");
        
        // creates a link from D -> B
        AddLinkEdit legalEdit = new AddLinkEdit(net, vD, vB, true);
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        
        // do ilegal add
        try {
            
            new AddNodeEdit(net, new Variable("E"), NodeType.CHANCE, new Point2D.Double()).doEdit();
            Variable vE = net.getVariable("E");
            
            // creates a link from E -> B
            AddLinkEdit ilegalEdit = new AddLinkEdit(net, vE, vB, true);
            pNESupport.announceEdit(ilegalEdit);
            ilegalEdit.doEdit();
            fail();
        } catch (ConstraintViolationException e) {
            // The constraint should have failed
        }
        
        Variable vE = net.getVariable("E");
        // creates a link from E - B
        legalEdit = new AddLinkEdit(net, vE, vB, false);
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        try {// modifies the link from E - B to E->B
            AddLinkEdit ilegalLinkEdit = new AddLinkEdit(net, net.getVariable("E"), net.getVariable("B"), true);
            pNESupport.announceEdit(ilegalLinkEdit);
            ilegalLinkEdit.doEdit();
            fail();
        } catch (ConstraintViolationException e) {
            // The constraint should have failed
        }
        
    }
    
}
