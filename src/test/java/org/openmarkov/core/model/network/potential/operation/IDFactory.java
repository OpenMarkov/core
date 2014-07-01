package org.openmarkov.core.model.network.potential.operation;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

/**
 * @author manuel
 * This class creates some influence diagrams for test purposes.
 */
public class IDFactory {

	public static ProbNet createNoKnowledge() {
		ProbNet noKnowledge = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Create disease variable and potential
		noKnowledge.addNode(disease, NodeType.CHANCE);
		double[] diseaseValues = {0.14, 0.86};
		TablePotential diseasePotential = new TablePotential(
				getVariablesList(disease), PotentialRole.CONDITIONAL_PROBABILITY, diseaseValues) ;
		noKnowledge.addPotential(diseasePotential);

		// Decision variable
		noKnowledge.addNode(therapy, NodeType.DECISION);
		
		// Create health state variable and potential
		noKnowledge.addNode(healthState, NodeType.UTILITY);
		TablePotential healthStatePotential = 
				new TablePotential(healthState, getVariablesList(therapy, disease));
		healthStatePotential.values = new double[]{10.0, 9.0, 3.0, 8.0};
		noKnowledge.addPotential(healthStatePotential);

		// Create cost of therapy variable and potential
		noKnowledge.addNode(costOfTherapy, NodeType.UTILITY);
		TablePotential costOfTherapyPotential = 
				new TablePotential(costOfTherapy, getVariablesList(therapy));
		costOfTherapyPotential.values = new double[]{0.0, -0.25};
		noKnowledge.addPotential(costOfTherapyPotential);

		return noKnowledge;
	}
	
	public static ProbNet createPerfectKnowledge() {
		ProbNet perfectKnowledge = createNoKnowledge();
		try {
			perfectKnowledge.addLink(disease, therapy, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
			System.err.println("Variable not found");
		}
		return perfectKnowledge;
	}
	
	public static ProbNet createTestDecisionID() {
		ProbNet testDecision = createNoKnowledge();
		testDecision.addNode(resultOfTest, NodeType.CHANCE);
		testDecision.addNode(doTest, NodeType.DECISION);
		testDecision.addNode(costOfTest, NodeType.UTILITY);
		try {
			testDecision.addLink(disease, resultOfTest, true);
			testDecision.addLink(resultOfTest, therapy, true);
			testDecision.addLink(doTest, resultOfTest, true);
			testDecision.addLink(doTest, costOfTest, true);
			testDecision.addLink(doTest, therapy, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
			System.err.println("Variable not found");
		}
		TablePotential costOfTherapyPotential = 
				new TablePotential(costOfTherapy, getVariablesList(resultOfTest));
		costOfTherapyPotential.values = new double[]{0.0, 20000.0, 70000.0};
		testDecision.addPotential(costOfTherapyPotential);
		TablePotential costOfTestPotential = 
				new TablePotential(costOfTest, getVariablesList(doTest));
		costOfTestPotential.values = new double[]{0.0, -0.2};
		testDecision.addPotential(costOfTherapyPotential);
		TablePotential resultOfTestPotential = 
				new TablePotential(resultOfTest, getVariablesList(resultOfTest, doTest, disease));
		resultOfTestPotential.values = new double[]{0.0, 0.0, 1.0, 0.03, 0.97, 0.0, 0.91, 0.09, 0.0};
		testDecision.addPotential(resultOfTestPotential);
		return testDecision;
	}

	/**
	 * @param variablesArray
	 * @return A <code>List</code> of <code>Variable</code>s
	 */
	private static List<Variable> getVariablesList(Variable... variablesArray) {
		List<Variable> variablesList = new ArrayList<Variable>(variablesArray.length);
		for (Variable variable : variablesArray) {
			variablesList.add(variable);
		}
		return variablesList;
	}

	private static Variable costOfTherapy = new Variable("Cost of therapy");
	private static Variable costOfTest = new Variable("Cost of test");
	private static Variable effectiveness = new Variable("Effectiveness");
	private static Variable disease = new Variable("Disease", "absent", "present");
	private static Variable resultOfTest = new Variable("Result of test", "negative", "positive", "not performed");
	private static Variable doTest = new Variable("Do test", "no", "yes");
	private static Variable therapy = new Variable("Therapy", "no", "yes");
	private static Variable healthState = new Variable("Health state");
	
}