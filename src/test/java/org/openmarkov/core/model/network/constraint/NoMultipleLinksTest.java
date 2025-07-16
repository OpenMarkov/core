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
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoMultipleLinksTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
    }
    
    @Test public void testCheckProbNet() {
        NoMultipleLinks testedConstraint = new NoMultipleLinks();
        influenceDiagram.addConstraint(testedConstraint);
        Variable vu = influenceDiagram.getVariable("U");
        Variable va = influenceDiagram.getVariable("A");
        Variable vd = influenceDiagram.getVariable("D");
        assertTrue(testedConstraint.checkProbNet(influenceDiagram));
        
        influenceDiagram.removeConstraint(testedConstraint);
        influenceDiagram.addLink(vu, va, true);
        influenceDiagram.addConstraint(testedConstraint);
        assertTrue(testedConstraint.checkProbNet(influenceDiagram));
        
        influenceDiagram.removeConstraint(testedConstraint);
        influenceDiagram.addLink(vd, va, false);
        influenceDiagram.addConstraint(testedConstraint);
        assertFalse(testedConstraint.checkProbNet(influenceDiagram));
    }
    
    @Disabled
    @Test public void testUndoableEditWillHappen() throws Exception {
        PNESupport pNESupport = new PNESupport(false);
        PNConstraint constraint = new NoMultipleLinks();
        
        influenceDiagram.addConstraint(constraint);
        pNESupport.addUndoableEditListener(constraint);
        
        // do legal AddLink: add an directed link between U and A
        Variable vU = influenceDiagram.getVariable("U");
        Variable vA = influenceDiagram.getVariable("A");
        // creates an undirected link from node A to D
        AddLinkEdit legalEdit = new AddLinkEdit(influenceDiagram, vU, vA, true);
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        // do ilegal LinkAdd. Add an undirected link between U and A
        AddLinkEdit ilegalAdd = new AddLinkEdit(influenceDiagram, vU, vA, false);
        try {
            pNESupport.announceEdit(ilegalAdd);
            ilegalAdd.doEdit();
            fail();
        } catch (ConstraintViolationException e) {
            // the ilegal edit should have thrown the exception
        }
        
        // do ilegal LinkEdit. Add an undirected link between U and A
        AddLinkEdit ilegalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("U"),
                                                     influenceDiagram.getVariable("A"), false);
        pNESupport.announceEdit(ilegalLinkEdit);
        ilegalLinkEdit.doEdit();
        
        // do legal invert link
        InvertLinkEdit legalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vA, true);
        pNESupport.announceEdit(legalInvertLinkEdit);
        legalInvertLinkEdit.doEdit();
        
        // do ilegal InvertLink. Add an directed link between U and A
        InvertLinkEdit ilegalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vA, false);
        try {
            pNESupport.announceEdit(ilegalInvertLinkEdit);
            ilegalInvertLinkEdit.doEdit();
            fail();
        } catch (ConstraintViolationException e) {
            // the ilegal edit should have thrown the exception
        }
    }
    
}
