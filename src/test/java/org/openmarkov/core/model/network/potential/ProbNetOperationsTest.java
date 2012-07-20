/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.model.network.potential;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.inference.InferenceAlgorithmTests;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;

/**
 * @author mluque
 *
 */
public class ProbNetOperationsTest {


	@Before
	public void setUp() throws Exception {
		
	}
	
	/**
	 * @throws ProbNodeNotFoundException
	 * Tests the a priori probabilities obtained in the network bN_ABC
	 */
	@Test
	public void testGetPrunedMethodBN_Asia()
			throws ProbNodeNotFoundException {
		ProbNet network;
		ProbNet outputNetwork;
		ProbNet intermediate;
		HashSet<Variable> variablesOfEvidence;
		ArrayList<Variable> variablesOfInterest;
		//Repeat the test, because the behaviour of method getPruned is non-deterministic
	for (int i=1;i<100;i++){
		
		network = NetsFactory.createBN_Asia();
		
		Variable variableD = InferenceAlgorithmTests.getVariableAndAssertNotNull(network,"D"); 
		Variable variableTOrC = InferenceAlgorithmTests.getVariableAndAssertNotNull(network,"TOrC");
		Variable variableT = InferenceAlgorithmTests.getVariableAndAssertNotNull(network,"T");
		variablesOfInterest = new ArrayList<>();
		variablesOfInterest.add(variableD);
		variablesOfEvidence = new HashSet<>();
		variablesOfEvidence.add(variableTOrC);
		variablesOfEvidence.add(variableT);
		intermediate = ProbNetOperations.removeBarrenNodes(network,variablesOfInterest,variablesOfEvidence);
		outputNetwork = ProbNetOperations.removeUnreachableNodes(intermediate,variablesOfInterest,variablesOfEvidence);
		//Nodes shouldn't appear
		assertFalse(outputNetwork.containsVariable("A"));
		assertFalse(outputNetwork.containsVariable("X"));
		//Nodes must appear
		assertTrue(outputNetwork.containsVariable("T"));
		assertTrue(outputNetwork.containsVariable("TOrC"));
		assertTrue(outputNetwork.containsVariable("S"));
		assertTrue(outputNetwork.containsVariable("L"));
		assertTrue(outputNetwork.containsVariable("B"));
		assertTrue(outputNetwork.containsVariable("D"));
	}
	}
	
	
}
