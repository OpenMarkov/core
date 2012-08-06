/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.inference;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import org.junit.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.FactoryExpandedSMM;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.TablePotentialTest;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

/**
 * @author mluque
 *
 */
public class FactoryExpandedSMMTest {
	/**
	 * Maximum error allowed in tests. It could be modified by subclasses
	 * if it is necessary (for example, approximate inference methods).
	 */
	protected double maxError = 1E-6;

	

	/*@Test
	public void test() {
		fail("Not yet implemented");
	}*/
	
	
	/**
	 * Test a SMM with three variables: Treatment, CostOfTreatment and QoL (temporal variable)
	 * It performs a battery of tests: for numSlices = 1, numSlices = 2, ..., numSlices = 100
	 */
	@Test
	public void testExpansionSMMWithoutStateVariable() {
		double qoLTreat;
		double qoLNoTreat;
		double costTreat;
		double costNoTreat;
		int maximumNumSlices;

		maximumNumSlices = 4;
		int startNumSlices = 1;

		qoLTreat = 0.9;
		qoLNoTreat = 1.0;
		costTreat = 40000;
		costNoTreat = 0;

		for (int numSlices = startNumSlices; numSlices <= maximumNumSlices; numSlices++) {
			
			//Create the SMM and expand it
			ProbNet network = NetsFactory.createSMMWithoutStateVariable(qoLTreat, qoLNoTreat,
					costTreat, costNoTreat);
			double discount = 0.01;
			ProbNet expandedNetwork = FactoryExpandedSMM.constructExpandedNetwork(numSlices, network, discount,true);
			
			ArrayList<TablePotential> tablePotentials = extractUtilityPotentialsProjecToTablesAndCheckVariables(expandedNetwork);

			TablePotential globalPotential = null;
			try {
				globalPotential = DiscretePotentialOperations.sum(tablePotentials);
			} catch (NotEnoughMemoryException e) {
				e.printStackTrace();
			}
			
			//Create a potential with the expected results
			double ratio = 1.0 / (1.0 + discount);
			double sumQoLTreatTerms = sumTermsGeometricProgression(qoLTreat, ratio, numSlices);
			double sumQoLNoTreatTerms = sumTermsGeometricProgression(qoLNoTreat, ratio, numSlices);
			ArrayList<Variable> variablesUtil;

			variablesUtil = new ArrayList<>();
			try {
				variablesUtil.add(expandedNetwork.getVariable("Treatment"));
			} catch (ProbNodeNotFoundException e) {
				e.printStackTrace();
			}
			variablesUtil.add(expandedNetwork.decisionCriteria);
			TablePotential expectedPotential = null;
			try {
				expectedPotential = new TablePotential(variablesUtil, PotentialRole.UTILITY);
			} catch (NotEnoughMemoryException e) {
				e.printStackTrace();
			}
			// TODO We should consider here the order of the states of
			// DecisionCriteria variable
			double values[] = { costTreat, costNoTreat, sumQoLTreatTerms, sumQoLNoTreatTerms };
			expectedPotential.setValues(values);
			
			//Compare the global utility potential of the expanded network with the expected results
			TablePotentialTest.checkEqualPotentials(globalPotential, expectedPotential, maxError);
			
		}

	}


		
	/**
	 * Test a SMM with three variables: Treatment, CostOfTreatment and QoL (temporal variable)
	 * It performs a battery of tests: for numSlices = 1, numSlices = 2, ..., numSlices = 100
	 */
	@Test
	public void testExpansionSMMWithStateVariable() {
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
			
			//Create the SMM and expand it
			ProbNet network = NetsFactory.createSMMWithStateVariable(qoLTreat, qoLNoTreat,
					costTreat, costNoTreat,0.7,0.5);
			double discount = 0.01;

			ProbNet expandedNetwork = FactoryExpandedSMM.constructExpandedNetwork(numSlices, network, discount, true);
			
		
			ArrayList<TablePotential> tablePotentials = extractUtilityPotentialsProjecToTablesAndCheckVariables(expandedNetwork);
			//Check utility potentials starting in slice 1
			double ratio = 1.0 / (1.0 + discount);
			for (TablePotential auxPot:tablePotentials){
				if (hasTemporalVariableRoleAndNotZeroSlice(auxPot,PotentialRole.UTILITY)){
					int slice = auxPot.getUtilityVariable().getTimeSlice();
					checkUtilityPotentialQoLSMMWithState(expandedNetwork,auxPot,qoLTreat,qoLNoTreat,ratio,slice);
				}
			}
			
			/*//Check probability potentials starting in slice 1
			for (TablePotential auxPot:tablePotentials){
				if (hasTemporalVariableRoleAndNotZeroSlice(auxPot)){
					int slice = auxPot.getUtilityVariable().getTimeSlice();
				
					checkUtilityPotentialQoLSMMWithState(expandedNetwork,auxPot,qoLTreat,qoLNoTreat,ratio,slice);
				}
			}
	*/
					
		}

	}





	private void checkUtilityPotentialQoLSMMWithState(ProbNet expandedNetwork, TablePotential auxPot, double qoLTreat, double qoLNoTreat, double ratio, int slice) {
		// TODO Auto-generated method stub
		
		ArrayList<Variable> variablesUtil = new ArrayList<>();
		try {
			variablesUtil.add(expandedNetwork.getVariable("Treatment"));
			variablesUtil.add(expandedNetwork.decisionCriteria);
			variablesUtil.add(expandedNetwork.getVariable(nameStateVariable(auxPot.getUtilityVariable())));

		} catch (ProbNodeNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		double termQoLTreat = termGeometricProgression(qoLTreat,ratio,slice);
		double termQoLNoTreat = termGeometricProgression(qoLNoTreat,ratio,slice);
		
		double expectedValues[] = {0.0,0.0,0.0,0.0,0.0,0.0,termQoLTreat,termQoLNoTreat};
		TablePotential expectedPotential = null;
		try {
			expectedPotential = new TablePotential(variablesUtil, PotentialRole.UTILITY);
		} catch (NotEnoughMemoryException e) {
			e.printStackTrace();
		}
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


	private ArrayList<TablePotential> extractUtilityPotentialsProjecToTablesAndCheckVariables(
			ProbNet expandedNetwork) {
		InferenceOptions inferenceOptions;
		ArrayList<Potential> utilityPotentials = expandedNetwork
				.getPotentialsRole(PotentialRole.UTILITY);
		
		
		inferenceOptions = new InferenceOptions(expandedNetwork, null);

		ArrayList<TablePotential> tablePotentials;
		tablePotentials = new ArrayList<>();
		for (Potential auxPotential : utilityPotentials) {
			assertNotNull(auxPotential.getUtilityVariable());
			try {
				ArrayList<TablePotential> tableProject = auxPotential.tableProject(null, inferenceOptions);
				//Check utilityVariables are not null
				for (TablePotential auxTable:tableProject){
					assertNotNull(auxTable.getUtilityVariable());
				}
				tablePotentials.addAll(tableProject);
			} catch (NotEnoughMemoryException | NonProjectablePotentialException
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


	
	

}
