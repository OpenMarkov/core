package org.openmarkov.core.model.network.factory;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

public class IDFactory extends NetsFactory {
	public static String decTestName = "Do test?";
	public static String therapyName = "Therapy";
	public static String healthStateName = "Health state";
	public static String therapyCostName = "Cost of therapy";
	public static String testCostName = "Cost of test";
	private static String symptomName = "Symptom";
	private static State[] symptomStates;

	
	/**
	 * @return An influence diagram without decisions, with only two nodes: X (chance) and U (utility).
	 * 
	 */
	public static ProbNet createSimpleIDWithoutDecisions(){
		double util[] = {20,90};
		return createSimpleIDWithoutDecisions(0.09,util);
	}
	
	/**
	 * @return An influence diagram without decisions, with only two nodes: X (chance) and U (utility).
	 * 
	 */
	public static ProbNet createSimpleIDWithoutDecisions(
			double prevalence,
			double[] tableUX) {
			
			ProbNet probNet;
			PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
			TablePotential potentialX;
			TablePotential potentialU;
						
			probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
			
			// Define the variables
			Variable variableX = new Variable(diseaseName,diseaseStates);
			Variable variableU = new Variable(healthStateName);
			
			//Add variables to the network			
			addVariables(probNet,NodeType.CHANCE,variableX);
			addVariables(probNet,NodeType.UTILITY,variableU);
			
			//additional properties
			String relevance = new String("Relevance");
			String value = new String("7.0");				
			setAdditionalProperties(relevance,value,variableX,variableU);		
				
			//Potential X
			potentialX = createPotentialDisease(prevalence,roleProbability,variableX);
			
			potentialU = createTablePotential(PotentialRole.UTILITY,tableUX,variableX);
			potentialU.setUtilityVariable(variableU);
			
			//Links throws NodeNotFoundException
			try {
				probNet.addLink(variableX, variableU, true);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
			addPotentials(probNet,potentialX,potentialU);
			
			return probNet;
		}
	
	
	
	
	/**
	 * @return An influence diagram with four nodes: X, Y, D and U. It represents a diagnosis problem.
	 * It is the example of influence diagram described in page 11 in the book available online at URL:
	 * http://www.cisiad.uned.es/techreports/decision-medicina.pdf
	 * The numerical parameters of this method are
	 */
	public static ProbNet createInfluenceDiagramDiagnosisProblem(
			double prevalence,
			double sensitivity,
			double specificity,
			double[] tableUXD) {
			
			ProbNet probNet;
			PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
			double [] tableYX;
			TablePotential potentialX;
			TablePotential potentialY;
			TablePotential potentialU;
						
			probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
			
			// Define the variables
			Variable variableX = new Variable(diseaseName,diseaseStates);
			Variable variableY = new Variable(testResultName,testResultStates);
			Variable variableD = new Variable(therapyName,yesNoStates);
			Variable variableU = new Variable(healthStateName);
			
			//Add variables to the network			
			addVariables(probNet,NodeType.CHANCE,variableX,variableY);
			addVariables(probNet,NodeType.DECISION,variableD);
			addVariables(probNet,NodeType.UTILITY,variableU);
			
			//additional properties
			String relevance = new String("Relevance");
			String value = new String("7.0");				
			setAdditionalProperties(relevance,value,variableX,variableY,variableD,variableU);		
				
			//Potential X
			potentialX = createPotentialDisease(prevalence,roleProbability,variableX);
				
			//Potential YX
			tableYX = valuesCPTResultTest(sensitivity,specificity);
			potentialY = createTablePotential(roleProbability, tableYX, variableY, variableX);
			
			potentialU = createTablePotential(PotentialRole.UTILITY,tableUXD,variableX, variableD);
			potentialU.setUtilityVariable(variableU);
			
			//Links throws NodeNotFoundException
			try {
				probNet.addLink(variableX, variableY, true);
				probNet.addLink(variableY, variableD, true);
				probNet.addLink(variableX, variableU, true);
				probNet.addLink(variableD, variableU, true);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
			addPotentials(probNet,potentialX,potentialY,potentialU);
			
			return probNet;
		}
	
	
	/**
	 * @return An influence diagram with three nodes: X, D and U. It represents a diagnosis problem without tests.
	 */
	public static ProbNet createIDNoKnowledge(
			double prevalence,
			double[] tableUXD) {
			
			ProbNet probNet;
			PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
			TablePotential potentialX;
			TablePotential potentialU;
						
			probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
			
			// Define the variables
			Variable variableX = new Variable(diseaseName,diseaseStates);
			Variable variableD = new Variable(therapyName,yesNoStates);
			Variable variableU = new Variable(healthStateName);
			
			//Add variables to the network			
			addVariables(probNet,NodeType.CHANCE,variableX);
			addVariables(probNet,NodeType.DECISION,variableD);
			addVariables(probNet,NodeType.UTILITY,variableU);
			
			//additional properties
			String relevance = new String("Relevance");
			String value = new String("7.0");				
			setAdditionalProperties(relevance,value,variableX,variableD,variableU);		
				
			//Potential X
			potentialX = createPotentialDisease(prevalence,roleProbability,variableX);
				
			potentialU = createTablePotential(PotentialRole.UTILITY,tableUXD,variableX, variableD);
			potentialU.setUtilityVariable(variableU);
			
			//Links throws NodeNotFoundException
			try {
				probNet.addLink(variableX, variableU, true);
				probNet.addLink(variableD, variableU, true);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
			addPotentials(probNet,potentialX,potentialU);
			
			return probNet;
		}
	
	/**
	 * @return An influence diagram with four nodes: X, Y, D and U. It represents a diagnosis problem.
	 * It is the example of influence diagram described in page 11 in the book available online at URL:
	 * http://www.cisiad.uned.es/techreports/decision-medicina.pdf
	 */
	public static ProbNet createInfluenceDiagramDiagnosisProblem() {
		double prevalence=0.07;
		double sensitivity=0.91;
		double specificity=0.97;
		double [] tableUXD ={78.0, 88.0, 28.0, 98.0};
		return createInfluenceDiagramDiagnosisProblem(prevalence,sensitivity,specificity,tableUXD);
	}
	
	/**
	 * @return An influence diagram with two nodes: D and U 
	 */
	public static ProbNet buildIDOneDecision() {
		double [] tableUXD ={87.4, 63.0};
		return createIDOneDecision(tableUXD);
	}
	
	/**
	 * @return An influence diagram with two nodes: D and U
	 */
	public static ProbNet createIDOneDecision(
			double[] tableUD) {
			
			ProbNet probNet;
			TablePotential potentialU;
						
			probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
			
			// Define the variables
			Variable variableD = new Variable(therapyName,yesNoStates);
			Variable variableU = new Variable(healthStateName);
			
			//Add variables to the network		
			addVariables(probNet,NodeType.DECISION,variableD);
			addVariables(probNet,NodeType.UTILITY,variableU);
			
			//additional properties
			String relevance = new String("Relevance");
			String value = new String("7.0");				
			setAdditionalProperties(relevance,value,variableD,variableU);	
				
			potentialU = createTablePotential(PotentialRole.UTILITY,tableUD, variableD);
			potentialU.setUtilityVariable(variableU);
			
			//Links throws NodeNotFoundException
			try {
				probNet.addLink(variableD, variableU, true);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
			addPotentials(probNet,potentialU);
			
			return probNet;
		}
	
	
	
	
	public static ProbNet buildIDNoKnowledge(){
		double [] tableUXD ={8.0, 9.0, 3.0, 10.0};
		return createIDNoKnowledge(0.14,tableUXD);
		
	}
	
	/**
	 * @return An influence diagram with four nodes: X, Y, D and U. It represents a diagnosis problem.
	 * It is the example of influence diagram described in page 11 in the book available online at URL:
	 * http://www.cisiad.uned.es/techreports/decision-medicina.pdf
	 */
	public static ProbNet createUniformInfluenceDiagramDiagnosisProblem() {
		double sameUtility = 10;
		double sameProb = 0.5;
		double prevalence=sameProb;
		double sensitivity=sameProb;
		double specificity=sameProb;
		double [] tableUXD ={sameUtility,sameUtility,sameUtility,sameUtility};
		return createInfluenceDiagramDiagnosisProblem(prevalence,sensitivity,specificity,tableUXD);
	}
	
	/**
	 * @return An influence diagram with four nodes: X, Y, D and U. It represents a diagnosis problem.
	 * It is the example of influence diagram described in page 11 in the book available online at URL:
	 * http://www.cisiad.uned.es/techreports/decision-medicina.pdf
	 * The numerical parameters of this method are
	 *//*
	public static ProbNet createInfluenceDiagramDecisionTestProblem(
			double prevalence,
			double sensitivity,
			double specificity) {
			
			ProbNet probNet;
			
			SumPotential potentialU;
								
			probNet = createInfluenceDiagramDecisionTestProblemWithoutSV(prevalence,sensitivity,specificity);

			// Define the variables
			Variable variableU1 = null;
			try {
				variableU1 = probNet.getVariable("U1");
			} catch (NodeNotFoundException e1) {
				e1.printStackTrace();
			}
			Variable variableU2 = null;
			try {
				variableU2 = probNet.getVariable("U2");
			} catch (NodeNotFoundException e1) {
				e1.printStackTrace();
			}
			Variable variableU = new Variable("U");
			
			//Add variables to the network			
			addVariables(probNet,NodeType.UTILITY,variableU);
			
			//additional properties
			String relevance = new String("Relevance");
			String value = new String("7.0");				
			setAdditionalProperties(relevance,value,variableU);	
			
			//Potential U2
			potentialU = createSumPotential(variableU,variableU1,variableU2);
				
			//Links throws NodeNotFoundException
			try {
				probNet.addLink(variableU1, variableU, true);
				probNet.addLink(variableU2, variableU, true);
				
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
			addPotentials(probNet,potentialU);
			
			return probNet;
		}
*/
	
	
	public static ProbNet buildIDDecideTest(){
		return buildIDDecideTest(0.14,0.91,0.97);
	}
	
	public static ProbNet buildIDDecideTest(
			double prevalence,
			double sensitivity,
			double specificity) {
			
			ProbNet probNet;
			PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
			double [] tableYXT;
			TablePotential potentialX;
			TablePotential potentialY;
			TablePotential potentialU1;
			TablePotential potentialU2;
			TablePotential potentialU3;
			double[] tableU1XD = {8.0, 9.0, 3.0, 10.0};
			double[] tableU2D = {-0.25, 0.0};
			double[] tableU3T = {-0.2,0.2};
						
			probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
			
			// Define the variables
			// Define the variables
			Variable variableX = new Variable(diseaseName,diseaseStates);
			Variable variableY = new Variable(testResultName,testResultStates[0],testResultStates[1],"noresult");
			Variable variableD = new Variable(therapyName,"yes","no");
			Variable variableT = new Variable(decTestName,"yes","no");
			Variable variableU1 = new Variable(healthStateName);
			Variable variableU2 = new Variable(therapyCostName);
			Variable variableU3 = new Variable(testCostName);
			
			//Add variables to the network			
			addVariables(probNet,NodeType.CHANCE,variableX,variableY);
			addVariables(probNet,NodeType.DECISION,variableD,variableT);
			addVariables(probNet,NodeType.UTILITY,variableU1,variableU2,variableU3);
			
			//additional properties
			String relevance = new String("Relevance");
			String value = new String("7.0");				
			setAdditionalProperties(relevance,value,variableX,variableY,variableD,variableT,variableU1,variableU2);	
			
			//Potential X
			potentialX = createPotentialDisease(prevalence,roleProbability,variableX);
			
			//Potential Y
			tableYXT = valuesCPTResultTestDecisionTestYXT(sensitivity,specificity);
			potentialY = createTablePotential(roleProbability, tableYXT, variableY, variableX, variableT);
			
			//Potential U1
			potentialU1 = createTablePotential(PotentialRole.UTILITY,tableU1XD,variableX, variableD);
			potentialU1.setUtilityVariable(variableU1);
			
			//Potential U2
			potentialU2 = createTablePotential(PotentialRole.UTILITY,tableU2D,variableD);
			potentialU2.setUtilityVariable(variableU2);
			
			//Potential U3
			potentialU3 = createTablePotential(PotentialRole.UTILITY,tableU3T,variableT);
			potentialU3.setUtilityVariable(variableU3);
				
			//Links throws NodeNotFoundException
			try {
				probNet.addLink(variableX, variableY, true);
				probNet.addLink(variableT, variableY, true);
				probNet.addLink(variableY, variableD, true);
				probNet.addLink(variableX, variableU1, true);
				probNet.addLink(variableD, variableU1, true);
				probNet.addLink(variableD, variableU2, true);
				probNet.addLink(variableT, variableU3, true);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
			addPotentials(probNet,potentialX,potentialY,potentialU1,potentialU2,potentialU3);
			
			return probNet;
	}
	
	public static ProbNet buildIDSVDecideTestSymptom() {
		ProbNet probNet = buildIDDecideTestSymptom();
		
		List<Node> utilNodes = probNet.getNodes(NodeType.UTILITY);
		Variable utilVariables[] = new Variable[utilNodes.size()];
		
		for (int i=0; i < utilNodes.size(); i++) {
			utilVariables[i] = utilNodes.get(i).getVariable();
		}
		
		Variable variableU = new Variable("U");

		// Add variables to the network
		addVariables(probNet, NodeType.UTILITY, variableU);

		// additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");
		setAdditionalProperties(relevance, value, variableU);

		

		// Potential U2
		SumPotential potentialU = createSumPotential(variableU,utilVariables);

		// Links throws NodeNotFoundException
		try {
			for (Variable utilVar:utilVariables) {
				probNet.addLink(utilVar, variableU, true);
			}
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		addPotentials(probNet, potentialU);
		
		return probNet;
	}
	
	public static ProbNet buildIDDecideTestSymptom() {
		  ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		  // Variables
		  Variable varDisease = new Variable("Disease", "absent", "present");
		  Variable varResult_of_test = new Variable("Result of test", "not-performed", "negative", "positive");
		  Variable varSymptom = new Variable("Symptom", "absent", "present");
		  Variable varTherapy = new Variable("Therapy", "no", "yes");
		  Variable varDo_test = new Variable("Do test?", "no", "yes");
		  Variable varHealth_state = new Variable("Health state");
		  Variable varCost_of_test = new Variable("Cost of test");
		  Variable varCost_of_therapy = new Variable("Cost of therapy");

		  // Nodes
		  Node nodeDisease= probNet.addNode(varDisease, NodeType.CHANCE);
		  Node nodeResult_of_test= probNet.addNode(varResult_of_test, NodeType.CHANCE);
		  Node nodeSymptom= probNet.addNode(varSymptom, NodeType.CHANCE);
		  Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		  Node nodeDo_test = probNet.addNode(varDo_test, NodeType.DECISION);
		  Node nodeHealth_state= probNet.addNode(varHealth_state, NodeType.UTILITY);
		  Node nodeCost_of_test= probNet.addNode(varCost_of_test, NodeType.UTILITY);
		  Node nodeCost_of_therapy= probNet.addNode(varCost_of_therapy, NodeType.UTILITY);

		  // Links
		  probNet.makeLinksExplicit(false);
		  probNet.addLink(nodeDisease, nodeHealth_state, true);
		  probNet.addLink(nodeDisease, nodeResult_of_test, true);
		  probNet.addLink(nodeDisease, nodeSymptom, true);
		  probNet.addLink(nodeResult_of_test, nodeTherapy, true);
		  probNet.addLink(nodeSymptom, nodeDo_test, true);
		  probNet.addLink(nodeTherapy, nodeHealth_state, true);
		  probNet.addLink(nodeTherapy, nodeCost_of_therapy, true);
		  probNet.addLink(nodeDo_test, nodeCost_of_test, true);
		  probNet.addLink(nodeDo_test, nodeTherapy, true);
		  probNet.addLink(nodeDo_test, nodeResult_of_test, true);

		  // Potentials
		  TablePotential potDisease = new TablePotential(Arrays.asList(varDisease), PotentialRole.CONDITIONAL_PROBABILITY);
		  potDisease.values = new double[]{0.98, 0.02};
		  nodeDisease.setPotential(potDisease);

		  TablePotential potResult_of_test = new TablePotential(Arrays.asList(varResult_of_test, varDisease, varDo_test), PotentialRole.CONDITIONAL_PROBABILITY);
		  potResult_of_test.values = new double[]{1, 0, 0, 1, 0, 0, 0, 0.97, 0.03, 0, 0.09, 0.91};
		  nodeResult_of_test.setPotential(potResult_of_test);

		  TablePotential potSymptom = new TablePotential(Arrays.asList(varSymptom, varDisease), PotentialRole.CONDITIONAL_PROBABILITY);
		  potSymptom.values = new double[]{0.95, 0.05, 0.2, 0.8};
		  nodeSymptom.setPotential(potSymptom);

		  TablePotential potHealth_state = new TablePotential(varHealth_state,Arrays.asList(varDisease, varTherapy));
		  potHealth_state.values = new double[]{10, 3, 9, 8};
		  nodeHealth_state.setPotential(potHealth_state);

		  TablePotential potCost_of_test = new TablePotential(varCost_of_test,Arrays.asList(varDo_test));
		  potCost_of_test.values = new double[]{0, -0.2};
		  nodeCost_of_test.setPotential(potCost_of_test);

		  TablePotential potCost_of_therapy = new TablePotential(varCost_of_therapy,Arrays.asList(varTherapy));
		  potCost_of_therapy.values = new double[]{0, -0.25};
		  nodeCost_of_therapy.setPotential(potCost_of_therapy);

		  // Link restrictions and revealing states
		  // Always observed nodes

		 return probNet;
		}
	
	public static ProbNet buildIDTestAlways(){
		return buildIDTestAlways(0.14,0.91,0.97);
	}
	
	protected static ProbNet buildIDTestAlways(
			double prevalence,
			double sensitivity,
			double specificity) {
			
			ProbNet probNet;
			PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
			double [] tableYXT;
			TablePotential potentialX;
			TablePotential potentialY;
			TablePotential potentialU1;
			TablePotential potentialU2;
			double[] tableU1XD = {7.8, 8.8, 2.8, 9.8};
			double[] tableU2D = {-0.25, 0.0};
						
			probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
			
			// Define the variables
			// Define the variables
			Variable variableX = new Variable(diseaseName,diseaseStates);
			Variable variableY = new Variable(testResultName,testResultStates[0],testResultStates[1],"noresult");
			Variable variableD = new Variable(therapyName,"yes","no");
			Variable variableU1 = new Variable(healthStateName);
			Variable variableU2 = new Variable(therapyCostName);
			
			//Add variables to the network			
			addVariables(probNet,NodeType.CHANCE,variableX,variableY);
			addVariables(probNet,NodeType.DECISION,variableD);
			addVariables(probNet,NodeType.UTILITY,variableU1,variableU2);
			
			//additional properties
			String relevance = new String("Relevance");
			String value = new String("7.0");				
			setAdditionalProperties(relevance,value,variableX,variableY,variableD,variableU1,variableU2);	
			
			//Potential X
			potentialX = createPotentialDisease(prevalence,roleProbability,variableX);
			
			//Potential Y
			tableYXT = valuesCPTResultTestDecisionTestYXT(sensitivity,specificity);
			potentialY = createTablePotential(roleProbability, tableYXT, variableY, variableX);
			
			//Potential U1
			potentialU1 = createTablePotential(PotentialRole.UTILITY,tableU1XD,variableX, variableD);
			potentialU1.setUtilityVariable(variableU1);
			
			//Potential U2
			potentialU2 = createTablePotential(PotentialRole.UTILITY,tableU2D,variableD);
			potentialU2.setUtilityVariable(variableU2);
			
			//Links throws NodeNotFoundException
			try {
				probNet.addLink(variableX, variableY, true);
				probNet.addLink(variableY, variableD, true);
				probNet.addLink(variableX, variableU1, true);
				probNet.addLink(variableD, variableU1, true);
				probNet.addLink(variableD, variableU2, true);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
			addPotentials(probNet,potentialX,potentialY,potentialU1,potentialU2);
			
			return probNet;
	}
	
	public static ProbNet buildIDPerfectKnowledge(){
		ProbNet perfectKnowledge = buildIDNoKnowledge();
			Variable disease = null;
			Variable therapy = null;
			try {
				disease = perfectKnowledge.getVariable(diseaseName);
				therapy = perfectKnowledge.getVariable(therapyName);
				perfectKnowledge.addLink(disease, therapy, true);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
				System.err.println("Variable not found");
			}
			return perfectKnowledge;
		}
	

}
