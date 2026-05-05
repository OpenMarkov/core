/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.decisiontree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Safety-net tests for {@link DecisionTreeBranch} introduced in Phase 0
 * of the decisiontree refactoring plan.
 */
class DecisionTreeBranchTest {

    private ProbNet probNet;
    private Variable decisionVariable;
    private State yes;
    private State no;
    private Node decisionNode;
    private Node chanceNode;

    @BeforeEach
    void setUp() {
        yes = new State("yes");
        no = new State("no");
        decisionVariable = new Variable("D", new State[]{yes, no});
        Variable chanceVariable = new Variable("C", new State[]{yes, no});

        probNet = new ProbNet();
        decisionNode = probNet.addNode(decisionVariable, NodeType.DECISION);
        chanceNode = probNet.addNode(chanceVariable, NodeType.CHANCE);
    }

    @Test
    void constructorWithVariableAndStateStoresThem() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable, yes);

        assertSame(decisionVariable, branch.getBranchVariable());
        assertSame(yes, branch.getBranchState());
    }

    @Test
    void constructorWithoutVariableLeavesBothNull() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet);

        assertNull(branch.getBranchVariable());
        assertNull(branch.getBranchState());
    }

    @Test
    void getBranchStatesAddsFindingForBranchVariable() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable, yes);

        EvidenceCase evidence = branch.getBranchStates();

        assertEquals(1, evidence.getFindings().size());
        assertSame(decisionVariable, evidence.getFindings().get(0).getVariable());
    }

    @Test
    void getBranchStatesWithoutVariableReturnsEmptyEvidence() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet);

        EvidenceCase evidence = branch.getBranchStates();

        assertNotNull(evidence);
        assertTrue(evidence.getFindings().isEmpty());
    }

    @Test
    void getBranchStatesIsCachedAcrossCalls() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable, yes);

        EvidenceCase first = branch.getBranchStates();
        EvidenceCase second = branch.getBranchStates();

        // Currently the implementation caches scenarioEvidence -> same instance.
        assertSame(first, second);
    }

    @Test
    void branchProbabilityZeroWhenParentScenarioIsZero() {
        StubDecisionTreeNode<Double> parent = new StubDecisionTreeNode<>(decisionNode, probNet);
        parent.setScenarioProbability(0.0);

        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable, yes);
        parent.addChild(branch);
        branch.setScenarioProbability(0.5);

        assertEquals(0.0, branch.getBranchProbability());
    }

    @Test
    void branchProbabilityIsScenarioRatio() {
        StubDecisionTreeNode<Double> parent = new StubDecisionTreeNode<>(decisionNode, probNet);
        parent.setScenarioProbability(0.4);

        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable, yes);
        parent.addChild(branch);
        branch.setScenarioProbability(0.1);

        assertEquals(0.25, branch.getBranchProbability(), 1e-12);
    }

    @Test
    void setChildLinksBranchAndNode() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable, yes);
        StubDecisionTreeNode<Double> child = new StubDecisionTreeNode<>(chanceNode, probNet);

        branch.setChild(child);

        assertSame(child, branch.getChild());
        assertEquals(1, branch.getChildren().size());
        assertSame(child, branch.getChildren().get(0));

        // Symmetry is verified indirectly: the child sees the branch's evidence
        // via its parent chain.
        EvidenceCase childEvidence = child.getBranchStates();
        assertEquals(1, childEvidence.getFindings().size());
        assertSame(decisionVariable, childEvidence.getFindings().get(0).getVariable());
    }

    @Test
    void getUtilityDelegatesToChild() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable, yes);
        StubDecisionTreeNode<Double> child = new StubDecisionTreeNode<>(chanceNode, probNet);
        child.setUtility(2.5);
        branch.setChild(child);

        assertEquals(2.5, branch.getUtility());
    }

    @Test
    void scenarioProbabilityRoundTrip() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable, yes);
        assertEquals(Double.NEGATIVE_INFINITY, branch.getScenarioProbability());

        branch.setScenarioProbability(0.7);
        assertEquals(0.7, branch.getScenarioProbability());
    }

    /**
     * Documents pre-existing bug: {@link DecisionTreeBranch#toString()} dereferences
     * branchVariable without null check. Phase 1 of the refactor will make this
     * test pass; until then, it is disabled to keep the suite green.
     */
    @Disabled("Pre-existing NPE; will be fixed in Phase 1 of decisiontree refactor")
    @Test
    void toStringDoesNotThrowWhenBranchVariableIsNull() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet);
        assertDoesNotThrow(branch::toString);
    }

    @Test
    void toStringWithBranchVariableContainsVariableName() {
        DecisionTreeBranch branch = new DecisionTreeBranch(probNet, decisionVariable, yes);
        assertTrue(branch.toString().contains("D"));
    }
}
