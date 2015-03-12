package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Arrays;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.ComplementFunction;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.LogNormalFunction;
import org.openmarkov.core.model.network.modelUncertainty.TriangularFunction;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

public class SensitivityAnalysisFactory {
	public static ProbNet buildIDDecideTestSA() {
		  ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		  // Variables
		  Variable varDisease = new Variable("Disease", "absent", "present");
		  Variable varResult_of_test = new Variable("Result of test", "not-performed", "negative", "positive");
		  Variable varTherapy = new Variable("Therapy", "no", "yes");
		  Variable varDo_test_ = new Variable("Do test?", "no", "yes");
		  Variable varHealth_state = new Variable("Health state");
		  Variable varCost_of_test = new Variable("Cost of test");
		  Variable varCost_of_therapy = new Variable("Cost of therapy");

		  // Nodes
		  Node nodeDisease= probNet.addNode(varDisease, NodeType.CHANCE);
		  Node nodeResult_of_test= probNet.addNode(varResult_of_test, NodeType.CHANCE);
		  Node nodeTherapy= probNet.addNode(varTherapy, NodeType.DECISION);
		  Node nodeDo_test_= probNet.addNode(varDo_test_, NodeType.DECISION);
		  Node nodeHealth_state= probNet.addNode(varHealth_state, NodeType.UTILITY);
		  Node nodeCost_of_test= probNet.addNode(varCost_of_test, NodeType.UTILITY);
		  Node nodeCost_of_therapy= probNet.addNode(varCost_of_therapy, NodeType.UTILITY);

		  // Links
		  probNet.makeLinksExplicit(false);
		  probNet.addLink(nodeDisease, nodeHealth_state, true);
		  probNet.addLink(nodeDisease, nodeResult_of_test, true);
		  probNet.addLink(nodeResult_of_test, nodeTherapy, true);
		  probNet.addLink(nodeTherapy, nodeHealth_state, true);
		  probNet.addLink(nodeTherapy, nodeCost_of_therapy, true);
		  probNet.addLink(nodeDo_test_, nodeCost_of_test, true);
		  probNet.addLink(nodeDo_test_, nodeTherapy, true);
		  probNet.addLink(nodeDo_test_, nodeResult_of_test, true);

		  // Potentials
		  TablePotential potDisease = new TablePotential(Arrays.asList(varDisease), PotentialRole.CONDITIONAL_PROBABILITY);
		  potDisease.values = new double[]{0.86, 0.14};
		  potDisease.uncertainValues = new UncertainValue[]{new UncertainValue(new ComplementFunction(1),""), new UncertainValue(new BetaFunction(14, 86),"prevalence")};
		  nodeDisease.setPotential(potDisease);

		  TablePotential potResult_of_test = new TablePotential(Arrays.asList(varResult_of_test, varDo_test_, varDisease), PotentialRole.CONDITIONAL_PROBABILITY);
		  potResult_of_test.values = new double[]{1, 0, 0, 0, 0.97, 0.03, 1, 0, 0, 0, 0.09, 0.91};
		  potResult_of_test.uncertainValues = new UncertainValue[]{null, null, null, new UncertainValue(new ExactFunction(0),""), new UncertainValue(new BetaFunction(97, 3),"specificity"), new UncertainValue(new ComplementFunction(1),""), null, null, null, new UncertainValue(new ExactFunction(0),""), new UncertainValue(new ComplementFunction(1),""), new UncertainValue(new BetaFunction(91, 9),"sensitivity")};
		  nodeResult_of_test.setPotential(potResult_of_test);

		  TablePotential potHealth_state = new TablePotential(varHealth_state,Arrays.asList(varDisease, varTherapy));
		  potHealth_state.values = new double[]{10, 4.94616381, 9, 8};
		  potHealth_state.uncertainValues = new UncertainValue[]{null, new UncertainValue(new LogNormalFunction(1.09861229, 1),"utility non-treated disease"), new UncertainValue(new TriangularFunction(8.5, 9.5, 9),"utility not treated"), new UncertainValue(new TriangularFunction(7.5, 8.5, 8),"utility treated disease")};
		  nodeHealth_state.setPotential(potHealth_state);

		  TablePotential potCost_of_test = new TablePotential(varCost_of_test,Arrays.asList(varDo_test_));
		  potCost_of_test.values = new double[]{0, -0.2};
		  potCost_of_test.uncertainValues = new UncertainValue[]{null, new UncertainValue(new TriangularFunction(-0.25, -0.15, -0.2),"")};
		  nodeCost_of_test.setPotential(potCost_of_test);

		  TablePotential potCost_of_therapy = new TablePotential(varCost_of_therapy,Arrays.asList(varTherapy));
		  potCost_of_therapy.values = new double[]{0, -0.2};
		  potCost_of_therapy.uncertainValues = new UncertainValue[]{null, new UncertainValue(new TriangularFunction(-0.3, -0.1, -0.2),"")};
		  nodeCost_of_therapy.setPotential(potCost_of_therapy);

		  // Link restrictions and revealing states
		  // Always observed nodes

		 return probNet;
		}
}
