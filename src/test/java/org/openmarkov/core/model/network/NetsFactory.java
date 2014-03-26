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
import org.openmarkov.core.model.network.potential.ProductPotential;
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
	
	public enum NamesNetworks {
		ONE_CHANCE_DAN,
		BLIND_TREATMENT_DAN,
		PERFECT_INFORMATION_TREATMENT_DAN,
		DECIDE_TEST_DAN,
		TWO_TEST_DAN,
		DIABETES_DAN,
		DATING_DAN,
		REACTOR_DAN,
		WOOER_DAN, 
		PERFECT_INFORMATION_TREATMENT_RESTRICTED_DAN,
		MEDIASTINET_DAN, 
		BRANCH_ACCEPT_DATING_SIMPLIFIED_DAN, 
		DATING_TV_BAD_DAN, 
		DATING_ACCEPT_NO_DAN,
		USED_CAR_BUYER_DAN,
		N_TESTS;
		
		
	}
	
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
		return createBN_XY("X","Y",prevalence,sensitivity,specificity);
}
	
	
	
	/**
	 * @return a Bayesian network with two nodes (X and Y) and a link X -> Y
	 * @throws Exception
	 */
	public static ProbNet createBN_XY(String nameX, String nameY, double prevalence,double sensitivity,double specificity) throws Exception {
		ProbNet probNet;
		double[] valuesX;
		double [] valuesYX;
				
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
					
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		
		// Define the variables
		Variable variableX = new Variable(nameX,diseaseStates);
		Variable variableY = new Variable(nameY,testResultStates);
			
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
	
	public static ProbNet buildOneChanceDAN() throws NodeNotFoundException {
		ProbNet oneChanceDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		oneChanceDAN.setName(NamesNetworks.ONE_CHANCE_DAN.toString());
		Variable variableX = new Variable("X", "absent", "present");
		Variable variableU = new Variable("U");
		
		ProbNode nodeX = oneChanceDAN.addProbNode(variableX, NodeType.CHANCE);
		ProbNode nodeU = oneChanceDAN.addProbNode(variableU, NodeType.UTILITY);
		
		oneChanceDAN.getGraph().makeLinksExplicit(false);
		oneChanceDAN.addLink(variableX, variableU, true);
		
		TablePotential potentialX = new TablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialX.values = new double [] {0.86, 0.14};
		nodeX.setPotential(potentialX);

		TablePotential potentialU = new TablePotential(variableU, Arrays.asList(variableX));
		potentialU.values = new double [] {100, 30};
		nodeU.setPotential(potentialU);
		
		return oneChanceDAN;
	}
	
	public static ProbNet buildBlindTreatmentDAN() throws NodeNotFoundException {
		return buildDecideTreatmentDAN(NamesNetworks.BLIND_TREATMENT_DAN,false);
	}
	
	public static ProbNet buildPerfectInformationTreatmentDAN() throws NodeNotFoundException {
		return buildDecideTreatmentDAN(NamesNetworks.PERFECT_INFORMATION_TREATMENT_DAN,true);
	}
	
	public static ProbNet buildDecideTreatmentDAN(NamesNetworks name,boolean isXKnown) throws NodeNotFoundException {
		ProbNet blindTreatmentDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		blindTreatmentDAN.setName(name.toString());
		Variable variableX = new Variable("X", "absent", "present");
		Variable variableT = new Variable("T", "no", "yes");
		Variable variableU = new Variable("U");
		
		ProbNode nodeX = blindTreatmentDAN.addProbNode(variableX, NodeType.CHANCE);
		blindTreatmentDAN.addProbNode(variableT, NodeType.DECISION);
		ProbNode nodeU = blindTreatmentDAN.addProbNode(variableU, NodeType.UTILITY);
		
		blindTreatmentDAN.getGraph().makeLinksExplicit(false);
		blindTreatmentDAN.addLink(variableX, variableU, true);
		blindTreatmentDAN.addLink(variableT, variableU, true);
		if (isXKnown){
			blindTreatmentDAN.addLink(variableX, variableT, true);
		}
		
		TablePotential potentialX = new TablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialX.values = new double [] {0.86, 0.14};
		nodeX.setPotential(potentialX);

		TablePotential potentialU = new TablePotential(variableU, Arrays.asList(variableX,variableT));
		potentialU.values = new double [] {100, 30, 90, 80};
		nodeU.setPotential(potentialU);
		
		nodeX.setAlwaysObserved(isXKnown);
		
		return blindTreatmentDAN;
	}
	
	
	public static ProbNet buildDecideTreatmentRestrictedDAN() throws NodeNotFoundException {
		ProbNet dan = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		dan.setName(NamesNetworks.PERFECT_INFORMATION_TREATMENT_RESTRICTED_DAN.toString());
		Variable variableX = new Variable("X", "absent", "present");
		Variable variableT = new Variable("T", "no", "yes");
		Variable variableU = new Variable("U");
		
		ProbNode nodeX = dan.addProbNode(variableX, NodeType.CHANCE);
		ProbNode nodeT = dan.addProbNode(variableT, NodeType.DECISION);
		ProbNode nodeU = dan.addProbNode(variableU, NodeType.UTILITY);
		
		dan.getGraph().makeLinksExplicit(false);
		dan.addLink(variableX, variableU, true);
		dan.addLink(variableT, variableU, true);
		dan.addLink(variableX, variableT, true);
		
		TablePotential potentialX = new TablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialX.values = new double [] {0.86, 0.14};
		nodeX.setPotential(potentialX);

		TablePotential potentialU = new TablePotential(variableU, Arrays.asList(variableX,variableT));
		potentialU.values = new double [] {100, 30, 90, 80};
		nodeU.setPotential(potentialU);
		
		nodeX.setAlwaysObserved(true);
		
		Link link = dan.getGraph().getLink(nodeX.getNode(), nodeT.getNode(), true);
		link.initializesRestrictionsPotential();
		TablePotential restrictionsPotential = (TablePotential)link.getRestrictionsPotential();
		restrictionsPotential.values = new double[]{0,1,1,1};
		
		return dan;
	}
	
	
	

	
	
	public static ProbNet buildDecideTestDAN() throws NodeNotFoundException {
		ProbNet decideTestDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		decideTestDAN.setName(NamesNetworks.DECIDE_TEST_DAN.toString());
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
		
		TablePotential potentialU2 = new TablePotential(variableU2, Arrays.asList(variableD));
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
		datingDAN.setName(NamesNetworks.DATING_DAN.toString());
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
		Variable variableUrExp = new Variable("U rExp");
		
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
		potentialAccept.values = new double [] {1, 0, 0.99, 0.01, 1, 0, 0.25, 0.75};
		nodeAccept.setPotential(potentialAccept);

		UniformPotential potentialLikesMe = new UniformPotential(Arrays.asList(variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeLikesMe.setPotential(potentialLikesMe);
		
		TablePotential potentialToDo = new TablePotential(Arrays.asList(variableToDo, variableAccept, variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialToDo.values = new double [] {0, 0, 0.65, 0.35, 0, 0, 0.15, 0.85};
		nodeToDo.setPotential(potentialToDo);		
		
		TablePotential potentialMMood = new TablePotential(Arrays.asList(variableMMood, variableMovie), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMMood.values = new double [] {0.25, 0.75, 0.88, 0.12};
		nodeMMood.setPotential(potentialMMood);

		TablePotential potentialMExp = new TablePotential(Arrays.asList(variableMExp, variableMovie, variableMMood), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMExp.values = new double [] {0.99, 0.01, 0.95, 0.05, 0.15, 0.85, 0.01, 0.99};
		nodeMExp.setPotential(potentialMExp);

		TablePotential potentialUmExp = new TablePotential(variableUmExp, Arrays.asList(variableMExp));
		potentialUmExp.values = new double [] {-10, 10};
		nodeUmExp.setPotential(potentialUmExp);
		
		TablePotential potentialRMood = new TablePotential(Arrays.asList(variableRMood, variableRest), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRMood.values = new double [] {0.5, 0.5, 0.2, 0.8};
		nodeRMood.setPotential(potentialRMood);

		TablePotential potentialRExp = new TablePotential(Arrays.asList(variableRExp, variableRest, variableRMood), PotentialRole.CONDITIONAL_PROBABILITY);
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
	
	public static ProbNet buildDatingAcceptNoDAN() throws NodeNotFoundException {
		ProbNet datingDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		datingDAN.setName(NamesNetworks.DATING_ACCEPT_NO_DAN.toString());
		Variable variableNClub = new Variable("NClub", "no", "yes");
		Variable variableTV = new Variable("TV","good","bad");
		Variable variableTVExp = new Variable("TVExp","negative","positive");
		Variable variableClub = new Variable("Club","negative","positive");
		Variable variableMeetFr = new Variable("MeetFr","negative","positive");
		Variable variableNCExp = new Variable("NCExp","negative","positive");
		Variable variableUTVExp = new Variable("U TVExp");
		Variable variableUNCExp = new Variable("U NCExp");
		
		ProbNode nodeNClub = datingDAN.addProbNode(variableNClub, NodeType.DECISION);
		ProbNode nodeTV = datingDAN.addProbNode(variableTV, NodeType.CHANCE);
		ProbNode nodeTVExp = datingDAN.addProbNode(variableTVExp, NodeType.CHANCE);
		ProbNode nodeClub = datingDAN.addProbNode(variableClub, NodeType.CHANCE);
		ProbNode nodeMeetFr = datingDAN.addProbNode(variableMeetFr, NodeType.CHANCE);
		ProbNode nodeNCExp = datingDAN.addProbNode(variableNCExp, NodeType.CHANCE);
		ProbNode nodeUTVExp = datingDAN.addProbNode(variableUTVExp, NodeType.UTILITY);
		ProbNode nodeUNCExp = datingDAN.addProbNode(variableUNCExp, NodeType.UTILITY);
		
		datingDAN.getGraph().makeLinksExplicit(false);
		datingDAN.addLink(variableTV, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableClub, true);
		datingDAN.addLink(variableNClub, variableMeetFr, true);
		datingDAN.addLink(variableClub, variableNCExp, true);
		datingDAN.addLink(variableMeetFr, variableNCExp, true);
		datingDAN.addLink(variableTVExp, variableUTVExp, true);
		datingDAN.addLink(variableNCExp, variableUNCExp, true);
		
				
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
	
	public static ProbNet buildDatingTVBadDAN() throws NodeNotFoundException {
		ProbNet datingDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		datingDAN.setName(NamesNetworks.DATING_TV_BAD_DAN.toString());
		Variable variableAsk = new Variable("Ask", "no", "yes");
		Variable variableNClub = new Variable("NClub", "no", "yes");
		Variable variableAccept = new Variable("Accept", "no", "yes");
		Variable variableLikesMe = new Variable("LikesMe","no","yes");
		Variable variableToDo = new Variable("ToDo","restaurant","movie");
		//Variable variableTV = new Variable("TV","good","bad");
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
		Variable variableUrExp = new Variable("U rExp");
		
		ProbNode nodeAsk = datingDAN.addProbNode(variableAsk, NodeType.DECISION);
		ProbNode nodeNClub = datingDAN.addProbNode(variableNClub, NodeType.DECISION);
		ProbNode nodeAccept = datingDAN.addProbNode(variableAccept, NodeType.CHANCE);
		ProbNode nodeLikesMe = datingDAN.addProbNode(variableLikesMe, NodeType.CHANCE);
		ProbNode nodeToDo = datingDAN.addProbNode(variableToDo, NodeType.CHANCE);
		//ProbNode nodeTV = datingDAN.addProbNode(variableTV, NodeType.CHANCE);
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
		//datingDAN.addLink(variableTV, variableTVExp, true);
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
		potentialAccept.values = new double [] {1, 0, 0.99, 0.01, 1, 0, 0.25, 0.75};
		nodeAccept.setPotential(potentialAccept);

		UniformPotential potentialLikesMe = new UniformPotential(Arrays.asList(variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeLikesMe.setPotential(potentialLikesMe);
		
		TablePotential potentialToDo = new TablePotential(Arrays.asList(variableToDo, variableAccept, variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialToDo.values = new double [] {0, 0, 0.65, 0.35, 0, 0, 0.15, 0.85};
		nodeToDo.setPotential(potentialToDo);		
		
		TablePotential potentialMMood = new TablePotential(Arrays.asList(variableMMood, variableMovie), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMMood.values = new double [] {0.25, 0.75, 0.88, 0.12};
		nodeMMood.setPotential(potentialMMood);

		TablePotential potentialMExp = new TablePotential(Arrays.asList(variableMExp, variableMovie, variableMMood), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMExp.values = new double [] {0.99, 0.01, 0.95, 0.05, 0.15, 0.85, 0.01, 0.99};
		nodeMExp.setPotential(potentialMExp);

		TablePotential potentialUmExp = new TablePotential(variableUmExp, Arrays.asList(variableMExp));
		potentialUmExp.values = new double [] {-10, 10};
		nodeUmExp.setPotential(potentialUmExp);
		
		TablePotential potentialRMood = new TablePotential(Arrays.asList(variableRMood, variableRest), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRMood.values = new double [] {0.5, 0.5, 0.2, 0.8};
		nodeRMood.setPotential(potentialRMood);

		TablePotential potentialRExp = new TablePotential(Arrays.asList(variableRExp, variableRest, variableRMood), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRExp.values = new double [] {0.95, 0.05, 1, 0, 0.01, 0.99, 0.08, 0.92};
		nodeRExp.setPotential(potentialRExp);

		TablePotential potentialUrExp = new TablePotential(variableUrExp, Arrays.asList(variableRExp));
		potentialUrExp.values = new double [] {-10, 10};
		nodeUrExp.setPotential(potentialUrExp);		
		
		TablePotential potentialTVExp = new TablePotential(Arrays.asList(variableTVExp, variableNClub), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTVExp.values = new double [] {1.0, 0.0, 0.0, 0.0};
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
		
		return datingDAN;
	}	
	
	public static ProbNet buildDatingBranchAcceptSimplifiedDAN() throws NodeNotFoundException {
		ProbNet datingDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		datingDAN.setName(NamesNetworks.BRANCH_ACCEPT_DATING_SIMPLIFIED_DAN.toString());
		Variable variableToDo = new Variable("ToDo","restaurant","movie");
		Variable variableMovie = new Variable("Movie","romantic","action");
		Variable variableRest = new Variable("Rest","cheap","expensive");
		Variable variableMExp = new Variable("mExp","negative","positive");
		Variable variableRExp = new Variable("rExp","negative","positive");
		Variable variableUmExp = new Variable("U mExp");
		Variable variableUrExp = new Variable("U rExp");
		Variable variableLikesMe = new Variable("LikesMe","no","yes");
		
		ProbNode nodeToDo = datingDAN.addProbNode(variableToDo, NodeType.CHANCE);
		ProbNode nodeMovie = datingDAN.addProbNode(variableMovie, NodeType.DECISION);
		ProbNode nodeRest = datingDAN.addProbNode(variableRest, NodeType.DECISION);
		ProbNode nodeMExp = datingDAN.addProbNode(variableMExp, NodeType.CHANCE);
		ProbNode nodeRExp = datingDAN.addProbNode(variableRExp, NodeType.CHANCE);
		ProbNode nodeUmExp = datingDAN.addProbNode(variableUmExp, NodeType.UTILITY);
		ProbNode nodeUrExp = datingDAN.addProbNode(variableUrExp, NodeType.UTILITY);
		ProbNode nodeLikesMe = datingDAN.addProbNode(variableLikesMe, NodeType.CHANCE);
		
		datingDAN.getGraph().makeLinksExplicit(false);
		datingDAN.addLink(variableToDo, variableMovie, true);
		datingDAN.addLink(variableToDo, variableRest, true);
		datingDAN.addLink(variableMovie, variableMExp, true);
		datingDAN.addLink(variableMExp, variableUmExp, true);
		datingDAN.addLink(variableRest, variableRExp, true);
		datingDAN.addLink(variableRExp, variableUrExp, true);
		datingDAN.addLink(variableLikesMe, variableToDo, true);
		
		
	    UniformPotential potentialLikesMe = new UniformPotential(Arrays.asList(variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeLikesMe.setPotential(potentialLikesMe);
		
		TablePotential potentialToDo = new TablePotential(Arrays.asList(variableToDo, variableLikesMe), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialToDo.values = new double [] {0.65, 0.35, 0.15, 0.85};
		nodeToDo.setPotential(potentialToDo);

		TablePotential potentialMExp = new TablePotential(Arrays.asList(variableMExp, variableMovie), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMExp.values = new double [] {0.01, 0.99, 0.95, 0.05};
		nodeMExp.setPotential(potentialMExp);

		TablePotential potentialUmExp = new TablePotential(variableUmExp, Arrays.asList(variableMExp));
		potentialUmExp.values = new double [] {-10, 10};
		nodeUmExp.setPotential(potentialUmExp);

		TablePotential potentialRExp = new TablePotential(Arrays.asList(variableRExp, variableRest), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRExp.values = new double [] {0.95, 0.05, 0.0, 1.0};
		nodeRExp.setPotential(potentialRExp);

		TablePotential potentialUrExp = new TablePotential(variableUrExp, Arrays.asList(variableRExp));
		potentialUrExp.values = new double [] {-10, 10};
		nodeUrExp.setPotential(potentialUrExp);		
		
			
		
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
		
		nodeToDo.setAlwaysObserved(true);
		
		
		
		return datingDAN;
	}	
	
	
	public static ProbNet buildReactorDAN () {
		
	  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
	  probNet.setName(NamesNetworks.REACTOR_DAN.toString());
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
	  probNet.addLink(nodeTest_decision, nodeBuild_decision, true);
	  probNet.addLink(nodeBuild_decision, nodeResult_of_advanced_reactor, true);
	  probNet.addLink(nodeBuild_decision, nodeResult_of_conventional_reactor, true);
	
	  // Potentials
	  TablePotential potResult_of_advanced_reactor = new TablePotential(Arrays.asList(varResult_of_advanced_reactor, varBuild_decision, varAdvanced_reactor_reliability), PotentialRole.CONDITIONAL_PROBABILITY);
	  potResult_of_advanced_reactor.values = new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0};
	  nodeResult_of_advanced_reactor.setPotential(potResult_of_advanced_reactor);
	
	  TablePotential potResult_of_test = new TablePotential(Arrays.asList(varResult_of_test, varTest_decision, varAdvanced_reactor_reliability), PotentialRole.CONDITIONAL_PROBABILITY);
	  potResult_of_test.values = new double[]{0.05, 0.6, 0.35, 0, 0, 0, 0.85, 0.1, 0.05, 0, 0, 0, 0.9, 0.08, 0.02, 0, 0, 0};
	  nodeResult_of_test.setPotential(potResult_of_test);
	
	  TablePotential potResult_of_conventional_reactor = new TablePotential(Arrays.asList(varResult_of_conventional_reactor, varBuild_decision), PotentialRole.CONDITIONAL_PROBABILITY);
	  potResult_of_conventional_reactor.values = new double[]{0, 0, 0.98, 0.02, 0, 0};
	  nodeResult_of_conventional_reactor.setPotential(potResult_of_conventional_reactor);
	
	  TablePotential potAdvanced_reactor_reliability = new TablePotential(Arrays.asList(varAdvanced_reactor_reliability), PotentialRole.CONDITIONAL_PROBABILITY);
	  potAdvanced_reactor_reliability.values = new double[]{0.88, 0.1, 0.02};
	  nodeAdvanced_reactor_reliability.setPotential(potAdvanced_reactor_reliability);
	
	  TablePotential potCost_of_test = new TablePotential(varCost_of_test,Arrays.asList(varTest_decision));
	  potCost_of_test.values = new double[]{-1, 0};
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
	
	public static ProbNet buildWooerDAN () {
		  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		  probNet.setName(NamesNetworks.WOOER_DAN.toString());
		  // Variables
		  Variable varResult_1 = new Variable("Result 1", "fail", "pass");
		  Variable varResult_2 = new Variable("Result 2", "fail", "pass");
		  Variable varResult_3 = new Variable("Result 3", "fail", "pass");
		  Variable varQuality_of_wooer = new Variable("Quality of wooer", "bad", "good");
		  Variable varWedding = new Variable("Wedding", "no", "yes");
		  Variable varQuality_of_general = new Variable("Quality of general", "bad", "good");
		  Variable varWealth = new Variable("Wealth", "poor", "wealthy");
		  Variable varOffspring = new Variable("Offspring", "no", "yes");
		  Variable varNoble_descent = new Variable("Noble descent", "no", "yes");
		  Variable varTask_1 = new Variable("Task 1", "kill dragon", "kill unicorn");
		  Variable varTask_2 = new Variable("Task 2", "night in tower", "night in tomb");
		  Variable varDec_Task_3 = new Variable("Dec Task 3", "climb", "swim");
		  Variable varMarriage = new Variable("Marriage", "no", "yes");
		  Variable varWar = new Variable("War", "no", "yes");
		  Variable varRetire = new Variable("Retire", "no", "yes");
		  Variable varU1 = new Variable("U1");
		  Variable varU2 = new Variable("U2");
		  Variable varU3 = new Variable("U3");
		  Variable varcost_task_1 = new Variable("cost task 1");
		  Variable varcost_task_2 = new Variable("cost task 2");
		  Variable varcost_task_3 = new Variable("cost task 3");
		  Variable varcost_marriage = new Variable("cost marriage");
		  Variable varcost_war = new Variable("cost war");
		  Variable varcost_retirement = new Variable("cost retirement");

		  // Nodes
		  ProbNode nodeResult_1= probNet.addProbNode(varResult_1, NodeType.CHANCE);
		  ProbNode nodeResult_2= probNet.addProbNode(varResult_2, NodeType.CHANCE);
		  ProbNode nodeResult_3= probNet.addProbNode(varResult_3, NodeType.CHANCE);
		  ProbNode nodeQuality_of_wooer= probNet.addProbNode(varQuality_of_wooer, NodeType.CHANCE);
		  ProbNode nodeWedding= probNet.addProbNode(varWedding, NodeType.CHANCE);
		  ProbNode nodeQuality_of_general= probNet.addProbNode(varQuality_of_general, NodeType.CHANCE);
		  ProbNode nodeWealth= probNet.addProbNode(varWealth, NodeType.CHANCE);
		  ProbNode nodeOffspring= probNet.addProbNode(varOffspring, NodeType.CHANCE);
		  ProbNode nodeNoble_descent= probNet.addProbNode(varNoble_descent, NodeType.CHANCE);
		  ProbNode nodeTask_1= probNet.addProbNode(varTask_1, NodeType.DECISION);
		  ProbNode nodeTask_2= probNet.addProbNode(varTask_2, NodeType.DECISION);
		  ProbNode nodeDec_Task_3= probNet.addProbNode(varDec_Task_3, NodeType.DECISION);
		  ProbNode nodeMarriage= probNet.addProbNode(varMarriage, NodeType.DECISION);
		  ProbNode nodeWar= probNet.addProbNode(varWar, NodeType.DECISION);
		  ProbNode nodeRetire= probNet.addProbNode(varRetire, NodeType.DECISION);
		  ProbNode nodeU1= probNet.addProbNode(varU1, NodeType.UTILITY);
		  ProbNode nodeU2= probNet.addProbNode(varU2, NodeType.UTILITY);
		  ProbNode nodeU3= probNet.addProbNode(varU3, NodeType.UTILITY);
		  ProbNode nodecost_task_1= probNet.addProbNode(varcost_task_1, NodeType.UTILITY);
		  ProbNode nodecost_task_2= probNet.addProbNode(varcost_task_2, NodeType.UTILITY);
		  ProbNode nodecost_task_3= probNet.addProbNode(varcost_task_3, NodeType.UTILITY);
		  ProbNode nodecost_marriage= probNet.addProbNode(varcost_marriage, NodeType.UTILITY);
		  ProbNode nodecost_war= probNet.addProbNode(varcost_war, NodeType.UTILITY);
		  ProbNode nodecost_retirement= probNet.addProbNode(varcost_retirement, NodeType.UTILITY);

		  // Links
		  probNet.getGraph().makeLinksExplicit(false);
		  probNet.addLink(nodeQuality_of_wooer, nodeQuality_of_general, true);
		  probNet.addLink(nodeQuality_of_wooer, nodeOffspring, true);
		  probNet.addLink(nodeQuality_of_wooer, nodeU1, true);
		  probNet.addLink(nodeQuality_of_wooer, nodeResult_1, true);
		  probNet.addLink(nodeQuality_of_wooer, nodeResult_2, true);
		  probNet.addLink(nodeQuality_of_wooer, nodeResult_3, true);
		  probNet.addLink(nodeWedding, nodeQuality_of_general, true);
		  probNet.addLink(nodeWedding, nodeOffspring, true);
		  probNet.addLink(nodeQuality_of_general, nodeWealth, true);
		  probNet.addLink(nodeWealth, nodeU3, true);
		  probNet.addLink(nodeOffspring, nodeU2, true);
		  probNet.addLink(nodeOffspring, nodeWealth, true);
		  probNet.addLink(nodeNoble_descent, nodeU2, true);
		  probNet.addLink(nodeTask_1, nodecost_task_1, true);
		  probNet.addLink(nodeTask_1, nodeMarriage, true);
		  probNet.addLink(nodeTask_1, nodeResult_1, true);
		  probNet.addLink(nodeTask_2, nodecost_task_2, true);
		  probNet.addLink(nodeTask_2, nodeMarriage, true);
		  probNet.addLink(nodeTask_2, nodeResult_2, true);
		  probNet.addLink(nodeDec_Task_3, nodecost_task_3, true);
		  probNet.addLink(nodeDec_Task_3, nodeMarriage, true);
		  probNet.addLink(nodeDec_Task_3, nodeResult_3, true);
		  probNet.addLink(nodeMarriage, nodeWedding, true);
		  probNet.addLink(nodeMarriage, nodecost_marriage, true);
		  probNet.addLink(nodeMarriage, nodeWar, true);
		  probNet.addLink(nodeWar, nodeU3, true);
		  probNet.addLink(nodeWar, nodeWealth, true);
		  probNet.addLink(nodeWar, nodeRetire, true);
		  probNet.addLink(nodeWar, nodecost_war, true);
		  probNet.addLink(nodeRetire, nodeU2, true);
		  probNet.addLink(nodeRetire, nodeU3, true);
		  probNet.addLink(nodeRetire, nodecost_retirement, true);

		  // Potentials
		  TablePotential potResult_1 = new TablePotential(Arrays.asList(varResult_1, varQuality_of_wooer, varTask_1), PotentialRole.CONDITIONAL_PROBABILITY);
		  potResult_1.values = new double[]{0.97, 0.03, 0.75, 0.25, 0.75, 0.25, 0.25, 0.75};
		  nodeResult_1.setPotential(potResult_1);

		  TablePotential potResult_2 = new TablePotential(Arrays.asList(varResult_2, varQuality_of_wooer, varTask_2), PotentialRole.CONDITIONAL_PROBABILITY);
		  potResult_2.values = new double[]{0.9, 0.1, 0.2, 0.8, 0.75, 0.25, 0.25, 0.75};
		  nodeResult_2.setPotential(potResult_2);

		  TablePotential potResult_3 = new TablePotential(Arrays.asList(varResult_3, varQuality_of_wooer, varDec_Task_3), PotentialRole.CONDITIONAL_PROBABILITY);
		  potResult_3.values = new double[]{0.6, 0.4, 0.4, 0.6, 0.55, 0.45, 0.45, 0.55};
		  nodeResult_3.setPotential(potResult_3);

		  UniformPotential potQuality_of_wooer = new UniformPotential(Arrays.asList(varQuality_of_wooer), PotentialRole.CONDITIONAL_PROBABILITY);
		  nodeQuality_of_wooer.setPotential(potQuality_of_wooer);

		  TablePotential potWedding = new TablePotential(Arrays.asList(varWedding, varMarriage), PotentialRole.CONDITIONAL_PROBABILITY);
		  potWedding.values = new double[]{1, 0, 0, 1};
		  nodeWedding.setPotential(potWedding);

		  UniformPotential potQuality_of_general = new UniformPotential(Arrays.asList(varQuality_of_general, varQuality_of_wooer, varWedding), PotentialRole.CONDITIONAL_PROBABILITY);
		  nodeQuality_of_general.setPotential(potQuality_of_general);

		  TablePotential potWealth = new TablePotential(Arrays.asList(varWealth, varQuality_of_general, varWar, varOffspring), PotentialRole.CONDITIONAL_PROBABILITY);
		  potWealth.values = new double[]{0.5, 0.5, 0.5, 0.5, 0.85, 0.15, 0.15, 0.85, 0.6, 0.4, 0.6, 0.4, 0.9, 0.1, 0.2, 0.8};
		  nodeWealth.setPotential(potWealth);

		  TablePotential potOffspring = new TablePotential(Arrays.asList(varOffspring, varQuality_of_wooer, varWedding), PotentialRole.CONDITIONAL_PROBABILITY);
		  potOffspring.values = new double[]{1, 0, 1, 0, 0.6, 0.4, 0.2, 0.8};
		  nodeOffspring.setPotential(potOffspring);

		  UniformPotential potNoble_descent = new UniformPotential(Arrays.asList(varNoble_descent), PotentialRole.CONDITIONAL_PROBABILITY);
		  nodeNoble_descent.setPotential(potNoble_descent);

		  UniformPotential potU1 = new UniformPotential(varU1,Arrays.asList(varQuality_of_wooer));
		  nodeU1.setPotential(potU1);

		  TablePotential potU2 = new TablePotential(varU2,Arrays.asList(varNoble_descent, varOffspring, varRetire));
		  potU2.values = new double[]{0, 0, 2, 3, 1, 3, 4, 7};
		  nodeU2.setPotential(potU2);

		  TablePotential potU3 = new TablePotential(varU3,Arrays.asList(varRetire, varWar, varWealth));
		  potU3.values = new double[]{0, 1, 2, 3, 6, 8, 7, 10};
		  nodeU3.setPotential(potU3);

		  TablePotential potcost_task_1 = new TablePotential(varcost_task_1,Arrays.asList(varTask_1));
		  potcost_task_1.values = new double[]{-1, -0.2};
		  nodecost_task_1.setPotential(potcost_task_1);

		  TablePotential potcost_task_2 = new TablePotential(varcost_task_2,Arrays.asList(varTask_2));
		  potcost_task_2.values = new double[]{-0.5, -0.2};
		  nodecost_task_2.setPotential(potcost_task_2);

		  TablePotential potcost_task_3 = new TablePotential(varcost_task_3,Arrays.asList(varDec_Task_3));
		  potcost_task_3.values = new double[]{-0.1, -0.05};
		  nodecost_task_3.setPotential(potcost_task_3);

		  UniformPotential potcost_marriage = new UniformPotential(varcost_marriage,Arrays.asList(varMarriage));
		  nodecost_marriage.setPotential(potcost_marriage);

		  TablePotential potcost_war = new TablePotential(varcost_war,Arrays.asList(varWar));
		  potcost_war.values = new double[]{0, -5};
		  nodecost_war.setPotential(potcost_war);

		  UniformPotential potcost_retirement = new UniformPotential(varcost_retirement,Arrays.asList(varRetire));
		  nodecost_retirement.setPotential(potcost_retirement);

		  // Link restrictions and revealing states
		  Link link_nodeWedding_nodeOffspring = probNet.getGraph().getLink(nodeWedding.getNode(),nodeOffspring.getNode(), true);
		  link_nodeWedding_nodeOffspring.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeWedding_nodeOffspring = (TablePotential)link_nodeWedding_nodeOffspring.getRestrictionsPotential();
		  restrictions_nodeWedding_nodeOffspring.values = new double[] {1, 1, 0, 1};
		  link_nodeWedding_nodeOffspring.setRevealingStates(Arrays.asList(varWedding.getStates()[1], varWedding.getStates()[0]));

		  Link link_nodeTask_1_nodeResult_1 = probNet.getGraph().getLink(nodeTask_1.getNode(),nodeResult_1.getNode(), true);
		  link_nodeTask_1_nodeResult_1.setRevealingStates(Arrays.asList(varTask_1.getStates()[1], varTask_1.getStates()[0]));

		  Link link_nodeTask_2_nodeResult_2 = probNet.getGraph().getLink(nodeTask_2.getNode(),nodeResult_2.getNode(), true);
		  link_nodeTask_2_nodeResult_2.setRevealingStates(Arrays.asList(varTask_2.getStates()[1], varTask_2.getStates()[0]));

		  Link link_nodeDec_Task_3_nodeResult_3 = probNet.getGraph().getLink(nodeDec_Task_3.getNode(),nodeResult_3.getNode(), true);
		  link_nodeDec_Task_3_nodeResult_3.setRevealingStates(Arrays.asList(varDec_Task_3.getStates()[1], varDec_Task_3.getStates()[0]));

		  Link link_nodeMarriage_nodeWedding = probNet.getGraph().getLink(nodeMarriage.getNode(),nodeWedding.getNode(), true);
		  link_nodeMarriage_nodeWedding.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeMarriage_nodeWedding = (TablePotential)link_nodeMarriage_nodeWedding.getRestrictionsPotential();
		  restrictions_nodeMarriage_nodeWedding.values = new double[] {1, 0, 0, 1};
		  link_nodeMarriage_nodeWedding.setRevealingStates(Arrays.asList(varMarriage.getStates()[1], varMarriage.getStates()[0]));

		  Link link_nodeWar_nodeWealth = probNet.getGraph().getLink(nodeWar.getNode(),nodeWealth.getNode(), true);
		  link_nodeWar_nodeWealth.setRevealingStates(Arrays.asList(varWar.getStates()[1], varWar.getStates()[0]));

		  // Always observed nodes
		  nodeNoble_descent.setAlwaysObserved(true);

		 return probNet;
		}
	
	public static ProbNet buildDiabetesDAN () {
		  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		  probNet.setName(NamesNetworks.DIABETES_DAN.toString());
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
		  probNet.setName(NamesNetworks.TWO_TEST_DAN.toString());
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
			  nodeCostOfTest[i]=probNet.addProbNode(varCost_of_Test[i], NodeType.UTILITY);
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
			  potentialCostOfTest[i] = new TablePotential(varCost_of_Test[i],Arrays.asList(varDec_Test[i]));
			  potentialCostOfTest[i].values =new double[]{0, -0.5};
			  nodeCostOfTest[i].setPotential(potentialCostOfTest[i]);
		  }
	
		  TablePotential potQuality_of_life = new TablePotential(varQuality_of_life,Arrays.asList(varDiabetes, varTherapy));
		  potQuality_of_life.values = new double[]{10, 3, 9, 8};
		  nodeQuality_of_life.setPotential(potQuality_of_life);

		  TablePotential potSymptom = new TablePotential(Arrays.asList(varSymptom, varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
		  potSymptom.values = new double[]{0.999, 0.001, 0.15, 0.85};
		  nodeSymptom.setPotential(potSymptom);

		  TablePotential potDiabetes = new TablePotential(Arrays.asList(varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
		  potDiabetes.values = new double[]{0.93, 0.07};
		  nodeDiabetes.setPotential(potDiabetes);

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
		  probNet.setName(NamesNetworks.N_TESTS.toString());
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
			  nodeCostOfTest[i]=probNet.addProbNode(varCost_of_Test[i], NodeType.UTILITY);
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
			  potentialCostOfTest[i] = new TablePotential(varCost_of_Test[i],Arrays.asList(varDec_Test[i]));
			  potentialCostOfTest[i].values =new double[]{0, -0.5};
			  nodeCostOfTest[i].setPotential(potentialCostOfTest[i]);
		  }
	
		  TablePotential potQuality_of_life = new TablePotential(varQuality_of_life,Arrays.asList(varDiabetes, varTherapy));
		  potQuality_of_life.values = new double[]{10, 3, 9, 8};
		  nodeQuality_of_life.setPotential(potQuality_of_life);

		  TablePotential potSymptom = new TablePotential(Arrays.asList(varSymptom, varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
		  potSymptom.values = new double[]{0.999, 0.001, 0.15, 0.85};
		  nodeSymptom.setPotential(potSymptom);

		  TablePotential potDiabetes = new TablePotential(Arrays.asList(varDiabetes), PotentialRole.CONDITIONAL_PROBABILITY);
		  potDiabetes.values = new double[]{0.93, 0.07};
		  nodeDiabetes.setPotential(potDiabetes);
		  

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
	
	public static ProbNet buildMediastinetDAN () {
		  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());

		  probNet.setName(NamesNetworks.MEDIASTINET_DAN.toString());
		  // Variables
		  Variable varN2_N3 = new Variable("N2 N3", "negative", "positive");
		  Variable varCT_scan = new Variable("CT scan", "negative", "positive");
		  Variable varTBNA = new Variable("TBNA", "negative", "positive");
		  Variable varPET = new Variable("PET", "no result", "negative", "positive");
		  Variable varEBUS = new Variable("EBUS", "negative", "positive");
		  Variable varEUS = new Variable("EUS", "negative", "positive");
		  Variable varMED = new Variable("MED", "negative", "positive");
		  Variable varMED_Sv = new Variable("MED Sv", "no", "yes");
		  Variable varDecTBNA = new Variable("Dec:TBNA", "no", "yes");
		  Variable varDecPET = new Variable("Dec:PET", "no", "yes");
		  Variable varDecMED = new Variable("Dec:MED", "no", "yes");
		  Variable varTreatment = new Variable("Treatment", "palliative", "chemotherapy", "thoracotomy");
		  Variable varDecEBUS = new Variable("Dec:EBUS", "no", "yes");
		  Variable varDecEUS = new Variable("Dec:EUS", "no", "yes");
		  Variable varSurvivors_QALE = new Variable("Survivors QALE");
		  Variable varInmediate_Survival = new Variable("Inmediate Survival");
		  Variable varMED_Survival = new Variable("MED Survival");
		  Variable varNet_QALE = new Variable("Net QALE");
		  Variable varTBNA_Morbidity = new Variable("TBNA Morbidity");
		  Variable varMED_Morbidity = new Variable("MED Morbidity");
		  Variable varEUS_Morbidity = new Variable("EUS Morbidity");
		  Variable varEBUS_Morbidity = new Variable("EBUS Morbidity");
		  Variable varTotal_QALE = new Variable("Total QALE");
		  Variable varCostCT_scan = new Variable("Cost:CT scan");
		  Variable varCostTBNA = new Variable("Cost:TBNA");
		  Variable varCostEBUS = new Variable("Cost:EBUS");
		  Variable varCostEUS = new Variable("Cost:EUS");
		  Variable varCostMED = new Variable("Cost:MED");
		  Variable varCostPET = new Variable("Cost:PET");
		  Variable varCostTreatment = new Variable("Cost:Treatment");
		  Variable varTotal_Economic_Cost = new Variable("Total Economic Cost");
		  Variable varC2E = new Variable("C2E");
		  Variable varWeighted_Economic_Cost = new Variable("Weighted Economic Cost");
		  Variable varNet_Effectiveness = new Variable("Net Effectiveness");

		  // Nodes
		  ProbNode nodeN2_N3= probNet.addProbNode(varN2_N3, NodeType.CHANCE);
		  ProbNode nodeCT_scan= probNet.addProbNode(varCT_scan, NodeType.CHANCE);
		  ProbNode nodeTBNA= probNet.addProbNode(varTBNA, NodeType.CHANCE);
		  ProbNode nodePET= probNet.addProbNode(varPET, NodeType.CHANCE);
		  ProbNode nodeEBUS= probNet.addProbNode(varEBUS, NodeType.CHANCE);
		  ProbNode nodeEUS= probNet.addProbNode(varEUS, NodeType.CHANCE);
		  ProbNode nodeMED= probNet.addProbNode(varMED, NodeType.CHANCE);
		  ProbNode nodeMED_Sv= probNet.addProbNode(varMED_Sv, NodeType.CHANCE);
		  ProbNode nodeDecTBNA= probNet.addProbNode(varDecTBNA, NodeType.DECISION);
		  ProbNode nodeDecPET= probNet.addProbNode(varDecPET, NodeType.DECISION);
		  ProbNode nodeDecMED= probNet.addProbNode(varDecMED, NodeType.DECISION);
		  ProbNode nodeTreatment= probNet.addProbNode(varTreatment, NodeType.DECISION);
		  ProbNode nodeDecEBUS= probNet.addProbNode(varDecEBUS, NodeType.DECISION);
		  ProbNode nodeDecEUS= probNet.addProbNode(varDecEUS, NodeType.DECISION);
		  ProbNode nodeSurvivors_QALE= probNet.addProbNode(varSurvivors_QALE, NodeType.UTILITY);
		  ProbNode nodeInmediate_Survival= probNet.addProbNode(varInmediate_Survival, NodeType.UTILITY);
		  ProbNode nodeMED_Survival= probNet.addProbNode(varMED_Survival, NodeType.UTILITY);
		  ProbNode nodeNet_QALE= probNet.addProbNode(varNet_QALE, NodeType.UTILITY);
		  ProbNode nodeTBNA_Morbidity= probNet.addProbNode(varTBNA_Morbidity, NodeType.UTILITY);
		  ProbNode nodeMED_Morbidity= probNet.addProbNode(varMED_Morbidity, NodeType.UTILITY);
		  ProbNode nodeEUS_Morbidity= probNet.addProbNode(varEUS_Morbidity, NodeType.UTILITY);
		  ProbNode nodeEBUS_Morbidity= probNet.addProbNode(varEBUS_Morbidity, NodeType.UTILITY);
		  ProbNode nodeTotal_QALE= probNet.addProbNode(varTotal_QALE, NodeType.UTILITY);
		  ProbNode nodeCostCT_scan= probNet.addProbNode(varCostCT_scan, NodeType.UTILITY);
		  ProbNode nodeCostTBNA= probNet.addProbNode(varCostTBNA, NodeType.UTILITY);
		  ProbNode nodeCostEBUS= probNet.addProbNode(varCostEBUS, NodeType.UTILITY);
		  ProbNode nodeCostEUS= probNet.addProbNode(varCostEUS, NodeType.UTILITY);
		  ProbNode nodeCostMED= probNet.addProbNode(varCostMED, NodeType.UTILITY);
		  ProbNode nodeCostPET= probNet.addProbNode(varCostPET, NodeType.UTILITY);
		  ProbNode nodeCostTreatment= probNet.addProbNode(varCostTreatment, NodeType.UTILITY);
		  ProbNode nodeTotal_Economic_Cost= probNet.addProbNode(varTotal_Economic_Cost, NodeType.UTILITY);
		  ProbNode nodeC2E= probNet.addProbNode(varC2E, NodeType.UTILITY);
		  ProbNode nodeWeighted_Economic_Cost= probNet.addProbNode(varWeighted_Economic_Cost, NodeType.UTILITY);
		  ProbNode nodeNet_Effectiveness= probNet.addProbNode(varNet_Effectiveness, NodeType.UTILITY);

		  // Links
		  probNet.getGraph().makeLinksExplicit(false);
		  probNet.addLink(nodeN2_N3, nodeCT_scan, true);
		  probNet.addLink(nodeN2_N3, nodeEBUS, true);
		  probNet.addLink(nodeN2_N3, nodeEUS, true);
		  probNet.addLink(nodeN2_N3, nodeMED, true);
		  probNet.addLink(nodeN2_N3, nodePET, true);
		  probNet.addLink(nodeN2_N3, nodeSurvivors_QALE, true);
		  probNet.addLink(nodeN2_N3, nodeTBNA, true);
		  probNet.addLink(nodeCT_scan, nodeEBUS, true);
		  probNet.addLink(nodeCT_scan, nodeMED, true);
		  probNet.addLink(nodeCT_scan, nodePET, true);
		  probNet.addLink(nodeCT_scan, nodeTBNA, true);
		  probNet.addLink(nodeCT_scan, nodeEUS, true);
		  probNet.addLink(nodePET, nodeEBUS, true);
		  probNet.addLink(nodePET, nodeEUS, true);
		  probNet.addLink(nodePET, nodeMED, true);
		  probNet.addLink(nodeMED_Sv, nodeMED_Morbidity, true);
		  probNet.addLink(nodeMED_Sv, nodeMED_Survival, true);
		  probNet.addLink(nodeDecTBNA, nodeCostTBNA, true);
		  probNet.addLink(nodeDecTBNA, nodeTBNA, true);
		  probNet.addLink(nodeDecTBNA, nodeTBNA_Morbidity, true);
		  probNet.addLink(nodeDecPET, nodeCostPET, true);
		  probNet.addLink(nodeDecPET, nodePET, true);
		  probNet.addLink(nodeDecPET, nodeDecEBUS, true);
		  probNet.addLink(nodeDecPET, nodeDecEUS, true);
		  probNet.addLink(nodeDecPET, nodeDecMED, true);
		  probNet.addLink(nodeDecMED, nodeCostMED, true);
		  probNet.addLink(nodeDecMED, nodeMED, true);
		  probNet.addLink(nodeDecMED, nodeMED_Sv, true);
		  probNet.addLink(nodeTreatment, nodeCostTreatment, true);
		  probNet.addLink(nodeTreatment, nodeInmediate_Survival, true);
		  probNet.addLink(nodeTreatment, nodeSurvivors_QALE, true);
		  probNet.addLink(nodeDecEBUS, nodeEBUS, true);
		  probNet.addLink(nodeDecEBUS, nodeCostEBUS, true);
		  probNet.addLink(nodeDecEBUS, nodeEBUS_Morbidity, true);
		  probNet.addLink(nodeDecEUS, nodeEUS, true);
		  probNet.addLink(nodeDecEUS, nodeEUS_Morbidity, true);
		  probNet.addLink(nodeDecEUS, nodeCostEUS, true);
		  probNet.addLink(nodeSurvivors_QALE, nodeNet_QALE, true);
		  probNet.addLink(nodeInmediate_Survival, nodeNet_QALE, true);
		  probNet.addLink(nodeMED_Survival, nodeNet_QALE, true);
		  probNet.addLink(nodeNet_QALE, nodeTotal_QALE, true);
		  probNet.addLink(nodeTBNA_Morbidity, nodeTotal_QALE, true);
		  probNet.addLink(nodeMED_Morbidity, nodeTotal_QALE, true);
		  probNet.addLink(nodeEUS_Morbidity, nodeTotal_QALE, true);
		  probNet.addLink(nodeEBUS_Morbidity, nodeTotal_QALE, true);
		  probNet.addLink(nodeTotal_QALE, nodeNet_Effectiveness, true);
		  probNet.addLink(nodeCostCT_scan, nodeTotal_Economic_Cost, true);
		  probNet.addLink(nodeCostTBNA, nodeTotal_Economic_Cost, true);
		  probNet.addLink(nodeCostEBUS, nodeTotal_Economic_Cost, true);
		  probNet.addLink(nodeCostEUS, nodeTotal_Economic_Cost, true);
		  probNet.addLink(nodeCostMED, nodeTotal_Economic_Cost, true);
		  probNet.addLink(nodeCostPET, nodeTotal_Economic_Cost, true);
		  probNet.addLink(nodeCostTreatment, nodeTotal_Economic_Cost, true);
		  probNet.addLink(nodeTotal_Economic_Cost, nodeWeighted_Economic_Cost, true);
		  probNet.addLink(nodeC2E, nodeWeighted_Economic_Cost, true);
		  probNet.addLink(nodeWeighted_Economic_Cost, nodeNet_Effectiveness, true);

		  // Potentials
		  TablePotential potN2_N3 = new TablePotential(Arrays.asList(varN2_N3), PotentialRole.CONDITIONAL_PROBABILITY);
		  potN2_N3.values = new double[]{0.7193, 0.2807};
		  nodeN2_N3.setPotential(potN2_N3);

		  TablePotential potCT_scan = new TablePotential(Arrays.asList(varCT_scan, varN2_N3), PotentialRole.CONDITIONAL_PROBABILITY);
		  potCT_scan.values = new double[]{0.85676, 0.14324, 0.48966, 0.51034};
		  nodeCT_scan.setPotential(potCT_scan);

		  TablePotential potTBNA = new TablePotential(Arrays.asList(varTBNA, varCT_scan, varN2_N3, varDecTBNA), PotentialRole.CONDITIONAL_PROBABILITY);
		  potTBNA.values = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0.92143, 0.07857, 0.90435, 0.09565, 0.98, 0.02, 0.54032, 0.45968};
		  nodeTBNA.setPotential(potTBNA);

		  TablePotential potPET = new TablePotential(Arrays.asList(varPET, varCT_scan, varN2_N3, varDecPET), PotentialRole.CONDITIONAL_PROBABILITY);
		  potPET.values = new double[]{1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92473, 0.07527, 0, 0.775, 0.225, 0, 0.25974, 0.74026, 0, 0.09524, 0.90476};
		  nodePET.setPotential(potPET);

		  TablePotential potEBUS = new TablePotential(Arrays.asList(varEBUS, varPET, varCT_scan, varN2_N3, varDecEBUS), PotentialRole.CONDITIONAL_PROBABILITY);
		  potEBUS.values = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.97778, 0.02222, 0.975, 0.025, 0.96667, 0.03333, 0.97368, 0.02632, 0.97561, 0.02439, 0.96552, 0.03448, 0.10811, 0.89189, 0.11905, 0.88095, 0.10811, 0.89189, 0.08108, 0.91892, 0.11111, 0.88889, 0.12121, 0.87879};
		  nodeEBUS.setPotential(potEBUS);

		  TablePotential potEUS = new TablePotential(Arrays.asList(varEUS, varPET, varCT_scan, varN2_N3, varDecEUS), PotentialRole.CONDITIONAL_PROBABILITY);
		  potEUS.values = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.92308, 0.07692, 0.9375, 0.0625, 0.92593, 0.07407, 0.92857, 0.07143, 0.93333, 0.06667, 0.93548, 0.06452, 0.2381, 0.7619, 0.43333, 0.56667, 0.41935, 0.58065, 0.14286, 0.85714, 0.13158, 0.86842, 0.13889, 0.86111};
		  nodeEUS.setPotential(potEUS);

		  TablePotential potMED = new TablePotential(Arrays.asList(varMED, varPET, varCT_scan, varN2_N3, varDecMED), PotentialRole.CONDITIONAL_PROBABILITY);
		  potMED.values = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.94444, 0.05556, 0.9375, 0.0625, 0.94737, 0.05263, 0.92857, 0.07143, 0.94118, 0.05882, 0.95, 0.05, 0.27273, 0.72727, 0.2, 0.8, 0.21429, 0.78571, 0.1875, 0.8125, 0.1875, 0.8125, 0.2, 0.8};
		  nodeMED.setPotential(potMED);

		  TablePotential potMED_Sv = new TablePotential(Arrays.asList(varMED_Sv, varDecMED), PotentialRole.CONDITIONAL_PROBABILITY);
		  potMED_Sv.values = new double[]{0, 1, 0.03704, 0.96296};
		  nodeMED_Sv.setPotential(potMED_Sv);

		  TablePotential potSurvivors_QALE = new TablePotential(varSurvivors_QALE,Arrays.asList(varN2_N3, varTreatment));
		  potSurvivors_QALE.values = new double[]{1.25, 0.5, 2, 0.83, 3, 0.66};
		  nodeSurvivors_QALE.setPotential(potSurvivors_QALE);

		  TablePotential potInmediate_Survival = new TablePotential(varInmediate_Survival,Arrays.asList(varTreatment));
		  potInmediate_Survival.values = new double[]{0.98113, 0.98039, 0.90909};
		  nodeInmediate_Survival.setPotential(potInmediate_Survival);

		  TablePotential potMED_Survival = new TablePotential(varMED_Survival,Arrays.asList(varMED_Sv));
		  potMED_Survival.values = new double[]{0, 1};
		  nodeMED_Survival.setPotential(potMED_Survival);

		  ProductPotential potNet_QALE = new ProductPotential(varNet_QALE,Arrays.asList(varInmediate_Survival, varMED_Survival, varSurvivors_QALE));
		  nodeNet_QALE.setPotential(potNet_QALE);

		  TablePotential potTBNA_Morbidity = new TablePotential(varTBNA_Morbidity,Arrays.asList(varDecTBNA));
		  potTBNA_Morbidity.values = new double[]{0, -0.0001};
		  nodeTBNA_Morbidity.setPotential(potTBNA_Morbidity);

		  TablePotential potMED_Morbidity = new TablePotential(varMED_Morbidity,Arrays.asList(varMED_Sv));
		  potMED_Morbidity.values = new double[]{0, -0.05};
		  nodeMED_Morbidity.setPotential(potMED_Morbidity);

		  TablePotential potEUS_Morbidity = new TablePotential(varEUS_Morbidity,Arrays.asList(varDecEUS));
		  potEUS_Morbidity.values = new double[]{0, -0.03};
		  nodeEUS_Morbidity.setPotential(potEUS_Morbidity);

		  TablePotential potEBUS_Morbidity = new TablePotential(varEBUS_Morbidity,Arrays.asList(varDecEBUS));
		  potEBUS_Morbidity.values = new double[]{0, -0.03};
		  nodeEBUS_Morbidity.setPotential(potEBUS_Morbidity);

		  SumPotential potTotal_QALE = new SumPotential(varTotal_QALE,Arrays.asList(varEBUS_Morbidity, varEUS_Morbidity, varMED_Morbidity, varNet_QALE, varTBNA_Morbidity));
		  nodeTotal_QALE.setPotential(potTotal_QALE);

		  TablePotential potCostCT_scan = new TablePotential(varCostCT_scan,new ArrayList<Variable>());
		  potCostCT_scan.values = new double[]{670};
		  nodeCostCT_scan.setPotential(potCostCT_scan);

		  TablePotential potCostTBNA = new TablePotential(varCostTBNA,Arrays.asList(varDecTBNA));
		  potCostTBNA.values = new double[]{0, 80};
		  nodeCostTBNA.setPotential(potCostTBNA);

		  TablePotential potCostEBUS = new TablePotential(varCostEBUS,Arrays.asList(varDecEBUS));
		  potCostEBUS.values = new double[]{0, 620};
		  nodeCostEBUS.setPotential(potCostEBUS);

		  TablePotential potCostEUS = new TablePotential(varCostEUS,Arrays.asList(varDecEUS));
		  potCostEUS.values = new double[]{0, 620};
		  nodeCostEUS.setPotential(potCostEUS);

		  TablePotential potCostMED = new TablePotential(varCostMED,Arrays.asList(varDecMED));
		  potCostMED.values = new double[]{0, 1620};
		  nodeCostMED.setPotential(potCostMED);

		  TablePotential potCostPET = new TablePotential(varCostPET,Arrays.asList(varDecPET));
		  potCostPET.values = new double[]{0, 2250};
		  nodeCostPET.setPotential(potCostPET);

		  TablePotential potCostTreatment = new TablePotential(varCostTreatment,Arrays.asList(varTreatment));
		  potCostTreatment.values = new double[]{3000, 11242, 19646};
		  nodeCostTreatment.setPotential(potCostTreatment);

		  SumPotential potTotal_Economic_Cost = new SumPotential(varTotal_Economic_Cost,Arrays.asList(varCostCT_scan, varCostEBUS, varCostEUS, varCostMED, varCostPET, varCostTBNA, varCostTreatment));
		  nodeTotal_Economic_Cost.setPotential(potTotal_Economic_Cost);

		  TablePotential potC2E = new TablePotential(varC2E,new ArrayList<Variable>());
		  potC2E.values = new double[]{-0.00003};
		  nodeC2E.setPotential(potC2E);

		  ProductPotential potWeighted_Economic_Cost = new ProductPotential(varWeighted_Economic_Cost,Arrays.asList(varC2E, varTotal_Economic_Cost));
		  nodeWeighted_Economic_Cost.setPotential(potWeighted_Economic_Cost);

		  SumPotential potNet_Effectiveness = new SumPotential(varNet_Effectiveness,Arrays.asList(varTotal_QALE, varWeighted_Economic_Cost));
		  nodeNet_Effectiveness.setPotential(potNet_Effectiveness);

		  // Link restrictions and revealing states
		  Link link_nodeDecTBNA_nodeTBNA = probNet.getGraph().getLink(nodeDecTBNA.getNode(),nodeTBNA.getNode(), true);
		  link_nodeDecTBNA_nodeTBNA.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDecTBNA_nodeTBNA = (TablePotential)link_nodeDecTBNA_nodeTBNA.getRestrictionsPotential();
		  restrictions_nodeDecTBNA_nodeTBNA.values = new double[] {0, 1, 0, 1};
		  link_nodeDecTBNA_nodeTBNA.setRevealingStates(Arrays.asList(varDecTBNA.getStates()[1]));

		  Link link_nodeDecPET_nodePET = probNet.getGraph().getLink(nodeDecPET.getNode(),nodePET.getNode(), true);
		  link_nodeDecPET_nodePET.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDecPET_nodePET = (TablePotential)link_nodeDecPET_nodePET.getRestrictionsPotential();
		  restrictions_nodeDecPET_nodePET.values = new double[] {1, 0, 0, 1, 0, 1};
		  link_nodeDecPET_nodePET.setRevealingStates(Arrays.asList(varDecPET.getStates()[0], varDecPET.getStates()[1]));

		  Link link_nodeDecMED_nodeMED = probNet.getGraph().getLink(nodeDecMED.getNode(),nodeMED.getNode(), true);
		  link_nodeDecMED_nodeMED.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDecMED_nodeMED = (TablePotential)link_nodeDecMED_nodeMED.getRestrictionsPotential();
		  restrictions_nodeDecMED_nodeMED.values = new double[] {0, 1, 0, 1};
		  link_nodeDecMED_nodeMED.setRevealingStates(Arrays.asList(varDecMED.getStates()[1]));

		  Link link_nodeDecEBUS_nodeEBUS = probNet.getGraph().getLink(nodeDecEBUS.getNode(),nodeEBUS.getNode(), true);
		  link_nodeDecEBUS_nodeEBUS.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDecEBUS_nodeEBUS = (TablePotential)link_nodeDecEBUS_nodeEBUS.getRestrictionsPotential();
		  restrictions_nodeDecEBUS_nodeEBUS.values = new double[] {0, 1, 0, 1};
		  link_nodeDecEBUS_nodeEBUS.setRevealingStates(Arrays.asList(varDecEBUS.getStates()[1]));

		  Link link_nodeDecEUS_nodeEUS = probNet.getGraph().getLink(nodeDecEUS.getNode(),nodeEUS.getNode(), true);
		  link_nodeDecEUS_nodeEUS.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDecEUS_nodeEUS = (TablePotential)link_nodeDecEUS_nodeEUS.getRestrictionsPotential();
		  restrictions_nodeDecEUS_nodeEUS.values = new double[] {0, 1, 0, 1};
		  link_nodeDecEUS_nodeEUS.setRevealingStates(Arrays.asList(varDecEUS.getStates()[1]));

		  // Always observed nodes
		  nodeCT_scan.setAlwaysObserved(true);

		 return probNet;
		}		

	public static ProbNet buildUsedCarBuyer () {
		  ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		  // Variables
		  probNet.setName(NamesNetworks.USED_CAR_BUYER_DAN.toString());
		  
		  Variable varCars_Condition = new Variable("Car's Condition", "lemon", "peach");
		  Variable varFirst_Result = new Variable("First Result", "no result", "no defect", "one defect", "two defects");
		  Variable varSecond_result = new Variable("Second result", "no defect", "one defect");
		  Variable varDec_Purchase = new Variable("Dec: Purchase", "do not buy", "buy without guarantee", "buy with guarantee");
		  Variable varDec_Second_Test = new Variable("Dec: Second Test", "no test", "differential");
		  Variable varDec_First_Test = new Variable("Dec: First Test", "no test", "fuel-electrical", "transmission", "steering");
		  Variable varCost_First_test = new Variable("Cost: First test");
		  Variable varCost_Second_Test = new Variable("Cost: Second Test");
		  Variable varBuySell_difference = new Variable("Buy-sell difference");
		  Variable varCost_Guarantee = new Variable("Cost: Guarantee");
		  Variable varCost_Repair = new Variable("Cost: Repair");
		  Variable varTotal = new Variable("Total");

		  // Nodes
		  ProbNode nodeCars_Condition= probNet.addProbNode(varCars_Condition, NodeType.CHANCE);
		  ProbNode nodeFirst_Result= probNet.addProbNode(varFirst_Result, NodeType.CHANCE);
		  ProbNode nodeSecond_result= probNet.addProbNode(varSecond_result, NodeType.CHANCE);
		  ProbNode nodeDec_Purchase= probNet.addProbNode(varDec_Purchase, NodeType.DECISION);
		  ProbNode nodeDec_Second_Test= probNet.addProbNode(varDec_Second_Test, NodeType.DECISION);
		  ProbNode nodeDec_First_Test= probNet.addProbNode(varDec_First_Test, NodeType.DECISION);
		  ProbNode nodeCost_First_test= probNet.addProbNode(varCost_First_test, NodeType.UTILITY);
		  ProbNode nodeCost_Second_Test= probNet.addProbNode(varCost_Second_Test, NodeType.UTILITY);
		  ProbNode nodeBuySell_difference= probNet.addProbNode(varBuySell_difference, NodeType.UTILITY);
		  ProbNode nodeCost_Guarantee= probNet.addProbNode(varCost_Guarantee, NodeType.UTILITY);
		  ProbNode nodeCost_Repair= probNet.addProbNode(varCost_Repair, NodeType.UTILITY);
		  ProbNode nodeTotal= probNet.addProbNode(varTotal, NodeType.UTILITY);

		  // Links
		  probNet.getGraph().makeLinksExplicit(false);
		  probNet.addLink(nodeCars_Condition, nodeFirst_Result, true);
		  probNet.addLink(nodeCars_Condition, nodeCost_Repair, true);
		  probNet.addLink(nodeCars_Condition, nodeSecond_result, true);
		  probNet.addLink(nodeFirst_Result, nodeSecond_result, true);
		  probNet.addLink(nodeDec_Purchase, nodeCost_Guarantee, true);
		  probNet.addLink(nodeDec_Purchase, nodeBuySell_difference, true);
		  probNet.addLink(nodeDec_Purchase, nodeCost_Repair, true);
		  probNet.addLink(nodeDec_Second_Test, nodeDec_Purchase, true);
		  probNet.addLink(nodeDec_Second_Test, nodeCost_Second_Test, true);
		  probNet.addLink(nodeDec_Second_Test, nodeSecond_result, true);
		  probNet.addLink(nodeDec_First_Test, nodeFirst_Result, true);
		  probNet.addLink(nodeDec_First_Test, nodeDec_Second_Test, true);
		  probNet.addLink(nodeDec_First_Test, nodeCost_First_test, true);
		  probNet.addLink(nodeDec_First_Test, nodeSecond_result, true);
		  probNet.addLink(nodeCost_First_test, nodeTotal, true);
		  probNet.addLink(nodeCost_Second_Test, nodeTotal, true);
		  probNet.addLink(nodeBuySell_difference, nodeTotal, true);
		  probNet.addLink(nodeCost_Guarantee, nodeTotal, true);
		  probNet.addLink(nodeCost_Repair, nodeTotal, true);

		  // Potentials
		  TablePotential potCars_Condition = new TablePotential(Arrays.asList(varCars_Condition), PotentialRole.CONDITIONAL_PROBABILITY);
		  potCars_Condition.values = new double[]{0.2, 0.8};
		  nodeCars_Condition.setPotential(potCars_Condition);

		  TablePotential potFirst_Result = new TablePotential(Arrays.asList(varFirst_Result, varCars_Condition, varDec_First_Test), PotentialRole.CONDITIONAL_PROBABILITY);
		  potFirst_Result.values = new double[]{1, 0, 0, 0, 1, 0, 0, 0, 0, 0.13, 0.53, 0.34, 0, 0.8, 0.2, 0, 0, 0.4, 0.6, 0, 0, 0.9, 0.1, 0, 0, 0.4, 0.6, 0, 0, 0.9, 0.1, 0};
		  nodeFirst_Result.setPotential(potFirst_Result);

		  TablePotential potSecond_result = new TablePotential(Arrays.asList(varSecond_result, varCars_Condition, varDec_Second_Test, varFirst_Result, varDec_First_Test), PotentialRole.CONDITIONAL_PROBABILITY);
		  potSecond_result.values = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.67, 0.33, 0.89, 0.11, 0, 0, 0, 0, 0.44, 0.56, 1, 0, 0, 0, 0, 0, 0.44, 0.56, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		  nodeSecond_result.setPotential(potSecond_result);

		  TablePotential potCost_First_test = new TablePotential(varCost_First_test,Arrays.asList(varDec_First_Test));
		  potCost_First_test.values = new double[]{0, -13, -10, -9};
		  nodeCost_First_test.setPotential(potCost_First_test);

		  TablePotential potCost_Second_Test = new TablePotential(varCost_Second_Test,Arrays.asList(varDec_Second_Test));
		  potCost_Second_Test.values = new double[]{0, -4};
		  nodeCost_Second_Test.setPotential(potCost_Second_Test);

		  TablePotential potBuySell_difference = new TablePotential(varBuySell_difference,Arrays.asList(varDec_Purchase));
		  potBuySell_difference.values = new double[]{0, 100, 100};
		  nodeBuySell_difference.setPotential(potBuySell_difference);

		  TablePotential potCost_Guarantee = new TablePotential(varCost_Guarantee,Arrays.asList(varDec_Purchase));
		  potCost_Guarantee.values = new double[]{0, 0, -60};
		  nodeCost_Guarantee.setPotential(potCost_Guarantee);

		  TablePotential potCost_Repair = new TablePotential(varCost_Repair,Arrays.asList(varCars_Condition, varDec_Purchase));
		  potCost_Repair.values = new double[]{0, 0, -200, -40, 0, -20};
		  nodeCost_Repair.setPotential(potCost_Repair);

		  SumPotential potTotal = new SumPotential(varTotal,Arrays.asList(varCost_First_test, varCost_Second_Test, varCost_Guarantee, varCost_Repair, varBuySell_difference));
		  nodeTotal.setPotential(potTotal);

		  // Link restrictions and revealing states
		  Link link_nodeFirst_Result_nodeSecond_result = probNet.getGraph().getLink(nodeFirst_Result.getNode(),nodeSecond_result.getNode(), true);
		  link_nodeFirst_Result_nodeSecond_result.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeFirst_Result_nodeSecond_result = (TablePotential)link_nodeFirst_Result_nodeSecond_result.getRestrictionsPotential();
		  restrictions_nodeFirst_Result_nodeSecond_result.values = new double[] {0, 1, 1, 1, 0, 1, 1, 1};

		  Link link_nodeDec_Second_Test_nodeSecond_result = probNet.getGraph().getLink(nodeDec_Second_Test.getNode(),nodeSecond_result.getNode(), true);
		  link_nodeDec_Second_Test_nodeSecond_result.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDec_Second_Test_nodeSecond_result = (TablePotential)link_nodeDec_Second_Test_nodeSecond_result.getRestrictionsPotential();
		  restrictions_nodeDec_Second_Test_nodeSecond_result.values = new double[] {0, 1, 0, 1};
		  link_nodeDec_Second_Test_nodeSecond_result.setRevealingStates(Arrays.asList(varDec_Second_Test.getStates()[1]));

		  Link link_nodeDec_First_Test_nodeFirst_Result = probNet.getGraph().getLink(nodeDec_First_Test.getNode(),nodeFirst_Result.getNode(), true);
		  link_nodeDec_First_Test_nodeFirst_Result.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDec_First_Test_nodeFirst_Result = (TablePotential)link_nodeDec_First_Test_nodeFirst_Result.getRestrictionsPotential();
		  restrictions_nodeDec_First_Test_nodeFirst_Result.values = new double[] {1, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0};
		  link_nodeDec_First_Test_nodeFirst_Result.setRevealingStates(Arrays.asList(varDec_First_Test.getStates()[3], varDec_First_Test.getStates()[2], varDec_First_Test.getStates()[1]));

		  Link link_nodeDec_First_Test_nodeDec_Second_Test = probNet.getGraph().getLink(nodeDec_First_Test.getNode(),nodeDec_Second_Test.getNode(), true);
		  link_nodeDec_First_Test_nodeDec_Second_Test.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDec_First_Test_nodeDec_Second_Test = (TablePotential)link_nodeDec_First_Test_nodeDec_Second_Test.getRestrictionsPotential();
		  restrictions_nodeDec_First_Test_nodeDec_Second_Test.values = new double[] {1, 1, 1, 1, 0, 0, 1, 0};

		  Link link_nodeDec_First_Test_nodeSecond_result = probNet.getGraph().getLink(nodeDec_First_Test.getNode(),nodeSecond_result.getNode(), true);
		  link_nodeDec_First_Test_nodeSecond_result.initializesRestrictionsPotential();
		  TablePotential restrictions_nodeDec_First_Test_nodeSecond_result = (TablePotential)link_nodeDec_First_Test_nodeSecond_result.getRestrictionsPotential();
		  restrictions_nodeDec_First_Test_nodeSecond_result.values = new double[] {0, 0, 1, 0, 0, 0, 1, 0};

		  // Always observed nodes

		 return probNet;
		}	
}
