package org.openmarkov.core;

import java.util.ArrayList;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
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
	 * @return a Bayesian network with two nodes (X and Y) and a link X -> Y
	 * @throws Exception
	 */
	public static ProbNet createProbNetXY(double prevalence,double sensitivity,double specificity) throws Exception {
		ProbNet probNet;
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
					
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		
		// Define the variables
		Variable variableX = new Variable("X","present","absent");
		
		// Define the variables
		Variable variableY = new Variable("Y",2);
		State[] statesOfY = {new State("positive"), new State("negative")};
		variableY.setStates(statesOfY);
			
		probNet.addVariable(variableX, NodeType.CHANCE);
		probNet.addVariable(variableY, NodeType.CHANCE);

		probNet.addLink(variableX,variableY, true);
			
		ArrayList<Variable> variablesX;
		variablesX = new ArrayList<Variable>();
		variablesX.add(variableX);
		
		ArrayList<Variable> variablesYX;
		variablesYX = new ArrayList<Variable>();
		variablesYX.add(variableY);
		variablesYX.add(variableX);

		double[] valuesX = {1.0-prevalence, prevalence};
		TablePotential potentialX = new TablePotential(variablesX, role, valuesX);
		probNet.addPotential (potentialX);
		
		double [] valuesYX = {specificity, 1.0-specificity, 1.0-sensitivity, sensitivity};
		TablePotential potentialYX = new TablePotential(variablesYX, role, valuesYX);
		probNet.addPotential(potentialYX);
		return probNet;
}
	
	
	/**
	 * @return A Bayesian network with three nodes (A, B and C) and two links A -> B, and A -> C.
	 * This network was stored in file "peque.elv"
	 */
	public static ProbNet createProbNetABC(){
		Variable variableA;
		Variable variableB;
		Variable variableC;
		
		//probNet peque					
		//Variables
		String a = new String("A");
		String b = new String("B");
		String c = new String("C");
		
		//finite States variables
		variableA = new Variable(a,2);
		variableB = new Variable(b,2);
		variableC = new Variable(c,2);
			
				
		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");
			
		variableA.setAdditionalProperty(relevance,value);
		variableB.setAdditionalProperty(relevance,value);
		variableC.setAdditionalProperty(relevance,value);
		
		//Setting variable states
		State absent = new State("ausente");
		State present = new State("presente");
		State [] states= {absent, present};
		
		variableA.setStates(states);
		variableB.setStates(states);
		variableC.setStates(states); 
		
		//Potentials
		//PotentialType type = PotentialType.TABLE;
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
		
		//Potential A
		double [] tableA ={0.2, 0.8};
		
		ArrayList<Variable> variablesA = new ArrayList<Variable>();
		variablesA.add(variableA);
		
		TablePotential potentialvaluesA = new TablePotential(variablesA,role, tableA);
		
		
		//Potential BA
		double [] tableBA ={0.7, 0.3, 0.9, 0.1};
		
		ArrayList<Variable> variablesBA = new ArrayList<Variable>();;
		variablesBA.add(variableB);
		variablesBA.add(variableA);
		
		
		TablePotential potentialvaluesBA = new TablePotential(variablesBA,role,tableBA);
		
		//potencial CAB
		//double [] tableCAB ={0.15, 0.29, 0.84, 0.98, 0.85, 0.71, 0.16, 0.02};
		double [] tableCAB ={0.15, 0.85, 0.84, 0.16, 0.29, 0.71, 0.98, 0.02};
		
		ArrayList<Variable> variablesCAB = new ArrayList<Variable>();
		variablesCAB.add(variableC);
		variablesCAB.add(variableA);
		variablesCAB.add(variableB);
		
		
		//notEnoughMemoryException 
		TablePotential potentialvaluesCAB = new TablePotential(variablesCAB,role,tableCAB);
		
		ProbNet peque = new ProbNet();
		
		NodeType nodeType = NodeType.CHANCE;
		
		peque.addVariable(variableA, nodeType);
		peque.addVariable(variableB, nodeType);
		peque.addVariable(variableC, nodeType);
		
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
		
		
		peque.addPotential((Potential)potentialvaluesA);
		peque.addPotential((Potential)potentialvaluesBA);
		peque.addPotential((Potential)potentialvaluesCAB);
	
	
	return peque;
}
	

	

}
