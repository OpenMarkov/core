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

	public static ProbNet createPerfectKnowledge() {
		ProbNet perfectKnowledge = createNoKnowledge();
		Variable disease = null;
		Variable therapy = null;
		try {
			disease = perfectKnowledge.getVariable("Disease");
			therapy = perfectKnowledge.getVariable("Therapy");
			perfectKnowledge.addLink(disease, therapy, true);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
			System.err.println("Variable not found");
		}
		return perfectKnowledge;
	}
	
	public static ProbNet createNoKnowledge() {
		ProbNet noKnowledge = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Create disease variable and potential
		Variable disease = new Variable("Disease", "absent", "present");
		noKnowledge.addNode(disease, NodeType.CHANCE);
		List<Variable> diseaseVariables = new ArrayList<Variable>(1);
		diseaseVariables.add(disease);
		double[] diseaseValues = {0.14, 0.86};
		TablePotential diseasePotential = new TablePotential(
				diseaseVariables, PotentialRole.CONDITIONAL_PROBABILITY, diseaseValues) ;
		noKnowledge.addPotential(diseasePotential);

		// Decision variable
		Variable therapy = new Variable("Therapy", "no", "yes");
		noKnowledge.addNode(therapy, NodeType.DECISION);
		
		// Create health state variable and potential
		Variable healthState = new Variable("Health state");
		noKnowledge.addNode(healthState, NodeType.UTILITY);
		List<Variable> healthStateVariables = new ArrayList<Variable>(2);
		healthStateVariables.add(therapy);
		healthStateVariables.add(disease);
		TablePotential healthStatePotential = new TablePotential(healthState, healthStateVariables);
		healthStatePotential.values = new double[]{10.0, 9.0, 3.0, 8.0};
		noKnowledge.addPotential(healthStatePotential);

		// Create cost of therapy variable and potential
		Variable costOfTherapy = new Variable("Cost of therapy");
		noKnowledge.addNode(costOfTherapy, NodeType.UTILITY);
		List<Variable> costOfTherapyVariables = new ArrayList<Variable>(1);
		costOfTherapyVariables.add(therapy);
		TablePotential costOfTherapyPotential = new TablePotential(costOfTherapy, costOfTherapyVariables);
		costOfTherapyPotential.values = new double[]{0.0, -0.25};
		noKnowledge.addPotential(costOfTherapyPotential);

		return noKnowledge;
	}
	

}
