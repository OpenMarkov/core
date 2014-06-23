package org.openmarkov.core.model.network.potential.operation;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

/**
 * @author manuel
 *
 */
public class IDFactory {

	public static ProbNet createPerfectKnowledge() {
		ProbNet perfectKnowledge = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Create disease variable and potential
		Variable disease = new Variable("Disease", "absent", "present");
		perfectKnowledge.addNode(disease, NodeType.CHANCE);
		List<Variable> diseaseVariables = new ArrayList<Variable>(1);
		diseaseVariables.add(disease);
		double[] diseaseValues = {0.2, 0.8};
		TablePotential diseasePotential = new TablePotential(
				diseaseVariables, PotentialRole.CONDITIONAL_PROBABILITY, diseaseValues) ;

		// Decision variable
		Variable therapy = new Variable("Therapy", "no", "yes");
		
		// Create health state variable and potential
		Variable healthState = new Variable("Health state");
		List<Variable> healthStateVariables = new ArrayList<Variable>(2);
		healthStateVariables.add(therapy);
		healthStateVariables.add(disease);
		TablePotential healthStatePotential = new TablePotential(healthState, healthStateVariables);

		// Create cost of therapy variable and potential
		Variable costOfTherapy = new Variable("Cost of therapy");
		List<Variable> costOfTherapyVariables = new ArrayList<Variable>(1);
		costOfTherapyVariables.add(therapy);
		TablePotential costOfTherapyPotential = new TablePotential(costOfTherapy, costOfTherapyVariables);

		// Create probNet perfectKnowledge
		// TODO
		return perfectKnowledge;
	}
	
}
