/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MPADType;

/**
 * @author manolo
 * This class is used for building ProbNets corresponding to some networks used in the tests.
 * Networks are built via Java sentences not requiring any parser.
 *
 */
public class NetsFactory {
	
	static String diseaseStates[]={"present","absent"};
	static String testResultStates[]={"positive","negative"};
	static String yesNoStates[]={"yes","no"};
	
	/**
	 * @param variables
	 * @return An ArrayList containing the variables
	 */
	private static List<Variable> createVariableList(Variable...variables){
		List<Variable> list = new ArrayList<Variable>();
		for (int i=0;i<variables.length;i++){
			list.add(variables[i]);
		}
		return list;
		
	}
	
	private static double[] valuesAPrioriDisease(double prevalence){
		double values[];
		
		values = new double[2];
		values[0] = prevalence;
		values[1] = 1.0-prevalence;
		
		return values;
	}
	
	private static double[] valuesCPTResultTest(double sensitivity, double specificity){
		
		double [] values = {sensitivity, 1.0-sensitivity, 1.0-specificity, specificity};
		//double [] values = {specificity, 1.0-specificity, 1.0-sensitivity, sensitivity};
				
		return values;
	}
	
	
private static double[] valuesCPTResultTestDecisionTestYXT(double sensitivity, double specificity){
		
		double [] values = {sensitivity, 1.0-sensitivity, 0.0, 1.0-specificity, specificity, 0.0,
				0.0, 0.0, 1.0, 0.0, 0.0, 1.0};
				
		return values;
	}
	
	
	
	/**
	 * @param net
	 * @param potentials
	 * It adds a list of potentials to the network.
	 */
	private static void addPotentials(ProbNet net, Potential...potentials){
		for (int i=0;i<potentials.length;i++){
			net.addPotential(potentials[i]);
		}
	}
	
	/**
	 * @param net Network
	 * @param nodeType The type of node
	 * @param variables List of variables to add
	 * It adds a list of variables to the network.
	 */
	private static void addVariables(ProbNet net,NodeType nodeType,Variable...variables){
		for (int i=0;i<variables.length;i++){
			net.addProbNode(variables[i],nodeType);
		}
	}
	
	/**
	 * @param role Role of the potential
	 * @param values Values of the potential
	 * @param variables Variables
	 * @return A TablePotential
	 */
	private static TablePotential createTablePotential(PotentialRole role,
			double[] values, Variable... variables) {
		
		return new TablePotential(createVariableList(variables), role, values);
	}
	
	/**
	 * Create utility potential
	 * @param values Values of the potential
	 * @param variables Variables
	 * @return A TablePotential
	 */
	private static SumPotential createSumPotential(Variable varSV,Variable... parents) {
		
		return new SumPotential(varSV, createVariableList(parents));
	}
	
	/**
	 * @param relevance
	 * @param value
	 * @param variables
	 * It sets the relevance to a set of variables
	 */
	private static void setAdditionalProperties(String relevance, String value,
			Variable... variables) {
		for (Variable variable:variables){
			variable.setAdditionalProperty(relevance, value);
		}
		
	}


	/**
	 * @return a Bayesian network with one node node (X)
	 * @throws Exception
	 */
	public static ProbNet createBN_X(double prevalence) throws Exception {
		ProbNet probNet;
		double[] valuesX;
				
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
					
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		
		// Define the variables
		Variable variableX = new Variable("X",diseaseStates);
			
		addVariables(probNet,NodeType.CHANCE,variableX);

		valuesX = valuesAPrioriDisease(prevalence);
		TablePotential potentialX = createTablePotential(role,valuesX,variableX);
		
		addPotentials(probNet,potentialX);
		
		return probNet;
}
	
	
	
	/**
	 * @return a Bayesian network with two nodes (X and Y) and a link X -> Y
	 * @throws Exception
	 */
	public static ProbNet createBN_XY(double prevalence,double sensitivity,double specificity) throws Exception {
		ProbNet probNet;
		double[] valuesX;
		double [] valuesYX;
				
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
					
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		
		// Define the variables
		Variable variableX = new Variable("X",diseaseStates);
		Variable variableY = new Variable("Y",testResultStates);
			
		addVariables(probNet,NodeType.CHANCE,variableX,variableY);

		probNet.addLink(variableX,variableY, true);		

		valuesX = valuesAPrioriDisease(prevalence);
		TablePotential potentialX = createTablePotential(role,valuesX,variableX);
		
		valuesYX = valuesCPTResultTest(sensitivity,specificity);
		TablePotential potentialYX = createTablePotential(role, valuesYX, variableY, variableX);
		
		addPotentials(probNet,potentialX,potentialYX);
		
		return probNet;
}
	
	/**
	 * @return a Bayesian network with three nodes (X, Y and Z) and two links X -> Y, and Y -> Z
	 * @throws Exception
	 */
	public static ProbNet createBN_XYZ(double prevalence,double sensitivityY,double specificityY,
			double sensitivityZ, double specificityZ) throws Exception {
		ProbNet probNet;
		double[] valuesX;
		double [] valuesYX;
		double [] valuesZY;
				
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
					
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		
		// Define the variables
		Variable variableX = new Variable("X",diseaseStates);
		Variable variableY = new Variable("Y",testResultStates);
		Variable variableZ = new Variable("Z",testResultStates);
			
		addVariables(probNet,NodeType.CHANCE,variableX,variableY,variableZ);

		probNet.addLink(variableX,variableY, true);		

		valuesX = valuesAPrioriDisease(prevalence);
		TablePotential potentialX = createTablePotential(role,valuesX,variableX);
		
		valuesYX = valuesCPTResultTest(sensitivityY,specificityY);
		TablePotential potentialYX = createTablePotential(role, valuesYX, variableY, variableX);
		
		valuesZY = valuesCPTResultTest(sensitivityZ,specificityZ);
		TablePotential potentialZY = createTablePotential(role, valuesZY, variableZ, variableY);
		
		addPotentials(probNet,potentialX,potentialYX,potentialZY);
		
		return probNet;
}
	
	
	


	/**
	 * @return A Bayesian network with three nodes (A, B and C) and two links A -> B, and A -> C.
	 * This network was stored in file "peque.elv"
	 */
	public static ProbNet createBN_ABC(){
		Variable variableA;
		Variable variableB;
		Variable variableC;
		double [] tableA;
		double [] tableBA;
		
		ProbNet peque = new ProbNet();
		
		String nameStates[]=diseaseStates;
		//Finite States variables}
		variableA = new Variable("A",nameStates);
		variableB = new Variable("B",nameStates);
		variableC = new Variable("C",nameStates);
			
				
		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");
		
		setAdditionalProperties(relevance,value,variableA);
			
		variableA.setAdditionalProperty(relevance,value);
		variableB.setAdditionalProperty(relevance,value);
		variableC.setAdditionalProperty(relevance,value);
		
		addVariables(peque,NodeType.CHANCE,variableA,variableB, variableC);
				
		//Potentials
		//PotentialType type = PotentialType.TABLE;
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
		
		//Potential A
		tableA = valuesAPrioriDisease(0.8);
		TablePotential potentialA = createTablePotential(role,tableA,variableA);
		
		//Potential BA
		tableBA = valuesCPTResultTest(0.1,0.7);
		TablePotential potentialBA = createTablePotential(role,tableBA,variableB,variableA);
		
		//potencial CAB
		double [] tableCAB = {0.02, 0.98, 0.71, 0.29, 0.16, 0.84, 0.85, 0.15};
		TablePotential potentialCAB = createTablePotential(role,tableCAB,variableC,variableA,variableB);
		
		NodeType nodeType = NodeType.CHANCE;
		
		addVariables(peque,nodeType,variableA,variableB,variableC);
		
		//Links throws NodeNotFoundException
		try {
			peque.addLink(variableA, variableB, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		try {
			peque.addLink(variableA, variableC, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		try {
			peque.addLink(variableB, variableC, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
				
		addPotentials(peque,potentialA,potentialBA,potentialCAB);
	
	
	return peque;
}
	
	
	/**
	 * @return A Bayesian network with three nodes (A, B and C) and two links A -> B, and A -> C.
	 * This network was stored in file "peque.elv"
	 */
	public static ProbNet createBN_Asia(){
		Variable variableA;
		Variable variableB;
		Variable variableT;
		Variable variableL;
		Variable variableTOrC;
		Variable variableX;
		Variable variableD;
		Variable variableS;
		
		ProbNet network = new ProbNet();
		
		//Finite States variables
		//"Visit to Asia"
		variableA = new Variable("A",yesNoStates);
		//"Smoker"
		variableS = new Variable("S",yesNoStates);
		//"Tuberculosis"
		variableT = new Variable("T",diseaseStates);
		//"Lung Cancer"
		variableL = new Variable("L",diseaseStates);
		//"Bronchitis"
		variableB = new Variable("B",diseaseStates);
		//"Tuberculosis or Cancer"
		variableTOrC = new Variable("TOrC",yesNoStates);
		//"Positive X-ray"
		variableX = new Variable("X",yesNoStates);
		//"Dyspnea"
		variableD = new Variable("D",yesNoStates);
				
		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");
		
		addVariables(network,NodeType.CHANCE,variableA,variableS,variableT,
				variableL,variableB,variableTOrC,variableX,variableD);
		
		List<Variable> variables2 = network.getVariables();
		setAdditionalProperties(relevance,value,(Variable[]) variables2.toArray(new Variable[variables2.size()]));
				
		//Potentials
		//PotentialType type = PotentialType.TABLE;
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
		
		//Potential A
		double [] tableA = {0.01, 0.99};
		TablePotential potentialA = createTablePotential(role,tableA,variableA);
				
		//Potential S
		double [] tableS = {0.5, 0.5};
		TablePotential potentialS = createTablePotential(role,tableS,variableS);
		
		//Potential T
		double [] tableT = {0.05, 0.95, 0.01, 0.99};
		TablePotential potentialT = createTablePotential(role,tableT,variableT,variableA);
		
		//Potential L
		double [] tableL = {0.1, 0.9, 0.01, 0.99};
		TablePotential potentialL = createTablePotential(role,tableL,variableL,variableS);
		
		//Potential B
		double [] tableB = {0.6, 0.4, 0.3, 0.7};
		TablePotential potentialB = createTablePotential(role,tableB,variableB,variableS);
				
		//Potential TOrC
		double [] tableTOrC = {1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0};
		TablePotential potentialTOrC = createTablePotential(role,tableTOrC,variableTOrC,variableL,variableT);
		
		//Potential X
		double [] tableX = {0.98, 0.02, 0.05, 0.95};
		TablePotential potentialX = createTablePotential(role,tableX,variableX,variableTOrC);
		
		//Potential D
		double [] tableD = {0.9, 0.1, 0.7, 0.3, 0.8, 0.2, 0.1, 0.9};
		TablePotential potentialD = createTablePotential(role,tableD,variableD,variableTOrC,variableB);
		
		addPotentials(network,potentialA,potentialS,potentialT,potentialL,potentialB,potentialTOrC,potentialX,potentialD);
	
	
	return network;
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
			Variable variableX = new Variable("X",diseaseStates);
			Variable variableY = new Variable("Y",testResultStates);
			Variable variableD = new Variable("D","yes","no");
			Variable variableU = new Variable("U");
			
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
	
	
	private static TablePotential createPotentialDisease(double prevalence,
			PotentialRole roleProbability, Variable variableX) {
		double[] tableX = valuesAPrioriDisease(prevalence);
		TablePotential potentialX = createTablePotential(roleProbability, tableX, variableX);
		return potentialX;
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
	 */
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
			} catch (ProbNodeNotFoundException e1) {
				e1.printStackTrace();
			}
			Variable variableU2 = null;
			try {
				variableU2 = probNet.getVariable("U2");
			} catch (ProbNodeNotFoundException e1) {
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

	public static ProbNet createInfluenceDiagramDecisionTestProblemWithoutSV(
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
			double[] tableU1XD = {80.0, 90.0, 30.0, 100.0};
			double[] tableU2T = {-2.0, 0.0};
						
			probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
			
			// Define the variables
			// Define the variables
			Variable variableX = new Variable("X",diseaseStates);
			Variable variableY = new Variable("Y",testResultStates[0],testResultStates[1],"noresult");
			Variable variableD = new Variable("D","yes","no");
			Variable variableT = new Variable("T","yes","no");
			Variable variableU1 = new Variable("U1");
			Variable variableU2 = new Variable("U2");
			
			//Add variables to the network			
			addVariables(probNet,NodeType.CHANCE,variableX,variableY);
			addVariables(probNet,NodeType.DECISION,variableD,variableT);
			addVariables(probNet,NodeType.UTILITY,variableU1,variableU2);
			
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
			potentialU2 = createTablePotential(PotentialRole.UTILITY,tableU2T,variableT);
			potentialU2.setUtilityVariable(variableU2);
				
			//Links throws NodeNotFoundException
			try {
				probNet.addLink(variableX, variableY, true);
				probNet.addLink(variableT, variableY, true);
				probNet.addLink(variableY, variableD, true);
				probNet.addLink(variableX, variableU1, true);
				probNet.addLink(variableD, variableU1, true);
				probNet.addLink(variableT, variableU2, true);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
			addPotentials(probNet,potentialX,potentialY,potentialU1,potentialU2);
			
			return probNet;
	}
	
	public static ProbNet createMPADWithoutStateVariable(){
		return createMPADWithoutStateVariable(0.9,1.0,40000,0);
	}
	
	public static ProbNet createMPADWithoutStateVariable(double qoLTreat,double qoLNoTreat,double costTreat,double costNoTreat){
		// Define the variables
		TablePotential potentialQoL;
		TablePotential potentialCostOfTreatment;
		double[] tableQoL = {qoLTreat, qoLNoTreat};
		double[] tableCostOfTreatment = {costTreat, costNoTreat};
		
		//Decision criteria
		ArrayList<StringWithProperties> decisionCriteria = new ArrayList<>();
		StringWithProperties cost = new StringWithProperties("cost");
		StringWithProperties effectiveness = new StringWithProperties("effectiveness");
		decisionCriteria.add(cost);
		decisionCriteria.add(effectiveness);
		
				
		Variable variableTreatment = new Variable("Treatment",yesNoStates);
		Variable variableCostOfTreatment = new Variable("Cost of treatment");
		variableCostOfTreatment.setDecisionCriterion(cost);
		Variable variableQoL = createTemporalVariable("QoL",0);
		variableQoL.setDecisionCriterion(effectiveness);
		ProbNet probNet = new ProbNet(MPADType.getUniqueInstance());

		//set decision criteria to the network
		probNet.setDecisionCriteria2(decisionCriteria);
		
		//Add variables to the network			
		addVariables(probNet,NodeType.DECISION,variableTreatment);
		addVariables(probNet,NodeType.UTILITY,variableQoL,variableCostOfTreatment);
		
		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");				
		setAdditionalProperties(relevance,value,variableTreatment,variableQoL,variableCostOfTreatment);
		
		//Potential QoL
		potentialQoL = createTablePotential(PotentialRole.UTILITY,tableQoL,variableTreatment);
		potentialQoL.setUtilityVariable(variableQoL);
		
		//Potential Treatment
		potentialCostOfTreatment = createTablePotential(PotentialRole.UTILITY,tableCostOfTreatment,variableTreatment);
		potentialCostOfTreatment.setUtilityVariable(variableCostOfTreatment);
		
		//Links throws NodeNotFoundException
		try {
			probNet.addLink(variableTreatment, variableQoL, true);
			probNet.addLink(variableTreatment, variableCostOfTreatment, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		
		addPotentials(probNet,potentialQoL,potentialCostOfTreatment);
		
		return probNet;
	}
	
	
	/**
	 * @return A simple Markov Model proposed for jdiez for testing cost-effectiveness analysis and inference
	 */
	public static ProbNet createMPADDeadAlive(){
		return createMPADWithStateVariable(0.8,1.0,40000,0,0.7,0.5);
	}
	
	
	
	public static ProbNet createMPADWithStateVariable(double qoLTreat,double qoLNoTreat,double costTreat,double costNoTreat,double probAliveIfTreat, double probAliveIfNoTreat){
		TablePotential potentialQoL;
		TablePotential potentialCostOfTreatment;
		double[] tableQoL = {0.0, qoLTreat, 0.0, qoLNoTreat};
		double[] tableCostOfTreatment = {costTreat, costNoTreat};
		String[] statesStateVariable = {"dead", "alive"};
		
		//Decision criteria
		ArrayList<StringWithProperties> decisionCriteria = new ArrayList<>();
		StringWithProperties cost = new StringWithProperties("cost");
		StringWithProperties effectiveness = new StringWithProperties("effectiveness");
		decisionCriteria.add(cost);
		decisionCriteria.add(effectiveness);
		
		Variable variableTreatment = new Variable("Treatment",yesNoStates);
		Variable variableCostOfTreatment = new Variable("Cost of treatment");
		variableCostOfTreatment.setDecisionCriterion(cost);
		Variable variableQoL = createTemporalVariable("QoL",0);
		variableQoL.setDecisionCriterion(effectiveness);
		Variable variableState0 = createTemporalVariable("State",0,statesStateVariable);
		Variable variableState1 = createTemporalVariable("State",1,statesStateVariable);
		
		ProbNet probNet = new ProbNet(MPADType.getUniqueInstance());

		//set decision criteria to the network
		probNet.setDecisionCriteria2(decisionCriteria);
		
		//Add variables to the network	
		addVariables(probNet,NodeType.CHANCE,variableState0,variableState1);
		addVariables(probNet,NodeType.DECISION,variableTreatment);
		addVariables(probNet,NodeType.UTILITY,variableQoL,variableCostOfTreatment);
		
		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");				
		setAdditionalProperties(relevance,value,variableState0,variableState1,variableTreatment,variableQoL,variableCostOfTreatment);
		
		//Potential State0
		double []probabilitiesState0 = {0.0,1.0};
		TablePotential potentialState0 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY,probabilitiesState0,variableState0);
		
		//Potential State1
		double []probabilitiesState1 = {1.0, 0.0, 1.0-probAliveIfTreat, probAliveIfTreat, 1.0, 0.0, 1.0-probAliveIfNoTreat, probAliveIfNoTreat};
		TablePotential potentialState1 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY,probabilitiesState1,variableState1,variableState0,variableTreatment);
					
		//Potential Treatment
		potentialCostOfTreatment = createTablePotential(PotentialRole.UTILITY,tableCostOfTreatment,variableTreatment);
		potentialCostOfTreatment.setUtilityVariable(variableCostOfTreatment);
		
		//Potential QoL
		potentialQoL = createTablePotential(PotentialRole.UTILITY,tableQoL,variableState0,variableTreatment);
		potentialQoL.setUtilityVariable(variableQoL);
		
		//Links throws NodeNotFoundException
		try {
			probNet.addLink(variableTreatment, variableCostOfTreatment, true);
			probNet.addLink(variableTreatment, variableQoL, true);
			probNet.addLink(variableTreatment, variableState1, true);
			probNet.addLink(variableState0, variableQoL, true);
			probNet.addLink(variableState0, variableState1, true);
			
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		
		addPotentials(probNet,potentialQoL,potentialCostOfTreatment,potentialState0,potentialState1);
		
		return probNet;
	}
	
	public static ProbNet createSemiMarkovOnlyChanceNet() {
		ProbNet probNet = new ProbNet(MPADType.getUniqueInstance());
		//Decision criteria
		ArrayList<StringWithProperties> decisionCriteria = new ArrayList<>();
		StringWithProperties cost = new StringWithProperties("cost");
		StringWithProperties effectiveness = new StringWithProperties("effectiveness");
		decisionCriteria.add(cost);
		decisionCriteria.add(effectiveness);
		
		//set decision criteria to the network
		probNet.setDecisionCriteria2(decisionCriteria);

		//Variables
		Variable duration0 = new Variable("Duration", true, 0.0, 20.0, true, 1);
		duration0.setBaseName("Duration");
		duration0.setName("Duration [0]");
		duration0.setTimeSlice(0);
		Variable duration1 = new Variable("Duration", true, 0.0, 20.0, true, 1);
		duration1.setBaseName("Duration");
		duration1.setName("Duration [1]");
		duration1.setTimeSlice(1);
		
		Variable state0 = new Variable("State", "dead", "alive");
		state0.setBaseName("State");
		state0.setName("State [0]");
		state0.setTimeSlice(0);
		Variable state1 = new Variable("State", "dead", "alive");
		state1.setBaseName("State");
		state1.setName("State [1]");
		state1.setTimeSlice(1);
		
		//Add variables to the network	
		addVariables(probNet,NodeType.CHANCE,duration0,duration1,state0,state1);
		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");				
		setAdditionalProperties(relevance,value,duration0,duration1,state0,state1);
		//Potential State0
		double []probabilitiesState0 = {0.0,1.0};
		TablePotential potentialState0 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY,probabilitiesState0,state0);
		//table
		double []branch1 = {0.5, 0.5, 0.0, 1.0};
		TablePotential table1 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch1, state1, state0);
		double []branch2 = {0.3, 0.7, 0.0, 1.0};
		TablePotential table2 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch2, state1, state0);
		//Potential state1
		ArrayList<Variable> variables = new ArrayList<>();
		variables.add(state1);
		variables.add(state0);
		variables.add(duration0);
		ArrayList<TreeADDBranch> branches = new ArrayList<>();
		branches.add(new TreeADDBranch(new Threshold(0, false), new Threshold(2, true), duration0, table1, variables));
		branches.add(new TreeADDBranch(new Threshold(2, true), new Threshold(20, true), duration0, table2, variables));
		TreeADDPotential potentialState1 = new TreeADDPotential(variables, duration0, PotentialRole.CONDITIONAL_PROBABILITY, branches);
		//potential duratio0
		ArrayList<Variable> variablesDuration0 = new ArrayList<>();
		variablesDuration0.add(duration0);
		UniformPotential potentialduration0 = new UniformPotential(variablesDuration0, PotentialRole.CONDITIONAL_PROBABILITY);
		//potential duration1
		ArrayList<Variable> variablesDuration1 = new ArrayList<>();
		variablesDuration1.add(duration1);
		variablesDuration1.add(duration0);
		CycleLengthShift potetialDuration1 = new CycleLengthShift(variablesDuration1);
		
		//links
		try {
			probNet.addLink(state0, state1, true);
			probNet.addLink(duration0, duration1, true);
			probNet.addLink(duration0, state1, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		
		//adding potentials to network
		addPotentials(probNet,potentialState0,potentialState1,potentialduration0,potetialDuration1);
		
				
		return probNet;
	}
	
	public static ProbNet createSemiMarkovModelNet() {
		ProbNet probNet = new ProbNet(MPADType.getUniqueInstance());
		//Decision criteria
		ArrayList<StringWithProperties> decisionCriteria = new ArrayList<>();
		StringWithProperties cost = new StringWithProperties("cost");
		StringWithProperties effectiveness = new StringWithProperties("effectiveness");
		decisionCriteria.add(cost);
		decisionCriteria.add(effectiveness);
		
		//set decision criteria to the network
		probNet.setDecisionCriteria2(decisionCriteria);

		//Variables
		Variable duration0 = new Variable("Duration", true, 0.0, 20.0, true, 1);
		duration0.setTimeSlice(0);
		duration0.setBaseName("Duration");
		duration0.setName("Duration [0]");
		Variable duration1 = new Variable("Duration", true, 0.0, 20.0, true, 1);
		duration1.setTimeSlice(1);
		duration1.setBaseName("Duration");
		duration1.setName("Duration [1]");
		
		Variable state0 = createTemporalVariable("State",0, "dead", "alive");
		Variable state1 = createTemporalVariable("State",1, "dead", "alive");
		
		
		Variable variableTreatment = new Variable("Treatment",yesNoStates);
		Variable variableCost = createTemporalVariable("Cost",0);
		variableCost.setDecisionCriterion(cost);
		Variable variableQoL = createTemporalVariable("QoL",0);
		variableQoL.setDecisionCriterion(effectiveness);
		
		//Add variables to the network	
		addVariables(probNet,NodeType.CHANCE,duration0,duration1,state0,state1,variableTreatment,variableCost,variableQoL);
		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");				
		setAdditionalProperties(relevance,value,duration0,duration1,state0,state1,variableTreatment,variableCost,variableQoL);
		//Potential State0
		double []probabilitiesState0 = {0.0,1.0};
		TablePotential potentialState0 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY,probabilitiesState0,state0);
		
		//potential State1
		ArrayList<Variable> variablesTree = new ArrayList<>();
		variablesTree.add(state1);
		variablesTree.add(variableTreatment);
		variablesTree.add(state0);
		variablesTree.add(duration0);
		
		double []branch1 = {0.0, 1.0};
		TablePotential table1 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch1, state1);
		
		ArrayList<State> statesNo = new ArrayList<>();
		try {
			statesNo.add(variableTreatment.getStates()[variableTreatment.getStateIndex("no")]);
		} catch (InvalidStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		TreeADDBranch branchNo = new TreeADDBranch(statesNo, variableTreatment, table1, variablesTree);
		//table
		double []branch11 = {0.5, 0.5, 0.0, 1.0};
		TablePotential table11 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch11, state1, state0);
		double []branch21 = {0.3, 0.7, 0.0, 1.0};
		TablePotential table2 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch21, state1, state0);
		//subtree
		ArrayList<Variable> variables = new ArrayList<>();
		variables.add(state1);
		variables.add(state0);
		variables.add(duration0);
		ArrayList<TreeADDBranch> branches = new ArrayList<>();
		branches.add(new TreeADDBranch(new Threshold(0, false), new Threshold(2, true), duration0, table11, variables));
		branches.add(new TreeADDBranch(new Threshold(2, true), new Threshold(20, true), duration0, table2, variables));
		TreeADDPotential subPotentialState1 = new TreeADDPotential(variables, duration0, PotentialRole.CONDITIONAL_PROBABILITY, branches);
		
		
		ArrayList<State> statesYes = new ArrayList<>();
		try {
			statesYes.add(variableTreatment.getStates()[variableTreatment.getStateIndex("yes")]);
		} catch (InvalidStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		TreeADDBranch branchYes = new TreeADDBranch(statesYes, variableTreatment, subPotentialState1, variablesTree);
		
		ArrayList<TreeADDBranch> treeBranches = new ArrayList<>();
		treeBranches.add(branchNo);
		treeBranches.add(branchYes);
		
		TreeADDPotential potentialState1 = new TreeADDPotential(variablesTree, variableTreatment, PotentialRole.CONDITIONAL_PROBABILITY, treeBranches);
		
		//potential duratio0
		ArrayList<Variable> variablesDuration0 = new ArrayList<>();
		variablesDuration0.add(duration0);
		UniformPotential potentialduration0 = new UniformPotential(variablesDuration0, PotentialRole.CONDITIONAL_PROBABILITY);
		//potential duration1
		ArrayList<Variable> variablesDuration1 = new ArrayList<>();
		variablesDuration1.add(duration1);
		variablesDuration1.add(duration0);
		CycleLengthShift potetialDuration1 = new CycleLengthShift(variablesDuration1);
		
		//potential cost [0]
		ArrayList<Variable> variablesCost = new ArrayList<>();
		//variablesCost.add(variableCost);
		variablesCost.add(variableTreatment);
		variablesCost.add(state0);
		
		//cost no treatment
		double costNoTreat []= {0.0};
		TablePotential costNoTreatment = createTablePotential(PotentialRole.UTILITY, costNoTreat);
		costNoTreatment.setUtilityVariable(variableCost);
		//cost treatment
		double costTreat []= {3000.0, 0.0};
		TablePotential costTreatment = createTablePotential(PotentialRole.UTILITY, costTreat, state0);
		costNoTreatment.setUtilityVariable(variableCost);
		
		ArrayList<TreeADDBranch> costBranches = new ArrayList<>();
		ArrayList<Variable> variablesBranchCost = new ArrayList<>();
		variablesBranchCost.add(variableCost);
		variablesBranchCost.addAll(variablesCost);
		costBranches.add(new TreeADDBranch(statesNo, variableTreatment, costNoTreatment, variablesBranchCost));
		costBranches.add(new TreeADDBranch(statesYes, variableTreatment, costTreatment, variablesBranchCost));
		
		TreeADDPotential potentialCost = new TreeADDPotential(variablesCost, variableTreatment, PotentialRole.UTILITY, costBranches);
		potentialCost.setUtilityVariable(variableCost);
		
		//potential Qol [0]
		ArrayList<Variable> variablesQoL = new ArrayList<>();
		//variablesCost.add(variableQoL);
		variablesQoL.add(variableTreatment);
		variablesQoL.add(state0);

		//cost no treatment
		double qolNoTreat []= {0.0};
		TablePotential qolNoTreatment = createTablePotential(PotentialRole.UTILITY, qolNoTreat);
		qolNoTreatment.setUtilityVariable(variableQoL);
		//cost treatment
		double qolTreat []= {1500.0, 0.0};
		TablePotential qolTreatment = createTablePotential(PotentialRole.UTILITY, qolTreat, state0);
		qolTreatment.setUtilityVariable(variableQoL);
		
		List<TreeADDBranch> qolBranches = new ArrayList<>();
		List<Variable> variablesBranchQoL = new ArrayList<>();
		variablesBranchQoL.add(variableQoL);
		variablesBranchQoL.addAll(variablesQoL);
		qolBranches.add(new TreeADDBranch(statesNo, variableTreatment, qolNoTreatment, variablesBranchQoL));
		qolBranches.add(new TreeADDBranch(statesYes, variableTreatment, qolTreatment, variablesBranchQoL));
		
		TreeADDPotential potentialQoL = new TreeADDPotential(variablesQoL, variableTreatment, PotentialRole.UTILITY, qolBranches);
		potentialQoL.setUtilityVariable(variableQoL);
		
		//links
		try {
			probNet.addLink(state0, state1, true);
			probNet.addLink(duration0, duration1, true);
			probNet.addLink(duration0, state1, true);
			probNet.addLink(variableTreatment, state1, true);
			probNet.addLink(variableTreatment, variableCost, true);
			probNet.addLink(variableTreatment, variableQoL, true);
			probNet.addLink(state0, variableQoL, true);
			probNet.addLink(state0, variableCost, true);
			
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		
		//adding potentials to network
		addPotentials(probNet,potentialState0,potentialState1,potentialduration0,potetialDuration1, potentialCost, potentialQoL);
		
				
		return probNet;
	}
	
	
	private static Variable createTemporalVariable(String baseName,int timeSlice, String... statesStateVariable){
		Variable variable = new Variable(baseName,statesStateVariable);
		variable.setBaseName(variable.getName());
		variable.setTimeSlice(timeSlice);
		return variable;
		
	}

	public static ProbNet buildDecideTestDAN() throws NodeNotFoundException {
		ProbNet decideTestDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		Variable variableX = new Variable("X", "absent", "present");
		Variable variableY = new Variable("Y", "negative", "positive");
		Variable variableD = new Variable("D","no","yes");
		Variable variableT = new Variable("T","no","yes");
		Variable variableU1 = new Variable("U1");
		Variable variableU2 = new Variable("U2");
		
		ProbNode nodeX = decideTestDAN.addProbNode(variableX, NodeType.CHANCE);
		ProbNode nodeY = decideTestDAN.addProbNode(variableY, NodeType.CHANCE);
		ProbNode nodeU1 = decideTestDAN.addProbNode(variableU1, NodeType.UTILITY);
		ProbNode nodeU2 = decideTestDAN.addProbNode(variableU2, NodeType.UTILITY);
		ProbNode nodeD = decideTestDAN.addProbNode(variableD, NodeType.DECISION);
		decideTestDAN.addProbNode(variableT, NodeType.DECISION);
		
		decideTestDAN.getGraph().makeLinksExplicit(false);
		decideTestDAN.addLink(variableX, variableY, true);
		decideTestDAN.addLink(variableX, variableU1, true);
		decideTestDAN.addLink(variableD, variableY, true);
		decideTestDAN.addLink(variableD, variableT, true);
		decideTestDAN.addLink(variableD, variableU2, true);
		decideTestDAN.addLink(variableT, variableU1, true);
		
		TablePotential potentialX = new TablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialX.values = new double [] {0.86, 0.14};
		nodeX.setPotential(potentialX);

		TablePotential potentialY = new TablePotential(Arrays.asList(variableY, variableD, variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialY.values = new double [] {0, 0, 0.97, 0.03, 0, 0, 0.09, 0.91};
		nodeY.setPotential(potentialY);
		
		TablePotential potentialU1 = new TablePotential(variableU1, Arrays.asList(variableX, variableT));
		potentialU1.values = new double [] {100, 30, 90, 80};
		nodeU1.setPotential(potentialU1);		
		
		TablePotential potentialU2 = new TablePotential(variableU1, Arrays.asList(variableD));
		potentialU2.values = new double [] {0, -2};
		nodeU2.setPotential(potentialU2);		
		
		Link link = decideTestDAN.getGraph().getLink(nodeD.getNode(), nodeY.getNode(), true);
		link.initializesRestrictionsPotential();
		TablePotential restrictionsPotential = (TablePotential)link.getRestrictionsPotential();
		restrictionsPotential.values = new double[]{0,1,0,1};
		
		link.setRevealingStates(Arrays.asList(variableD.getStates()[1]));
		
		return decideTestDAN;
	}
	
	public static ProbNet buildDatingDAN() throws NodeNotFoundException {
		ProbNet datingDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		Variable variableAsk = new Variable("Ask", "no", "yes");
		Variable variableNClub = new Variable("NClub", "no", "yes");
		Variable variableAccept = new Variable("Accept", "no", "yes");
		Variable variableLikesMe = new Variable("LikesMe","no","yes");
		Variable variableToDo = new Variable("ToDo","restaurant","movie");
		Variable variableTV = new Variable("TV","good","bad");
		Variable variableTVExp = new Variable("TVExp","negative","positive");
		Variable variableClub = new Variable("Club","negative","positive");
		Variable variableMeetFr = new Variable("MeetFr","negative","positive");
		Variable variableNCExp = new Variable("NCExp","negative","positive");
		Variable variableMovie = new Variable("Movie","romantic","action");
		Variable variableRest = new Variable("Rest","cheap","expensive");
		Variable variableMMood = new Variable("mMood","bad","good");
		Variable variableRMood = new Variable("rMood","bad","good");
		Variable variableMExp = new Variable("mExp","negative","positive");
		Variable variableRExp = new Variable("rExp","negative","positive");
		Variable variableUTVExp = new Variable("U TVExp");
		Variable variableUNCExp = new Variable("U NCExp");
		Variable variableUmExp = new Variable("U mExp");
		Variable variableUrExp = new Variable("U rCExp");
		
		ProbNode nodeAsk = datingDAN.addProbNode(variableAsk, NodeType.DECISION);
		ProbNode nodeNClub = datingDAN.addProbNode(variableNClub, NodeType.DECISION);
		ProbNode nodeAccept = datingDAN.addProbNode(variableAccept, NodeType.CHANCE);
		ProbNode nodeLikesMe = datingDAN.addProbNode(variableLikesMe, NodeType.CHANCE);
		ProbNode nodeToDo = datingDAN.addProbNode(variableToDo, NodeType.CHANCE);
		ProbNode nodeTV = datingDAN.addProbNode(variableTV, NodeType.CHANCE);
		ProbNode nodeTVExp = datingDAN.addProbNode(variableTVExp, NodeType.CHANCE);
		ProbNode nodeClub = datingDAN.addProbNode(variableClub, NodeType.CHANCE);
		ProbNode nodeMeetFr = datingDAN.addProbNode(variableMeetFr, NodeType.CHANCE);
		ProbNode nodeNCExp = datingDAN.addProbNode(variableNCExp, NodeType.CHANCE);
		ProbNode nodeMovie = datingDAN.addProbNode(variableMovie, NodeType.DECISION);
		ProbNode nodeRest = datingDAN.addProbNode(variableRest, NodeType.DECISION);
		ProbNode nodeMMood = datingDAN.addProbNode(variableMMood, NodeType.CHANCE);
		ProbNode nodeRMood = datingDAN.addProbNode(variableRMood, NodeType.CHANCE);
		ProbNode nodeMExp = datingDAN.addProbNode(variableMExp, NodeType.CHANCE);
		ProbNode nodeRExp = datingDAN.addProbNode(variableRExp, NodeType.CHANCE);
		ProbNode nodeUTVExp = datingDAN.addProbNode(variableUTVExp, NodeType.UTILITY);
		ProbNode nodeUNCExp = datingDAN.addProbNode(variableUNCExp, NodeType.UTILITY);
		ProbNode nodeUmExp = datingDAN.addProbNode(variableUmExp, NodeType.UTILITY);
		ProbNode nodeUrExp = datingDAN.addProbNode(variableUrExp, NodeType.UTILITY);
		
		datingDAN.getGraph().makeLinksExplicit(false);
		datingDAN.addLink(variableAsk, variableAccept, true);
		datingDAN.addLink(variableLikesMe, variableAccept, true);
		datingDAN.addLink(variableLikesMe, variableToDo, true);
		datingDAN.addLink(variableAccept, variableNClub, true);
		datingDAN.addLink(variableAccept, variableToDo, true);
		datingDAN.addLink(variableTV, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableClub, true);
		datingDAN.addLink(variableNClub, variableMeetFr, true);
		datingDAN.addLink(variableClub, variableNCExp, true);
		datingDAN.addLink(variableMeetFr, variableNCExp, true);
		datingDAN.addLink(variableTVExp, variableUTVExp, true);
		datingDAN.addLink(variableNCExp, variableUNCExp, true);
		datingDAN.addLink(variableToDo, variableMovie, true);
		datingDAN.addLink(variableToDo, variableRest, true);
		datingDAN.addLink(variableMovie, variableMMood, true);
		datingDAN.addLink(variableMovie, variableMExp, true);
		datingDAN.addLink(variableMMood, variableMExp, true);
		datingDAN.addLink(variableMExp, variableUmExp, true);
		datingDAN.addLink(variableRest, variableRMood, true);
		datingDAN.addLink(variableRest, variableRExp, true);
		datingDAN.addLink(variableRMood, variableRExp, true);
		datingDAN.addLink(variableRExp, variableUrExp, true);
		
		TablePotential potentialAccept = new TablePotential(Arrays.asList(variableAccept, variableAsk, variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialAccept.values = new double [] {1, 0, 0.99, 0.01, 1, 0, 0.9, 0.1};
		nodeAccept.setPotential(potentialAccept);

		UniformPotential potentialLikesMe = new UniformPotential(Arrays.asList(variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeLikesMe.setPotential(potentialLikesMe);
		
		TablePotential potentialToDo = new TablePotential(Arrays.asList(variableToDo, variableAccept, variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialToDo.values = new double [] {0, 0, 0.65, 0.35, 0, 0, 0.15, 0.85};
		nodeToDo.setPotential(potentialToDo);		
		
		TablePotential potentialMMood = new TablePotential(Arrays.asList(variableMMood, variableMovie), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMMood.values = new double [] {0.25, 0.75, 0.88, 0.12};
		nodeMMood.setPotential(potentialMMood);

		TablePotential potentialMExp = new TablePotential(Arrays.asList(variableMExp, variableMMood, variableMovie), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMExp.values = new double [] {0.99, 0.01, 0.95, 0.05, 0.15, 0.85, 0.01, 0.99};
		nodeMExp.setPotential(potentialMExp);

		TablePotential potentialUmExp = new TablePotential(variableUmExp, Arrays.asList(variableMExp));
		potentialUmExp.values = new double [] {-10, 10};
		nodeUmExp.setPotential(potentialUmExp);
		
		TablePotential potentialRMood = new TablePotential(Arrays.asList(variableRMood, variableRest), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRMood.values = new double [] {0.5, 0.5, 0.2, 0.8};
		nodeRMood.setPotential(potentialRMood);

		TablePotential potentialRExp = new TablePotential(Arrays.asList(variableRExp, variableRMood, variableRest), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRExp.values = new double [] {0.95, 0.05, 1, 0, 0.01, 0.99, 0.08, 0.92};
		nodeRExp.setPotential(potentialRExp);

		TablePotential potentialUrExp = new TablePotential(variableUrExp, Arrays.asList(variableRExp));
		potentialUrExp.values = new double [] {-10, 10};
		nodeUrExp.setPotential(potentialUrExp);		
		
		TablePotential potentialTV = new TablePotential(Arrays.asList(variableTV), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTV.values = new double [] {0.15, 0.85};
		nodeTV.setPotential(potentialTV);	
		
		TablePotential potentialTVExp = new TablePotential(Arrays.asList(variableTVExp, variableNClub, variableTV), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTVExp.values = new double [] {0, 1, 0, 0, 1, 0, 0, 0};
		nodeTVExp.setPotential(potentialTVExp);		

		TablePotential potentialClub = new TablePotential(Arrays.asList(variableClub, variableNClub), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialClub.values = new double [] {0, 0, 0.22, 0.78};
		nodeClub.setPotential(potentialClub);

		TablePotential potentialMeetFr = new TablePotential(Arrays.asList(variableMeetFr, variableNClub), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMeetFr.values = new double [] {0, 0, 0.16, 0.84};
		nodeMeetFr.setPotential(potentialMeetFr);
		
		ICIPotential potentialNCExp = new MaxPotential(Arrays.asList(variableNCExp, variableMeetFr, variableClub));
		potentialNCExp.setLeakyParameters(new double [] {0.99, 0.01});
		nodeNCExp.setPotential(potentialNCExp);			
		
		TablePotential potentialUTVExp = new TablePotential(variableUTVExp, Arrays.asList(variableTVExp));
		potentialUTVExp.values = new double [] {-10, 10};
		nodeUTVExp.setPotential(potentialUTVExp);		

		TablePotential potentialUNCExp = new TablePotential(variableUNCExp, Arrays.asList(variableNCExp));
		potentialUNCExp.values = new double [] {-10, 10};
		nodeUNCExp.setPotential(potentialUNCExp);		
		
		
		Link linkAskAccept = datingDAN.getGraph().getLink(nodeAsk.getNode(), nodeAccept.getNode(), true);
		linkAskAccept.initializesRestrictionsPotential();
		TablePotential restrictionsAskAccept = (TablePotential)linkAskAccept.getRestrictionsPotential();
		restrictionsAskAccept.values = new double[]{1,1,0,1};
		linkAskAccept.setRevealingStates(Arrays.asList(variableAsk.getStates()[0], variableAsk.getStates()[1]));

		Link linkAcceptNClub = datingDAN.getGraph().getLink(nodeAccept.getNode(), nodeNClub.getNode(), true);
		linkAcceptNClub.initializesRestrictionsPotential();
		TablePotential restrictionsAcceptNClub = (TablePotential)linkAcceptNClub.getRestrictionsPotential();
		restrictionsAcceptNClub.values = new double[]{1,0,1,0};

		Link linkAcceptToDo = datingDAN.getGraph().getLink(nodeAccept.getNode(), nodeToDo.getNode(), true);
		linkAcceptToDo.initializesRestrictionsPotential();
		TablePotential restrictionsAcceptToDo = (TablePotential)linkAcceptToDo.getRestrictionsPotential();
		restrictionsAcceptToDo.values = new double[]{0,1,0,1};
		linkAcceptToDo.setRevealingStates(Arrays.asList(variableAccept.getStates()[1]));

		Link linkToDoMovie = datingDAN.getGraph().getLink(nodeToDo.getNode(), nodeMovie.getNode(), true);
		linkToDoMovie.initializesRestrictionsPotential();
		TablePotential restrictionsToDoMovie = (TablePotential)linkToDoMovie.getRestrictionsPotential();
		restrictionsToDoMovie.values = new double[]{0,1,0,1};

		Link linkToDoRest = datingDAN.getGraph().getLink(nodeToDo.getNode(), nodeRest.getNode(), true);
		linkToDoRest.initializesRestrictionsPotential();
		TablePotential restrictionsToDoRest = (TablePotential)linkToDoRest.getRestrictionsPotential();
		restrictionsToDoRest.values = new double[]{1,0,1,0};

		Link linkMovieMEXp = datingDAN.getGraph().getLink(nodeMovie.getNode(), nodeMExp.getNode(), true);
		linkMovieMEXp.setRevealingStates(Arrays.asList(variableMovie.getStates()[0], variableMovie.getStates()[1]));

		Link linkRestREXp = datingDAN.getGraph().getLink(nodeRest.getNode(), nodeRExp.getNode(), true);
		linkRestREXp.setRevealingStates(Arrays.asList(variableRest.getStates()[0], variableRest.getStates()[1]));
		
		Link linkNClubTVExp = datingDAN.getGraph().getLink(nodeNClub.getNode(), nodeTVExp.getNode(), true);
		linkNClubTVExp.initializesRestrictionsPotential();
		TablePotential restrictionsNClubTVExp = (TablePotential)linkNClubTVExp.getRestrictionsPotential();
		restrictionsNClubTVExp.values = new double[]{1,0,1,0};
		linkNClubTVExp.setRevealingStates(Arrays.asList(variableNClub.getStates()[0]));

		Link linkNClubClub = datingDAN.getGraph().getLink(nodeNClub.getNode(), nodeClub.getNode(), true);
		linkNClubClub.initializesRestrictionsPotential();
		TablePotential restrictionsNClubClub = (TablePotential)linkNClubClub.getRestrictionsPotential();
		restrictionsNClubClub.values = new double[]{0,1,0,1};
		linkNClubClub.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));

		Link linkNClubMeetFr = datingDAN.getGraph().getLink(nodeNClub.getNode(), nodeMeetFr.getNode(), true);
		linkNClubMeetFr.initializesRestrictionsPotential();
		TablePotential restrictionsNClubMeetFr = (TablePotential)linkNClubMeetFr.getRestrictionsPotential();
		restrictionsNClubMeetFr.values = new double[]{0,1,0,1};
		linkNClubMeetFr.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));
		
		nodeTV.setAlwaysObserved(true);
		
		return datingDAN;
	}	

	public static ProbNet buildReactorDAN () {
		
	  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
	  // Variables
	  Variable varResult_of_advanced_reactor = new Variable("Result of advanced reactor", "success", "limited accident", "major accident");
	  Variable varResult_of_test = new Variable("Result of test", "bad", "good", "excellent");
	  Variable varResult_of_conventional_reactor = new Variable("Result of conventional reactor", "success", "failure");
	  Variable varAdvanced_reactor_reliability = new Variable("Advanced reactor reliability", "success", "limited accident", "major accident");
	  Variable varTest_decision = new Variable("Test decision", "test", "notest");
	  Variable varBuild_decision = new Variable("Build decision", "build advanced", "build conventional", "build none");
	  Variable varCost_of_test = new Variable("Cost of test");
	  Variable varBenefit_of_advanced_reactor = new Variable("Benefit of advanced reactor");
	  Variable varBenefit_of_conventional_reactor = new Variable("Benefit of conventional reactor");
	
	  // Nodes
	  ProbNode nodeResult_of_advanced_reactor= probNet.addProbNode(varResult_of_advanced_reactor, NodeType.CHANCE);
	  ProbNode nodeResult_of_test= probNet.addProbNode(varResult_of_test, NodeType.CHANCE);
	  ProbNode nodeResult_of_conventional_reactor= probNet.addProbNode(varResult_of_conventional_reactor, NodeType.CHANCE);
	  ProbNode nodeAdvanced_reactor_reliability= probNet.addProbNode(varAdvanced_reactor_reliability, NodeType.CHANCE);
	  ProbNode nodeTest_decision= probNet.addProbNode(varTest_decision, NodeType.DECISION);
	  ProbNode nodeBuild_decision= probNet.addProbNode(varBuild_decision, NodeType.DECISION);
	  ProbNode nodeCost_of_test= probNet.addProbNode(varCost_of_test, NodeType.UTILITY);
	  ProbNode nodeBenefit_of_advanced_reactor= probNet.addProbNode(varBenefit_of_advanced_reactor, NodeType.UTILITY);
	  ProbNode nodeBenefit_of_conventional_reactor= probNet.addProbNode(varBenefit_of_conventional_reactor, NodeType.UTILITY);
	
	  // Links
	  probNet.getGraph().makeLinksExplicit(false);
	  probNet.addLink(nodeResult_of_advanced_reactor, nodeBenefit_of_advanced_reactor, true);
	  probNet.addLink(nodeResult_of_test, nodeBuild_decision, true);
	  probNet.addLink(nodeResult_of_conventional_reactor, nodeBenefit_of_conventional_reactor, true);
	  probNet.addLink(nodeAdvanced_reactor_reliability, nodeResult_of_test, true);
	  probNet.addLink(nodeAdvanced_reactor_reliability, nodeResult_of_advanced_reactor, true);
	  probNet.addLink(nodeTest_decision, nodeCost_of_test, true);
	  probNet.addLink(nodeTest_decision, nodeResult_of_test, true);
	  probNet.addLink(nodeBuild_decision, nodeResult_of_advanced_reactor, true);
	  probNet.addLink(nodeBuild_decision, nodeResult_of_conventional_reactor, true);
	
	  // Potentials
	  TablePotential potResult_of_advanced_reactor = new TablePotential(Arrays.asList(varResult_of_advanced_reactor, varBuild_decision, varAdvanced_reactor_reliability), PotentialRole.CONDITIONAL_PROBABILITY);
	  potResult_of_advanced_reactor.values = new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0};
	  nodeResult_of_advanced_reactor.setPotential(potResult_of_advanced_reactor);
	
	  TablePotential potResult_of_test = new TablePotential(Arrays.asList(varResult_of_test, varTest_decision, varAdvanced_reactor_reliability), PotentialRole.CONDITIONAL_PROBABILITY);
	  potResult_of_test.values = new double[]{0.33333, 0.33333, 0.33333, 0, 0, 0, 0.33333, 0.33333, 0.33333, 0, 0, 0, 0.33333, 0.33333, 0.33333, 0, 0, 0};
	  nodeResult_of_test.setPotential(potResult_of_test);
	
	  TablePotential potResult_of_conventional_reactor = new TablePotential(Arrays.asList(varResult_of_conventional_reactor, varBuild_decision), PotentialRole.CONDITIONAL_PROBABILITY);
	  potResult_of_conventional_reactor.values = new double[]{0, 0, 0.98, 0.02, 0, 0};
	  nodeResult_of_conventional_reactor.setPotential(potResult_of_conventional_reactor);
	
	  TablePotential potAdvanced_reactor_reliability = new TablePotential(Arrays.asList(varAdvanced_reactor_reliability), PotentialRole.CONDITIONAL_PROBABILITY);
	  potAdvanced_reactor_reliability.values = new double[]{0.33333, 0.33333, 0.33333};
	  nodeAdvanced_reactor_reliability.setPotential(potAdvanced_reactor_reliability);
	
	  TablePotential potCost_of_test = new TablePotential(varCost_of_test,Arrays.asList(varTest_decision));
	  potCost_of_test.values = new double[]{1, 0};
	  nodeCost_of_test.setPotential(potCost_of_test);
	
	  TablePotential potBenefit_of_advanced_reactor = new TablePotential(varBenefit_of_advanced_reactor,Arrays.asList(varResult_of_advanced_reactor));
	  potBenefit_of_advanced_reactor.values = new double[]{12, -6, -10};
	  nodeBenefit_of_advanced_reactor.setPotential(potBenefit_of_advanced_reactor);
	
	  TablePotential potBenefit_of_conventional_reactor = new TablePotential(varBenefit_of_conventional_reactor,Arrays.asList(varResult_of_conventional_reactor));
	  potBenefit_of_conventional_reactor.values = new double[]{8, -4};
	  nodeBenefit_of_conventional_reactor.setPotential(potBenefit_of_conventional_reactor);
	
	
	  // Link restrictions and revealing states
	  Link link_nodeResult_of_test_nodeBuild_decision = probNet.getGraph().getLink(nodeResult_of_test.getNode(),nodeBuild_decision.getNode(), true);
	  link_nodeResult_of_test_nodeBuild_decision.initializesRestrictionsPotential();
	  TablePotential restrictions_nodeResult_of_test_nodeBuild_decision = (TablePotential)link_nodeResult_of_test_nodeBuild_decision.getRestrictionsPotential();
	  restrictions_nodeResult_of_test_nodeBuild_decision.values = new double[] {0, 1, 1, 1, 1, 1, 1, 1, 1};
	
	  Link link_nodeTest_decision_nodeResult_of_test = probNet.getGraph().getLink(nodeTest_decision.getNode(),nodeResult_of_test.getNode(), true);
	  link_nodeTest_decision_nodeResult_of_test.initializesRestrictionsPotential();
	  TablePotential restrictions_nodeTest_decision_nodeResult_of_test = (TablePotential)link_nodeTest_decision_nodeResult_of_test.getRestrictionsPotential();
	  restrictions_nodeTest_decision_nodeResult_of_test.values = new double[] {1, 0, 1, 0, 1, 0};
	  link_nodeTest_decision_nodeResult_of_test.setRevealingStates(Arrays.asList(varTest_decision.getStates()[0]));
	
	  Link link_nodeBuild_decision_nodeResult_of_advanced_reactor = probNet.getGraph().getLink(nodeBuild_decision.getNode(),nodeResult_of_advanced_reactor.getNode(), true);
	  link_nodeBuild_decision_nodeResult_of_advanced_reactor.initializesRestrictionsPotential();
	  TablePotential restrictions_nodeBuild_decision_nodeResult_of_advanced_reactor = (TablePotential)link_nodeBuild_decision_nodeResult_of_advanced_reactor.getRestrictionsPotential();
	  restrictions_nodeBuild_decision_nodeResult_of_advanced_reactor.values = new double[] {1, 0, 0, 1, 0, 0, 1, 0, 0};
	  link_nodeBuild_decision_nodeResult_of_advanced_reactor.setRevealingStates(Arrays.asList(varBuild_decision.getStates()[0]));
	
	  Link link_nodeBuild_decision_nodeResult_of_conventional_reactor = probNet.getGraph().getLink(nodeBuild_decision.getNode(),nodeResult_of_conventional_reactor.getNode(), true);
	  link_nodeBuild_decision_nodeResult_of_conventional_reactor.initializesRestrictionsPotential();
	  TablePotential restrictions_nodeBuild_decision_nodeResult_of_conventional_reactor = (TablePotential)link_nodeBuild_decision_nodeResult_of_conventional_reactor.getRestrictionsPotential();
	  restrictions_nodeBuild_decision_nodeResult_of_conventional_reactor.values = new double[] {0, 1, 0, 0, 1, 0};
	  link_nodeBuild_decision_nodeResult_of_conventional_reactor.setRevealingStates(Arrays.asList(varBuild_decision.getStates()[1]));
	  
	  return probNet;
	}	
	
	public static ProbNet buildDiabetesDAN () {
		  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		  // Variables
		  Variable varUrine_test_result = new Variable("Urine test result", "negative", "positive");
		  Variable varSymptom = new Variable("Symptom", "absent", "present");
		  Variable varDiabetes = new Variable("Diabetes", "absent", "present");
		  Variable varBlood_test_result = new Variable("Blood test result", "negative", "positive");
		  Variable varDec_Blood_Test = new Variable("Dec: Blood Test", "no", "yes");
		  Variable varDec_Urine_test = new Variable("Dec: Urine test", "no", "yes");
		  Variable varTherapy = new Variable("Therapy", "no", "yes");
		  Variable varCost_of_blood_test = new Variable("Cost of blood test");
		  Variable varCost_of_urine_test = new Variable("Cost of urine test");
		  Variable varQuality_of_life = new Variable("Quality of life");

		  // Nodes
		  ProbNode nodeUrine_test_result= probNet.addProbNode(varUrine_test_result, NodeType.CHANCE);
		  ProbNode nodeSymptom= probNet.addProbNode(varSymptom, NodeType.CHANCE);
		  ProbNode nodeDiabetes= probNet.addProbNode(varDiabetes, NodeType.CHANCE);
		  ProbNode nodeBlood_test_result= probNet.addProbNode(varBlood_test_result, NodeType.CHANCE);
		  ProbNode nodeDec_Blood_Test= probNet.addProbNode(varDec_Blood_Test, NodeType.DECISION);
		  ProbNode nodeDec_Urine_test= probNet.addProbNode(varDec_Urine_test, NodeType.DECISION);
		  ProbNode nodeTherapy= probNet.addProbNode(varTherapy, NodeType.DECISION);
		  ProbNode nodeCost_of_blood_test= probNet.addProbNode(varCost_of_blood_test, NodeType.UTILITY);
		  ProbNode nodeCost_of_urine_test= probNet.addProbNode(varCost_of_urine_test, NodeType.UTILITY);
		  ProbNode nodeQuality_of_life= probNet.addProbNode(varQuality_of_life, NodeType.UTILITY);

		  // Links
		  probNet.getGraph().makeLinksExplicit(false);
		  probNet.addLink(nodeDiabetes, nodeSymptom, true);
		  probNet.addLink(nodeDiabetes, nodeUrine_test_result, true);
		  probNet.addLink(nodeDiabetes, nodeBlood_test_result, true);
		  probNet.addLink(nodeDiabetes, nodeQuality_of_life, true);
		  probNet.addLink(nodeDec_Blood_Test, nodeBlood_test_result, true);
		  probNet.addLink(nodeDec_Blood_Test, nodeTherapy, true);
		  probNet.addLink(nodeDec_Blood_Test, nodeCost_of_blood_test, true);
		  probNet.addLink(nodeDec_Urine_test, nodeUrine_test_result, true);
		  probNet.addLink(nodeDec_Urine_test, nodeTherapy, true);
		  probNet.addLink(nodeDec_Urine_test, nodeCost_of_urine_test, true);
		  probNet.addLink(nodeTherapy, nodeQuality_of_life, true);

		  // Potentials
		  TablePotential potUrine_test_result = new TablePotential(Arrays.asList(varUrine_test_result, varDec_Urine_test, varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
		  potUrine_test_result.values = new double[]{0, 0, 0.99, 0.01, 0, 0, 0.03, 0.97};
		  nodeUrine_test_result.setPotential(potUrine_test_result);

		  TablePotential potSymptom = new TablePotential(Arrays.asList(varSymptom, varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
		  potSymptom.values = new double[]{0.999, 0.001, 0.15, 0.85};
		  nodeSymptom.setPotential(potSymptom);

		  TablePotential potDiabetes = new TablePotential(Arrays.asList(varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
		  potDiabetes.values = new double[]{0.93, 0.07};
		  nodeDiabetes.setPotential(potDiabetes);

		  TablePotential potBlood_test_result = new TablePotential(Arrays.asList(varBlood_test_result, varDec_Blood_Test, varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
		  potBlood_test_result.values = new double[]{0, 0, 0.98, 0.02, 0, 0, 0.04, 0.96};
		  nodeBlood_test_result.setPotential(potBlood_test_result);

		  TablePotential potCost_of_blood_test = new TablePotential(varCost_of_blood_test,Arrays.asList(varDec_Blood_Test));
		  potCost_of_blood_test.values = new double[]{0, -0.05};
		  nodeCost_of_blood_test.setPotential(potCost_of_blood_test);

		  TablePotential potCost_of_urine_test = new TablePotential(varCost_of_urine_test,Arrays.asList(varDec_Urine_test));
		  potCost_of_urine_test.values = new double[]{0, -0.03};
		  nodeCost_of_urine_test.setPotential(potCost_of_urine_test);

		  TablePotential potQuality_of_life = new TablePotential(varQuality_of_life,Arrays.asList(varDiabetes, varTherapy));
		  potQuality_of_life.values = new double[]{10, 3, 9, 8};
		  nodeQuality_of_life.setPotential(potQuality_of_life);


		  // Link restrictions and revealing states
		  Link link_nodeDec_Blood_Test_nodeBlood_test_result = probNet.getGraph().getLink(nodeDec_Blood_Test.getNode(),nodeBlood_test_result.getNode(), true);
		  link_nodeDec_Blood_Test_nodeBlood_test_result.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDec_Blood_Test_nodeBlood_test_result = (TablePotential)link_nodeDec_Blood_Test_nodeBlood_test_result.getRestrictionsPotential();
		  restrictions_nodeDec_Blood_Test_nodeBlood_test_result.values = new double[] {0, 1, 0, 1};
		  link_nodeDec_Blood_Test_nodeBlood_test_result.setRevealingStates(Arrays.asList(varDec_Blood_Test.getStates()[1]));

		  Link link_nodeDec_Urine_test_nodeUrine_test_result = probNet.getGraph().getLink(nodeDec_Urine_test.getNode(),nodeUrine_test_result.getNode(), true);
		  link_nodeDec_Urine_test_nodeUrine_test_result.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDec_Urine_test_nodeUrine_test_result = (TablePotential)link_nodeDec_Urine_test_nodeUrine_test_result.getRestrictionsPotential();
		  restrictions_nodeDec_Urine_test_nodeUrine_test_result.values = new double[] {0, 1, 0, 1};
		  link_nodeDec_Urine_test_nodeUrine_test_result.setRevealingStates(Arrays.asList(varDec_Urine_test.getStates()[1]));

		  nodeSymptom.setAlwaysObserved(true);

		 return probNet;
	}	
	
	public static ProbNet buildTwoTestDAN () {
		  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		  // Variables
		  Variable varDisease = new Variable("Disease", "absent", "present");
		  Variable varR_T1 = new Variable("R T1", "negative", "positive");
		  Variable varR_T2 = new Variable("R T2", "negative", "positive");
		  Variable varT1 = new Variable("T1", "no", "yes");
		  Variable varT2 = new Variable("T2", "no", "yes");
		  Variable varTh = new Variable("Th", "no", "yes");
		  Variable varU = new Variable("U");
		  Variable varU1 = new Variable("U1");
		  Variable varU2 = new Variable("U2");

		  // Nodes
		  ProbNode nodeDisease= probNet.addProbNode(varDisease, NodeType.CHANCE);
		  ProbNode nodeR_T1= probNet.addProbNode(varR_T1, NodeType.CHANCE);
		  ProbNode nodeR_T2= probNet.addProbNode(varR_T2, NodeType.CHANCE);
		  ProbNode nodeT1= probNet.addProbNode(varT1, NodeType.DECISION);
		  ProbNode nodeT2= probNet.addProbNode(varT2, NodeType.DECISION);
		  ProbNode nodeTh= probNet.addProbNode(varTh, NodeType.DECISION);
		  ProbNode nodeU= probNet.addProbNode(varU, NodeType.UTILITY);
		  ProbNode nodeU1= probNet.addProbNode(varU1, NodeType.UTILITY);
		  ProbNode nodeU2= probNet.addProbNode(varU2, NodeType.UTILITY);

		  // Links
		  probNet.getGraph().makeLinksExplicit(false);
		  probNet.addLink(nodeDisease, nodeR_T1, true);
		  probNet.addLink(nodeDisease, nodeR_T2, true);
		  probNet.addLink(nodeDisease, nodeU, true);
		  probNet.addLink(nodeT1, nodeR_T1, true);
		  probNet.addLink(nodeT1, nodeU1, true);
		  probNet.addLink(nodeT1, nodeTh, true);
		  probNet.addLink(nodeT2, nodeR_T2, true);
		  probNet.addLink(nodeT2, nodeU2, true);
		  probNet.addLink(nodeT2, nodeTh, true);
		  probNet.addLink(nodeTh, nodeU, true);

		  // Potentials
		  TablePotential potDisease = new TablePotential(Arrays.asList(varDisease), PotentialRole.CONDITIONAL_PROBABILITY);
		  potDisease.values = new double[]{0.85, 0.15};
		  nodeDisease.setPotential(potDisease);

		  TablePotential potR_T1 = new TablePotential(Arrays.asList(varR_T1, varDisease, varT1), PotentialRole.CONDITIONAL_PROBABILITY);
		  potR_T1.values = new double[]{0, 0, 0, 0, 0.85, 0.15, 0.1, 0.9};
		  nodeR_T1.setPotential(potR_T1);

		  TablePotential potR_T2 = new TablePotential(Arrays.asList(varR_T2, varDisease, varT2), PotentialRole.CONDITIONAL_PROBABILITY);
		  potR_T2.values = new double[]{0, 0, 0, 0, 0.99, 0.01, 0.005, 0.995};
		  nodeR_T2.setPotential(potR_T2);

		  TablePotential potU = new TablePotential(varU,Arrays.asList(varTh, varDisease));
		  potU.values = new double[]{10, 8, 2, 7};
		  nodeU.setPotential(potU);

		  TablePotential potU1 = new TablePotential(varU1,Arrays.asList(varT1));
		  potU1.values = new double[]{0, -0.05};
		  nodeU1.setPotential(potU1);

		  TablePotential potU2 = new TablePotential(varU2,Arrays.asList(varT2));
		  potU2.values = new double[]{0, -0.33};
		  nodeU2.setPotential(potU2);


		  // Link restrictions and revealing states
		  Link link_nodeT1_nodeR_T1 = probNet.getGraph().getLink(nodeT1.getNode(),nodeR_T1.getNode(), true);
		  link_nodeT1_nodeR_T1.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeT1_nodeR_T1 = (TablePotential)link_nodeT1_nodeR_T1.getRestrictionsPotential();
		  restrictions_nodeT1_nodeR_T1.values = new double[] {0, 1, 0, 1};
		  link_nodeT1_nodeR_T1.setRevealingStates(Arrays.asList(varT1.getStates()[1]));

		  Link link_nodeT2_nodeR_T2 = probNet.getGraph().getLink(nodeT2.getNode(),nodeR_T2.getNode(), true);
		  link_nodeT2_nodeR_T2.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeT2_nodeR_T2 = (TablePotential)link_nodeT2_nodeR_T2.getRestrictionsPotential();
		  restrictions_nodeT2_nodeR_T2.values = new double[] {0, 1, 0, 1};
		  link_nodeT2_nodeR_T2.setRevealingStates(Arrays.asList(varT2.getStates()[1]));


		  // Always observed nodes

		 return probNet;
		}	
	
	/**
	 * @return A DAN with 5 tests, similarly to the diabetes problem, but where the partial order of tests Ti is: {T0,T1}<{T2}<{T3,T4}.
	 * Thus, the SDAG has 3 phases of tests.
	 */
	public static ProbNet buildThreePhasesOfTestsDAN () {
		 int numTests = 5;
		 
		  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		  // Variables
		
		  Variable varSymptom = new Variable("Symptom", "absent", "present");
		  Variable varDiabetes = new Variable("Diabetes", "absent", "present");
		  Variable varTherapy = new Variable("Therapy", "no", "yes");
		  Variable varQuality_of_life = new Variable("Quality of life");
		  Variable varDec_Test[]=new Variable[numTests];
		  Variable varTest_Result[]=new Variable[numTests];
		  Variable varCost_of_Test[]=new Variable[numTests];
		  for (int i=0;i<numTests;i++){
			  varDec_Test[i] = new Variable("Dec: Test "+i, "no", "yes");
			  varTest_Result[i] = new Variable("Test Result "+i, "negative", "positive");
			  varCost_of_Test[i] = new Variable("Cost of test "+i);
		  }

		  // Nodes
		  ProbNode nodeSymptom= probNet.addProbNode(varSymptom, NodeType.CHANCE);
		  ProbNode nodeDiabetes= probNet.addProbNode(varDiabetes, NodeType.CHANCE);
		  ProbNode nodeTherapy= probNet.addProbNode(varTherapy, NodeType.DECISION);
		  ProbNode nodeQuality_of_life= probNet.addProbNode(varQuality_of_life, NodeType.UTILITY);
		  ProbNode nodeDecTest[]=new ProbNode[numTests];
		  ProbNode nodeTestResult[]=new ProbNode[numTests];
		  ProbNode nodeCostOfTest[]=new ProbNode[numTests];
		  for (int i=0;i<numTests;i++){
			  nodeTestResult[i]=probNet.addProbNode(varTest_Result[i], NodeType.CHANCE);
			  nodeDecTest[i]=probNet.addProbNode(varDec_Test[i], NodeType.DECISION);
			  nodeCostOfTest[i]=probNet.addProbNode(varTest_Result[i], NodeType.UTILITY);
		  }
	
		  // Links
		  probNet.getGraph().makeLinksExplicit(false);
		  probNet.addLink(nodeDiabetes, nodeSymptom, true);
		  for (int i=0;i<numTests;i++){
			  probNet.addLink(nodeDiabetes, nodeTestResult[i], true);
			  probNet.addLink(nodeDecTest[i], nodeTestResult[i], true);
			  probNet.addLink(nodeDecTest[i], nodeCostOfTest[i], true);
		  }
		  probNet.addLink(nodeDiabetes, nodeQuality_of_life, true);
		  probNet.addLink(nodeTherapy, nodeQuality_of_life, true);
		  //Create the three phases of tests
		  for (int i=0;i<2;i++){
			  for (int j=2;j<5;j++){
				  probNet.addLink(nodeDecTest[i], nodeDecTest[j], true);
			  }
		  }
		  for (int j=3;j<5;j++){
			  probNet.addLink(nodeDecTest[2], nodeDecTest[j], true);
		  }

		  //TODO Assign different numbers to potentials.
		  // Potentials for test results
		  TablePotential potentialTestResult[] = new TablePotential[numTests];
		  for (int i=0;i<numTests;i++){
			  potentialTestResult[i] = new TablePotential(Arrays.asList(varTest_Result[i],varDec_Test[i], varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
			  potentialTestResult[i].values = new double[]{0, 0, 0.99, 0.01, 0, 0, 0.03, 0.97};
			  nodeTestResult[i].setPotential(potentialTestResult[i]);
		  }
		  
		  //TODO Assign different numbers to potentials.
		  //Potentials for costs of tests
		  TablePotential potentialCostOfTest[] = new TablePotential[numTests];
		  for (int i=0;i<numTests;i++){
			  potentialCostOfTest[i] = new TablePotential(varCost_of_Test[i],Arrays.asList(varDec_Test));
			  potentialCostOfTest[i].values =new double[]{0, 50};
			  nodeCostOfTest[i].setPotential(potentialCostOfTest[i]);
		  }
	
		  TablePotential potQuality_of_life = new TablePotential(varQuality_of_life,Arrays.asList(varDiabetes, varTherapy));
		  potQuality_of_life.values = new double[]{10, 3, 9, 8};
		  nodeQuality_of_life.setPotential(potQuality_of_life);


		  // Link restrictions and revealing states
		  for (int i=0;i<numTests;i++){
			  Link link_Dec_To_Test_Result = probNet.getGraph().getLink(nodeDecTest[i].getNode(),nodeTestResult[i].getNode(), true);
			  link_Dec_To_Test_Result.initializesRestrictionsPotential();
			  TablePotential restrictions_nodeDec_Test_node_test_result = (TablePotential)link_Dec_To_Test_Result.getRestrictionsPotential();
			  restrictions_nodeDec_Test_node_test_result.values = new double[] {0, 1, 0, 1};
			  link_Dec_To_Test_Result.setRevealingStates(Arrays.asList(varDec_Test[i].getStates()[1]));
		  }

		  nodeSymptom.setAlwaysObserved(true);

		 return probNet;
	}	
	
	
	/**
	 * @return A DAN with 2*numTestsPerPhase tests, similarly to the diabetes problem, but where the partial order of tests Ti is: {T0,T1,...,Tn-1}<{Tn,Tn+1,T2n-1}.
	 */
	public static ProbNet buildTwoPhasesOfTestsDAN (int numTestsPerPhase) {
		
		int numTests = numTestsPerPhase * 2;
		 
		  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		  // Variables
		
		  Variable varSymptom = new Variable("Symptom", "absent", "present");
		  Variable varDiabetes = new Variable("Diabetes", "absent", "present");
		  Variable varTherapy = new Variable("Therapy", "no", "yes");
		  Variable varQuality_of_life = new Variable("Quality of life");
		  Variable varDec_Test[]=new Variable[numTests];
		  Variable varTest_Result[]=new Variable[numTests];
		  Variable varCost_of_Test[]=new Variable[numTests];
		  for (int i=0;i<numTests;i++){
			  varDec_Test[i] = new Variable("Dec: Test "+i, "no", "yes");
			  varTest_Result[i] = new Variable("Test Result "+i, "negative", "positive");
			  varCost_of_Test[i] = new Variable("Cost of test "+i);
		  }

		  // Nodes
		  ProbNode nodeSymptom= probNet.addProbNode(varSymptom, NodeType.CHANCE);
		  ProbNode nodeDiabetes= probNet.addProbNode(varDiabetes, NodeType.CHANCE);
		  ProbNode nodeTherapy= probNet.addProbNode(varTherapy, NodeType.DECISION);
		  ProbNode nodeQuality_of_life= probNet.addProbNode(varQuality_of_life, NodeType.UTILITY);
		  ProbNode nodeDecTest[]=new ProbNode[numTests];
		  ProbNode nodeTestResult[]=new ProbNode[numTests];
		  ProbNode nodeCostOfTest[]=new ProbNode[numTests];
		  for (int i=0;i<numTests;i++){
			  nodeTestResult[i]=probNet.addProbNode(varTest_Result[i], NodeType.CHANCE);
			  nodeDecTest[i]=probNet.addProbNode(varDec_Test[i], NodeType.DECISION);
			  nodeCostOfTest[i]=probNet.addProbNode(varTest_Result[i], NodeType.UTILITY);
		  }
	
		  // Links
		  probNet.getGraph().makeLinksExplicit(false);
		  probNet.addLink(nodeDiabetes, nodeSymptom, true);
		  for (int i=0;i<numTests;i++){
			  probNet.addLink(nodeDiabetes, nodeTestResult[i], true);
			  probNet.addLink(nodeDecTest[i], nodeTestResult[i], true);
			  probNet.addLink(nodeDecTest[i], nodeCostOfTest[i], true);
		  }
		  probNet.addLink(nodeDiabetes, nodeQuality_of_life, true);
		  probNet.addLink(nodeTherapy, nodeQuality_of_life, true);
		  //Create the three phases of tests
		  for (int i=0;i<numTests/2;i++){
			  for (int j=numTests/2;j<numTests;j++){
				  probNet.addLink(nodeDecTest[i], nodeDecTest[j], true);
			  }
		  }
		
		  //TODO Assign different numbers to potentials.
		  // Potentials for test results
		  TablePotential potentialTestResult[] = new TablePotential[numTests];
		  for (int i=0;i<numTests;i++){
			  potentialTestResult[i] = new TablePotential(Arrays.asList(varTest_Result[i],varDec_Test[i], varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
			  potentialTestResult[i].values = new double[]{0, 0, 0.99, 0.01, 0, 0, 0.03, 0.97};
			  nodeTestResult[i].setPotential(potentialTestResult[i]);
		  }
		  
		  //TODO Assign different numbers to potentials.
		  //Potentials for costs of tests
		  TablePotential potentialCostOfTest[] = new TablePotential[numTests];
		  for (int i=0;i<numTests;i++){
			  potentialCostOfTest[i] = new TablePotential(varCost_of_Test[i],Arrays.asList(varDec_Test));
			  potentialCostOfTest[i].values =new double[]{0, 50};
			  nodeCostOfTest[i].setPotential(potentialCostOfTest[i]);
		  }
	
		  TablePotential potQuality_of_life = new TablePotential(varQuality_of_life,Arrays.asList(varDiabetes, varTherapy));
		  potQuality_of_life.values = new double[]{10, 3, 9, 8};
		  nodeQuality_of_life.setPotential(potQuality_of_life);


		  // Link restrictions and revealing states
		  for (int i=0;i<numTests;i++){
			  Link link_Dec_To_Test_Result = probNet.getGraph().getLink(nodeDecTest[i].getNode(),nodeTestResult[i].getNode(), true);
			  link_Dec_To_Test_Result.initializesRestrictionsPotential();
			  TablePotential restrictions_nodeDec_Test_node_test_result = (TablePotential)link_Dec_To_Test_Result.getRestrictionsPotential();
			  restrictions_nodeDec_Test_node_test_result.values = new double[] {0, 1, 0, 1};
			  link_Dec_To_Test_Result.setRevealingStates(Arrays.asList(varDec_Test[i].getStates()[1]));
		  }

		  nodeSymptom.setAlwaysObserved(true);

		 return probNet;
	}	
	
	/**
	 * @return A DAN with n tests. The diabetes problem is an instance where n=2.
	 */
	public static ProbNet buildNTestsDAN (int numTests) {
				 
		  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		  // Variables
		
		  Variable varSymptom = new Variable("Symptom", "absent", "present");
		  Variable varDiabetes = new Variable("Disease", "absent", "present");
		  Variable varTherapy = new Variable("Therapy", "no", "yes");
		  Variable varQuality_of_life = new Variable("Quality of life");
		  Variable varDec_Test[]=new Variable[numTests];
		  Variable varTest_Result[]=new Variable[numTests];
		  Variable varCost_of_Test[]=new Variable[numTests];
		  for (int i=0;i<numTests;i++){
			  varDec_Test[i] = new Variable("Dec: Test "+i, "no", "yes");
			  varTest_Result[i] = new Variable("Test Result "+i, "negative", "positive");
			  varCost_of_Test[i] = new Variable("Cost of test "+i);
		  }

		  // Nodes
		  ProbNode nodeSymptom= probNet.addProbNode(varSymptom, NodeType.CHANCE);
		  ProbNode nodeDiabetes= probNet.addProbNode(varDiabetes, NodeType.CHANCE);
		  ProbNode nodeTherapy= probNet.addProbNode(varTherapy, NodeType.DECISION);
		  ProbNode nodeQuality_of_life= probNet.addProbNode(varQuality_of_life, NodeType.UTILITY);
		  ProbNode nodeDecTest[]=new ProbNode[numTests];
		  ProbNode nodeTestResult[]=new ProbNode[numTests];
		  ProbNode nodeCostOfTest[]=new ProbNode[numTests];
		  for (int i=0;i<numTests;i++){
			  nodeTestResult[i]=probNet.addProbNode(varTest_Result[i], NodeType.CHANCE);
			  nodeDecTest[i]=probNet.addProbNode(varDec_Test[i], NodeType.DECISION);
			  nodeCostOfTest[i]=probNet.addProbNode(varTest_Result[i], NodeType.UTILITY);
		  }
	
		  // Links
		  probNet.getGraph().makeLinksExplicit(false);
		  probNet.addLink(nodeDiabetes, nodeSymptom, true);
		  for (int i=0;i<numTests;i++){
			  probNet.addLink(nodeDiabetes, nodeTestResult[i], true);
			  probNet.addLink(nodeDecTest[i], nodeTestResult[i], true);
			  probNet.addLink(nodeDecTest[i], nodeCostOfTest[i], true);
		  }
		  probNet.addLink(nodeDiabetes, nodeQuality_of_life, true);
		  probNet.addLink(nodeTherapy, nodeQuality_of_life, true);
		  
		  //TODO Assign different numbers to potentials.
		  // Potentials for test results
		  TablePotential potentialTestResult[] = new TablePotential[numTests];
		  for (int i=0;i<numTests;i++){
			  potentialTestResult[i] = new TablePotential(Arrays.asList(varTest_Result[i],varDec_Test[i], varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
			  potentialTestResult[i].values = new double[]{0, 0, 0.99, 0.01, 0, 0, 0.03, 0.97};
			  nodeTestResult[i].setPotential(potentialTestResult[i]);
		  }
		  
		  //TODO Assign different numbers to potentials.
		  //Potentials for costs of tests
		  TablePotential potentialCostOfTest[] = new TablePotential[numTests];
		  for (int i=0;i<numTests;i++){
			  potentialCostOfTest[i] = new TablePotential(varCost_of_Test[i],Arrays.asList(varDec_Test));
			  potentialCostOfTest[i].values =new double[]{0, 50};
			  nodeCostOfTest[i].setPotential(potentialCostOfTest[i]);
		  }
	
		  TablePotential potQuality_of_life = new TablePotential(varQuality_of_life,Arrays.asList(varDiabetes, varTherapy));
		  potQuality_of_life.values = new double[]{10, 3, 9, 8};
		  nodeQuality_of_life.setPotential(potQuality_of_life);


		  // Link restrictions and revealing states
		  for (int i=0;i<numTests;i++){
			  Link link_Dec_To_Test_Result = probNet.getGraph().getLink(nodeDecTest[i].getNode(),nodeTestResult[i].getNode(), true);
			  link_Dec_To_Test_Result.initializesRestrictionsPotential();
			  TablePotential restrictions_nodeDec_Test_node_test_result = (TablePotential)link_Dec_To_Test_Result.getRestrictionsPotential();
			  restrictions_nodeDec_Test_node_test_result.values = new double[] {0, 1, 0, 1};
			  link_Dec_To_Test_Result.setRevealingStates(Arrays.asList(varDec_Test[i].getStates()[1]));
		  }

		  nodeSymptom.setAlwaysObserved(true);

		 return probNet;
	}	

	
}
