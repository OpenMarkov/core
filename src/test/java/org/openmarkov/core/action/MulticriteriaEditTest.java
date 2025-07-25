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
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MulticriteriaEditTest {
    
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
    public void multiCriteriaOptionsTest() throws DoEditException {
        
        MulticriteriaOptions multicriteriaOptions = new MulticriteriaOptions();
        multicriteriaOptions.setMainUnit("Unit A");
        multicriteriaOptions.setMulticriteriaType(MulticriteriaOptions.Type.UNICRITERION);
        
        List<Criterion> decisionCriteria = new ArrayList<>();
        Criterion criterion1 = new Criterion("Criterion A");
        decisionCriteria.add(criterion1);
        probNet.setDecisionCriteria(decisionCriteria);
        MulticriteriaEdit edit = new MulticriteriaEdit(probNet, decisionCriteria, multicriteriaOptions);
        
        probNet.getPNESupport().doEdit(edit);
        assertEquals("Unit A", probNet.getInferenceOptions().getMultiCriteriaOptions().getMainUnit());
        assertEquals(probNet.getDecisionCriteria(), decisionCriteria);
        
        MulticriteriaOptions multicriteriaOptions2 = new MulticriteriaOptions();
        multicriteriaOptions2.setMainUnit("Unit B");
        multicriteriaOptions2.setMulticriteriaType(MulticriteriaOptions.Type.COST_EFFECTIVENESS);
        List<Criterion> decisionCriteria2 = new ArrayList<>();
        Criterion criterion2 = new Criterion("Criterion B");
        decisionCriteria2.add(criterion2);
        probNet.setDecisionCriteria(decisionCriteria2);
        
        MulticriteriaEdit edit2 = new MulticriteriaEdit(probNet, decisionCriteria2, multicriteriaOptions2);
        probNet.getPNESupport().doEdit(edit2);
        assertEquals("Unit B", probNet.getInferenceOptions().getMultiCriteriaOptions().getMainUnit());
        assertEquals(probNet.getDecisionCriteria(), decisionCriteria2);
        
        probNet.getPNESupport().undo();
        
        assertEquals("Unit A", probNet.getInferenceOptions().getMultiCriteriaOptions().getMainUnit());
        assertNotEquals(probNet.getDecisionCriteria(), decisionCriteria);
        
    }
}
