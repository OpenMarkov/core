/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableTest;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

/** Test of <code>Potential</code> class. As this class is abstract we use the
 * class <code>TablePotential</code>.  */
public class PotentialTest {

	/** Creates a potential */
	@Test
	public void testCreation() {
		TablePotential potential = new TablePotential(null, PotentialRole.CONDITIONAL_PROBABILITY);
		assertNotNull(potential);
		// test number of variables of created potential
		List<Variable> variables = potential.getVariables();
		assertEquals(0, variables.size());

		// check that potential creation with an array of zero variables
		// produces the same result as above.
		potential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
		assertNotNull(potential);
		// test number of variables of created potential
		variables = potential.getVariables();
		assertEquals(0, variables.size());
	}

	// TODO Sobrecargar equals y poner en el comentario que 
	// equals ya NO consiste en comparar la dirección de memoria de dos objetos
	// TODO Cada tipo de potential tiene que tener un método equals y llamar al del padre
	/** Compares potential1 and potential2.
	 * @param potential1. <code>Potential</code>
	 * @param potential2. <code>Potential</code>
	 * @return <code>true</code> if both potentials are equal. */
	public static boolean equalPotentials(Potential potential1, Potential potential2) {
		boolean equals = true;
		if (potential1.getPotentialRole() == potential2.getPotentialRole() &&
				potential1.getClass() == potential2.getClass() &&
				potential1.getComment().contentEquals(potential2.getComment()) &&
				potential1.getNumVariables() == potential2.getNumVariables() &&
				potential1.isUtility() == potential2.isUtility()) {
		    List<Variable> variables1 = potential1.getVariables();
		    List<Variable> variables2 = potential2.getVariables();
			int numVariables = variables1.size();
			int i = 0;
			while (i < numVariables && equals) {
				if (!VariableTest.equalVariables(variables1.get(i), variables2.get(i))) {
					equals = false;
				}
				i++;
			}
			// TODO Mover a TablePotential
			if (potential1 instanceof TablePotential && potential2 instanceof TablePotential) {
				TablePotentialTest.checkEqualPotentials((TablePotential)potential1, (TablePotential)potential2,0.0001);
				equals = true;
			}
		}
		return equals;
	}
	
	@Test
	public void scalePotentialTest() {
		ProbNet probNet = getProbNet4ScaleTest();
		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
		for(Node node : utilityNodes){
			
			// Scale any potential by 0.5
			node.getPotentials().get(0).scalePotential(0.5);
			
			switch(node.getVariable().getName()){
			case "TreeAddUtility":
				TreeADDBranch b1 = ((TreeADDPotential) node.getPotentials().get(0)).getBranches().get(0);
				double[] expectedBranch1Values = {2.5};
				Assert.assertArrayEquals(expectedBranch1Values, ((TablePotential) b1.getPotential()).getValues(), 0.001);
				
				TreeADDBranch b2 = ((TreeADDPotential) node.getPotentials().get(0)).getBranches().get(1);				
				double[] expectedBranch2Values = {2.5};
				Assert.assertArrayEquals(expectedBranch2Values, ((LinearCombinationPotential) b2.getPotential()).getCoefficients(), 0.001);
				
				break;
			case "TableUtility":
				double[] expectedTableValues = {5.0, 4.5};
				Assert.assertArrayEquals(expectedTableValues, ((TablePotential) node.getPotentials().get(0)).getValues(), 0.001);
				break;
			case "LCUtility":
				double[] expectedLCValues = {1.5, 2.5, 4.5};
				Assert.assertArrayEquals(expectedLCValues, ((LinearCombinationPotential) node.getPotentials().get(0)).getCoefficients(), 0.001);
				break;
			case "ExponentialUtility":
				double[] expectedExponentialValues = {2.30685281944005, 7};
				Assert.assertArrayEquals(expectedExponentialValues, ((ExponentialPotential) node.getPotentials().get(0)).getCoefficients(), 0.001);
				break;
				
			}
		}
		
	}
	
	
	public static ProbNet getProbNet4ScaleTest () {
		  ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		  // Variables
		  Variable varA = new Variable("A", "absent", "present");
		  Variable varB = new Variable("B");
		  Variable varC = new Variable("C", "absent", "present");
		  Variable varTreeAddUtility = new Variable("TreeAddUtility");
		  Variable varTableUtility = new Variable("TableUtility");
		  Variable varLCUtility = new Variable("LCUtility");
		  Variable varExponentialUtility = new Variable("ExponentialUtility");

		  // Nodes
		  Node nodeA= probNet.addNode(varA, NodeType.CHANCE);
		  Node nodeB= probNet.addNode(varB, NodeType.CHANCE);
		  Node nodeC= probNet.addNode(varC, NodeType.CHANCE);
		  Node nodeTreeAddUtility= probNet.addNode(varTreeAddUtility, NodeType.UTILITY);
		  Node nodeTableUtility= probNet.addNode(varTableUtility, NodeType.UTILITY);
		  Node nodeLCUtility= probNet.addNode(varLCUtility, NodeType.UTILITY);
		  Node nodeExponentialUtility= probNet.addNode(varExponentialUtility, NodeType.UTILITY);

		  // Links
		  probNet.makeLinksExplicit(false);
		  probNet.addLink(nodeA, nodeTreeAddUtility, true);
		  probNet.addLink(nodeA, nodeTableUtility, true);
		  probNet.addLink(nodeB, nodeLCUtility, true);
		  probNet.addLink(nodeC, nodeExponentialUtility, true);
		  probNet.addLink(nodeC, nodeLCUtility, true);

		  // Potentials
		  UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		  nodeA.setPotential(potA);

		  UniformPotential potB = new UniformPotential(Arrays.asList(varB), PotentialRole.CONDITIONAL_PROBABILITY);
		  nodeB.setPotential(potB);

		  UniformPotential potC = new UniformPotential(Arrays.asList(varC), PotentialRole.CONDITIONAL_PROBABILITY);
		  nodeC.setPotential(potC);

		  TreeADDPotential potTreeAddUtility = new TreeADDPotential(varTreeAddUtility,Arrays.asList(varA));
		  TablePotential tablePotentialBranch1 = new TablePotential(potTreeAddUtility.getBranches().get(0).getRootVariable(),
				  potTreeAddUtility.getBranches().get(0).getParentVariables());
		  tablePotentialBranch1.values = new double[]{5};
		  potTreeAddUtility.getBranches().get(0).setPotential(tablePotentialBranch1);
		  
		  LinearCombinationPotential lcPotentialBranch2 = new LinearCombinationPotential(potTreeAddUtility.getBranches().get(1).getRootVariable(),
				  potTreeAddUtility.getBranches().get(1).getParentVariables());
		  double[] coefficientsLCBranch2 = {5};
		  lcPotentialBranch2.setCoefficients(coefficientsLCBranch2);
		  potTreeAddUtility.getBranches().get(1).setPotential(lcPotentialBranch2);
		  
		  nodeTreeAddUtility.setPotential(potTreeAddUtility);

		  TablePotential potTableUtility = new TablePotential(varTableUtility,Arrays.asList(varA));
		  potTableUtility.values = new double[]{10, 9};
		  nodeTableUtility.setPotential(potTableUtility);

		  LinearCombinationPotential potLCUtility = new LinearCombinationPotential(varLCUtility,Arrays.asList(varB, varC));
		  double[] coefficientsLC = {3.0, 5.0, 9.0};
		  potLCUtility.setCoefficients(coefficientsLC);
		  nodeLCUtility.setPotential(potLCUtility);

		  ExponentialPotential potExponentialUtility = new ExponentialPotential(varExponentialUtility,Arrays.asList(varC));
		  double[] coefficientsExp = {3.0, 7.0};
		  potExponentialUtility.setCoefficients(coefficientsExp);
		  nodeExponentialUtility.setPotential(potExponentialUtility);


		 return probNet;
		}
}
