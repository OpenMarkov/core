package org.openmarkov.core.model.network;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmarkov.core.inference.MulticriteriaOptions.Type;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

public class UtilityOperationsTest {
	
	//@Test
	public void transformToUnicriterionTest(){
		ProbNet probNet = getProbNet4Test();
		
		UtilityOperations.transformToUnicriterion(probNet);
		
		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
		for(Node node : utilityNodes){
			assertTrue(node.getVariable().getDecisionCriterion().getCriterionName().equals("GlobalUtility"));
		}
		
	}
	/* TODO - Change the method. Use a 0 scale?
	@Test
	public void removeTerminalNullCostEffectivenessNodesTest(){
		ProbNet probNet = getProbNet4Test();
		UtilityOperations.removeTerminalNullCostEffectivenessNodes(probNet);
		
		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
		assertTrue(utilityNodes.size() == 1);
		
		for(Node node : utilityNodes){
			assertTrue(node.getVariable().getDecisionCriterion().getCECriterion() != CECriterion.Null);
		}
		
	}*/
	
	public static ProbNet getProbNet4Test () {
		  ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		  // Variables
		  Variable varDisease = new Variable("Disease", "absent", "present");
		  Variable varResult_of_test = new Variable("Result of test", "not-performed", "negative", "positive");
		  Variable varTherapy = new Variable("Therapy", "no", "yes");
		  Variable varDo_test_ = new Variable("Do test?", "no", "yes");
		  Variable varHealth_state = new Variable("Health state");
		  Variable varCost_of_test = new Variable("Cost of test");

		  // ProbNet Criteria
		  List<Criterion> decisionCriteria = new ArrayList<>();
		  
		  Criterion criHealth_state = new Criterion("Effectiveness", "QALY");
		  criHealth_state.setUnicriteriaScale(0.8);
		  criHealth_state.setCECriterion(CECriterion.Effectiveness);
		  decisionCriteria.add(criHealth_state);
		  
		  Criterion criCost_of_test = new Criterion("Cost", "€");
		  criCost_of_test.setUnicriteriaScale(1);
		  criCost_of_test.setCECriterion(CECriterion.Cost);
		  decisionCriteria.add(criCost_of_test);
		  
		  probNet.setDecisionCriteria(decisionCriteria);
		  
		  // Assign criteria to variables
		  varHealth_state.setDecisionCriterion(criHealth_state);
		  varCost_of_test.setDecisionCriterion(criCost_of_test);
		  
		  probNet.getInferenceOptions().getMultiCriteriaOptions().setMainUnit("€");
		  probNet.getInferenceOptions().getMultiCriteriaOptions().setMulticriteriaType(Type.UNICRITERION);
		  
		  // Nodes
		  Node nodeDisease= probNet.addNode(varDisease, NodeType.CHANCE);
		  Node nodeResult_of_test= probNet.addNode(varResult_of_test, NodeType.CHANCE);
		  Node nodeTherapy= probNet.addNode(varTherapy, NodeType.DECISION);
		  Node nodeDo_test_= probNet.addNode(varDo_test_, NodeType.DECISION);
		  Node nodeHealth_state= probNet.addNode(varHealth_state, NodeType.UTILITY);
		  Node nodeCost_of_test= probNet.addNode(varCost_of_test, NodeType.UTILITY);

		  // Links
		  probNet.makeLinksExplicit(false);
		  probNet.addLink(nodeDisease, nodeHealth_state, true);
		  probNet.addLink(nodeDisease, nodeResult_of_test, true);
		  probNet.addLink(nodeResult_of_test, nodeTherapy, true);
		  probNet.addLink(nodeTherapy, nodeHealth_state, true);
		  probNet.addLink(nodeDo_test_, nodeCost_of_test, true);
		  probNet.addLink(nodeDo_test_, nodeTherapy, true);
		  probNet.addLink(nodeDo_test_, nodeResult_of_test, true);


		  // Potentials
		  TablePotential potDisease = new TablePotential(Arrays.asList(varDisease), PotentialRole.CONDITIONAL_PROBABILITY);
		  potDisease.values = new double[]{0.86, 0.14};
		  nodeDisease.setPotential(potDisease);

		  TablePotential potResult_of_test = new TablePotential(Arrays.asList(varResult_of_test, varDo_test_, varDisease), PotentialRole.CONDITIONAL_PROBABILITY);
		  potResult_of_test.values = new double[]{1, 0, 0, 0, 0.97, 0.03, 1, 0, 0, 0, 0.09, 0.91};
		  nodeResult_of_test.setPotential(potResult_of_test);

		  ExactDistrPotential potHealth_state = new ExactDistrPotential(Arrays.asList(varHealth_state,varDisease, varTherapy));
		  potHealth_state.setValues(new double[]{10, 3, 9, 8});
		  nodeHealth_state.setPotential(potHealth_state);

		ExactDistrPotential potCost_of_test = new ExactDistrPotential(Arrays.asList(varCost_of_test,varDo_test_));
		  potCost_of_test.setValues(new double[]{0, -0.2});
		  nodeCost_of_test.setPotential(potCost_of_test);


		  // Link restrictions and revealing states
		  // Always observed nodes

		 return probNet;
		}
}
