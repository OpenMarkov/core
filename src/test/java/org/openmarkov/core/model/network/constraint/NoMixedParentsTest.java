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
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoMixedParentsTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getOnlyUtilityChildrenInfluenceDiagram();
    }
    
    @Test public void testCheckProbNet() {
        NoMixedParents testedConstraint = new NoMixedParents();
        influenceDiagram.addConstraint(testedConstraint);
        assertTrue(testedConstraint.checkProbNet(influenceDiagram));
        
        Variable vc = influenceDiagram.getVariable("C");
        Variable ve = new Variable("E", 2);
        influenceDiagram.addNode(ve, NodeType.DECISION);
        influenceDiagram.addLink(ve, vc, true);
        assertFalse(testedConstraint.checkProbNet(influenceDiagram));
    }
    
    @Disabled
    @Test public void testUndoableEditWillHappen() throws DoEditException.ConstraintViolated, org.openmarkov.core.exception.DoEditException.CannotInvertLink {
        // Add constraints as listeners.
        PNESupport pNESupport = new PNESupport(false);
        PNConstraint constraint = new NoMixedParents();
        influenceDiagram.addConstraint(new NoMixedParents());
        pNESupport.addUndoableEditListener(constraint);
        
        //do legal AddLink: add link from utility node E to utility node C
        new AddNodeEdit(influenceDiagram, new Variable("E"), NodeType.UTILITY).doEdit();
        Variable vE = influenceDiagram.getNode("E", NodeType.UTILITY).getVariable();
        Variable vC = influenceDiagram.getVariable("C");
        // creates a link from utility node E to utility node C
        AddLinkEdit legalEdit = new AddLinkEdit(influenceDiagram, vE, vC, true);
        pNESupport.announceEdit(legalEdit);
        legalEdit.doEdit();
        
        //do legal LinkEdit:  add directed link from chance node F to utility node U
        new AddNodeEdit(influenceDiagram, new Variable("F"), NodeType.CHANCE, new Point2D.Double()).doEdit();
        
        AddLinkEdit legalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("F"),
                                                    influenceDiagram.getVariable("U"), true);
        pNESupport.announceEdit(legalLinkEdit);
        legalLinkEdit.doEdit();
        
        //do legal invertLink: invert link from E to U
        
        InvertLinkEdit legalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vC, vE, true);
        pNESupport.announceEdit(legalInvertLinkEdit);
        legalInvertLinkEdit.doEdit();
        
        //do ilegal invertLink: invert link from U to C
        
        Variable vU = influenceDiagram.getVariable("U");
        InvertLinkEdit ilegalInvertLinkEdit = new InvertLinkEdit(influenceDiagram, vU, vC, true);
        boolean exceptionLaunched = false;
        try {
            pNESupport.announceEdit(ilegalInvertLinkEdit);
            ilegalInvertLinkEdit.doEdit();
            fail();
        } catch (DoEditException.ConstraintViolated | DoEditException.CannotInvertLink cve) {
            //The constraint should have failed
        }
        
        //do ilegal AddLink: add link from utility node G to utility node U
        
        new AddNodeEdit(influenceDiagram, new Variable("G"), NodeType.UTILITY, new Point2D.Double()).doEdit();
        Variable vG = influenceDiagram.getNode("G", NodeType.UTILITY).getVariable();
        
        // creates a link from utility node G to utility node U
        AddLinkEdit ilegalEdit = new AddLinkEdit(influenceDiagram, vG, vU, true);
        try {
            pNESupport.announceEdit(ilegalEdit);
            ilegalEdit.doEdit();
            fail();
        } catch (DoEditException.ConstraintViolated cve) {
            //The constraint should have failed
        }
        
        // do ilegal LinkEdit: add link from utility node G to utility node U
        AddLinkEdit ilegalLinkEdit = new AddLinkEdit(influenceDiagram, influenceDiagram.getVariable("G"),
                                                     influenceDiagram.getVariable("U"), true);
        try {
            pNESupport.announceEdit(ilegalLinkEdit);
            ilegalLinkEdit.doEdit();
            fail();
        } catch (DoEditException.ConstraintViolated cve) {
            //The constraint should have failed
        }
        
    }
    
}
