/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.inference.TransitionTime;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TemporalOptionsEditTest {
    
    private ProbNet probNet;
    
    private static ProbNet getProbNet4Test() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        // Variables
        Variable varA = new Variable("A", "absent", "mild", "moderate", "severe");
        Variable varB = new Variable("B", "yes", "its possible", "maybe not", "no");
        Variable varC = new Variable("C");
        
        // Nodes
        Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
        Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
        Node nodeC = probNet.addNode(varC, NodeType.CHANCE);
        
        nodeB.getVariable().setPartitionedInterval(new PartitionedInterval(nodeB.getVariable().getDefaultInterval(4),
                                                                           nodeB.getVariable().getDefaultBelongs(4)));
        
        // Links
        probNet.makeLinksExplicit(false);
        probNet.addLink(nodeA, nodeB, true);
        probNet.addLink(nodeA, nodeC, true);
        
        // Potentials
        UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeA.setPotential(potA);
        
        UniformPotential potB = new UniformPotential(Arrays.asList(varB, varA), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeB.setPotential(potB);
        
        UniformPotential potC = new UniformPotential(Arrays.asList(varC, varA), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeC.setPotential(potC);
        
        // Link restrictions and revealing states
        // Always observed nodes
        
        return probNet;
    }
    
    @BeforeEach public void setUp() {
        this.probNet = getProbNet4Test();
        probNet.getPNESupport().setWithUndo(true);
    }
    
    @Test
    public void temporalOptionsTest() throws DoEditException, NonProjectablePotentialException, WrongCriterionException {
        TemporalOptions temporalOptions = new TemporalOptions();
        temporalOptions.setHorizon(50);
        temporalOptions.setTransition(TransitionTime.END);
        TemporalOptionsEdit edit = new TemporalOptionsEdit(probNet, temporalOptions);
        
        probNet.getPNESupport().doEdit(edit);
        assertEquals(50, probNet.getInferenceOptions().getTemporalOptions().getHorizon());
        assertEquals(probNet.getInferenceOptions()
                            .getTemporalOptions()
                            .getTransition()
                            .toString(), TransitionTime.END.toString());
        
        TemporalOptions temporalOptions2 = new TemporalOptions();
        temporalOptions2.setHorizon(10);
        temporalOptions2.setTransition(TransitionTime.HALF);
        TemporalOptionsEdit edit2 = new TemporalOptionsEdit(probNet, temporalOptions2);
        
        probNet.getPNESupport().doEdit(edit2);
        assertEquals(10, probNet.getInferenceOptions().getTemporalOptions().getHorizon());
        assertEquals(probNet.getInferenceOptions()
                            .getTemporalOptions()
                            .getTransition()
                            .toString(), TransitionTime.HALF.toString());
        
        
        probNet.getPNESupport().undo();
        
        assertEquals(50, probNet.getInferenceOptions().getTemporalOptions().getHorizon());
        assertEquals(probNet.getInferenceOptions()
                            .getTemporalOptions()
                            .getTransition()
                            .toString(), TransitionTime.END.toString());
        
    }
}
