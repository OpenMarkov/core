package org.openmarkov.core.model.network;

import java.util.ArrayList;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

/**
 * @author manolo
 * This class is used for building ProbNets corresponding to some networks used in the tests.
 * Networks are built via Java sentences not requiring any parser.
 *
 */
public class NetsFactory {
	
	/**
	 * @param variables
	 * @return An ArrayList containing the variables
	 */
	private static ArrayList<Variable> createArrayListVariables(Variable...variables){
		ArrayList<Variable> arrayList;
		
		arrayList = new ArrayList<Variable>();
		for (int i=0;i<variables.length;i++){
			arrayList.add(variables[i]);
		}
		return arrayList;
		
	}
	
	private static double[] valuesAPrioriDisease(double prevalence){
		double values[];
		
		values = new double[2];
		values[0] = 1.0-prevalence;
		values[1] = prevalence;
		
		return values;
	}
	
	private static double[] valuesCPTResultTest(double sensitivity, double specificity){
		
		double [] values = {specificity, 1.0-specificity, 1.0-sensitivity, sensitivity};
				
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
			net.addVariable(variables[i],nodeType);
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
		
		ArrayList<Variable> arrayListVariables = createArrayListVariables(variables);
		
		return new TablePotential(arrayListVariables, role, values);
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
	 * @return a Bayesian network with two nodes (X and Y) and a link X -> Y
	 * @throws Exception
	 */
	public static ProbNet createBayesianNetworkXY(double prevalence,double sensitivity,double specificity) throws Exception {
		ProbNet probNet;
		double[] valuesX;
		double [] valuesYX;
				
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
					
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		
		// Define the variables
		Variable variableX = new Variable("X","present","absent");
		Variable variableY = new Variable("Y","positive","negative");
			
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
	 * @return A Bayesian network with three nodes (A, B and C) and two links A -> B, and A -> C.
	 * This network was stored in file "peque.elv"
	 */
	public static ProbNet createBayesianNetworkABC(){
		Variable variableA;
		Variable variableB;
		Variable variableC;
		double [] tableA;
		double [] tableBA;
		
		ProbNet peque = new ProbNet();
		
		String nameStates[]={"ausente","presente"};
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
		//double [] tableCAB ={0.15, 0.29, 0.84, 0.98, 0.85, 0.71, 0.16, 0.02};
		double [] tableCAB ={0.15, 0.85, 0.84, 0.16, 0.29, 0.71, 0.98, 0.02};
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
	 * @return An influence diagram with four nodes: X, Y, D and U. It represents a diagnosis problem.
	 * It is the example of influence diagram described in page 11 in the book available online at URL:
	 * http://www.cisiad.uned.es/techreports/decision-medicina.pdf
	 */
	public static ProbNet createInfluenceDiagramDiagnosisProblem() {
			
			ProbNet probNet;
			PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
			double prevalence=0.07;
			double sensitivity=0.91;
			double specificity=0.97;
			double [] tableX;
			double [] tableYX;
			TablePotential potentialX;
			TablePotential potentialY;
			TablePotential potentialU;
						
			probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
			
			// Define the variables
			Variable variableX = new Variable("X","present","absent");
			Variable variableY = new Variable("Y","positive","negative");
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
			tableX = valuesAPrioriDisease(prevalence);
			potentialX = createTablePotential(roleProbability, tableX, variableX);
			
			//Potential YX
			tableYX = valuesCPTResultTest(sensitivity,specificity);
			potentialY = createTablePotential(roleProbability, tableYX, variableY, variableX);
			
			//Potential UXD
			double [] tableUXD ={98.0, 28.0, 88.0, 78.0};
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
	

}
