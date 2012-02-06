/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.DynamicBayesianNetwork;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	OnlyDirectedLinksTest.class,
	NoCyclesTest.class,
	OnlyChanceNodesTest.class,
	NoUtilityParentTest.class,
	OnlyFiniteStateVariablesTest.class,
	DistinctLinksTest.class,
	NoMixedParentsTest.class,
	NoMultipleLinksTest.class,
	OnlyAtemporalVariablesTest.class,
	OnlyNumericVariablesTest.class,
	OnlyTemporalVariablesTest.class
})

/** Test constraints applied to networks. 
 * Supplies methods to create different types of <code>ProbNet</code> */
public class ConstraintsTests {

	/** Creates a simple <code>probNet</code> for test purposes: A -> B -- C.
	 * @return <code>ProbNet</code> */
	public static ProbNet getTestProbNetMixed() {
		ProbNet probNet = new ProbNet();
		Variable va = new Variable("A", 2);
		Variable vb = new Variable("B", 2);
		Variable vc = new Variable("C", 2);
		try {
			probNet.addVariable(va, NodeType.CHANCE);
			probNet.addVariable(vb, NodeType.DECISION);
			probNet.addVariable(vc, NodeType.CHANCE);
			probNet.addLink(va, vb, true);
			probNet.addLink(vb, vc, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return probNet;
	}

	/** Creates a simple <code>probNet</code> for test purposes: A -- B -- C.
	 * @return <code>ProbNet</code> */
	public static ProbNet getTestProbNetUndirected() {
		ProbNet probNet = new ProbNet();
		Variable va = new Variable("A", 2);
		Variable vb = new Variable("B", 2);
		Variable vc = new Variable("C", 2);
		try {
			probNet.addVariable(va, NodeType.CHANCE);
			probNet.addVariable(vb, NodeType.CHANCE);
			probNet.addVariable(vc, NodeType.CHANCE);
			probNet.addLink(va, vb, false);
			probNet.addLink(vb, vc, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return probNet;
	}

	/** Creates a simple <code>probNet</code> for test purposes: A -> B -> C.
	 * @return <code>ProbNet</code> */
	public static ProbNet getTestProbNetDirected() {
		ProbNet probNet = new ProbNet();
		Variable va = new Variable("A", 2);
		Variable vb = new Variable("B", 2);
		Variable vc = new Variable("C", 2);
		try {
			probNet.addVariable(va, NodeType.CHANCE);
			probNet.addVariable(vb, NodeType.CHANCE);
			probNet.addVariable(vc, NodeType.CHANCE);
			probNet.addLink(va, vb, true);
			probNet.addLink(vb, vc, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return probNet;
	}
	
	/** Creates a simple influence diagram (<code>probNet</code>) for test 
	 * purposes: A->D, U(A,D).
	 * @return <code>ProbNet</code> */
	public static ProbNet getInfuenceDiagram() {
		ProbNet influenceDiagram = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		Variable vA = new Variable("A", 2);
		Variable vD = new Variable("D", 2);
		Variable vU = new Variable("U");
		ArrayList<Variable> variables = new ArrayList<Variable>();
		variables.add(vA);
		variables.add(vD);
		try {
			influenceDiagram.addVariable(vA, NodeType.CHANCE);
			influenceDiagram.addVariable(vD, NodeType.DECISION);
			influenceDiagram.addVariable(vU, NodeType.UTILITY);
			TablePotential utilityPotential = 
				new TablePotential(variables, PotentialRole.UTILITY);
			utilityPotential.setUtilityVariable(vU);
			influenceDiagram.addPotential(utilityPotential);
			influenceDiagram.addLink(vA, vD, true);
			influenceDiagram.getGraph().makeLinksExplicit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return influenceDiagram;
	}
	
	/** Creates a influence diagram (<code>probNet</code>) for test 
	 * purposes with not only utility Children: A->D, U->C(decision), U(A,D).
	 * @return <code>ProbNet</code> */
	public static ProbNet getNotOnlyUtilityChildrenInfluenceDiagram() {
		ProbNet influenceDiagram = getInfuenceDiagram();
		Variable vC = new Variable("C", 2);
		try {
		Variable vU=influenceDiagram.getVariable("U");
			influenceDiagram.addVariable(vC, NodeType.DECISION);
			influenceDiagram.addLink(vU, vC, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return influenceDiagram;
	}
	
	/** Creates a influence diagram (<code>probNet</code>) for test 
	 * purposes with only Utility children: A->D, U->C(utility), U(A,D).
	 * @return <code>ProbNet</code> */
	public static ProbNet getOnlyUtilityChildrenInfluenceDiagram() {
		ProbNet influenceDiagram = getInfuenceDiagram();
		Variable vC = new Variable("C", 2);
		
		try {
			Variable vU=influenceDiagram.getVariable("U");
			influenceDiagram.addVariable(vC, NodeType.UTILITY);
			influenceDiagram.addLink(vU, vC, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return influenceDiagram;
	}
	
	
	
	/** Creates a simple influence diagram (<code>probNet</code>) for test 
	 * purposes: A->D, U(A,D).
	 * @return <code>ProbNet</code> */
	public static ProbNet getNumericInfluenceDiagram()
	{
		ProbNet influenceDiagram = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		Variable vA = new Variable("A");
		Variable vD = new Variable("D");
		Variable vU = new Variable("U");
		ArrayList<Variable> variables = new ArrayList<Variable>();
		variables.add(vA);
		variables.add(vD);
		try {
			influenceDiagram.addVariable(vA, NodeType.CHANCE);
			influenceDiagram.addVariable(vD, NodeType.DECISION);
			influenceDiagram.addVariable(vU, NodeType.UTILITY);
			TablePotential utilityPotential = 
				new TablePotential(variables, PotentialRole.UTILITY);
			utilityPotential.setUtilityVariable(vU);
			influenceDiagram.addPotential(utilityPotential);
			influenceDiagram.addLink(vA, vD, true);
			influenceDiagram.getGraph().makeLinksExplicit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return influenceDiagram;
		
		
		
	}

	
	/** Creates a simple probabilistic net (<code>probNet</code>) for test 
	 * purposes with temporal variables: A->D, U(A,D).
	 * @return <code>ProbNet</code> */
	public static ProbNet getTemporalVarNet()
	{
		ProbNet influenceDiagram = new ProbNet(DynamicBayesianNetwork.getUniqueInstance());
		Variable vA = new Variable(" [12]","YES","NO");
		Variable vD = new Variable(" [14]","YES","NO");
		Variable vU = new Variable(" [15]","YES","NO");
		ArrayList<Variable> variables = new ArrayList<Variable>();
		variables.add(vA);
		variables.add(vD);
		try {
			influenceDiagram.addVariable(vA, NodeType.CHANCE);
			influenceDiagram.addVariable(vD, NodeType.DECISION);
			influenceDiagram.addVariable(vU, NodeType.UTILITY);
			TablePotential utilityPotential = 
				new TablePotential(variables, PotentialRole.UTILITY);
			utilityPotential.setUtilityVariable(vU);
			influenceDiagram.addPotential(utilityPotential);
			influenceDiagram.addLink(vA, vD, true);
			influenceDiagram.getGraph().makeLinksExplicit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return influenceDiagram;
		
		
		
	}

}
