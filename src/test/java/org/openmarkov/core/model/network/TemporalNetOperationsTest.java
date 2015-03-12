/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.model.network;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.CycleLength.DiscountUnit;
import org.openmarkov.core.model.network.CycleLength.Unit;
import org.openmarkov.core.model.network.factory.MarkovFactory;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TablePotentialTest;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.MPADType;

/**
 * @author mluque
 *
 */
public class TemporalNetOperationsTest {
	/**
	 * Maximum error allowed in tests. It could be modified by subclasses
	 * if it is necessary (for example, approximate inference methods).
	 */
	protected double maxError = 1E-6;

	/**
	 * Test a MPAD with three variables: Treatment, CostOfTreatment and QoL (temporal variable)
	 * It performs a battery of tests: for numSlices = 1, numSlices = 2, ..., numSlices = 100
	 */
	//@Test
	public void testExpansionMPADWithoutStateVariable() {
		double qoLTreat;
		double qoLNoTreat;
		double costTreat;
		double costNoTreat;
		int maximumNumSlices;

		maximumNumSlices = 2;
		int startNumSlices = 1;

		qoLTreat = 0.9;
		qoLNoTreat = 1.0;
		costTreat = 40000;
		costNoTreat = 0;

		for (int numSlices = startNumSlices; numSlices <= maximumNumSlices; numSlices++) {
			
			//Create the MPAD and expand it
			ProbNet network = MarkovFactory.createMPADWithoutStateVariable(qoLTreat, qoLNoTreat,
					costTreat, costNoTreat);
			double discount = 0.01;
			
            ProbNet expandedNetwork = TemporalNetOperations.expandNetwork(network);
			
			List<TablePotential> tablePotentials = extractUtilityPotentialsProjecToTablesAndCheckVariables(expandedNetwork);

			TablePotential globalPotential = DiscretePotentialOperations.sum(tablePotentials);
			
			//Create a potential with the expected results
			double ratio = 1.0 / (1.0 + discount);
			double sumQoLTreatTerms = sumTermsGeometricProgression(qoLTreat, ratio, numSlices);
			double sumQoLNoTreatTerms = sumTermsGeometricProgression(qoLNoTreat, ratio, numSlices);
			ArrayList<Variable> variablesUtil;

			variablesUtil = new ArrayList<>();
			try {
				variablesUtil.add(expandedNetwork.getVariable("Treatment"));
				variablesUtil.add(expandedNetwork.getVariable("Decision criteria"));
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			TablePotential expectedPotential = new TablePotential(variablesUtil, PotentialRole.UTILITY);
			// TODO We should consider here the order of the states of
			// DecisionCriteria variable
			double values[] = { costTreat, costNoTreat, sumQoLTreatTerms, sumQoLNoTreatTerms };
			expectedPotential.setValues(values);
			
			//Compare the global utility potential of the expanded network with the expected results
			TablePotentialTest.checkEqualPotentials(globalPotential, expectedPotential, maxError);
			
		}

	}


		
	/**
	 * Test a MPAD with three variables: Treatment, CostOfTreatment and QoL (temporal variable)
	 * It performs a battery of tests: for numSlices = 1, numSlices = 2, ..., numSlices = 100
	 */
	//@Test
	public void testExpansionMPADWithStateVariable() {
		double qoLTreat;
		double qoLNoTreat;
		double costTreat;
		double costNoTreat;
		int maximumNumSlices;

		maximumNumSlices = 5;
		int startNumSlices = 1;

		qoLTreat = 0.9;
		qoLNoTreat = 1.0;
		costTreat = 40000;
		costNoTreat = 0;

		for (int numSlices = startNumSlices; numSlices <= maximumNumSlices; numSlices++) {
			
			//Create the MPAD and expand it
			ProbNet network = MarkovFactory.createMPADWithStateVariable(qoLTreat, qoLNoTreat,
					costTreat, costNoTreat,0.7,0.5);
			double discount = 0.01;

			ProbNet expandedNetwork = TemporalNetOperations.expandNetwork(network);
			
		
			List<TablePotential> tablePotentials = extractUtilityPotentialsProjecToTablesAndCheckVariables(expandedNetwork);
			//Check utility potentials starting in slice 1
			double ratio = 1.0 / (1.0 + discount);
			for (TablePotential auxPot:tablePotentials){
				if (hasTemporalVariableRoleAndNotZeroSlice(auxPot,PotentialRole.UTILITY)){
					int slice = auxPot.getUtilityVariable().getTimeSlice();
					checkUtilityPotentialQoLMPADWithState(expandedNetwork,auxPot,qoLTreat,qoLNoTreat,ratio,slice);
				}
			}
			
			/*//Check probability potentials starting in slice 1
			for (TablePotential auxPot:tablePotentials){
				if (hasTemporalVariableRoleAndNotZeroSlice(auxPot)){
					int slice = auxPot.getUtilityVariable().getTimeSlice();
				
					checkUtilityPotentialQoLMPADWithState(expandedNetwork,auxPot,qoLTreat,qoLNoTreat,ratio,slice);
				}
			}
	*/
					
		}
		
		
	}

//	@Test
	/*public public void testSemimarkovExpansion() {
		int maximumNumSlices;

		maximumNumSlices = 5;
		int startNumSlices = 1;

		for (int numSlices = startNumSlices; numSlices <= maximumNumSlices; numSlices++) {
			
			//Create the MPAD and expand it
			ProbNet network = NetsFactory.createSemiMarkovOnlyChanceNet();
			double discount = 0.0;

			ProbNet expandedNetwork = FactoryExpandedMPAD.constructExpandedNetwork(numSlices, network, discount*100.0, discount*100.0, true);
			
		
			ArrayList<TablePotential> tablePotentials = extractUtilityPotentialsProjecToTablesAndCheckVariables(expandedNetwork);
			//Check utility potentials starting in slice 1
			double ratio = 1.0 / (1.0 + discount);
			for (TablePotential auxPot:tablePotentials){
				if (hasTemporalVariableRoleAndNotZeroSlice(auxPot,PotentialRole.UTILITY)){
					int slice = auxPot.getUtilityVariable().getTimeSlice();
					checkUtilityPotentialQoLMPADWithState(expandedNetwork,auxPot,qoLTreat,qoLNoTreat,ratio,slice);
				}
			}
			
			
					
		}
	}*/


	public void checkUtilityPotentialQoLMPADWithState(ProbNet expandedNetwork, TablePotential auxPot, double qoLTreat, double qoLNoTreat, double ratio, int slice) {
	
		
		ArrayList<Variable> variablesUtil = new ArrayList<>();
		try {
			variablesUtil.add(expandedNetwork.getVariable("Treatment"));
			variablesUtil.add(expandedNetwork.getVariable("Decision criteria"));
			variablesUtil.add(expandedNetwork.getVariable(nameStateVariable(auxPot.getUtilityVariable())));

		} catch (NodeNotFoundException e1) {
			e1.printStackTrace();
		}
				
		double termQoLTreat = termGeometricProgression(qoLTreat,ratio,slice);
		double termQoLNoTreat = termGeometricProgression(qoLNoTreat,ratio,slice);
		
		double expectedValues[] = {0.0,0.0,0.0,0.0,0.0,0.0,termQoLTreat,termQoLNoTreat};
		TablePotential expectedPotential = new TablePotential(variablesUtil, PotentialRole.UTILITY);
		expectedPotential.setValues(expectedValues);
		//Compare the global utility potential of the expanded network with the expected results
		TablePotentialTest.checkEqualPotentials(auxPot, expectedPotential, maxError);
		
	}


	private String nameStateVariable(Variable variable) {
		Variable aux;
		aux = new Variable("State");
		aux.setBaseName(aux.getName());
		aux.setTimeSlice(variable.getTimeSlice());
		return aux.getName();
	}


	private boolean hasTemporalVariableRoleAndNotZeroSlice(TablePotential auxPot, PotentialRole role) {
		
		boolean has;
		has = false;
		Variable varToAnalyze=null;
		if (auxPot.getPotentialRole()==role){
			switch (role){
			case CONDITIONAL_PROBABILITY:
					varToAnalyze = auxPot.getVariables().get(0);
					break;
			case UTILITY:
					varToAnalyze = auxPot.getUtilityVariable();
					break;
			default:
				break;
			
			}
			
			has = (varToAnalyze!=null) && varToAnalyze.isTemporal() && varToAnalyze.getTimeSlice()>0;
		}
		return has;
	}


	private List<TablePotential> extractUtilityPotentialsProjecToTablesAndCheckVariables(
			ProbNet expandedNetwork) {
		InferenceOptions inferenceOptions;
		List<Potential> utilityPotentials = expandedNetwork
				.getPotentialsByRole(PotentialRole.UTILITY);
		
		
		inferenceOptions = new InferenceOptions(expandedNetwork, null);

		List<TablePotential> tablePotentials;
		tablePotentials = new ArrayList<>();
		for (Potential auxPotential : utilityPotentials) {
			assertNotNull(auxPotential.getUtilityVariable());
			try {
				List<TablePotential> tableProject = auxPotential.tableProject(null, inferenceOptions);
				//Check utilityVariables are not null
				for (TablePotential auxTable:tableProject){
					assertNotNull(auxTable.getUtilityVariable());
				}
				tablePotentials.addAll(tableProject);
			} catch (NonProjectablePotentialException
					| WrongCriterionException e) {
				e.printStackTrace();
			}
		}
		return tablePotentials;
	}
	
	/**
	 * @param firstTerm
	 * @param ratio
	 * @param numTerms
	 * @return The sum of 'numTerms' terms of a geometric progression whose first term is 'firsTerm'
	 * and its ratio is 'ratio'
	 */
	public static double sumTermsGeometricProgression(double firstTerm,double ratio,int numTerms){
		return (firstTerm-firstTerm*Math.pow(ratio, numTerms))/(1.0-ratio);
	}
	
	/**
	 * @param firstTerm
	 * @param ratio
	 * @param numTerm
	 * @return The term of a geometric progression whose first term is 'firsTerm'
	 * and its ratio is 'ratio'
	 */
	private double termGeometricProgression(double firstTerm,double ratio,int numTerm){
		return (firstTerm*Math.pow(ratio, numTerm));
	}
	
	// TODO - @Test
	public void applyDiscountToUtilityNodesTest(){
		ProbNet probNet = getProbNet4Test();
		TemporalNetOperations.applyDiscountToUtilityNodes(probNet);
		
		
	}
	
	public static ProbNet getProbNet4Test () {
		  ProbNet probNet = new ProbNet(MPADType.getUniqueInstance());
		  // Variables and criteria
		  
		  Variable varA = new Variable("varA");
		  
		  Variable varEfectividad0 = new Variable("Efectividad [0]");
		  varEfectividad0.setTimeSlice(0);
		  
		  Variable varEfectividad1 = new Variable("Efectividad [1]");
		  varEfectividad1.setTimeSlice(1);
		  

		  Criterion efectividad = new Criterion("efectividad", "QALY");
		  efectividad.setDiscount(0.5);
		  efectividad.setDiscountUnit(DiscountUnit.CYCLE);
		  varEfectividad0.setDecisionCriterion(efectividad);
		  varEfectividad1.setDecisionCriterion(efectividad);
		  
		  // Nodes
		  Node nodeA= probNet.addNode(varA, NodeType.CHANCE);
		  Node nodeEfectividad= probNet.addNode(varEfectividad0, NodeType.UTILITY);
		  Node nodeEfectividad1 = probNet.addNode(varEfectividad1, NodeType.UTILITY);

		  // Links
		  probNet.makeLinksExplicit(false);
		  probNet.addLink(nodeA, nodeEfectividad, true);
		  probNet.addLink(nodeA, nodeEfectividad1, true);

		  // Potentials
		  UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		  nodeA.setPotential(potA);


		  TablePotential potEfectividad = new TablePotential(varEfectividad0,Arrays.asList(varA));
		  potEfectividad.values = new double[]{0, 10};
		  nodeEfectividad.setPotential(potEfectividad);
		  
		  TablePotential potEfectividad1 = new TablePotential(varEfectividad1,Arrays.asList(varA));
		  potEfectividad1.values = new double[]{15, 20};
		  nodeEfectividad.setPotential(potEfectividad1);

		  // ProbNet cycle length
		  probNet.setCycleLength(new CycleLength(Unit.YEAR, 1));

		 return probNet;
		}
	
	
	
	


	
	

}
